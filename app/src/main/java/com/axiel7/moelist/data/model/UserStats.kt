package com.axiel7.moelist.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Jikan's user statistics. Every field has a default, so a missing or `null` value (Jikan is
 * unofficial and not always complete) no longer fails the whole response.
 *
 * [MangaStats] is also built locally from the user's MAL manga list (MAL's official API has no
 * manga statistics); then only [MangaStats.days] comes from Jikan, and stays `null` if Jikan is
 * unavailable.
 */
@Serializable
data class UserStats(
    val anime: AnimeStats = AnimeStats(),
    val manga: MangaStats = MangaStats(),
) {

    @Serializable
    data class AnimeStats(
        @SerialName("days_watched")
        override val days: Float = 0f,
        @SerialName("mean_score")
        override val meanScore: Float = 0f,
        @SerialName("watching")
        override val current: Int = 0,
        @SerialName("completed")
        override val completed: Int = 0,
        @SerialName("on_hold")
        override val onHold: Int = 0,
        @SerialName("dropped")
        override val dropped: Int = 0,
        @SerialName("plan_to_watch")
        override val planned: Int = 0,
        @SerialName("total_entries")
        override val totalEntries: Int = 0,
        @SerialName("rewatched")
        override val repeat: Int = 0,
        @SerialName("episodes_watched")
        val episodesWatched: Int = 0,
    ) : MediaStats

    @Serializable
    data class MangaStats(
        /** `null` when unknown (only Jikan provides it). */
        @SerialName("days_read")
        override val days: Float? = null,
        @SerialName("mean_score")
        override val meanScore: Float = 0f,
        @SerialName("reading")
        override val current: Int = 0,
        @SerialName("completed")
        override val completed: Int = 0,
        @SerialName("on_hold")
        override val onHold: Int = 0,
        @SerialName("dropped")
        override val dropped: Int = 0,
        @SerialName("plan_to_read")
        override val planned: Int = 0,
        @SerialName("total_entries")
        override val totalEntries: Int = 0,
        @SerialName("reread")
        override val repeat: Int = 0,
        @SerialName("chapters_read")
        val chaptersRead: Int = 0,
        @SerialName("volumes_read")
        val volumesRead: Int = 0,
    ) : MediaStats

    interface MediaStats {
        val days: Float?
        val meanScore: Float
        val current: Int
        val completed: Int
        val onHold: Int
        val dropped: Int
        val planned: Int
        val totalEntries: Int
        val repeat: Int
    }
}
