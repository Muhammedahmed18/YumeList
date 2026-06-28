package com.axiel7.moelist.ui.more.settings.appearance

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.ui.base.ColorPalette
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

    override fun setColorPalette(value: ColorPalette) {
        viewModelScope.launch {
            defaultPreferencesRepository.setColorPalette(value)
        }
    }

    init {
        defaultPreferencesRepository.theme
            .onEach { value -> mutableUiState.update { it.copy(theme = value) } }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.colorPalette
            .onEach { value -> mutableUiState.update { it.copy(colorPalette = value) } }
            .launchIn(viewModelScope)
    }
}
