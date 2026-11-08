package com.axiel7.moelist.data.model.anime

import com.axiel7.moelist.data.model.media.BaseRecommendation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnimeRecommendation(
    @SerialName("node")
    override val node: AnimeNode,
    @SerialName("num_recommendations")
    override val numRecommendations: Int = 0,
) : BaseRecommendation
