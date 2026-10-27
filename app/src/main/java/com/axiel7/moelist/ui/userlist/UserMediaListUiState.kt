package com.axiel7.moelist.ui.userlist

import androidx.compose.runtime.Stable
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.manga.MangaNode
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaFormat
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.state.PagedUiState
import java.time.LocalDate

@Stable
data class UserMediaListUiState(
    val mediaType: MediaType,
    val listStatus: ListStatus?,
    val listSort: MediaSort? = null,
    val mediaList: List<BaseUserMediaList<out BaseMediaNode>> = emptyList(),
    val filteredMediaList: List<BaseUserMediaList<out BaseMediaNode>> = emptyList(),
    val selectedFormat: MediaFormat? = null,
    val formatCounts: Map<MediaFormat?, Int> = emptyMap(),
    val isLoadingMore: Boolean = false,
    val loadedStatuses: Set<ListStatus> = emptySet(),
    val openSortDialog: Boolean = false,
    val openFormatSheet: Boolean = false,
    val openSetScoreDialog: Boolean = false,
    val setScoreDialogFinishDate: LocalDate? = null,
    val openActionSheet: Boolean = false,
    val selectedItem: BaseUserMediaList<*>? = null,
    val showRandomButton: Boolean = false,
    val randomId: Int? = null,
    val isLoadingRandom: Boolean = false,
    val scrollToTopTrigger: Int = 0,
    val isError: Boolean = false,
    override val nextPage: String? = null,
    override val loadMore: Boolean = true,
    override val isLoading: Boolean = true,
    override val message: String? = null,
    /** Title of the entry just updated (+1 / progress), shown once as a success snackbar. */
    val updatedTitle: String? = null,
) : PagedUiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    // Clearing the message also clears the one-shot success title
    override fun setMessage(value: String?) =
        copy(message = value, updatedTitle = if (value == null) null else updatedTitle)

    val mediaInfo: BaseMediaNode?
        get() = (selectedItem?.node as? AnimeNode) ?: (selectedItem?.node as? MangaNode)

    val myListStatus: BaseMyListStatus?
        get() = selectedItem?.listStatus
        
    fun isStatusLoaded(status: ListStatus?) = status != null && loadedStatuses.contains(status)
}
