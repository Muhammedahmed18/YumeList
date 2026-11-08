package com.axiel7.moelist.ui.recommendations

import com.axiel7.moelist.data.model.recommendation.RecommendedMedia
import com.axiel7.moelist.ui.base.event.UiEvent

interface RecommendationsEvent : UiEvent {
    /** First load (cached result when fresh). */
    fun load(isLoggedIn: Boolean)

    /** Pull to refresh / Retry: rebuild from the current list. */
    fun refresh(isLoggedIn: Boolean)

    fun addToPlan(item: RecommendedMedia)
    fun notInterested(item: RecommendedMedia)
    fun undoNotInterested(item: RecommendedMedia)
    fun onActionResultShown()
}
