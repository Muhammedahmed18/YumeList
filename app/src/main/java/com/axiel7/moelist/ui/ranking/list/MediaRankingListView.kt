package com.axiel7.moelist.ui.ranking.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.axiel7.moelist.ui.composables.LoadingState
import com.axiel7.moelist.ui.composables.OnBottomReached
import com.axiel7.moelist.ui.composables.PosterSection
import com.axiel7.moelist.ui.composables.TextIconHorizontal
import com.axiel7.moelist.ui.composables.media.MediaItemRow
import com.axiel7.moelist.ui.composables.media.MediaItemRowPlaceholder
import com.axiel7.moelist.ui.composables.media.MediaStatusIndicator
import com.axiel7.moelist.ui.composables.posterSharedKey
import com.axiel7.moelist.ui.composables.rememberLoadErrorState
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.outlined.CloudOff
import com.axiel7.moelist.ui.icons.outlined.Inbox
import com.axiel7.moelist.ui.ranking.MediaRankingEvent
import com.axiel7.moelist.ui.ranking.MediaRankingUiState
import com.axiel7.moelist.ui.ranking.MediaRankingViewModel
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.ShapeChip
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

    // Full-screen error stays until Retry (snackbar only when content is already showing)
    val loadError = rememberLoadErrorState(
        message = uiState.message,
        hasContent = uiState.mediaList.isNotEmpty(),
        onMessageConsumed = { event?.onMessageDisplayed() },
        onRetry = { event?.retry() },
    )

    @Composable
    fun ItemView(item: BaseRanking) {
        val status = item.node.myListStatus?.status
        val posterKey = posterSharedKey(PosterSection.RANKING, mediaType, item.node.id)
        MediaItemRow(
            title = item.node.userPreferredTitle(),
            imageUrl = item.node.mainPicture?.large,
            posterKey = posterKey,
            topBadge = item.ranking?.rank?.let { rank ->
                { RankBadge(rank = rank) }
            },
            tags = listOfNotNull(
                item.node.mediaFormat?.localized(),
                if (item.node.totalDuration().toStringPositiveValueOrNull() != null) item.node.durationText()
                else null,
            ),
            meanScore = if (!uiState.hideScore) item.node.mean.toStringPositiveValueOrUnknown() else null,
            members = item.node.numListUsers?.format() ?: UNKNOWN_CHAR,
            personalScore = item.node.myListStatus?.score ?: 0,
            status = status,
            onClick = dropUnlessResumed {
                navActionManager.toMediaDetails(
                    mediaType,
                    item.node.id,
                    posterUrl = item.node.mainPicture?.large,
                    posterKey = posterKey,
                )
            }
        )
    }

    when {
        uiState.isLoading && uiState.mediaList.isEmpty() -> {
            LoadingState()
        }
        loadError.value != null && uiState.mediaList.isEmpty() -> {
            ErrorState(
                icon = Icons.Outlined.CloudOff,
                message = loadError.value,
                onAction = {
                    loadError.value = null
                    // Was loadMore(), which does nothing after a failed first page
                    event?.retry()
                }
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
                        contentType = { "media" }
                    ) { item ->
                        ItemView(item = item)
                    }
                    if (uiState.isLoading) {
                        items(10, contentType = { "placeholder" }) {
                            MediaItemRowPlaceholder()
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
                        contentType = { "media" }
                    ) { item ->
                        ItemView(item = item)
                    }
                    if (uiState.isLoading) {
                        items(10, contentType = { "placeholder" }) {
                            MediaItemRowPlaceholder()
                        }
                    }
                }//:LazyColumn
            }
        }
    }
}

/**
 * Rank on the poster, in the M3 Expressive emphasized type styles.
 * The top 3 get a filled Expressive shape (sunny, 9- and 7-sided cookies) in the primary,
 * tertiary and secondary colours; every other rank is a compact emphasized pill.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RankBadge(rank: Int) {
    val colors = MaterialTheme.colorScheme
    val podium = when (rank) {
        1 -> Triple(MaterialShapes.Sunny, colors.primary, colors.onPrimary)
        2 -> Triple(MaterialShapes.Cookie9Sided, colors.tertiary, colors.onTertiary)
        3 -> Triple(MaterialShapes.Cookie7Sided, colors.secondary, colors.onSecondary)
        else -> null
    }
    val description = "${stringResource(R.string.ranked)} #$rank"

    if (podium != null) {
        val (polygon, container, content) = podium
        Box(
            modifier = Modifier
                .size(RankPodiumSize)
                .clip(polygon.toShape())
                .background(container)
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rank.toString(),
                style = MaterialTheme.typography.titleLargeEmphasized,
                color = content,
                maxLines = 1
            )
        }
    } else {
        Surface(
            shape = ShapeChip,
            color = colors.surfaceContainerHighest.copy(alpha = 0.92f),
            contentColor = colors.onSurface,
            modifier = Modifier.semantics { contentDescription = description }
        ) {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.labelLargeEmphasized,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

private val RankPodiumSize = 40.dp

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
