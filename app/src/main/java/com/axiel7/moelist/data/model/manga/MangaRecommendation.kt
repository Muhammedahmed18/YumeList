package com.axiel7.moelist.data.model.manga

import com.axiel7.moelist.data.model.media.BaseRecommendation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MangaRecommendation(
    @SerialName("node")
    override val node: MangaNode,
    @SerialName("num_recommendations")
    override val numRecommendations: Int = 0,
) : BaseRecommendation
