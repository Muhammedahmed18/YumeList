package com.axiel7.moelist.ui.more.settings.navigation

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.ui.base.StartTab
import com.axiel7.moelist.ui.base.TabletMode
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NavigationSettingsViewModel(
    private val defaultPreferencesRepository: DefaultPreferencesRepository
) : BaseViewModel<NavigationSettingsUiState>(), NavigationSettingsEvent {

    override val mutableUiState = MutableStateFlow(NavigationSettingsUiState())

    override fun setStartTab(value: StartTab) {
        viewModelScope.launch {
            defaultPreferencesRepository.setStartTab(value)
        }
    }

    override fun setPinnedNavBar(value: Boolean) {
        viewModelScope.launch {
            defaultPreferencesRepository.setPinnedNavBar(value)
        }
    }

    override fun setTabletMode(value: TabletMode) {
        viewModelScope.launch {
            defaultPreferencesRepository.setTabletMode(value)
        }
    }

    init {
        defaultPreferencesRepository.startTab
            .filterNotNull()
            .onEach { value -> mutableUiState.update { it.copy(startTab = value) } }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.pinnedNavBar
            .onEach { value -> mutableUiState.update { it.copy(pinnedNavBar = value) } }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.tabletMode
            .filterNotNull()
            .onEach { value -> mutableUiState.update { it.copy(tabletMode = value) } }
            .launchIn(viewModelScope)
    }
}
