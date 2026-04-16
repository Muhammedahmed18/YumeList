package com.axiel7.moelist.ui.profile

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.Stat
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.LoginRepository
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

                // get manga stats from jikan api because the official api has not implemented it
                user.name?.let { username ->
                    getMangaStats(username)
                }
            }
        }
    }

    private fun getMangaStats(username: String) {
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoadingManga = true, isMangaError = false) }
            try {
                // convert the username to lowercase because a bug in the jikan api
                val jikanUserStats = withContext(Dispatchers.IO) {
                    userRepository.getUserStats(username.lowercase())
                }

                jikanUserStats.data?.manga?.let { stats ->
                    val tempMangaStatList = mutableListOf<Stat<ListStatus>>()
                    tempMangaStatList.add(
                        Stat(
                            type = ListStatus.READING,
                            value = stats.current.toFloat()
                        )
                    )
                    tempMangaStatList.add(
                        Stat(
                            type = ListStatus.COMPLETED,
                            value = stats.completed.toFloat()
                        )
                    )
                    tempMangaStatList.add(
                        Stat(
                            type = ListStatus.ON_HOLD,
                            value = stats.onHold.toFloat()
                        )
                    )
                    tempMangaStatList.add(
                        Stat(
                            type = ListStatus.DROPPED,
                            value = stats.dropped.toFloat()
                        )
                    )
                    tempMangaStatList.add(
                        Stat(
                            type = ListStatus.PLAN_TO_READ,
                            value = stats.planned.toFloat()
                        )
                    )
                    mutableUiState.update {
                        it.copy(
                            mangaStats = tempMangaStatList,
                            userMangaStats = stats,
                            isMangaError = false
                        )
                    }
                } ?: run {
                    mutableUiState.update { it.copy(isMangaError = true) }
                }
            } catch (e: Exception) {
                mutableUiState.update { it.copy(isMangaError = true) }
            } finally {
                mutableUiState.update { it.copy(isLoadingManga = false) }
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
