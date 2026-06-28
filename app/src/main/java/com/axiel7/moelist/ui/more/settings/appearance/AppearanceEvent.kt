package com.axiel7.moelist.ui.more.settings.appearance

import com.axiel7.moelist.ui.base.ColorPalette
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.base.event.UiEvent

interface AppearanceEvent : UiEvent {
    fun setTheme(value: ThemeStyle)
    fun setColorPalette(value: ColorPalette)
}
