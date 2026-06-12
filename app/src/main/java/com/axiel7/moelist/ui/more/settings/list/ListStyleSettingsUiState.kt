package com.axiel7.moelist.ui.more.settings.list

import com.axiel7.moelist.ui.base.ItemsPerRow
import com.axiel7.moelist.ui.base.ListStyle
import com.axiel7.moelist.ui.base.state.UiState

data class ListStyleSettingsUiState(
    val useGeneralListStyle: Boolean = true,
    val generalListStyle: ListStyle = ListStyle.STANDARD,
    val itemsPerRow: ItemsPerRow = ItemsPerRow.DEFAULT,
    val useListTabs: Boolean = false,
    override val isLoading: Boolean = false,
    override val message: String? = null,
) : UiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)
}
