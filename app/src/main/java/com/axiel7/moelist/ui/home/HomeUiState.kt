package com.axiel7.moelist.ui.home

import androidx.compose.runtime.Immutable
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.ui.base.state.UiState

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
    override val message: String? = null
) : UiState() {
    /** True while either section is loading. */
    override val isLoading: Boolean
        get() = isLoadingToday || isLoadingSeason

    override fun setLoading(value: Boolean) =
        copy(isLoadingToday = value, isLoadingSeason = value)

    override fun setMessage(value: String?) = copy(message = value)
}
