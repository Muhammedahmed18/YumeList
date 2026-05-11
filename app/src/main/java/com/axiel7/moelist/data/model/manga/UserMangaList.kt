package com.axiel7.moelist.data.model.manga

import com.axiel7.moelist.data.model.media.BaseUserMediaList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserMangaList(
    @SerialName("node")
    override val node: MangaNode,
    @SerialName("list_status")
    override val listStatus: MyMangaListStatus? = null,
) : BaseUserMediaList<MangaNode>() {

    override fun copyProgress(progress: Int): UserMangaList {
        return if (listStatus?.isUsingVolumeProgress() == true) {
            copy(listStatus = listStatus.copy(numVolumesRead = progress))
        } else {
            copy(listStatus = listStatus?.copy(progress = progress))
        }
    }
}