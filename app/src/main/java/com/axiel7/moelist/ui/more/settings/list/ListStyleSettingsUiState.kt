package com.axiel7.moelist.ui.more.settings.list

import com.axiel7.moelist.ui.base.state.UiState

data class ListStyleSettingsUiState(
    val useListTabs: Boolean = false,
    override val isLoading: Boolean = false,
    override val message: String? = null,
) : UiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)
}
