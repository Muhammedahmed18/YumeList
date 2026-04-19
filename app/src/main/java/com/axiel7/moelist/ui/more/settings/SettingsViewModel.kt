package com.axiel7.moelist.ui.more.settings

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.ui.base.ItemsPerRow
import com.axiel7.moelist.ui.base.ListStyle
import com.axiel7.moelist.ui.base.StartTab
import com.axiel7.moelist.ui.base.TabletMode
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val defaultPreferencesRepository: DefaultPreferencesRepository
) : BaseViewModel<SettingsUiState>(), SettingsEvent {

    override val mutableUiState = MutableStateFlow(SettingsUiState())

    override fun setTheme(value: ThemeStyle) {
        viewModelScope.launch {
            defaultPreferencesRepository.setTheme(value)
        }
    }

    override fun setUseBlackColors(value: Boolean) {
        viewModelScope.launch {
            defaultPreferencesRepository.setUseBlackColors(value)
            if (!value) {
                defaultPreferencesRepository.setUseMonochrome(false)
            }
        }
    }

    override fun setUseMonochrome(value: Boolean) {
        viewModelScope.launch {
            defaultPreferencesRepository.setUseMonochrome(value)
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

    override fun setUseGeneralListStyle(value: Boolean) {
        viewModelScope.launch {
            defaultPreferencesRepository.setUseGeneralListStyle(value)
        }
    }

    override fun setGeneralListStyle(value: ListStyle) {
        viewModelScope.launch {
            defaultPreferencesRepository.setGeneralListStyle(value)
        }
    }

    override fun setItemsPerRow(value: ItemsPerRow) {
        viewModelScope.launch {
            defaultPreferencesRepository.setGridItemsPerRow(value)
        }
    }

    override fun setStartTab(value: StartTab) {
        viewModelScope.launch {
            defaultPreferencesRepository.setStartTab(value)
        }
    }

    override fun setTabletMode(value: TabletMode) {
        viewModelScope.launch {
            defaultPreferencesRepository.setTabletMode(value)
        }
    }

    override fun setPinnedNavBar(value: Boolean) {
        viewModelScope.launch {
            defaultPreferencesRepository.setPinnedNavBar(value)
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
        defaultPreferencesRepository.theme
            .onEach { value ->
                mutableUiState.update { it.copy(theme = value) }
            }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.useBlackColors
            .onEach { value ->
                mutableUiState.update { it.copy(useBlackColors = value) }
            }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.useMonochrome
            .onEach { value ->
                mutableUiState.update { it.copy(useMonochrome = value) }
            }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.nsfw
            .onEach { value ->
                mutableUiState.update { it.copy(showNsfw = value) }
            }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.hideScores
            .onEach { value ->
                mutableUiState.update { it.copy(hideScores = value) }
            }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.useGeneralListStyle
            .onEach { value ->
                mutableUiState.update { it.copy(useGeneralListStyle = value) }
            }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.generalListStyle
            .onEach { value ->
                mutableUiState.update { it.copy(generalListStyle = value) }
            }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.gridItemsPerRow
            .onEach { value ->
                mutableUiState.update { it.copy(itemsPerRow = value) }
            }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.startTab
            .filterNotNull()
            .onEach { value ->
                mutableUiState.update { it.copy(startTab = value) }
            }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.tabletMode
            .filterNotNull()
            .onEach { value ->
                mutableUiState.update { it.copy(tabletMode = value) }
            }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.pinnedNavBar
            .onEach { value ->
                mutableUiState.update { it.copy(pinnedNavBar = value) }
            }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.titleLang
            .onEach { value ->
                mutableUiState.update { it.copy(titleLanguage = value) }
            }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.loadCharacters
            .onEach { value ->
                mutableUiState.update { it.copy(loadCharacters = value) }
            }
            .launchIn(viewModelScope)

        defaultPreferencesRepository.randomListEntryEnabled
            .onEach { value ->
                mutableUiState.update { it.copy(randomListEntryEnabled = value) }
            }
            .launchIn(viewModelScope)
    }
}
