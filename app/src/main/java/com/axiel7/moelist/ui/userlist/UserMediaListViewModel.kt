package com.axiel7.moelist.ui.userlist

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.ListType
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import com.axiel7.moelist.utils.NumExtensions.isGreaterThanZero
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class UserMediaListViewModel(
    private val mediaType: MediaType,
    initialListStatus: ListStatus? = null,
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
    private val defaultPreferencesRepository: DefaultPreferencesRepository,
) : BaseViewModel<UserMediaListUiState>(), UserMediaListEvent {

    private val defaultListStatus = when (mediaType) {
        MediaType.ANIME -> ListStatus.WATCHING
        MediaType.MANGA -> ListStatus.READING
    }

    override val mutableUiState = MutableStateFlow(
        UserMediaListUiState(
            mediaType = mediaType,
            listStatus = initialListStatus
        )
    )

    private val isFetching = MutableStateFlow(false)

    override fun onChangeStatus(value: ListStatus) {
        viewModelScope.launch {
            isFetching.value = true
            when (mutableUiState.value.mediaType) {
                MediaType.ANIME -> defaultPreferencesRepository.setAnimeListStatus(value)
                MediaType.MANGA -> defaultPreferencesRepository.setMangaListStatus(value)
            }
            mutableUiState.update {
                it.copy(
                    listStatus = value,
                    nextPage = null,
                    loadMore = true,
                    message = null
                )
            }
        }
    }

    override fun onChangeSort(value: MediaSort) {
        viewModelScope.launch {
            isFetching.value = true
            when (mutableUiState.value.mediaType) {
                MediaType.ANIME -> defaultPreferencesRepository.setAnimeListSort(value)
                MediaType.MANGA -> defaultPreferencesRepository.setMangaListSort(value)
            }
            mutableUiState.update {
                it.copy(
                    listSort = value,
                    nextPage = null,
                    loadMore = true,
                    message = null
                )
            }
        }
    }

    override fun onChangeItemMyListStatus(value: BaseMyListStatus?, removed: Boolean) {
        // Handled reactively
    }

    override fun refreshList() {
        isFetching.value = true
        mutableUiState.update { it.copy(nextPage = null, loadMore = true, message = null) }
    }

    override fun loadMore() {
        mutableUiState.value.run {
            if (canLoadMore && !isLoadingMore) {
                mutableUiState.update { it.copy(loadMore = true, message = null) }
            }
        }
    }

    override fun onUpdateProgress(item: BaseUserMediaList<out BaseMediaNode>) {
        val newProgress = (item.userProgress() ?: 0) + 1
        onUpdateProgress(item, newProgress)
    }

    override fun onUpdateProgress(item: BaseUserMediaList<out BaseMediaNode>, progress: Int) {
        mutableUiState.update { it.copy(selectedItem = item) }
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            val nowDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            var newStatus: ListStatus? = null
            when (item) {
                is UserAnimeList -> {
                    val maxProgress = item.node.numEpisodes.takeIf { it != 0 }
                    val isCompleted = maxProgress != null && progress >= maxProgress
                    val isPlanning = item.listStatus?.status == ListStatus.PLAN_TO_WATCH
                    newStatus = when {
                        isCompleted -> ListStatus.COMPLETED
                        isPlanning -> ListStatus.WATCHING
                        else -> null
                    }
                    val result = animeRepository.updateAnimeEntry(
                        animeId = item.node.id,
                        watchedEpisodes = progress,
                        status = newStatus,
                        startDate = nowDate.takeIf {
                            isPlanning || !item.listStatus?.progress.isGreaterThanZero()
                        },
                        endDate = nowDate.takeIf { isCompleted }
                    )
                    if (result != null && newStatus == ListStatus.COMPLETED && result.score == 0) {
                        viewModelScope.launch { toggleSetScoreDialog(true) }
                    }
                }

                is UserMangaList -> {
                    val isVolumeProgress = item.listStatus?.isUsingVolumeProgress() == true
                    val maxProgress =
                        (if (isVolumeProgress) item.node.numVolumes else item.node.numChapters)
                            .takeIf { it != 0 }
                    val isCompleted = maxProgress != null && progress >= maxProgress
                    val isPlanning = item.listStatus?.status == ListStatus.PLAN_TO_READ
                    newStatus = when {
                        isCompleted -> ListStatus.COMPLETED
                        isPlanning -> ListStatus.READING
                        else -> null
                    }
                    mangaRepository.updateMangaEntry(
                        mangaId = item.node.id,
                        chaptersRead = progress.takeIf { !isVolumeProgress },
                        volumesRead = progress.takeIf { isVolumeProgress },
                        status = newStatus,
                        startDate = nowDate.takeIf {
                            isPlanning || item.listStatus?.progress.isGreaterThanZero()
                        },
                        endDate = nowDate.takeIf { isCompleted }
                    )
                }
            }
            setLoading(false)
        }
    }

    override fun onUpdateStatus(item: BaseUserMediaList<out BaseMediaNode>, status: ListStatus) {
        mutableUiState.update { it.copy(selectedItem = item) }
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            if (mediaType == MediaType.ANIME) {
                animeRepository.updateAnimeEntry(
                    animeId = item.node.id,
                    status = status
                )
            } else {
                mangaRepository.updateMangaEntry(
                    mangaId = item.node.id,
                    status = status
                )
            }
            setLoading(false)
        }
    }

    override fun onItemSelected(item: BaseUserMediaList<*>) {
        mutableUiState.update { it.copy(selectedItem = item) }
    }

    override fun setScore(score: Int) {
        mutableUiState.value.selectedItem?.let {
            setScore(it, score)
        }
    }

    override fun setScore(item: BaseUserMediaList<*>, score: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            if (mediaType == MediaType.ANIME) {
                animeRepository.updateAnimeEntry(
                    animeId = item.node.id,
                    score = score
                )
            } else {
                mangaRepository.updateMangaEntry(
                    mangaId = item.node.id,
                    score = score
                )
            }
            mutableUiState.update {
                it.copy(openSetScoreDialog = false, isLoading = false)
            }
        }
    }

    override fun toggleSortDialog(open: Boolean) {
        mutableUiState.update { it.copy(openSortDialog = open) }
    }

    override fun toggleSetScoreDialog(open: Boolean) {
        mutableUiState.update { it.copy(openSetScoreDialog = open) }
    }

    override fun toggleActionSheet(open: Boolean) {
        mutableUiState.update { it.copy(openActionSheet = open) }
    }

    override fun getRandomIdOfList() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState.update { state ->
                state.copy(isLoadingRandom = true)
            }
            val result = if (mutableUiState.value.mediaType == MediaType.ANIME) {
                animeRepository.getAnimeIdsOfUserList(
                    status = mutableUiState.value.listStatus ?: defaultListStatus
                )
            } else {
                mangaRepository.getMangaIdsOfUserList(
                    status = mutableUiState.value.listStatus ?: defaultListStatus
                )
            }
            if (!result.data.isNullOrEmpty()) {
                mutableUiState.update { state ->
                    state.copy(
                        randomId = result.data.random(),
                        isLoadingRandom = false,
                    )
                }
            } else {
                mutableUiState.update { state ->
                    state.copy(isLoadingRandom = false)
                }
            }
        }
    }

    override fun onRandomIdOpen() {
        mutableUiState.update { it.copy(randomId = null) }
    }

    init {
        if (initialListStatus == null) {
            val listStatusFlow = when (mediaType) {
                MediaType.ANIME -> defaultPreferencesRepository.animeListStatus
                MediaType.MANGA -> defaultPreferencesRepository.mangaListStatus
            }
            viewModelScope.launch {
                mutableUiState.update {
                    it.copy(listStatus = listStatusFlow.first())
                }
            }
        }

        val listSortFlow = when (mediaType) {
            MediaType.ANIME -> defaultPreferencesRepository.animeListSort
            MediaType.MANGA -> defaultPreferencesRepository.mangaListSort
        }
        viewModelScope.launch {
            mutableUiState.update {
                it.copy(listSort = listSortFlow.first())
            }
        }

        combine(
            defaultPreferencesRepository.useGeneralListStyle,
            defaultPreferencesRepository.generalListStyle
        ) { useGeneral, generalStyle ->
            if (useGeneral) {
                mutableUiState.update { it.copy(listStyle = generalStyle) }
            } else {
                mutableUiState
                    .filter { it.listStatus != null }
                    .flatMapLatest {
                        ListType(it.listStatus!!, it.mediaType)
                            .stylePreference(defaultPreferencesRepository)
                    }.collect { listStyle ->
                        mutableUiState.update { it.copy(listStyle = listStyle) }
                    }
            }
        }.launchIn(viewModelScope)

        defaultPreferencesRepository.gridItemsPerRow
            .onEach { value ->
                mutableUiState.update { it.copy(itemsPerRow = value) }
            }
            .launchIn(viewModelScope)


        defaultPreferencesRepository.randomListEntryEnabled
            .onEach { value ->
                mutableUiState.update { it.copy(showRandomButton = value) }
            }
            .launchIn(viewModelScope)

        val repositoryFlow = if (mediaType == MediaType.ANIME) {
            animeRepository.userAnimeList
        } else {
            mangaRepository.userMangaList
        }

        combine(
            repositoryFlow,
            mutableUiState.map { it.listStatus }.distinctUntilChanged(),
            mutableUiState.map { it.listSort }.distinctUntilChanged(),
            isFetching
        ) { list: List<BaseUserMediaList<out BaseMediaNode>>,
            status: ListStatus?,
            sort: MediaSort?,
            fetching: Boolean ->
            var filteredList = list.filter { it.listStatus?.status == status }

            // Explicitly sort the list to match UI selection
            filteredList = when (sort) {
                MediaSort.ANIME_TITLE, MediaSort.MANGA_TITLE -> filteredList.sortedBy { it.node.title }
                MediaSort.SCORE, MediaSort.ANIME_SCORE -> filteredList.sortedByDescending { it.listStatus?.score }
                MediaSort.UPDATED -> filteredList.sortedByDescending { it.listStatus?.updatedAt }
                MediaSort.ANIME_START_DATE, MediaSort.MANGA_START_DATE -> filteredList.sortedByDescending { it.node.startDate }
                else -> filteredList
            }

            mutableUiState.update { state ->
                val alreadyLoaded = state.isStatusLoaded(status)
                state.copy(
                    mediaList = filteredList,
                    isLoading = fetching || (!alreadyLoaded && filteredList.isEmpty()),
                    loadedStatuses = if (!fetching && status != null) state.loadedStatuses + status else state.loadedStatuses
                )
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState
                .distinctUntilChanged { old, new ->
                    old.loadMore == new.loadMore
                            && old.listStatus == new.listStatus
                            && old.listSort == new.listSort
                }
                .filter { it.listStatus != null && it.listSort != null && it.loadMore }
                .collectLatest { uiState ->
                    isFetching.value = true
                    mutableUiState.update {
                        it.copy(
                            isLoadingMore = true,
                            isLoading = true
                        )
                    }
                    val result = if (uiState.mediaType == MediaType.ANIME) {
                        animeRepository.getUserAnimeList(
                            status = uiState.listStatus!!,
                            sort = uiState.listSort!!,
                            page = uiState.nextPage
                        )
                    } else {
                        mangaRepository.getUserMangaList(
                            status = uiState.listStatus!!,
                            sort = uiState.listSort!!,
                            page = uiState.nextPage
                        )
                    }

                    if (result.data != null) {
                        mutableUiState.update {
                            it.copy(
                                loadMore = false,
                                nextPage = result.paging?.next,
                                isLoadingMore = false,
                            )
                        }
                    } else {
                        mutableUiState.update {
                            it.copy(
                                loadMore = false,
                                isLoadingMore = false,
                                message = result.message ?: result.error
                            )
                        }
                    }
                    isFetching.value = false
                }
        }
    }
}
