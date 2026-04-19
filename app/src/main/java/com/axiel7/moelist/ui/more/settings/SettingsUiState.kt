package com.axiel7.moelist.ui.more.settings

import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.ui.base.ItemsPerRow
import com.axiel7.moelist.ui.base.ListStyle
import com.axiel7.moelist.ui.base.StartTab
import com.axiel7.moelist.ui.base.TabletMode
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.base.state.UiState

data class SettingsUiState(
    val theme: ThemeStyle = ThemeStyle.FOLLOW_SYSTEM,
    val useBlackColors: Boolean = false,
    val useMonochrome: Boolean = false,
    val showNsfw: Boolean = false,
    val hideScores: Boolean = false,
    val useGeneralListStyle: Boolean = true,
    val generalListStyle: ListStyle = ListStyle.STANDARD,
    val itemsPerRow: ItemsPerRow = ItemsPerRow.DEFAULT,
    val startTab: StartTab = StartTab.LAST_USED,
    val tabletMode: TabletMode = TabletMode.AUTO,
    val pinnedNavBar: Boolean = false,
    val titleLanguage: TitleLanguage = TitleLanguage.ROMAJI,
    val loadCharacters: Boolean = false,
    val randomListEntryEnabled: Boolean = false,
    override val isLoading: Boolean = false,
    override val message: String? = null
) : UiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)
}
