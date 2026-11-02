package com.axiel7.moelist.ui.home

import com.axiel7.moelist.ui.base.event.UiEvent

interface HomeEvent : UiEvent {
    /** Loads both sections now (first load, Retry). */
    fun initRequestChain(isLoggedIn: Boolean)

    /**
     * Loads only if needed: Home is rebuilt every time you come back to it, and this skips the
     * reload when both sections already loaded for the same login state.
     */
    fun loadIfNeeded(isLoggedIn: Boolean) = initRequestChain(isLoggedIn)
}
