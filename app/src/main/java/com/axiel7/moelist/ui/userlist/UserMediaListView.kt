package com.axiel7.moelist.ui.userlist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.EmptyState
import com.axiel7.moelist.ui.composables.ErrorState
import com.axiel7.moelist.ui.composables.LocalSnackbarHostState
import com.axiel7.moelist.ui.composables.PosterSection
import com.axiel7.moelist.ui.composables.SnackbarType
import com.axiel7.moelist.ui.composables.posterSharedKey
import com.axiel7.moelist.ui.composables.showAppSnackbar
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.outlined.MenuBook
import com.axiel7.moelist.ui.icons.outlined.Bookmark
import com.axiel7.moelist.ui.icons.outlined.Cancel
import com.axiel7.moelist.ui.icons.outlined.CheckCircle
import com.axiel7.moelist.ui.icons.outlined.CloudOff
import com.axiel7.moelist.ui.icons.outlined.Inbox
import com.axiel7.moelist.ui.icons.outlined.PauseCircle
import com.axiel7.moelist.ui.icons.outlined.PlayCircle
import com.axiel7.moelist.ui.theme.Motion
import com.axiel7.moelist.ui.userlist.composables.SetScoreDialog
import com.axiel7.moelist.ui.userlist.composables.StandardUserMediaListItem
import com.axiel7.moelist.ui.userlist.composables.StandardUserMediaListItemPlaceholder

/** Placeholder rows shown below the list while more items load. */
private const val LOAD_MORE_PLACEHOLDERS = 5

/** Placeholder rows shown while the list loads for the first time. */
private const val LOADING_PLACEHOLDERS = 8

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMediaListView(
    uiState: UserMediaListUiState,
    event: UserMediaListEvent?,
    navActionManager: NavActionManager,
    isCompactScreen: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    onShowEditSheet: (BaseUserMediaList<out BaseMediaNode>) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val haptic = LocalHapticFeedback.current
    @Suppress("SpellCheckingInspection")
    val snackbarHostState = LocalSnackbarHostState.current
    val pullRefreshState = rememberPullToRefreshState()

    // Hoist states to maintain scroll position during sort/status changes
    val listState = rememberLazyListState()
    val tabletGridState = rememberLazyGridState()

    val updatedLabel = stringResource(R.string.updated)
    LaunchedEffect(uiState.message, uiState.updatedTitle) {
        when {
            uiState.updatedTitle != null -> {
                snackbarHostState.showAppSnackbar(
                    message = updatedLabel,
                    type = SnackbarType.SUCCESS,
                    highlight = uiState.updatedTitle,
                )
                event?.onMessageDisplayed()
            }
            uiState.message != null -> {
                snackbarHostState.showAppSnackbar(uiState.message, type = SnackbarType.ERROR)
                event?.onMessageDisplayed()
            }
        }
    }

    // Scroll to top when the trigger changes, as signalled by the ViewModel
    var lastScrollToTopTrigger by remember { mutableIntStateOf(uiState.scrollToTopTrigger) }
    LaunchedEffect(uiState.scrollToTopTrigger, uiState.isLoading) {
        if (uiState.scrollToTopTrigger > lastScrollToTopTrigger && !uiState.isLoading) {
            // Wait until loading is finished to prevent Compose from anchoring to items that move down
            listState.scrollToItem(0)
            tabletGridState.scrollToItem(0)
            lastScrollToTopTrigger = uiState.scrollToTopTrigger
        }
    }

    @Composable
    fun StandardItemView(
        item: BaseUserMediaList<out BaseMediaNode>,
        modifier: Modifier = Modifier,
        index: Int = 0,
        count: Int = 1,
    ) {
        val posterKey = posterSharedKey(PosterSection.USER_LIST, uiState.mediaType, item.node.id)
        StandardUserMediaListItem(
            item = item,
            listStatus = uiState.listStatus,
            index = index,
            count = count,
            posterKey = posterKey,
            onClick = dropUnlessResumed {
                navActionManager.toMediaDetails(
                    uiState.mediaType,
                    item.node.id,
                    posterUrl = item.node.mainPicture?.large,
                    posterKey = posterKey,
                )
            },
            onLongClick = {
                onShowEditSheet(item)
            },
            onClickPlus = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                event?.onUpdateProgress(item)
            },
            modifier = modifier
        )
    }


    // AnimatedContent's transitionSpec isn't @Composable, so read the motion spec here
    val stateCrossfade = Motion.effects<Float>()

    // Segmented list: every row needs its position and the group size. Load-more
    // placeholders continue the same group, so the last real row isn't rounded too early.
    val listSize = uiState.filteredMediaList.size
    val segmentCount = listSize + if (uiState.isLoadingMore) LOAD_MORE_PLACEHOLDERS else 0

    PullToRefreshBox(
        isRefreshing = uiState.isLoading && uiState.filteredMediaList.isNotEmpty(),
        onRefresh = { event?.refreshList() },
        modifier = modifier.fillMaxSize(),
        state = pullRefreshState,
    ) {
        AnimatedContent(
            targetState = when {
                uiState.filteredMediaList.isNotEmpty() -> 3
                uiState.isLoading -> 0
                uiState.isError -> 1
                !uiState.isLoading && uiState.isStatusLoaded(uiState.listStatus) -> 2
                else -> 0
            },
            transitionSpec = {
                fadeIn(animationSpec = stateCrossfade) togetherWith
                        fadeOut(animationSpec = stateCrossfade)
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart),
            contentAlignment = Alignment.TopStart,
            label = "listContent"
        ) { state ->
            when (state) {
                1 -> ErrorState(
                    modifier = Modifier.padding(contentPadding),
                    icon = Icons.Outlined.CloudOff,
                    message = uiState.message,
                    onAction = { event?.refreshList() }
                )

                2 -> {
                    val empty = emptyStateFor(uiState.listStatus, uiState.mediaType)
                    EmptyState(
                        modifier = Modifier.padding(contentPadding),
                        icon = empty.icon,
                        title = stringResource(empty.titleRes),
                        description = stringResource(empty.descRes),
                        actionLabel = stringResource(R.string.refresh),
                        onAction = { event?.refreshList() }
                    )
                }

                3 -> {
                    if (isCompactScreen) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth(),
                            state = listState,
                            contentPadding = PaddingValues(
                                start = contentPadding.calculateStartPadding(layoutDirection),
                                top = contentPadding.calculateTopPadding(),
                                end = contentPadding.calculateEndPadding(layoutDirection),
                                bottom = contentPadding.calculateBottomPadding() + 8.dp
                            ),
                        ) {
                            itemsIndexed(
                                items = uiState.filteredMediaList,
                                key = { _, it -> it.node.id },
                                contentType = { _, _ -> "media" }
                            ) { index, item ->
                                StandardItemView(
                                    item = item,
                                    modifier = Modifier.animateItem(),
                                    index = index,
                                    count = segmentCount,
                                )
                            }
                            if (uiState.isLoadingMore) {
                                items(LOAD_MORE_PLACEHOLDERS, contentType = { "placeholder" }) {
                                    StandardUserMediaListItemPlaceholder(
                                        index = listSize + it,
                                        count = segmentCount,
                                    )
                                }
                            }
                        }//:LazyColumn
                    } else { // tablet ui
                        // Standard cards have enough density to work as 2-column on tablet
                        // Two columns can't form one segmented group, so each card stands alone
                        // (fully rounded) with a gap between rows
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            state = tabletGridState,
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(
                                start = contentPadding.calculateStartPadding(layoutDirection),
                                top = contentPadding.calculateTopPadding(),
                                end = contentPadding.calculateEndPadding(layoutDirection),
                                bottom = contentPadding.calculateBottomPadding() + 8.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(
                                items = uiState.filteredMediaList,
                                key = { it.node.id },
                                contentType = { "media" }
                            ) { item ->
                                StandardItemView(
                                    item = item,
                                    modifier = Modifier.animateItem()
                                )
                            }
                            if (uiState.isLoadingMore) {
                                items(4, contentType = { "placeholder" }) {
                                    StandardUserMediaListItemPlaceholder()
                                }
                            }
                        }
                    }
                }

                else -> LoadingPlaceholder(uiState, contentPadding)
            }
        }
    }

    if (uiState.openSetScoreDialog) {
        val item = uiState.selectedItem
        if (item != null) {
            SetScoreDialog(
                mediaTitle = item.node.userPreferredTitle(),
                finishDate = uiState.setScoreDialogFinishDate,
                onDismiss = { event?.skipSetScoreDialog() },
                onFinish = { score ->
                    event?.setScore(item, score)
                }
            )
        }
    }
}

@Composable
fun LoadingPlaceholder(
    uiState: UserMediaListUiState,
    contentPadding: PaddingValues
) {
    val layoutDirection = LocalLayoutDirection.current
    LazyColumn(
        contentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(layoutDirection),
            top = contentPadding.calculateTopPadding(),
            end = contentPadding.calculateEndPadding(layoutDirection),
            bottom = contentPadding.calculateBottomPadding() + 8.dp
        ),
        userScrollEnabled = false
    ) {
        items(LOADING_PLACEHOLDERS) { index ->
            // Same segmented group as the loaded list
            StandardUserMediaListItemPlaceholder(index, LOADING_PLACEHOLDERS)
        }
    }
}

private data class EmptyStateSpec(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    @field:androidx.annotation.StringRes val titleRes: Int,
    @field:androidx.annotation.StringRes val descRes: Int,
)

private fun emptyStateFor(status: ListStatus?, mediaType: MediaType): EmptyStateSpec {
    val isAnime = mediaType == MediaType.ANIME
    return when (status) {
        ListStatus.WATCHING -> EmptyStateSpec(
            icon = Icons.Outlined.PlayCircle,
            titleRes = R.string.empty_watching_title,
            descRes = R.string.empty_watching_desc,
        )

        ListStatus.READING -> EmptyStateSpec(
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            titleRes = R.string.empty_reading_title,
            descRes = R.string.empty_reading_desc,
        )

        ListStatus.COMPLETED -> EmptyStateSpec(
            icon = Icons.Outlined.CheckCircle,
            titleRes = if (isAnime) R.string.empty_completed_anime_title else R.string.empty_completed_manga_title,
            descRes = if (isAnime) R.string.empty_completed_anime_desc else R.string.empty_completed_manga_desc,
        )

        ListStatus.ON_HOLD -> EmptyStateSpec(
            icon = Icons.Outlined.PauseCircle,
            titleRes = if (isAnime) R.string.empty_on_hold_anime_title else R.string.empty_on_hold_manga_title,
            descRes = if (isAnime) R.string.empty_on_hold_anime_desc else R.string.empty_on_hold_manga_desc,
        )

        ListStatus.DROPPED -> EmptyStateSpec(
            icon = Icons.Outlined.Cancel,
            titleRes = if (isAnime) R.string.empty_dropped_anime_title else R.string.empty_dropped_manga_title,
            descRes = if (isAnime) R.string.empty_dropped_anime_desc else R.string.empty_dropped_manga_desc,
        )

        ListStatus.PLAN_TO_WATCH -> EmptyStateSpec(
            icon = Icons.Outlined.Bookmark,
            titleRes = R.string.empty_ptw_title,
            descRes = R.string.empty_ptw_desc,
        )

        ListStatus.PLAN_TO_READ -> EmptyStateSpec(
            icon = Icons.Outlined.Bookmark,
            titleRes = R.string.empty_ptr_title,
            descRes = R.string.empty_ptr_desc,
        )

        null -> EmptyStateSpec(
            icon = Icons.Outlined.Inbox,
            titleRes = R.string.no_results,
            descRes = if (isAnime) R.string.empty_watching_desc else R.string.empty_reading_desc,
        )
    }
}