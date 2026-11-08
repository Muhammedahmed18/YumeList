package com.axiel7.moelist.ui.home

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.data.model.media.BasicMyListStatus
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.RecommendationRepository
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.recommendation.RecommendedMedia
import com.axiel7.moelist.ui.recommendations.composables.RecommendationActionResult
import com.axiel7.moelist.utils.ApiErrorMapper
import com.axiel7.moelist.utils.toUiMessage
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import com.axiel7.moelist.utils.SeasonCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class HomeViewModel(
    private val animeRepository: AnimeRepository,
    private val recommendationRepository: RecommendationRepository,
    private val defaultPreferencesRepository: DefaultPreferencesRepository,
) : BaseViewModel<HomeUiState>(), HomeEvent {

    override val mutableUiState = MutableStateFlow(HomeUiState())

    /** The current (or last) load of both sections. */
    private var loadJob: Job? = null

    /** Login state the sections were last loaded for (null = never loaded). */
    private var loadedForLoggedIn: Boolean? = null

    /** The current (or last) load of the For You section. */
    private var forYouJob: Job? = null

    override fun initRequestChain(isLoggedIn: Boolean) {
        loadJob?.cancel()
        val loginChanged = loadedForLoggedIn != isLoggedIn
        // Another account (or none) now: the remembered recommendations aren't theirs
        if (loginChanged && loadedForLoggedIn != null) recommendationRepository.clearMemory()
        loadedForLoggedIn = isLoggedIn
        if (loginChanged) {
            mutableUiState.update {
                it.copy(forYou = null, forYouLoaded = false, forYouError = null)
            }
        }
        loadForYou(isLoggedIn, forceRefresh = false)
        mutableUiState.update {
            it.copy(
                isLoadingToday = true,
                isLoadingSeason = true,
                // A different login state means different data: drop it and show the
                // skeleton again instead of the old lists
                todayAnimes = if (loginChanged) emptyList() else it.todayAnimes,
                seasonalAnimes = if (loginChanged) emptyList() else it.seasonalAnimes,
                todayLoaded = it.todayLoaded && !loginChanged,
                seasonLoaded = it.seasonLoaded && !loginChanged,
            )
        }
        // Both sections load at the same time, each finishing on its own
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            launch { getTodayAiringAnimes() }
            launch { getSeasonalAnimes() }
        }
    }

    override fun loadIfNeeded(isLoggedIn: Boolean) {
        val state = mutableUiState.value
        val upToDate = loadedForLoggedIn == isLoggedIn &&
            (loadJob?.isActive == true || (state.todayLoaded && state.seasonLoaded))
        if (!upToDate) initRequestChain(isLoggedIn)
        else if (!state.forYouLoaded && forYouJob?.isActive != true) {
            loadForYou(isLoggedIn, forceRefresh = false)
        }
    }

    // ── For You ──────────────────────────────────────────────────────────────

    override fun retryForYou(isLoggedIn: Boolean) {
        loadForYou(isLoggedIn, forceRefresh = true)
    }

    private fun loadForYou(isLoggedIn: Boolean, forceRefresh: Boolean) {
        forYouJob?.cancel()
        forYouJob = viewModelScope.launch(Dispatchers.IO) {
            if (!defaultPreferencesRepository.showForYou.first()) {
                mutableUiState.update { it.copy(isLoadingForYou = false) }
                return@launch
            }
            // The saved picks show straight away; the request below brings them up to date
            if (mutableUiState.value.forYou == null) {
                recommendationRepository.peekForYou(MediaType.ANIME, isLoggedIn)?.let { cached ->
                    mutableUiState.update { it.copy(forYou = cached) }
                }
            }
            mutableUiState.update { it.copy(isLoadingForYou = true, forYouError = null) }
            val response = recommendationRepository.getForYou(
                mediaType = MediaType.ANIME,
                isLoggedIn = isLoggedIn,
                forceRefresh = forceRefresh,
            )
            val data = response.data
            mutableUiState.update {
                if (data != null) {
                    it.copy(forYou = data, isLoadingForYou = false, forYouLoaded = true)
                } else {
                    it.copy(
                        isLoadingForYou = false,
                        forYouError = ApiErrorMapper.mapApiError(response.message ?: response.error),
                    )
                }
            }
        }
    }

    override fun addRecommendationToPlan(item: RecommendedMedia) {
        viewModelScope.launch(Dispatchers.IO) {
            val error = recommendationRepository.addToPlan(item)
            mutableUiState.update {
                it.copy(
                    recommendationAction = if (error == null) {
                        RecommendationActionResult(RecommendationActionResult.Kind.ADDED_TO_PLAN, item)
                    } else {
                        RecommendationActionResult(
                            kind = RecommendationActionResult.Kind.FAILED,
                            item = item,
                            errorMessage = error.toUiMessage(),
                        )
                    }
                )
            }
        }
    }

    override fun hideRecommendation(item: RecommendedMedia) {
        mutableUiState.update {
            it.copy(
                forYou = it.forYou?.without(setOf(item.id)),
                recommendationAction = RecommendationActionResult(
                    RecommendationActionResult.Kind.HIDDEN, item
                ),
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            recommendationRepository.hide(item.mediaType, item.id)
        }
    }

    override fun undoHideRecommendation(item: RecommendedMedia) {
        viewModelScope.launch(Dispatchers.IO) {
            recommendationRepository.unhide(item.mediaType, item.id)
            val isLoggedIn = loadedForLoggedIn ?: return@launch
            // The saved picks still have it: reading them again puts it back in place
            recommendationRepository.peekForYou(MediaType.ANIME, isLoggedIn)?.let { restored ->
                mutableUiState.update { it.copy(forYou = restored) }
            }
        }
    }

    override fun onRecommendationActionShown() {
        mutableUiState.update { it.copy(recommendationAction = null) }
    }

    /** The local date the Today row was built for, to rebuild it after midnight. */
    private var todayLoadedFor: LocalDate? = null

    /** Rebuilds the Today row when the user comes back on a new day (e.g. app left open). */
    fun refreshTodayIfDayChanged() {
        val loadedFor = todayLoadedFor ?: return
        if (loadedFor != LocalDate.now()) {
            // todayLoaded stays true, so what's on screen stays until the new day's list is in
            mutableUiState.update { it.copy(isLoadingToday = true) }
            viewModelScope.launch(Dispatchers.IO) { getTodayAiringAnimes() }
        }
    }

    private suspend fun getTodayAiringAnimes() {
        val result = animeRepository.getAnimeRanking(
            rankingType = RankingType.AIRING,
            limit = 100,
            fields = AnimeRepository.TODAY_FIELDS
        )
        if (result.data != null) {
            // "Today" is the user's day in their own time zone, not Japan's: a Thursday 01:30
            // JST episode airs on Wednesday evening in Pakistan, Wednesday morning in New York.
            val today = LocalDate.now()
            val zone = ZoneId.systemDefault()
            val tempList = result.data
                .filter { anime ->
                    val status = anime.node.myListStatus?.status
                    val isInList = status == ListStatus.WATCHING || status == ListStatus.PLAN_TO_WATCH
                    anime.node.status == MediaStatus.AIRING
                        && isInList
                        && anime.node.broadcast?.airsOn(today, zone) == true
                }
                .distinctBy { it.node.id }
                // Earliest local air time first; day-only broadcasts (no time) go last
                .sortedWith(
                    compareBy(nullsLast<Long>()) { anime: AnimeRanking ->
                        anime.node.broadcast?.localAiringOn(today, zone)?.toEpochSecond()
                    }
                )
            todayLoadedFor = today
            mutableUiState.update {
                it.copy(todayAnimes = tempList, isLoadingToday = false, todayLoaded = true)
            }
        } else {
            mutableUiState.update { it.copy(isLoadingToday = false) }
            showMessage(result.message ?: result.error)
        }
    }

    private suspend fun getSeasonalAnimes() {
        val result = animeRepository.getSeasonalAnimes(
            sort = MediaSort.ANIME_NUM_USERS,
            startSeason = SeasonCalendar.currentStartSeason,
            limit = 20,
            fields = AnimeRepository.SEASONAL_FIELDS
        )
        if (result.data != null) {
            mutableUiState.update {
                it.copy(seasonalAnimes = result.data, isLoadingSeason = false, seasonLoaded = true)
            }
        } else {
            mutableUiState.update { it.copy(isLoadingSeason = false) }
            showMessage(result.message ?: result.error)
        }
    }

    init {
        defaultPreferencesRepository.hideScores
            .onEach { value ->
                mutableUiState.update { it.copy(hideScore = value) }
            }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.showForYou
            .onEach { value ->
                val wasShown = mutableUiState.value.showForYou
                mutableUiState.update { it.copy(showForYou = value) }
                // Turned on in settings while Home was already loaded
                val isLoggedIn = loadedForLoggedIn
                if (value && !wasShown && isLoggedIn != null && !mutableUiState.value.forYouLoaded) {
                    loadForYou(isLoggedIn, forceRefresh = false)
                }
            }
            .launchIn(viewModelScope)

        recommendationRepository.listStatuses(MediaType.ANIME)
            .onEach { statuses -> mutableUiState.update { it.copy(forYouStatuses = statuses) } }
            .launchIn(viewModelScope)

        // Sync home lists with local list status
        animeRepository.userAnimeList
            .onEach { userAnimes ->
                mutableUiState.update { state ->
                    state.copy(
                        todayAnimes = state.todayAnimes.map { item ->
                            val localStatus = userAnimes.find { it.node.id == item.node.id }?.listStatus?.status
                            if (localStatus != item.node.myListStatus?.status) {
                                item.copy(
                                    node = item.node.copy(
                                        myListStatus = localStatus?.let { BasicMyListStatus(it) }
                                    )
                                )
                            } else item
                        },
                        seasonalAnimes = state.seasonalAnimes.map { item ->
                            val localStatus = userAnimes.find { it.node.id == item.node.id }?.listStatus?.status
                            if (localStatus != item.node.myListStatus?.status) {
                                item.copy(
                                    node = item.node.copy(
                                        myListStatus = localStatus?.let { BasicMyListStatus(it) }
                                    )
                                )
                            } else item
                        }
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
