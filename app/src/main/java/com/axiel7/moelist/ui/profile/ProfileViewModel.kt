package com.axiel7.moelist.ui.profile

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.Stat
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.LoginRepository
import com.axiel7.moelist.data.repository.UserRepository
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val loginRepository: LoginRepository,
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

    private fun refreshUserProfile() {
        viewModelScope.launch {
            setLoading(true)
            val user = withContext(Dispatchers.IO) { userRepository.getMyUser() }

            if (user == null || user.message != null) {
                showMessage(user?.message)
                setLoading(false)
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
                        isLoadingManga = user.name != null,
                    )
                }

                // MAL's API has no manga statistics: count them from the user's manga list
                user.name?.let { username ->
                    getMangaStats(username)
                }
            }
        }
    }

    /**
     * Manga stats: the counts, chapters, volumes, mean score and rereads are counted from the
     * user's own MAL manga list (MAL's API has no manga statistics). Only "days read" comes from
     * Jikan, fetched at the same time; if Jikan fails, just that tile shows "─".
     * If MAL fails, the stats show "─" with a retry button and the real reason in a snackbar.
     */
    private fun getMangaStats(username: String) {
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoadingManga = true, isMangaError = false) }
            // Jikan's usernames are case sensitive in a buggy way: lowercase works
            val jikanDays = async(Dispatchers.IO) {
                userRepository.getUserStats(username.lowercase()).data?.manga?.days
            }
            val result = withContext(Dispatchers.IO) { userRepository.getMyMangaStats() }
            val stats = result.data

            if (stats == null) {
                jikanDays.cancel()
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
            // Show the MAL numbers right away; "days read" fills in when Jikan answers
            mutableUiState.update {
                it.copy(
                    mangaStats = tempMangaStatList,
                    userMangaStats = stats,
                    isMangaError = false,
                    isLoadingManga = false,
                )
            }
            jikanDays.await()?.let { days ->
                mutableUiState.update { it.copy(userMangaStats = it.userMangaStats?.copy(days = days)) }
            }
        }
    }

    override fun refreshMangaStats() {
        uiState.value.user?.name?.let { getMangaStats(it) }
    }

    override fun logOut() {
        viewModelScope.launch {
            loginRepository.logOut()
        }
    }
}
