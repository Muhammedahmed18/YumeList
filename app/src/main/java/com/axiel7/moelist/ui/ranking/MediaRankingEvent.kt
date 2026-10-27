package com.axiel7.moelist.ui.ranking

import com.axiel7.moelist.ui.base.event.PagedUiEvent

interface MediaRankingEvent : PagedUiEvent {
    /** Reload from the first page, e.g. after the initial load failed. */
    fun retry()
}