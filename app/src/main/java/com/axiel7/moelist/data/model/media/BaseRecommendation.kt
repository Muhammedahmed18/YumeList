package com.axiel7.moelist.data.model.media

/**
 * One "if you liked this, try…" entry of a details response (MAL's `recommendations` field):
 * the recommended title and how many MAL users recommended it.
 */
interface BaseRecommendation {
    val node: BaseMediaNode
    val numRecommendations: Int
}
