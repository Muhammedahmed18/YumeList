package com.axiel7.moelist.data.model.media

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatisticsStatus(
    @SerialName("watching")
    val watching: Int? = null,
    @SerialName("reading")
    val reading: Int? = null,
    @SerialName("completed")
    val completed: Int = 0,
    @SerialName("on_hold")
    val onHold: Int = 0,
    @SerialName("dropped")
    val dropped: Int = 0,
    @SerialName("plan_to_watch")
    val planToWatch: Int? = null,
    @SerialName("plan_to_read")
    val planToRead: Int? = null
) {
    fun toStats(isAnime: Boolean) = listOf(
        Stat(
            type = if (isAnime) ListStatus.WATCHING else ListStatus.READING,
            value = (if (isAnime) watching else reading)?.toFloat() ?: 0f
        ),
        Stat(
            type = ListStatus.COMPLETED,
            value = completed.toFloat()
        ),
        Stat(
            type = ListStatus.ON_HOLD,
            value = onHold.toFloat()
        ),
        Stat(
            type = ListStatus.DROPPED,
            value = dropped.toFloat()
        ),
        Stat(
            type = if (isAnime) ListStatus.PLAN_TO_WATCH else ListStatus.PLAN_TO_READ,
            value = (if (isAnime) planToWatch else planToRead)?.toFloat() ?: 0f
        )
    ).sortedByDescending { it.value }
}