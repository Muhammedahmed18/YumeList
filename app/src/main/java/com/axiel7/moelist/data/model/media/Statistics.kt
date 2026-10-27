package com.axiel7.moelist.data.model.media

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Statistics(
    @SerialName("status")
    val status: StatisticsStatus? = null,
    @SerialName("num_list_users")
    val numListUsers: Int = 0
)