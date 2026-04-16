package com.axiel7.moelist.ui.home

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.base.navigation.NavActionManager.Companion.rememberNavActionManager
import com.axiel7.moelist.ui.composables.HeaderHorizontalList
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
    padding: PaddingValues,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeViewContent(
        uiState = uiState,
        event = viewModel,
        isLoggedIn = isLoggedIn,
        navActionManager = navActionManager,
        padding = padding,
    )
}

@Composable
private fun HomeViewContent(
    uiState: HomeUiState,
    event: HomeEvent?,
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
    padding: PaddingValues = PaddingValues(),
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val airingListState = rememberLazyListState()
    val seasonalListState = rememberLazyListState()

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
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(padding)
    ) {
        // Hero Section Header
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Discover",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "What will you watch today?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Bento Grid Shortcuts
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HomeCard(
                text = stringResource(R.string.anime_ranking),
                icon = R.drawable.ic_round_movie_24,
                modifier = Modifier.weight(1f).height(72.dp),
                onClick = dropUnlessResumed {
                    navActionManager.toMediaRanking(MediaType.ANIME)
                },
            )

            HomeCard(
                text = stringResource(R.string.seasonal_chart),
                icon = SeasonCalendar.currentSeason.icon,
                modifier = Modifier.weight(1f).height(72.dp),
                onClick = dropUnlessResumed {
                    navActionManager.toSeasonChart()
                },
            )
        }

        Row(
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HomeCard(
                text = stringResource(R.string.manga_ranking),
                icon = R.drawable.ic_round_menu_book_24,
                modifier = Modifier.weight(1f).height(72.dp),
                onClick = dropUnlessResumed {
                    navActionManager.toMediaRanking(MediaType.MANGA)
                },
            )

            HomeCard(
                text = stringResource(R.string.calendar),
                icon = R.drawable.ic_round_event_24,
                modifier = Modifier.weight(1f).height(72.dp),
                onClick = dropUnlessResumed {
                    navActionManager.toCalendar()
                },
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Airing Today
        HeaderHorizontalList(
            text = stringResource(R.string.today),
            onClick = dropUnlessResumed { navActionManager.toCalendar() }
        )
        if (!isLoggedIn) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                onClick = dropUnlessResumed { navActionManager.toLogin() },
                shape = RoundedCornerShape(24.dp),
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = stringResource(R.string.please_login_to_use_this_feature),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
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
            } else {
                LazyRow(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .sizeIn(minHeight = MEDIA_POSTER_SMALL_HEIGHT.dp),
                    state = airingListState,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    flingBehavior = rememberSnapFlingBehavior(lazyListState = airingListState),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // This Season
        HeaderHorizontalList(
            text = stringResource(R.string.this_season),
            onClick = dropUnlessResumed { navActionManager.toSeasonChart() }
        )
        LazyRow(
            modifier = Modifier
                .padding(top = 4.dp)
                .sizeIn(minHeight = MEDIA_ITEM_VERTICAL_HEIGHT.dp),
            state = seasonalListState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = seasonalListState),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = uiState.seasonalAnimes,
                key = { it.node.id },
                contentType = { it.node }
            ) {
                MediaItemVertical(
                    imageUrl = it.node.mainPicture?.large,
                    title = it.node.userPreferredTitle(),
                    modifier = Modifier,
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
                navActionManager = rememberNavActionManager()
            )
        }
    }
}
