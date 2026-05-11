package com.axiel7.moelist.ui.userlist

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.ListType
import com.axiel7.moelist.data.model.media.MediaFormat
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val defaultSort = when (mediaType) {
        MediaType.ANIME -> MediaSort.ANIME_TITLE
        MediaType.MANGA -> MediaSort.MANGA_TITLE
    }

    override val mutableUiState = MutableStateFlow(
        UserMediaListUiState(
            mediaType = mediaType,
            listStatus = initialListStatus,
            listSort = defaultSort
        )
    )

    override fun onChangeStatus(value: ListStatus) {
        if (mutableUiState.value.listStatus == value) return
        viewModelScope.launch {
            when (mutableUiState.value.mediaType) {
                MediaType.ANIME -> defaultPreferencesRepository.setAnimeListStatus(value)
                MediaType.MANGA -> defaultPreferencesRepository.setMangaListStatus(value)
            }
            mutableUiState.update { state ->
                state.copy(
                    listStatus = value,
                    nextPage = null,
                    loadMore = true,
                    isLoading = true,
                    isError = false,
                    scrollToTopTrigger = state.scrollToTopTrigger + 1
                )
            }
        }
    }

    override fun onChangeSort(value: MediaSort) {
        if (mutableUiState.value.listSort == value) return
        viewModelScope.launch {
            when (mutableUiState.value.mediaType) {
                MediaType.ANIME -> defaultPreferencesRepository.setAnimeListSort(value)
                MediaType.MANGA -> defaultPreferencesRepository.setMangaListSort(value)
            }
            mutableUiState.update { state ->
                state.copy(
                    listSort = value,
                    nextPage = null,
                    loadMore = true,
                    isLoading = true,
                    isError = false,
                    scrollToTopTrigger = state.scrollToTopTrigger + 1
                )
            }
        }
    }

    override fun onChangeFormat(value: MediaFormat?) {
        if (mutableUiState.value.selectedFormat == value) return
        mutableUiState.update { state ->
            val filtered = if (value == null) {
                state.mediaList
            } else {
                state.mediaList.filter { it.node.mediaFormat == value }
            }
            state.copy(
                selectedFormat = value,
                filteredMediaList = filtered,
                scrollToTopTrigger = state.scrollToTopTrigger + 1
            )
        }
    }

    override fun onChangeItemMyListStatus(value: BaseMyListStatus?, removed: Boolean) {
        // Handled reactively
    }

    override fun refreshList() {
        mutableUiState.update { state -> state.copy(nextPage = null, loadMore = true, isError = false) }
    }

    override fun loadMore() {
        mutableUiState.value.run {
            if (canLoadMore && !isLoadingMore) {
                mutableUiState.update { state -> state.copy(loadMore = true) }
            }
        }
    }

    override fun onUpdateProgress(item: BaseUserMediaList<out BaseMediaNode>) {
        val newProgress = (item.userProgress() ?: 0) + 1
        onUpdateProgress(item, newProgress)
    }

    override fun onUpdateProgress(item: BaseUserMediaList<out BaseMediaNode>, progress: Int) {
        mutableUiState.update { state -> state.copy(selectedItem = item) }
        viewModelScope.launch(Dispatchers.IO) {
            val nowDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            var newStatus: ListStatus? = null
            var success = false

            // Optimistic update
            withContext(Dispatchers.Main) {
                mutableUiState.update { state ->
                    val newList = state.mediaList.map { 
                        if (it.node.id == item.node.id) item.copyProgress(progress) else it 
                    }
                    val newFiltered = state.filteredMediaList.map {
                        if (it.node.id == item.node.id) item.copyProgress(progress) else it
                    }
                    state.copy(mediaList = newList, filteredMediaList = newFiltered)
                }
            }

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
                    if (result != null) {
                        success = true
                        if (newStatus == ListStatus.COMPLETED && result.score == 0) {
                            viewModelScope.launch { toggleSetScoreDialog(true) }
                        }
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
                    val result = mangaRepository.updateMangaEntry(
                        mangaId = item.node.id,
                        chaptersRead = progress.takeIf { !isVolumeProgress },
                        volumesRead = progress.takeIf { isVolumeProgress },
                        status = newStatus,
                        startDate = nowDate.takeIf {
                            isPlanning || item.listStatus?.progress.isGreaterThanZero()
                        },
                        endDate = nowDate.takeIf { isCompleted }
                    )
                    if (result != null) success = true
                }
            }
            if (success) {
                mutableUiState.update { state ->
                    state.copy(message = "Updated ${item.node.userPreferredTitle()}") 
                }
            } else {
                // Revert optimistic update on failure
                withContext(Dispatchers.Main) {
                    mutableUiState.update { state ->
                        val newList = state.mediaList.map { 
                            if (it.node.id == item.node.id) item else it 
                        }
                        val newFiltered = state.filteredMediaList.map {
                            if (it.node.id == item.node.id) item else it
                        }
                        state.copy(mediaList = newList, filteredMediaList = newFiltered)
                    }
                }
            }
        }
    }

    override fun onUpdateStatus(item: BaseUserMediaList<out BaseMediaNode>, status: ListStatus) {
        mutableUiState.update { state -> state.copy(selectedItem = item) }
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
        mutableUiState.update { state -> state.copy(selectedItem = item) }
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
            mutableUiState.update { state ->
                state.copy(openSetScoreDialog = false, isLoading = false)
            }
        }
    }

    override fun toggleSortDialog(open: Boolean) {
        mutableUiState.update { state -> state.copy(openSortDialog = open) }
    }

    override fun toggleFormatSheet(open: Boolean) {
        mutableUiState.update { state -> state.copy(openFormatSheet = open) }
    }

    override fun toggleSetScoreDialog(open: Boolean) {
        mutableUiState.update { state -> state.copy(openSetScoreDialog = open) }
    }

    override fun toggleActionSheet(open: Boolean) {
        mutableUiState.update { state -> state.copy(openActionSheet = open) }
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
        mutableUiState.update { state -> state.copy(randomId = null) }
    }

    override fun showMessage(message: String?) {
        mutableUiState.update { state -> state.copy(message = message) }
    }

    override fun onMessageDisplayed() {
        mutableUiState.update { state -> state.copy(message = null) }
    }

    private fun sortMediaList(list: List<BaseUserMediaList<out BaseMediaNode>>, sort: MediaSort): List<BaseUserMediaList<out BaseMediaNode>> {
        return when (sort) {
            MediaSort.ANIME_TITLE, MediaSort.MANGA_TITLE -> list.sortedBy { it.node.title }
            MediaSort.SCORE -> list.sortedByDescending { it.listStatus?.score ?: 0 }
            MediaSort.UPDATED -> list.sortedByDescending { it.listStatus?.updatedAt }
            MediaSort.ANIME_START_DATE, MediaSort.MANGA_START_DATE -> list.sortedByDescending { it.node.startDate }
            MediaSort.ANIME_NUM_USERS -> list.sortedByDescending { it.node.numListUsers }
            else -> list
        }
    }

    init {
        if (initialListStatus == null) {
            val listStatusFlow = when (mediaType) {
                MediaType.ANIME -> defaultPreferencesRepository.animeListStatus
                MediaType.MANGA -> defaultPreferencesRepository.mangaListStatus
            }
            viewModelScope.launch {
                val status = listStatusFlow.first()
                mutableUiState.update { state ->
                    state.copy(listStatus = status)
                }
            }
        }

        val listSortFlow = when (mediaType) {
            MediaType.ANIME -> defaultPreferencesRepository.animeListSort
            MediaType.MANGA -> defaultPreferencesRepository.mangaListSort
        }
        viewModelScope.launch {
            val sort = listSortFlow.first() ?: defaultSort
            mutableUiState.update { state ->
                state.copy(listSort = sort)
            }
        }

        combine(
            defaultPreferencesRepository.useGeneralListStyle,
            defaultPreferencesRepository.generalListStyle
        ) { useGeneral, generalStyle ->
            useGeneral to generalStyle
        }.flatMapLatest { (useGeneral, generalStyle) ->
            if (useGeneral) {
                MutableStateFlow(generalStyle)
            } else {
                mutableUiState
                    .filter { it.listStatus != null }
                    .flatMapLatest { state ->
                        ListType(state.listStatus!!, state.mediaType)
                            .stylePreference(defaultPreferencesRepository)
                    }
            }
        }.onEach { listStyle ->
            mutableUiState.update { state -> state.copy(listStyle = listStyle) }
        }.launchIn(viewModelScope)

        defaultPreferencesRepository.gridItemsPerRow
            .onEach { value ->
                mutableUiState.update { state -> state.copy(itemsPerRow = value) }
            }
            .launchIn(viewModelScope)


        defaultPreferencesRepository.randomListEntryEnabled
            .onEach { value ->
                mutableUiState.update { state -> state.copy(showRandomButton = value) }
            }
            .launchIn(viewModelScope)

        // Observe repository flow for reactive updates
        val userListFlow = if (mediaType == MediaType.ANIME) {
            animeRepository.userAnimeList
        } else {
            mangaRepository.userMangaList
        }

        userListFlow
            .onEach { fullList ->
                val currentStatus = mutableUiState.value.listStatus ?: return@onEach
                val currentSort = mutableUiState.value.listSort ?: defaultSort
                val currentFormat = mutableUiState.value.selectedFormat
                
                // 1. Filter and Sort FIRST on background thread to ensure initial alignment
                val statusFilteredList = fullList.filter { it.listStatus?.status == currentStatus }
                
                // Calculate counts based on statusFilteredList
                val counts = statusFilteredList.groupBy { it.node.mediaFormat }
                    .mapValues { it.value.size }
                    .toMutableMap() as MutableMap<MediaFormat?, Int>
                counts[null] = statusFilteredList.size // All count

                val targetList = sortMediaList(statusFilteredList, currentSort)
                val filteredList = if (currentFormat == null) {
                    targetList
                } else {
                    targetList.filter { it.node.mediaFormat == currentFormat }
                }

                withContext(Dispatchers.Main) {
                    mutableUiState.update { it.copy(
                        mediaList = targetList,
                        filteredMediaList = filteredList,
                        formatCounts = counts
                    ) }
                }

                // Update selected item for dialogs
                mutableUiState.value.selectedItem?.let { selected ->
                    fullList.find { it.node.id == selected.node.id }?.let { updated ->
                        if (updated != selected) {
                            mutableUiState.update { state -> state.copy(selectedItem = updated) }
                        }
                    }
                }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState
                .distinctUntilChanged { old, new ->
                    old.loadMore == new.loadMore
                            && old.listStatus == new.listStatus
                            && old.listSort == new.listSort
                }
                .filter { it.listStatus != null && it.listSort != null && it.loadMore }
                .collectLatest { uiState ->
                    mutableUiState.update { state ->
                        state.copy(
                            isLoadingMore = true,
                            isLoading = uiState.nextPage == null,
                            isError = false
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

                    withContext(Dispatchers.Main) {
                        if (result.data != null) {
                            mutableUiState.update { state ->
                                state.copy(
                                    loadMore = false,
                                    nextPage = result.paging?.next,
                                    isLoadingMore = false,
                                    isLoading = false,
                                    isError = false,
                                    loadedStatuses = state.loadedStatuses + uiState.listStatus!!
                                )
                            }
                        } else {
                            mutableUiState.update { state ->
                                state.copy(
                                    loadMore = false,
                                    isLoadingMore = false,
                                    isLoading = false,
                                    isError = state.mediaList.isEmpty(),
                                    message = result.message ?: result.error,
                                    loadedStatuses = state.loadedStatuses + uiState.listStatus!!
                                )
                            }
                        }
                    }
                }
        }
    }
}
