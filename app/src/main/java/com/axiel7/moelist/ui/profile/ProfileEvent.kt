package com.axiel7.moelist.ui.profile

import com.axiel7.moelist.ui.base.event.UiEvent

interface ProfileEvent : UiEvent {
    fun refreshMangaStats()

    /** Pull to refresh: fetch the profile and stats again, keeping what's on screen meanwhile. */
    fun refresh()
}
