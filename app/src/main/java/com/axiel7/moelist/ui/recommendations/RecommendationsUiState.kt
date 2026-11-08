package com.axiel7.moelist.ui.recommendations

import androidx.compose.runtime.Immutable
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.recommendation.ForYouResult
import com.axiel7.moelist.ui.base.state.UiState
import com.axiel7.moelist.ui.recommendations.composables.RecommendationActionResult

/**
 * One tab (anime or manga) of the Recommendations screen.
 *
 * @param isRefreshing a pull-to-refresh rebuild is running (what's shown stays)
 * @param statuses list status of titles the app knows are on the user's list
 */
@Immutable
data class RecommendationsUiState(
    val mediaType: MediaType,
    val result: ForYouResult? = null,
    val isRefreshing: Boolean = false,
    val statuses: Map<Int, ListStatus> = emptyMap(),
    val hideScore: Boolean = false,
    val actionResult: RecommendationActionResult? = null,
    override val isLoading: Boolean = true,
    override val message: String? = null,
) : UiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)

    val hasContent get() = !result?.items.isNullOrEmpty()
}
