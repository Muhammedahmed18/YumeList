package com.axiel7.moelist.ui.userlist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import com.axiel7.moelist.ui.base.ListStyle
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.EmptyState
import com.axiel7.moelist.ui.composables.ErrorState
import com.axiel7.moelist.ui.composables.OnBottomReached
import com.axiel7.moelist.ui.composables.collapsable
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_MEDIUM_WIDTH
import com.axiel7.moelist.ui.userlist.composables.CompactUserMediaListItem
import com.axiel7.moelist.ui.userlist.composables.CompactUserMediaListItemPlaceholder
import com.axiel7.moelist.ui.userlist.composables.GridUserMediaListItem
import com.axiel7.moelist.ui.userlist.composables.GridUserMediaListItemPlaceholder
import com.axiel7.moelist.ui.userlist.composables.MinimalUserMediaListItem
import com.axiel7.moelist.ui.userlist.composables.MinimalUserMediaListItemPlaceholder
import com.axiel7.moelist.ui.userlist.composables.StandardUserMediaListItem
import com.axiel7.moelist.ui.userlist.composables.StandardUserMediaListItemPlaceholder
import com.axiel7.moelist.utils.ContextExtensions.showToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMediaListView(
    uiState: UserMediaListUiState,
    event: UserMediaListEvent?,
    navActionManager: NavActionManager,
    isCompactScreen: Boolean,
    modifier: Modifier = Modifier,
    topBarHeightPx: Float = 0f,
    topBarOffsetY: Animatable<Float, AnimationVector1D> = Animatable(0f),
    contentPadding: PaddingValues = PaddingValues(),
    onShowEditSheet: (BaseUserMediaList<out BaseMediaNode>) -> Unit,
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val haptic = LocalHapticFeedback.current
    val pullRefreshState = rememberPullToRefreshState()

    // Hoist states to maintain scroll position during sort/status changes
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val tabletGridState = rememberLazyGridState()

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    // Scroll to top when the trigger changes from the ViewModel
    var lastScrollToTopTrigger by remember { mutableIntStateOf(uiState.scrollToTopTrigger) }
    LaunchedEffect(uiState.scrollToTopTrigger, uiState.isLoading) {
        if (uiState.scrollToTopTrigger > lastScrollToTopTrigger && !uiState.isLoading) {
            // Wait until loading is finished to prevent Compose from anchoring to items that move down
            listState.scrollToItem(0)
            gridState.scrollToItem(0)
            tabletGridState.scrollToItem(0)
            // Reset top bar offset so it's fully expanded when we return to top
            topBarOffsetY.snapTo(0f)
            lastScrollToTopTrigger = uiState.scrollToTopTrigger
        }
    }

    @Composable
    fun StandardItemView(item: BaseUserMediaList<out BaseMediaNode>, modifier: Modifier = Modifier) {
        StandardUserMediaListItem(
            item = item,
            listStatus = uiState.listStatus,
            onClick = dropUnlessResumed {
                navActionManager.toMediaDetails(uiState.mediaType, item.node.id)
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

    @Composable
    fun CompactItemView(item: BaseUserMediaList<out BaseMediaNode>, modifier: Modifier = Modifier) {
        CompactUserMediaListItem(
            item = item,
            listStatus = uiState.listStatus,
            onClick = dropUnlessResumed {
                navActionManager.toMediaDetails(uiState.mediaType, item.node.id)
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

    @Composable
    fun MinimalItemView(item: BaseUserMediaList<out BaseMediaNode>, modifier: Modifier = Modifier) {
        MinimalUserMediaListItem(
            item = item,
            listStatus = uiState.listStatus,
            onClick = dropUnlessResumed {
                navActionManager.toMediaDetails(uiState.mediaType, item.node.id)
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

    @Composable
    fun GridItemView(item: BaseUserMediaList<out BaseMediaNode>, modifier: Modifier = Modifier) {
        GridUserMediaListItem(
            item = item,
            onClick = dropUnlessResumed {
                navActionManager.toMediaDetails(uiState.mediaType, item.node.id)
            },
            onLongClick = {
                onShowEditSheet(item)
            },
            modifier = modifier
        )
    }

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
                fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith
                        fadeOut(animationSpec = tween(durationMillis = 300))
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
                    message = uiState.message,
                    onAction = { event?.refreshList() }
                )

                2 -> EmptyState(
                    modifier = Modifier.padding(contentPadding),
                    actionLabel = stringResource(R.string.refresh),
                    onAction = { event?.refreshList() }
                )

                3 -> {
                    if (uiState.listStyle == ListStyle.GRID) {
                        // Prevent pagination trigger during initial load of a new sort
                        if (!uiState.isLoading) {
                            gridState.OnBottomReached(buffer = 3) {
                                event?.loadMore()
                            }
                        }
                        LazyVerticalGrid(
                            columns = if (uiState.itemsPerRow.value > 0) GridCells.Fixed(uiState.itemsPerRow.value)
                            else GridCells.Adaptive(minSize = (MEDIA_POSTER_MEDIUM_WIDTH + 8).dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .collapsable(
                                    state = gridState,
                                    topBarHeightPx = topBarHeightPx,
                                    topBarOffsetY = topBarOffsetY,
                                ),
                            state = gridState,
                            contentPadding = PaddingValues(
                                start = contentPadding.calculateStartPadding(layoutDirection) + 8.dp,
                                top = contentPadding.calculateTopPadding(),
                                end = contentPadding.calculateEndPadding(layoutDirection) + 8.dp,
                                bottom = contentPadding.calculateBottomPadding() + 8.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                        ) {
                            items(
                                items = uiState.filteredMediaList,
                                key = { it.node.id },
                                contentType = { it.node }
                            ) { item ->
                                GridItemView(
                                    item = item,
                                    modifier = Modifier.animateItem()
                                )
                            }
                            if (uiState.isLoadingMore) {
                                items(9, contentType = { it }) {
                                    GridUserMediaListItemPlaceholder()
                                }
                            }
                            item(contentType = { 0 }) {
                                if (uiState.canLoadMore) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .padding(16.dp)
                                        )
                                    }
                                    LaunchedEffect(true) {
                                        event?.loadMore()
                                    }
                                }
                            }
                        }
                    } else if (isCompactScreen) {
                        if (!uiState.isLoading) {
                            listState.OnBottomReached(buffer = 3) {
                                event?.loadMore()
                            }
                        }
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .collapsable(
                                    state = listState,
                                    topBarHeightPx = topBarHeightPx,
                                    topBarOffsetY = topBarOffsetY,
                                ),
                            state = listState,
                            contentPadding = PaddingValues(
                                start = contentPadding.calculateStartPadding(layoutDirection),
                                top = contentPadding.calculateTopPadding(),
                                end = contentPadding.calculateEndPadding(layoutDirection),
                                bottom = contentPadding.calculateBottomPadding() + 8.dp
                            ),
                        ) {
                            when (uiState.listStyle) {
                                ListStyle.STANDARD -> {
                                    items(
                                        items = uiState.filteredMediaList,
                                        key = { it.node.id },
                                        contentType = { it.node }
                                    ) { item ->
                                        StandardItemView(
                                            item = item,
                                            modifier = Modifier.animateItem()
                                        )
                                    }
                                    if (uiState.isLoadingMore) {
                                        items(5, contentType = { it }) {
                                            StandardUserMediaListItemPlaceholder()
                                        }
                                    }
                                }

                                ListStyle.COMPACT -> {
                                    items(
                                        items = uiState.filteredMediaList,
                                        key = { it.node.id },
                                        contentType = { it.node }
                                    ) { item ->
                                        CompactItemView(
                                            item = item,
                                            modifier = Modifier.animateItem()
                                        )
                                    }
                                    if (uiState.isLoadingMore) {
                                        items(5, contentType = { it }) {
                                            CompactUserMediaListItemPlaceholder()
                                        }
                                    }
                                }

                                ListStyle.MINIMAL -> {
                                    items(
                                        items = uiState.filteredMediaList,
                                        key = { it.node.id },
                                        contentType = { it.node }
                                    ) { item ->
                                        MinimalItemView(
                                            item = item,
                                            modifier = Modifier.animateItem()
                                        )
                                    }
                                    if (uiState.isLoadingMore) {
                                        items(5, contentType = { it }) {
                                            MinimalUserMediaListItemPlaceholder()
                                        }
                                    }
                                }

                                else -> {}
                            }
                        }//:LazyColumn
                    } else { // tablet ui
                        if (!uiState.isLoading) {
                            tabletGridState.OnBottomReached(buffer = 3) {
                                event?.loadMore()
                            }
                        }
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            state = tabletGridState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .collapsable(
                                    state = tabletGridState,
                                    topBarHeightPx = topBarHeightPx,
                                    topBarOffsetY = topBarOffsetY,
                                ),
                            contentPadding = PaddingValues(
                                start = contentPadding.calculateStartPadding(layoutDirection),
                                top = contentPadding.calculateTopPadding(),
                                end = contentPadding.calculateEndPadding(layoutDirection),
                                bottom = contentPadding.calculateBottomPadding() + 8.dp
                            ),
                        ) {
                            when (uiState.listStyle) {
                                ListStyle.STANDARD -> {
                                    items(
                                        items = uiState.filteredMediaList,
                                        key = { it.node.id },
                                        contentType = { it.node }
                                    ) { item ->
                                        StandardItemView(
                                            item = item,
                                            modifier = Modifier.animateItem()
                                        )
                                    }
                                    if (uiState.isLoadingMore) {
                                        items(5, contentType = { it }) {
                                            StandardUserMediaListItemPlaceholder()
                                        }
                                    }
                                }

                                ListStyle.COMPACT -> {
                                    items(
                                        items = uiState.filteredMediaList,
                                        key = { it.node.id },
                                        contentType = { it.node }
                                    ) { item ->
                                        CompactItemView(
                                            item = item,
                                            modifier = Modifier.animateItem()
                                        )
                                    }
                                    if (uiState.isLoadingMore) {
                                        items(5, contentType = { it }) {
                                            CompactUserMediaListItemPlaceholder()
                                        }
                                    }
                                }

                                ListStyle.MINIMAL -> {
                                    items(
                                        items = uiState.filteredMediaList,
                                        key = { it.node.id },
                                        contentType = { it.node }
                                    ) { item ->
                                        MinimalItemView(
                                            item = item,
                                            modifier = Modifier.animateItem()
                                        )
                                    }
                                    if (uiState.isLoadingMore) {
                                        items(5, contentType = { it }) {
                                            MinimalUserMediaListItemPlaceholder()
                                        }
                                    }
                                }

                                else -> {}
                            }
                            item(contentType = { 0 }) {
                                if (uiState.canLoadMore) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .padding(16.dp)
                                        )
                                    }
                                    LaunchedEffect(true) {
                                        event?.loadMore()
                                    }
                                }
                            }
                        }
                    }
                }

                else -> LoadingPlaceholder(uiState, contentPadding)
            }
        }
    }
}

@Composable
fun LoadingPlaceholder(
    uiState: UserMediaListUiState,
    contentPadding: PaddingValues
) {
    val layoutDirection = LocalLayoutDirection.current
    if (uiState.listStyle == ListStyle.GRID) {
        LazyVerticalGrid(
            columns = if (uiState.itemsPerRow.value > 0) GridCells.Fixed(uiState.itemsPerRow.value)
            else GridCells.Adaptive(minSize = (MEDIA_POSTER_MEDIUM_WIDTH + 8).dp),
            contentPadding = PaddingValues(
                start = contentPadding.calculateStartPadding(layoutDirection) + 8.dp,
                top = contentPadding.calculateTopPadding(),
                end = contentPadding.calculateEndPadding(layoutDirection) + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            userScrollEnabled = false
        ) {
            items(12) {
                GridUserMediaListItemPlaceholder()
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(
                start = contentPadding.calculateStartPadding(layoutDirection),
                top = contentPadding.calculateTopPadding(),
                end = contentPadding.calculateEndPadding(layoutDirection),
                bottom = contentPadding.calculateBottomPadding() + 8.dp
            ),
            userScrollEnabled = false
        ) {
            items(8) {
                when (uiState.listStyle) {
                    ListStyle.STANDARD -> StandardUserMediaListItemPlaceholder()
                    ListStyle.COMPACT -> CompactUserMediaListItemPlaceholder()
                    ListStyle.MINIMAL -> MinimalUserMediaListItemPlaceholder()
                    else -> {}
                }
            }
        }
    }
}
