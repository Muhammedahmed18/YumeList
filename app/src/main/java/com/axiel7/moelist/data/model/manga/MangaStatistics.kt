package com.axiel7.moelist.data.model.manga

import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.Stat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JikanMangaStatisticsResponse(
    val data: MangaStatisticsData? = null
)

@Serializable
data class MangaStatisticsData(
    val reading: Int = 0,
    val completed: Int = 0,
    @SerialName("on_hold")
    val onHold: Int = 0,
    val dropped: Int = 0,
    @SerialName("plan_to_read")
    val planToRead: Int = 0,
    val total: Int = 0,
) {
    fun toStats() = listOf(
        Stat(type = ListStatus.READING, value = reading.toFloat()),
        Stat(type = ListStatus.COMPLETED, value = completed.toFloat()),
        Stat(type = ListStatus.ON_HOLD, value = onHold.toFloat()),
        Stat(type = ListStatus.DROPPED, value = dropped.toFloat()),
        Stat(type = ListStatus.PLAN_TO_READ, value = planToRead.toFloat()),
    ).sortedByDescending { it.value }
}
