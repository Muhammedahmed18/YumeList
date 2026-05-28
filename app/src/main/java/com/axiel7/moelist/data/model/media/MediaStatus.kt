package com.axiel7.moelist.data.model.media

import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.Localizable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MediaStatus : Localizable {
    @SerialName("currently_airing")
    AIRING,

    @SerialName("finished_airing")
    FINISHED_AIRING,

    @SerialName("not_yet_aired")
    NOT_AIRED,

    @SerialName("currently_publishing")
    PUBLISHING,

    @SerialName("finished")
    FINISHED,

    @SerialName("on_hiatus")
    HIATUS,

    @SerialName("discontinued")
    DISCONTINUED;

    override val labelRes: Int
        get() = when (this) {
            AIRING -> R.string.airing
            FINISHED_AIRING -> R.string.finished
            NOT_AIRED -> R.string.not_yet_aired
            PUBLISHING -> R.string.publishing
            FINISHED -> R.string.finished
            HIATUS -> R.string.on_hiatus
            DISCONTINUED -> R.string.discontinued
        }
}