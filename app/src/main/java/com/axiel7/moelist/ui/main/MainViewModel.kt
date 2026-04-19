package com.axiel7.moelist.ui.main

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.LoginRepository
import com.axiel7.moelist.data.repository.UserRepository
import com.axiel7.moelist.ui.base.ThemeStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SessionStatus {
    LOADING,
    LOGGED_IN,
    LOGGED_OUT
}

class MainViewModel(
    private val loginRepository: LoginRepository,
    private val userRepository: UserRepository,
    private val defaultPreferencesRepository: DefaultPreferencesRepository
) : ViewModel() {

    val titleLanguage = defaultPreferencesRepository.titleLang

    val startTab = defaultPreferencesRepository.startTab

    val lastTab = defaultPreferencesRepository.lastTab

    fun saveLastTab(value: Int) = viewModelScope.launch {
        defaultPreferencesRepository.setLastTab(value)
    }

    val pinnedNavBar = defaultPreferencesRepository.pinnedNavBar

    val tabletMode = defaultPreferencesRepository.tabletMode

    val theme = defaultPreferencesRepository.theme
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeStyle.FOLLOW_SYSTEM)

    val useBlackColors = defaultPreferencesRepository.useBlackColors

    val useMonochrome = defaultPreferencesRepository.useMonochrome

    val isOnboardingCompleted = defaultPreferencesRepository.isOnboardingCompleted
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val accessToken = defaultPreferencesRepository.accessToken
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val isParsingIntent = MutableStateFlow(false)

    val sessionStatus = combine(defaultPreferencesRepository.accessToken, isParsingIntent) { token, parsing ->
        if (parsing) SessionStatus.LOADING
        else if (token.isNullOrEmpty()) SessionStatus.LOGGED_OUT
        else SessionStatus.LOGGED_IN
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SessionStatus.LOADING)

    val useListTabs = defaultPreferencesRepository.useListTabs
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val profilePicture = defaultPreferencesRepository.profilePicture
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val username = defaultPreferencesRepository.username
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        // Sanity check: If we have a token but no profile picture, fetch it.
        viewModelScope.launch {
            combine(accessToken, profilePicture) { token, picture ->
                token != null && picture == null
            }.collectLatest { shouldFetch ->
                if (shouldFetch) {
                    fetchUserData()
                }
            }
        }
    }

    suspend fun generateLoginUrl(): String {
        return loginRepository.generateLoginUrl()
    }

    fun parseIntentData(uri: Uri) = viewModelScope.launch {
        isParsingIntent.value = true
        try {
            val code = uri.getQueryParameter("code")
            val receivedState = uri.getQueryParameter("state")
            if (code != null && receivedState == LoginRepository.STATE) {
                val response = loginRepository.getAccessToken(code)
                if (response.data != null) {
                    fetchUserData()
                }
            }
        } finally {
            isParsingIntent.value = false
        }
    }

    private fun fetchUserData() = viewModelScope.launch {
        val user = withContext(Dispatchers.IO) { userRepository.getMyUser() }
        user?.picture?.let {
            defaultPreferencesRepository.setProfilePicture(it)
        }
        user?.name?.let {
            defaultPreferencesRepository.setUsername(it)
        }
    }

    fun setTheme(value: ThemeStyle) = viewModelScope.launch {
        defaultPreferencesRepository.setTheme(value)
    }

    fun setUseBlackColors(value: Boolean) = viewModelScope.launch {
        defaultPreferencesRepository.setUseBlackColors(value)
    }

    fun setUseMonochrome(value: Boolean) = viewModelScope.launch {
        defaultPreferencesRepository.setUseMonochrome(value)
    }

    fun completeOnboarding() = viewModelScope.launch {
        defaultPreferencesRepository.setOnboardingCompleted(true)
    }
}
