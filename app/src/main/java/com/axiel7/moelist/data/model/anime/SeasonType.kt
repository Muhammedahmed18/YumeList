package com.axiel7.moelist.data.model.anime

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.Localizable
import com.axiel7.moelist.utils.SeasonCalendar

enum class SeasonType : Localizable {
    PREVIOUS,
    CURRENT,
    NEXT;

    @Composable
    override fun localized() = when (this) {
        PREVIOUS -> stringResource(R.string.previous_season)
        CURRENT -> stringResource(R.string.current_season)
        NEXT -> stringResource(R.string.next_season)
    }

    val season
        get() = when (this) {
            PREVIOUS -> SeasonCalendar.prevStartSeason
            CURRENT -> SeasonCalendar.currentStartSeason
            NEXT -> SeasonCalendar.nextStartSeason
        }

    companion object {
        /**
         * The relative type matching the given [startSeason] against today, or null when the
         * season is further away than the immediate previous/current/next window.
         */
        fun of(startSeason: StartSeason): SeasonType? =
            entries.find { it.season == startSeason }
    }
}