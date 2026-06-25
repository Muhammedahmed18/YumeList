package com.axiel7.moelist.data.model.anime

import androidx.compose.runtime.Composable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StartSeason(
    @SerialName("year")
    val year: Int,
    @SerialName("season")
    val season: Season
) {

    @Composable
    fun seasonYearText() = "${season.localized()} $year"

    /** The season chronologically after this one, rolling the year over after fall. */
    fun next(): StartSeason =
        if (season == Season.FALL) StartSeason(year + 1, Season.WINTER)
        else StartSeason(year, season.next)

    /** The season chronologically before this one, rolling the year back before winter. */
    fun previous(): StartSeason =
        if (season == Season.WINTER) StartSeason(year - 1, Season.FALL)
        else StartSeason(year, season.previous)
}