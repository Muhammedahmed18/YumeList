package com.axiel7.moelist.ui.more.settings.appearance

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.ui.base.ColorPalette
import com.axiel7.moelist.ui.base.StartTab
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppearanceViewModel(
    private val defaultPreferencesRepository: DefaultPreferencesRepository
) : BaseViewModel<AppearanceUiState>(), AppearanceEvent {

    override val mutableUiState = MutableStateFlow(AppearanceUiState())

    override fun setTheme(value: ThemeStyle) {
        viewModelScope.launch {
            defaultPreferencesRepository.setTheme(value)
        }
    }

    override fun setPureBlack(value: Boolean) {
        viewModelScope.launch {
            defaultPreferencesRepository.setPureBlack(value)
        }
    }

    override fun setColorPalette(value: ColorPalette) {
        viewModelScope.launch {
            defaultPreferencesRepository.setColorPalette(value)
        }
    }

    override fun setStartTab(value: StartTab) {
        viewModelScope.launch {
            defaultPreferencesRepository.setStartTab(value)
        }
    }

    override fun setTitleLanguage(value: TitleLanguage) {
        viewModelScope.launch {
            defaultPreferencesRepository.setTitleLang(value)
        }
    }

    override fun setUseListTabs(value: Boolean) {
        viewModelScope.launch {
            defaultPreferencesRepository.setUseListTabs(value)
        }
    }

    init {
        defaultPreferencesRepository.theme
            .onEach { value -> mutableUiState.update { it.copy(theme = value) } }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.pureBlack
            .onEach { value -> mutableUiState.update { it.copy(pureBlack = value) } }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.colorPalette
            .onEach { value -> mutableUiState.update { it.copy(colorPalette = value) } }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.startTab
            .filterNotNull()
            .onEach { value -> mutableUiState.update { it.copy(startTab = value) } }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.titleLang
            .onEach { value -> mutableUiState.update { it.copy(titleLanguage = value) } }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.useListTabs
            .onEach { value -> mutableUiState.update { it.copy(useListTabs = value) } }
            .launchIn(viewModelScope)
    }
}
