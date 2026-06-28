package com.axiel7.moelist.ui.more.settings.navigation

import com.axiel7.moelist.ui.base.StartTab
import com.axiel7.moelist.ui.base.TabletMode
import com.axiel7.moelist.ui.base.state.UiState

data class NavigationSettingsUiState(
    val startTab: StartTab = StartTab.LAST_USED,
    val pinnedNavBar: Boolean = false,
    val tabletMode: TabletMode = TabletMode.AUTO,
    override val isLoading: Boolean = false,
    override val message: String? = null,
) : UiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)
}
