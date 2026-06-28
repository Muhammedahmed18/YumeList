package com.axiel7.moelist.ui.ranking.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.BaseRanking
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.EmptyState
import com.axiel7.moelist.ui.composables.ErrorState
import com.axiel7.moelist.ui.composables.LocalSnackbarHostState
import com.axiel7.moelist.ui.composables.showSnackbarShort
import com.axiel7.moelist.ui.composables.LoadingState
import com.axiel7.moelist.ui.composables.OnBottomReached
import com.axiel7.moelist.ui.composables.TextIconHorizontal
import com.axiel7.moelist.ui.composables.media.MediaItemDetailed
import com.axiel7.moelist.ui.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.ui.composables.media.MediaStatusIndicator
import com.axiel7.moelist.ui.ranking.MediaRankingEvent
import com.axiel7.moelist.ui.ranking.MediaRankingUiState
import com.axiel7.moelist.ui.ranking.MediaRankingViewModel
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.NumExtensions.format
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrNull
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown
import com.axiel7.moelist.utils.UNKNOWN_CHAR
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MediaRankingListView(
    mediaType: MediaType,
    rankingType: RankingType,
    isCompactScreen: Boolean,
    navActionManager: NavActionManager,
) {
    val viewModel: MediaRankingViewModel =
        koinViewModel(key = rankingType.name) { parametersOf(rankingType) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MediaRankingListViewContent(
        uiState = uiState,
        event = viewModel,
        mediaType = mediaType,
        isCompactScreen = isCompactScreen,
        navActionManager = navActionManager,
    )
}

@Composable
private fun MediaRankingListViewContent(
    uiState: MediaRankingUiState,
    event: MediaRankingEvent?,
    mediaType: MediaType,
    isCompactScreen: Boolean,
    navActionManager: NavActionManager,
) {
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            snackbarHostState.showSnackbarShort(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    @Composable
    fun ItemView(item: BaseRanking) {
        val status = item.node.myListStatus?.status
        MediaItemDetailed(
            title = item.node.userPreferredTitle(),
            imageUrl = item.node.mainPicture?.large,
            topBadgeContent = {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                ) {
                    Text(
                        text = "#${item.ranking?.rank}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            subtitle1 = {
                Text(
                    text = buildString {
                        append(item.node.mediaFormat?.localized())
                        if (item.node.totalDuration().toStringPositiveValueOrNull() != null) {
                            append(" (${item.node.durationText()})")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            subtitle2 = {
                if (!uiState.hideScore) {
                    TextIconHorizontal(
                        text = item.node.mean.toStringPositiveValueOrUnknown(),
                        icon = R.drawable.ic_round_details_star_24,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        iconSize = 16.dp
                    )
                }
            },
            subtitle3 = {
                TextIconHorizontal(
                    text = item.node.numListUsers?.format() ?: UNKNOWN_CHAR,
                    icon = R.drawable.ic_round_group_24,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    iconSize = 16.dp
                )
                if (status != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    MediaStatusIndicator(status = status)
                }
            },
            onClick = dropUnlessResumed {
                navActionManager.toMediaDetails(mediaType, item.node.id)
            }
        )
    }

    when {
        uiState.isLoading && uiState.mediaList.isEmpty() -> {
            LoadingState()
        }
        uiState.message != null && uiState.mediaList.isEmpty() -> {
            ErrorState(
                icon = Icons.Outlined.CloudOff,
                message = uiState.message,
                onAction = { event?.loadMore() }
            )
        }
        !uiState.isLoading && uiState.mediaList.isEmpty() -> {
            EmptyState(
                icon = Icons.Outlined.Inbox,
                title = stringResource(R.string.no_results),
            )
        }
        else -> {
            if (!isCompactScreen) {
                val listState = rememberLazyGridState()
                listState.OnBottomReached(buffer = 3) {
                    event?.loadMore()
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    ),
                ) {
                    items(
                        items = uiState.mediaList,
                        key = { it.node.id },
                        contentType = { it.node }
                    ) { item ->
                        ItemView(item = item)
                    }
                    if (uiState.isLoading) {
                        items(10) {
                            MediaItemDetailedPlaceholder()
                        }
                    }
                }
            } else {
                val listState = rememberLazyListState()
                listState.OnBottomReached(buffer = 3) {
                    event?.loadMore()
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    ),
                    state = listState
                ) {
                    items(
                        items = uiState.mediaList,
                        key = { it.node.id },
                        contentType = { it.node }
                    ) { item ->
                        ItemView(item = item)
                    }
                    if (uiState.isLoading) {
                        items(10) {
                            MediaItemDetailedPlaceholder()
                        }
                    }
                }//:LazyColumn
            }
        }
    }
}

@Preview
@Composable
fun MediaRankingPreview() {
    MoeListTheme {
        Surface {
            MediaRankingListViewContent(
                uiState = MediaRankingUiState(rankingType = RankingType.SCORE),
                event = null,
                mediaType = MediaType.ANIME,
                isCompactScreen = true,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}
