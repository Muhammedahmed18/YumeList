package com.axiel7.moelist.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.HeaderHorizontalList
import com.axiel7.moelist.ui.composables.collapsable
import com.axiel7.moelist.ui.composables.media.MEDIA_ITEM_VERTICAL_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_HEIGHT
import com.axiel7.moelist.ui.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.MediaItemVerticalPlaceholder
import com.axiel7.moelist.ui.composables.score.SmallScoreIndicator
import com.axiel7.moelist.ui.home.composables.AiringAnimeHorizontalItem
import com.axiel7.moelist.ui.home.composables.HomeCard
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.SeasonCalendar
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeView(
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
    topBarHeightPx: Float,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
    padding: PaddingValues,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeViewContent(
        uiState = uiState,
        event = viewModel,
        isLoggedIn = isLoggedIn,
        navActionManager = navActionManager,
        topBarHeightPx = topBarHeightPx,
        topBarOffsetY = topBarOffsetY,
        padding = padding,
    )
}

@Composable
private fun HomeViewContent(
    uiState: HomeUiState,
    event: HomeEvent?,
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
    topBarHeightPx: Float = 0f,
    topBarOffsetY: Animatable<Float, AnimationVector1D> = Animatable(0f),
    padding: PaddingValues = PaddingValues(),
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val airingListState = rememberLazyListState()
    val watchingListState = rememberLazyListState()

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    LaunchedEffect(isLoggedIn) {
        event?.initRequestChain(isLoggedIn)
    }

    Column(
        modifier = Modifier
            .collapsable(
                state = scrollState,
                topBarHeightPx = topBarHeightPx,
                topBarOffsetY = topBarOffsetY,
            )
            .verticalScroll(scrollState)
            .padding(padding)
    ) {
        // Main Shortcuts
        Row(
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            HomeCard(
                text = stringResource(R.string.anime_ranking),
                icon = R.drawable.ic_round_movie_24,
                modifier = Modifier.weight(1f),
                onClick = dropUnlessResumed {
                    navActionManager.toMediaRanking(MediaType.ANIME)
                },
            )

            Spacer(modifier = Modifier.padding(horizontal = 4.dp))

            HomeCard(
                text = stringResource(R.string.manga_ranking),
                icon = R.drawable.ic_round_menu_book_24,
                modifier = Modifier.weight(1f),
                onClick = dropUnlessResumed {
                    navActionManager.toMediaRanking(MediaType.MANGA)
                },
            )
        }

        Row(
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
        ) {
            HomeCard(
                text = stringResource(R.string.seasonal_chart),
                icon = SeasonCalendar.currentSeason.icon,
                modifier = Modifier.weight(1f),
                onClick = dropUnlessResumed {
                    navActionManager.toSeasonChart()
                },
            )

            Spacer(modifier = Modifier.padding(horizontal = 4.dp))

            HomeCard(
                text = stringResource(R.string.calendar),
                icon = R.drawable.ic_round_event_24,
                modifier = Modifier.weight(1f),
                onClick = dropUnlessResumed {
                    navActionManager.toCalendar()
                },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Airing Today
        HeaderHorizontalList(
            text = stringResource(R.string.today),
            onClick = dropUnlessResumed { navActionManager.toCalendar() }
        )
        if (!uiState.isLoading && uiState.todayAnimes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MEDIA_POSTER_SMALL_HEIGHT.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.nothing_today),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else LazyRow(
            modifier = Modifier
                .padding(top = 8.dp)
                .sizeIn(minHeight = MEDIA_POSTER_SMALL_HEIGHT.dp),
            state = airingListState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = airingListState)
        ) {
            items(
                items = uiState.todayAnimes,
                key = { it.node.id },
                contentType = { it.node }
            ) {
                AiringAnimeHorizontalItem(
                    item = it,
                    hideScore = uiState.hideScore,
                    onClick = dropUnlessResumed {
                        navActionManager.toMediaDetails(MediaType.ANIME, it.node.id)
                    }
                )
            }
            if (uiState.isLoading) {
                items(5) {
                    MediaItemDetailedPlaceholder()
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Currently Watching
        if (isLoggedIn) {
            HeaderHorizontalList(
                text = stringResource(R.string.watching),
                onClick = dropUnlessResumed { navActionManager.toUserList(MediaType.ANIME, ListStatus.WATCHING) }
            )
            if (!uiState.isLoading && uiState.watchingAnimes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MEDIA_POSTER_SMALL_HEIGHT.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_anime_on_list),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else LazyRow(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .sizeIn(minHeight = MEDIA_ITEM_VERTICAL_HEIGHT.dp),
                state = watchingListState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                flingBehavior = rememberSnapFlingBehavior(lazyListState = watchingListState)
            ) {
                items(
                    items = uiState.watchingAnimes,
                    key = { it.node.id },
                    contentType = { it.node }
                ) {
                    MediaItemVertical(
                        imageUrl = it.node.mainPicture?.large,
                        title = it.node.userPreferredTitle(),
                        modifier = Modifier.padding(end = 12.dp),
                        badgeContent = if (it.listStatus?.progress != null) {
                            {
                                Text(
                                    text = it.listStatus.progress.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        } else null,
                        subtitle = if (!uiState.hideScore) {
                            {
                                SmallScoreIndicator(
                                    score = it.node.mean,
                                    fontSize = 13.sp
                                )
                            }
                        } else null,
                        minLines = 2,
                        onClick = dropUnlessResumed {
                            navActionManager.toMediaDetails(MediaType.ANIME, it.node.id)
                        }
                    )
                }
                if (uiState.isLoading) {
                    items(10) {
                        MediaItemVerticalPlaceholder()
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview
@Composable
fun HomePreview() {
    MoeListTheme {
        Surface {
            HomeViewContent(
                uiState = HomeUiState(),
                event = null,
                isLoggedIn = false,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}
