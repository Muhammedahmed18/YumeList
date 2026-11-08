package com.axiel7.moelist.ui.home

import androidx.compose.runtime.Immutable
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.recommendation.ForYouResult
import com.axiel7.moelist.ui.base.state.UiState
import com.axiel7.moelist.ui.recommendations.composables.RecommendationActionResult

/**
 * Home state. Today and This Season load independently, each with its own flags:
 * - `isLoading…`: a request for that section is running
 * - `…Loaded`: that section has loaded successfully at least once. After that, a reload keeps
 *   what's on screen (cards or the empty card) instead of going back to the skeleton.
 */
@Immutable
data class HomeUiState(
    val todayAnimes: List<AnimeRanking> = emptyList(),
    val seasonalAnimes: List<AnimeSeasonal> = emptyList(),
    val hideScore: Boolean = false,
    val isLoadingToday: Boolean = true,
    val isLoadingSeason: Boolean = true,
    val todayLoaded: Boolean = false,
    val seasonLoaded: Boolean = false,
    // For You: its own flags and error, so a failure here never turns Today / Season into an error
    val showForYou: Boolean = true,
    val forYou: ForYouResult? = null,
    val isLoadingForYou: Boolean = false,
    val forYouLoaded: Boolean = false,
    val forYouError: String? = null,
    val forYouStatuses: Map<Int, ListStatus> = emptyMap(),
    val recommendationAction: RecommendationActionResult? = null,
    override val message: String? = null
) : UiState() {
    /** True while either section is loading. */
    override val isLoading: Boolean
        get() = isLoadingToday || isLoadingSeason

    override fun setLoading(value: Boolean) =
        copy(isLoadingToday = value, isLoadingSeason = value)

    override fun setMessage(value: String?) = copy(message = value)
}
