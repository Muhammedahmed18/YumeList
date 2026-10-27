package com.axiel7.moelist.ui.more.settings.list

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListStyleSettingsViewModel(
    private val defaultPreferencesRepository: DefaultPreferencesRepository
) : BaseViewModel<ListStyleSettingsUiState>(), ListStyleSettingsEvent {

    override val mutableUiState = MutableStateFlow(ListStyleSettingsUiState())

    override fun setUseListTabs(value: Boolean) {
        viewModelScope.launch {
            defaultPreferencesRepository.setUseListTabs(value)
        }
    }

    init {
        defaultPreferencesRepository.useListTabs
            .onEach { value -> mutableUiState.update { it.copy(useListTabs = value) } }
            .launchIn(viewModelScope)
    }
}
