package com.axiel7.moelist.ui.more.settings.appearance

import com.axiel7.moelist.ui.base.ColorPalette
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.base.state.UiState

data class AppearanceUiState(
    val theme: ThemeStyle = ThemeStyle.FOLLOW_SYSTEM,
    val colorPalette: ColorPalette = ColorPalette.DYNAMIC,
    override val isLoading: Boolean = false,
    override val message: String? = null
) : UiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)
}
