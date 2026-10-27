package com.axiel7.moelist.data.model.media

import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.Localizable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MediaFormat : Localizable {
    @SerialName("tv")
    TV,

    @SerialName("tv_special")
    TV_SPECIAL,

    @SerialName("ova")
    OVA,

    @SerialName("ona")
    ONA,

    @SerialName("movie")
    MOVIE,

    @SerialName("special")
    SPECIAL,

    @SerialName("cm")
    CM,

    @SerialName("pv")
    PV,

    @SerialName("music")
    MUSIC,

    @SerialName("manga")
    MANGA,

    @SerialName("one_shot")
    ONE_SHOT,

    @SerialName("manhwa")
    MANHWA,

    @SerialName("manhua")
    MANHUA,

    @SerialName("novel")
    NOVEL,

    @SerialName("light_novel")
    LIGHT_NOVEL,

    @SerialName("doujinshi")
    DOUJINSHI,

    @SerialName("unknown")
    UNKNOWN;

    override val labelRes: Int
        get() = when (this) {
            TV -> R.string.tv
            TV_SPECIAL -> R.string.tv_special
            OVA -> R.string.ova
            ONA -> R.string.ona
            MOVIE -> R.string.movie
            SPECIAL -> R.string.special
            CM -> R.string.cm
            PV -> R.string.pv
            MUSIC -> R.string.music
            UNKNOWN -> R.string.unknown
            MANGA -> R.string.manga
            ONE_SHOT -> R.string.one_shot
            MANHWA -> R.string.manhwa
            MANHUA -> R.string.manhua
            NOVEL -> R.string.novel
            LIGHT_NOVEL -> R.string.light_novel
            DOUJINSHI -> R.string.doujinshi
        }
}