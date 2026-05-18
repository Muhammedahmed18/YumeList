package com.axiel7.moelist.ui.season

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.data.model.anime.SeasonType
import com.axiel7.moelist.data.model.anime.StartSeason
import com.axiel7.moelist.data.model.media.BasicMyListStatus
import com.axiel7.moelist.data.model.media.MediaFormat
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SeasonChartViewModel(
    private val animeRepository: AnimeRepository,
    private val defaultPreferencesRepository: DefaultPreferencesRepository
) : BaseViewModel<SeasonChartUiState>(), SeasonChartEvent {

    override val mutableUiState = MutableStateFlow(SeasonChartUiState())
    private var fetchJob: Job? = null

    override fun loadMore() {} // No longer used as we fetch all at once

    override fun setSeason(season: Season?, year: Int?) {
        mutableUiState.update { uiState ->
            val startSeason = when {
                season != null && year != null -> StartSeason(year, season)
                season != null -> uiState.season.copy(season = season)
                year != null -> uiState.season.copy(year = year)
                else -> uiState.season
            }
            uiState.copy(
                season = startSeason,
                seasonType = SeasonType.entries.find { it.season == startSeason }
            )
        }
    }

    override fun setSeason(type: SeasonType) {
        mutableUiState.update {
            it.copy(
                season = type.season,
                seasonType = type
            )
        }
    }

    override fun onChangeSort(value: MediaSort) {
        mutableUiState.update { it.copy(sort = value) }
    }

    override fun onChangeIsNew(value: Boolean) {
        mutableUiState.update { it.copy(isNew = value) }
    }

    override fun onChangeFormat(value: MediaFormat?) {
        mutableUiState.update { it.copy(selectedFormat = value) }
    }

    override fun onApplyFilters() {
        fetchFullSeason()
    }

    private fun fetchFullSeason() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch(Dispatchers.IO) {
            val currentState = mutableUiState.value
            mutableUiState.update { it.copy(isLoading = true, loadMore = false) }
            
            // Clear current list for new fetch
            uiState.value.animes.clear()

            var nextPage: String? = null
            var hasMore = true

            while (hasMore) {
                val result = animeRepository.getSeasonalAnimes(
                    sort = currentState.sort,
                    startSeason = currentState.season,
                    isNew = currentState.isNew,
                    limit = 500, // Fetch as many as possible in one go
                    fields = AnimeRepository.SEASONAL_FIELDS,
                    page = nextPage,
                )

                if (result.data != null) {
                    uiState.value.animes.addAll(result.data)
                    nextPage = result.paging?.next
                    hasMore = nextPage != null
                } else {
                    hasMore = false
                    if (result.message != null) {
                        mutableUiState.update { it.copy(message = result.message) }
                    }
                }
            }

            mutableUiState.update {
                it.copy(
                    nextPage = null,
                    loadMore = false,
                    isLoading = false
                )
            }
        }
    }

    init {
        mutableUiState
            .distinctUntilChanged { old, new ->
                old.season == new.season
                        && old.sort == new.sort
                        && old.isNew == new.isNew
            }
            .onEach { 
                fetchFullSeason()
            }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.hideScores
            .onEach { value ->
                mutableUiState.update { it.copy(hideScore = value) }
            }
            .launchIn(viewModelScope)

        animeRepository.userAnimeList
            .onEach { userList ->
                val seasonalList = mutableUiState.value.animes
                seasonalList.forEachIndexed { index, seasonalAnime ->
                    val userEntry = userList.find { it.node.id == seasonalAnime.node.id }
                    val newStatus = userEntry?.listStatus?.let {
                        BasicMyListStatus(it.status, it.score)
                    }
                    if (seasonalAnime.node.myListStatus != newStatus) {
                        seasonalList[index] = seasonalAnime.copy(
                            node = seasonalAnime.node.copy(myListStatus = newStatus)
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }
}
