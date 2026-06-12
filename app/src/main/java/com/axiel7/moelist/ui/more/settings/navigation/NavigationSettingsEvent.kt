package com.axiel7.moelist.ui.more.settings.navigation

import com.axiel7.moelist.ui.base.StartTab
import com.axiel7.moelist.ui.base.TabletMode
import com.axiel7.moelist.ui.base.event.UiEvent

interface NavigationSettingsEvent : UiEvent {
    fun setStartTab(value: StartTab)
    fun setPinnedNavBar(value: Boolean)
    fun setTabletMode(value: TabletMode)
}
