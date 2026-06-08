package com.axiel7.moelist.ui.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.SearchHistory
import com.axiel7.moelist.data.model.anime.AnimeList
import com.axiel7.moelist.data.model.manga.MangaList
import com.axiel7.moelist.data.model.media.BaseMediaList
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.EmptyState
import com.axiel7.moelist.ui.composables.LocalSnackbarHostState
import com.axiel7.moelist.ui.composables.showSnackbarShort
import com.axiel7.moelist.ui.composables.ErrorState
import com.axiel7.moelist.ui.composables.LoadingState
import com.axiel7.moelist.ui.composables.OnBottomReached
import com.axiel7.moelist.ui.composables.media.MediaItemDetailed
import com.axiel7.moelist.ui.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.ui.composables.media.PosterStatusBadge
import com.axiel7.moelist.ui.composables.score.PersonalScoreBadge
import com.axiel7.moelist.ui.composables.score.PosterScoreChip
import com.axiel7.moelist.utils.DateUtils.parseDateAndLocalize
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrNull
import com.axiel7.moelist.utils.UNKNOWN_CHAR

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchHistoryList(
    history: List<SearchHistory>,
    onHistoryItemClick: (String) -> Unit,
    onHistoryItemRemove: (SearchHistory) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(history) { item ->
            val haptic = LocalHapticFeedback.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onHistoryItemClick(item.keyword) },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onHistoryItemRemove(item)
                        }
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_history_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item.keyword,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchViewContent(
    uiState: SearchUiState,
    event: SearchEvent?,
    query: String,
    isCompactScreen: Boolean,
    navActionManager: NavActionManager,
    contentPadding: PaddingValues = PaddingValues(),
    showHistory: Boolean = false,
    onHistoryItemClick: (String) -> Unit = {}
) {
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            snackbarHostState.showSnackbarShort(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    @Composable
    fun FilterRow() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MediaType.entries.forEach {
                FilterChip(
                    selected = uiState.mediaType == it,
                    onClick = { event?.onChangeMediaType(it) },
                    label = {
                        Text(
                            text = it.localized(),
                            fontWeight = if (uiState.mediaType == it) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = if (uiState.mediaType == it) {
                        {
                            Icon(
                                painter = painterResource(R.drawable.round_check_24),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = null,
                    shape = MaterialTheme.shapes.medium
                )
            }
        }
    }

    @Composable
    fun ItemView(item: BaseMediaList) {
        val userScore = item.node.myListStatus?.score ?: 0
        val meanScore = item.node.mean
        MediaItemDetailed(
            title = item.node.userPreferredTitle(),
            imageUrl = item.node.mainPicture?.large,
            topBadgeContent = if (userScore > 0) {
                { PersonalScoreBadge(score = userScore) }
            } else null,
            badgeContent = {
                item.node.myListStatus?.status?.let { status ->
                    PosterStatusBadge(status = status, iconSize = 20.dp)
                }
                if (!uiState.hideScore && meanScore != null && meanScore > 0f) {
                    PosterScoreChip(score = meanScore)
                }
            },
            subtitle1 = {
                Text(
                    text = buildString {
                        append(item.node.mediaFormat?.localized() ?: UNKNOWN_CHAR)
                        if (item.node.totalDuration().toStringPositiveValueOrNull() != null) {
                            append(" (${item.node.durationText()})")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            subtitle2 = {
                Text(
                    text = when (item) {
                        is AnimeList -> item.node.startSeason?.seasonYearText()
                            ?: stringResource(R.string.unknown)

                        is MangaList -> item.node.startDate?.parseDateAndLocalize()
                            ?: stringResource(R.string.unknown)
                        else -> stringResource(R.string.unknown)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            subtitle3 = {},
            onClick = dropUnlessResumed {
                navActionManager.toMediaDetails(uiState.mediaType, item.node.id)
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        FilterRow()
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        if (showHistory) {
            SearchHistoryList(
                history = uiState.searchHistoryList,
                onHistoryItemClick = onHistoryItemClick,
                onHistoryItemRemove = { event?.onRemoveSearchHistory(it) }
            )
        } else {
            when {
                uiState.isLoading && uiState.mediaList.isEmpty() -> {
                    LoadingState()
                }
                uiState.message != null && uiState.mediaList.isEmpty() -> {
                    ErrorState(
                        icon = Icons.Outlined.CloudOff,
                        message = uiState.message,
                        onAction = { event?.search(query) }
                    )
                }
                uiState.noResults -> {
                    EmptyState(
                        icon = Icons.Outlined.SearchOff,
                        title = stringResource(R.string.no_matches_for_query),
                        description = stringResource(R.string.try_different_keywords, query)
                    )
                }
                uiState.mediaList.isNotEmpty() -> {
                    if (!isCompactScreen) {
                        val gridState = rememberLazyGridState()
                        gridState.OnBottomReached(buffer = 4) { event?.loadMore() }
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            state = gridState,
                            contentPadding = contentPadding
                        ) {
                            items(
                                items = uiState.mediaList,
                                contentType = { it.node }
                            ) {
                                ItemView(item = it)
                            }
                        }
                    } else {
                        val listState = rememberLazyListState()
                        listState.OnBottomReached(buffer = 3) { event?.loadMore() }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState,
                            contentPadding = contentPadding
                        ) {
                            items(
                                items = uiState.mediaList,
                                contentType = { it.node }
                            ) {
                                ItemView(item = it)
                            }
                            if (uiState.isLoading) {
                                items(5) {
                                    MediaItemDetailedPlaceholder()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
