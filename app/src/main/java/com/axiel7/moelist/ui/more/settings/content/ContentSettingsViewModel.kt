package com.axiel7.moelist.ui.more.settings.content

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContentSettingsViewModel(
    private val defaultPreferencesRepository: DefaultPreferencesRepository
) : BaseViewModel<ContentUiState>(), ContentEvent {

    override val mutableUiState = MutableStateFlow(ContentUiState())

    override fun setTitleLanguage(value: TitleLanguage) {
        viewModelScope.launch {
            defaultPreferencesRepository.setTitleLang(value)
        }
    }

    override fun setShowNsfw(value: Boolean) {
        viewModelScope.launch {
            defaultPreferencesRepository.setNsfw(value)
        }
    }

    override fun setHideScores(value: Boolean) {
        viewModelScope.launch {
            defaultPreferencesRepository.setHideScores(value)
        }
    }

    override fun setLoadCharacters(value: Boolean) {
        viewModelScope.launch {
            defaultPreferencesRepository.setLoadCharacters(value)
        }
    }

    override fun setRandomListEntryEnabled(value: Boolean) {
        viewModelScope.launch {
            defaultPreferencesRepository.setRandomListEntryEnabled(value)
        }
    }

    init {
        defaultPreferencesRepository.titleLang
            .onEach { value -> mutableUiState.update { it.copy(titleLanguage = value) } }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.nsfw
            .onEach { value -> mutableUiState.update { it.copy(showNsfw = value) } }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.hideScores
            .onEach { value -> mutableUiState.update { it.copy(hideScores = value) } }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.loadCharacters
            .onEach { value -> mutableUiState.update { it.copy(loadCharacters = value) } }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.randomListEntryEnabled
            .onEach { value -> mutableUiState.update { it.copy(randomListEntryEnabled = value) } }
            .launchIn(viewModelScope)
    }
}
