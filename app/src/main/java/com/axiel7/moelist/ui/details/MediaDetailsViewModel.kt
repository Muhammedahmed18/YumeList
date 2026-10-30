package com.axiel7.moelist.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.anime.MyAnimeListStatus
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.WeekDay
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.MangaRepository
import com.axiel7.moelist.ui.base.navigation.Route
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import com.axiel7.moelist.utils.ApiErrorMapper
import com.axiel7.moelist.utils.toUiMessage
import com.axiel7.moelist.worker.NotificationWorkerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import kotlin.reflect.typeOf

@Suppress("UNCHECKED_CAST")
class MediaDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
    private val notificationWorkerManager: NotificationWorkerManager,
    defaultPreferencesRepository: DefaultPreferencesRepository,
) : BaseViewModel<MediaDetailsUiState>(), MediaDetailsEvent {

    private val args = savedStateHandle.toRoute<Route.MediaDetails>(
        typeMap = mapOf(typeOf<MediaType>() to MediaType.navType)
    )
    private val mediaType = args.mediaType
    private val mediaId = args.mediaId

    override val mutableUiState = MutableStateFlow(MediaDetailsUiState())

    override fun onChangedMyListStatus(value: BaseMyListStatus?, removed: Boolean) {
        mutableUiState.update {
            when (it.mediaDetails) {
                is AnimeDetails -> {
                    it.copy(
                        mediaDetails = it.mediaDetails.copy(
                            myListStatus = (value as? MyAnimeListStatus).takeIf { !removed }
                        )
                    )
                }

                is MangaDetails -> {
                    it.copy(
                        mediaDetails = it.mediaDetails.copy(
                            myListStatus = (value as? MyMangaListStatus).takeIf { !removed }
                        )
                    )
                }

                else -> it
            }
        }
    }

    override fun setListStatus(status: ListStatus) {
        val state = mutableUiState.value
        // One change at a time: ignore taps while a save or undo is still running
        if (state.isUpdatingStatus) return
        val details = state.mediaDetails ?: return
        val previousEntry = details.myListStatus
        val previous = previousEntry?.status
        val today = LocalDate.now().toString() // ISO yyyy-MM-dd, as the API expects
        val isCompleting = status == ListStatus.COMPLETED
        val isStarting = status.isCurrent() && (previous == null || previous.isPlanning())

        // Show the new status on the button right away, with a loading indicator
        mutableUiState.update { it.copy(pendingStatus = status, isUpdatingStatus = true) }

        viewModelScope.launch(Dispatchers.IO) {
            var error: Throwable? = null
            val result: BaseMyListStatus? = when (details) {
                is AnimeDetails -> animeRepository.updateAnimeEntry(
                    animeId = details.id,
                    status = status,
                    watchedEpisodes = details.numEpisodes?.takeIf { isCompleting && it > 0 },
                    startDate = today.takeIf { isStarting },
                    endDate = today.takeIf { isCompleting },
                    onError = { error = it },
                )

                is MangaDetails -> mangaRepository.updateMangaEntry(
                    mangaId = details.id,
                    status = status,
                    chaptersRead = details.numChapters?.takeIf { isCompleting && it > 0 },
                    volumesRead = details.numVolumes?.takeIf { isCompleting && it > 0 },
                    startDate = today.takeIf { isStarting },
                    endDate = today.takeIf { isCompleting },
                    onError = { error = it },
                )

                else -> null
            }
            if (result != null) onChangedMyListStatus(result)
            finishStatusChange(
                if (result != null) {
                    StatusChangeResult(
                        kind = if (previousEntry == null) StatusChangeResult.Kind.ADDED
                        else StatusChangeResult.Kind.CHANGED,
                        status = status,
                        previous = previousEntry,
                    )
                } else {
                    // The button falls back to the real (unchanged) status
                    StatusChangeResult(
                        kind = StatusChangeResult.Kind.FAILED,
                        status = status,
                        previous = previousEntry,
                        errorMessage = error.toReadableMessage(),
                    )
                }
            )
        }
    }

    override fun undoStatusChange(previous: BaseMyListStatus?) {
        val state = mutableUiState.value
        if (state.isUpdatingStatus) return
        val details = state.mediaDetails ?: return

        mutableUiState.update { it.copy(pendingStatus = previous?.status, isUpdatingStatus = true) }

        viewModelScope.launch(Dispatchers.IO) {
            var error: Throwable? = null
            val success: Boolean
            if (previous == null) {
                // It was a new entry: take it off the list again
                success = when (details) {
                    is AnimeDetails -> animeRepository.deleteAnimeEntry(details.id)
                    is MangaDetails -> mangaRepository.deleteMangaEntry(details.id)
                    else -> false
                }
                if (success) onChangedMyListStatus(null, removed = true)
            } else {
                // Put back the previous status, progress and dates (Completed fills them in)
                val result: BaseMyListStatus? = when (details) {
                    is AnimeDetails -> animeRepository.updateAnimeEntry(
                        animeId = details.id,
                        status = previous.status,
                        watchedEpisodes = previous.progress,
                        startDate = previous.startDate,
                        endDate = previous.finishDate,
                        onError = { error = it },
                    )

                    is MangaDetails -> mangaRepository.updateMangaEntry(
                        mangaId = details.id,
                        status = previous.status,
                        chaptersRead = previous.progress,
                        volumesRead = (previous as? MyMangaListStatus)?.numVolumesRead,
                        startDate = previous.startDate,
                        endDate = previous.finishDate,
                        onError = { error = it },
                    )

                    else -> null
                }
                success = result != null
                if (result != null) onChangedMyListStatus(result)
            }
            finishStatusChange(
                StatusChangeResult(
                    kind = when {
                        !success -> StatusChangeResult.Kind.FAILED
                        previous == null -> StatusChangeResult.Kind.REMOVED
                        else -> StatusChangeResult.Kind.RESTORED
                    },
                    status = previous?.status,
                    previous = null,
                    errorMessage = if (success) null else error.toReadableMessage(),
                    fromUndo = true,
                )
            )
        }
    }

    override fun onStatusResultShown() {
        mutableUiState.update { it.copy(statusResult = null) }
    }

    private fun finishStatusChange(result: StatusChangeResult) {
        mutableUiState.update {
            it.copy(pendingStatus = null, isUpdatingStatus = false, statusResult = result)
        }
    }

    /** "No connection", "Session expired"... instead of a raw exception text */
    private fun Throwable?.toReadableMessage(): String =
        this?.toUiMessage() ?: ApiErrorMapper.mapApiError(null)

    override fun getCharacters() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState.update { it.copy(isLoadingCharacters = true) }

            val result = animeRepository.getAnimeCharacters(
                animeId = mediaId,
                limit = 40,
                offset = null,
                page = null,
            )

            if (result.wasError) {
                mutableUiState.update {
                    it.copy(
                        isLoadingCharacters = false,
                        message = result.message ?: "Error loading characters"
                    )
                }
            } else {
                mutableUiState.update {
                    it.copy(
                        characters = result.data.orEmpty().sortedBy { it.role },
                        isLoadingCharacters = false
                    )
                }
            }
        }
    }

    override fun scheduleAiringAnimeNotification(
        title: String,
        animeId: Int,
        weekDay: WeekDay,
        jpHour: LocalTime
    ) {
        viewModelScope.launch {
            notificationWorkerManager.scheduleAiringAnimeNotification(
                title,
                animeId,
                weekDay,
                jpHour
            )
        }
    }

    override fun scheduleAnimeStartNotification(
        title: String,
        animeId: Int,
        startDate: LocalDate,
    ) {
        viewModelScope.launch {
            notificationWorkerManager.scheduleAnimeStartNotification(title, animeId, startDate)
        }
    }

    override fun removeAiringAnimeNotification(animeId: Int) {
        viewModelScope.launch {
            notificationWorkerManager.removeAiringAnimeNotification(animeId)
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            val mediaDetails = if (mediaType == MediaType.ANIME) {
                animeRepository.getAnimeDetails(mediaId)
            } else {
                mangaRepository.getMangaDetails(mediaId)
            }

            if (mediaDetails == null) showMessage("Unable to reach server")
            else if (mediaDetails.error != null) showMessage(mediaDetails.error)
            else {
                val picturesUrls = listOf(mediaDetails.mainPicture?.large.orEmpty())
                    .plus(mediaDetails.pictures?.map { it.large ?: it.medium.orEmpty() }
                        .orEmpty())

                val animeStatusDistribution = if (mediaType == MediaType.ANIME) {
                    mediaDetails.statistics?.status?.toStats(true)
                } else null

                mutableUiState.update {
                    it.copy(
                        mediaDetails = mediaDetails,
                        statusDistribution = animeStatusDistribution,
                        isLoadingStatusDistribution = mediaType == MediaType.MANGA,
                        relatedAnime = mediaDetails.relatedAnime.orEmpty(),
                        relatedManga = mediaDetails.relatedManga.orEmpty(),
                        picturesUrls = picturesUrls,
                        isLoading = false
                    )
                }

                if (mediaType == MediaType.ANIME
                    && defaultPreferencesRepository.loadCharacters.first()
                ) {
                    getCharacters()
                }

                if (mediaType == MediaType.MANGA) {
                    val mangaStats = mangaRepository.getMangaStatistics(mediaId)
                    mutableUiState.update {
                        it.copy(
                            statusDistribution = mangaStats?.toStats(),
                            isLoadingStatusDistribution = false
                        )
                    }
                }
            }
        }

        defaultPreferencesRepository.hideScores
            .onEach { value ->
                mutableUiState.update { it.copy(hideScore = value) }
            }
            .launchIn(viewModelScope)

        notificationWorkerManager.getNotification(mediaId)
            .onEach { notification ->
                mutableUiState.update { it.copy(notification = notification) }
            }
            .launchIn(viewModelScope)

        notificationWorkerManager.getStartNotification(mediaId)
            .onEach { startNotification ->
                mutableUiState.update { it.copy(startNotification = startNotification) }
            }
            .launchIn(viewModelScope)
    }
}
