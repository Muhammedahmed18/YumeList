package com.axiel7.moelist.ui.profile

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.Stat
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.UserRepository
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val defaultPreferencesRepository: DefaultPreferencesRepository
) : BaseViewModel<ProfileUiState>(), ProfileEvent {

    override val mutableUiState = MutableStateFlow(ProfileUiState())

    init {
        // Observe profile picture from repository for instant UI updates
        viewModelScope.launch {
            defaultPreferencesRepository.profilePicture.collectLatest { picture ->
                mutableUiState.update { it.copy(profilePictureUrl = picture) }
            }
        }

        refreshUserProfile()
    }

    /**
     * @param isRefresh pull to refresh: keep the current data on screen and only flag
     * [ProfileUiState.isRefreshing], instead of the first-load skeletons.
     */
    private fun refreshUserProfile(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                mutableUiState.update { it.copy(isRefreshing = true) }
            } else {
                setLoading(true)
            }
            val user = withContext(Dispatchers.IO) { userRepository.getMyUser() }

            if (user == null || user.message != null) {
                showMessage(user?.message)
                mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        // Manga stats need the username, so they can't load either
                        isLoadingManga = false,
                        isMangaError = it.mangaStats.isEmpty(),
                        isProfileError = it.user == null,
                    )
                }
            } else {
                val tempAnimeStatList = mutableListOf<Stat<ListStatus>>()
                user.animeStatistics?.let { stats ->
                    tempAnimeStatList.add(
                        Stat(
                            type = ListStatus.WATCHING,
                            value = stats.numItemsWatching?.toFloat() ?: 0f
                        )
                    )
                    tempAnimeStatList.add(
                        Stat(
                            type = ListStatus.COMPLETED,
                            value = stats.numItemsCompleted?.toFloat() ?: 0f
                        )
                    )
                    tempAnimeStatList.add(
                        Stat(
                            type = ListStatus.ON_HOLD,
                            value = stats.numItemsOnHold?.toFloat() ?: 0f
                        )
                    )
                    tempAnimeStatList.add(
                        Stat(
                            type = ListStatus.DROPPED,
                            value = stats.numItemsDropped?.toFloat() ?: 0f
                        )
                    )
                    tempAnimeStatList.add(
                        Stat(
                            type = ListStatus.PLAN_TO_WATCH,
                            value = stats.numItemsPlanToWatch?.toFloat() ?: 0f
                        )
                    )

                    // Sync picture if it changed on the server
                    if (user.picture != null && user.picture != mutableUiState.value.profilePictureUrl) {
                        defaultPreferencesRepository.setProfilePicture(user.picture)
                    }
                }

                mutableUiState.update {
                    it.copy(
                        user = user,
                        animeStats = tempAnimeStatList,
                        isLoading = false,
                        isRefreshing = false,
                        isProfileError = false,
                        isLoadingManga = user.name != null,
                    )
                }

                // MAL's API has no manga statistics: count them from the user's manga list
                getMangaStats()
            }
        }
    }

    /**
     * Manga status counts, counted from the user's own MAL manga list (MAL's API has no manga
     * statistics). If it fails, the cards show a retry button and the reason in a snackbar.
     */
    private fun getMangaStats() {
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoadingManga = true, isMangaError = false) }
            val result = withContext(Dispatchers.IO) { userRepository.getMyMangaStats() }
            val stats = result.data

            if (stats == null) {
                mutableUiState.update { it.copy(isMangaError = true, isLoadingManga = false) }
                showMessage(result.message ?: result.error)
                return@launch
            }

            val tempMangaStatList = listOf(
                Stat(type = ListStatus.READING, value = stats.current.toFloat()),
                Stat(type = ListStatus.COMPLETED, value = stats.completed.toFloat()),
                Stat(type = ListStatus.ON_HOLD, value = stats.onHold.toFloat()),
                Stat(type = ListStatus.DROPPED, value = stats.dropped.toFloat()),
                Stat(type = ListStatus.PLAN_TO_READ, value = stats.planned.toFloat()),
            )
            mutableUiState.update {
                it.copy(
                    mangaStats = tempMangaStatList,
                    isMangaError = false,
                    isLoadingManga = false,
                )
            }
        }
    }

    override fun refreshMangaStats() {
        if (uiState.value.user != null) getMangaStats()
    }

    override fun refresh() {
        if (uiState.value.isRefreshing || uiState.value.isLoading) return
        refreshUserProfile(isRefresh = true)
    }
}
