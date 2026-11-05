package com.axiel7.moelist.ui.more.settings.appearance

import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.ui.base.ColorPalette
import com.axiel7.moelist.ui.base.StartTab
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.base.state.UiState

data class AppearanceUiState(
    val theme: ThemeStyle = ThemeStyle.FOLLOW_SYSTEM,
    val pureBlack: Boolean = false,
    val colorPalette: ColorPalette = ColorPalette.DYNAMIC,
    val startTab: StartTab = StartTab.LAST_USED,
    val titleLanguage: TitleLanguage = TitleLanguage.ROMAJI,
    val useListTabs: Boolean = false,
    override val isLoading: Boolean = false,
    override val message: String? = null
) : UiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)
}
