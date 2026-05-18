package com.axiel7.moelist.data.model.manga

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserMangaStatistics(
    @SerialName("num_items_reading")
    val numItemsReading: Int?,
    @SerialName("num_items_completed")
    val numItemsCompleted: Int?,
    @SerialName("num_items_on_hold")
    val numItemsOnHold: Int?,
    @SerialName("num_items_dropped")
    val numItemsDropped: Int?,
    @SerialName("num_items_plan_to_read")
    val numItemsPlanToRead: Int?,
    @SerialName("num_items")
    val numItems: Int?,
    @SerialName("num_days_read")
    val numDaysRead: Float?,
    @SerialName("num_days_reading")
    val numDaysReading: Float?,
    @SerialName("num_days_completed")
    val numDaysCompleted: Float?,
    @SerialName("num_days_on_hold")
    val numDaysOnHold: Float?,
    @SerialName("num_days_dropped")
    val numDaysDropped: Float?,
    @SerialName("num_days")
    val numDays: Float?,
    @SerialName("num_chapters")
    val numChapters: Int?,
    @SerialName("num_volumes")
    val numVolumes: Int?,
    @SerialName("num_times_reread")
    val numTimesReread: Int?,
    @SerialName("mean_score")
    val meanScore: Float?,
)