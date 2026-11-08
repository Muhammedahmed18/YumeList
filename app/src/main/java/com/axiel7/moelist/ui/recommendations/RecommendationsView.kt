package com.axiel7.moelist.ui.recommendations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.recommendation.ForYouResult
import com.axiel7.moelist.data.model.recommendation.RecommendationEngine
import com.axiel7.moelist.data.model.recommendation.RecommendationGroup
import com.axiel7.moelist.data.model.recommendation.RecommendedMedia
import com.axiel7.moelist.ui.base.TabRowItem
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.BackIconButton
import com.axiel7.moelist.ui.composables.EmptyState
import com.axiel7.moelist.ui.composables.ErrorState
import com.axiel7.moelist.ui.composables.LoadingState
import com.axiel7.moelist.ui.composables.PosterSection
import com.axiel7.moelist.ui.composables.TabRowWithPager
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.composables.posterSharedKey
import com.axiel7.moelist.ui.composables.rememberLoadErrorState
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.outlined.CloudOff
import com.axiel7.moelist.ui.icons.rounded.AutoAwesome
import com.axiel7.moelist.ui.recommendations.composables.RecommendationActionSnackbar
import com.axiel7.moelist.ui.recommendations.composables.RecommendationListItem
import com.axiel7.moelist.ui.recommendations.composables.RecommendationPosterCard
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/** How many picks are shown as full cards before the "Because you liked" rows. */
private const val TOP_PICKS = 10

/**
 * Recommendations screen ("See all" of Home's For You): Anime and Manga tabs, each with the top
 * picks, a "Because you liked …" row per favourite, then the rest. Pull down to rebuild.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsView(
    mediaType: MediaType,
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
) {
    val tabs = remember {
        arrayOf(
            TabRowItem(value = MediaType.ANIME, title = R.string.anime),
            TabRowItem(value = MediaType.MANGA, title = R.string.manga),
        )
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text(text = stringResource(R.string.recommendations)) },
                navigationIcon = { BackIconButton(onClick = navActionManager::goBack) },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRowWithPager(
                tabs = tabs,
                modifier = Modifier.fillMaxSize(),
                initialPage = if (mediaType == MediaType.MANGA) 1 else 0,
            ) { page, _ ->
                RecommendationsPage(
                    mediaType = tabs[page].value,
                    isLoggedIn = isLoggedIn,
                    navActionManager = navActionManager,
                )
            }
        }
    }
}

@Composable
private fun RecommendationsPage(
    mediaType: MediaType,
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
) {
    val viewModel: RecommendationsViewModel =
        koinViewModel(key = "recommendations_${mediaType.name}") { parametersOf(mediaType) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(isLoggedIn) { viewModel.load(isLoggedIn) }

    RecommendationsPageContent(
        uiState = uiState,
        event = viewModel,
        isLoggedIn = isLoggedIn,
        navActionManager = navActionManager,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecommendationsPageContent(
    uiState: RecommendationsUiState,
    event: RecommendationsEvent?,
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
) {
    val loadError = rememberLoadErrorState(
        message = uiState.message,
        hasContent = uiState.hasContent,
        onMessageConsumed = { event?.onMessageDisplayed() },
        onRetry = { event?.refresh(isLoggedIn) },
    )

    RecommendationActionSnackbar(
        result = uiState.actionResult,
        onShown = { event?.onActionResultShown() },
        onUndoHide = { event?.undoNotInterested(it) },
    )

    val result = uiState.result
    when {
        uiState.isLoading && !uiState.hasContent -> LoadingState()

        loadError.value != null && !uiState.hasContent -> ErrorState(
            icon = Icons.Outlined.CloudOff,
            message = loadError.value,
            onAction = {
                loadError.value = null
                event?.refresh(isLoggedIn)
            },
        )

        result == null || result.items.isEmpty() -> EmptyState(
            icon = Icons.Rounded.AutoAwesome,
            title = stringResource(R.string.no_recommendations),
            description = stringResource(R.string.no_recommendations_desc),
            actionLabel = stringResource(R.string.retry),
            onAction = { event?.refresh(isLoggedIn) },
        )

        else -> PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { event?.refresh(isLoggedIn) },
            modifier = Modifier.fillMaxSize(),
        ) {
            RecommendationsList(
                uiState = uiState,
                result = result,
                event = event,
                navActionManager = navActionManager,
            )
        }
    }
}

/**
 * What the screen shows, with every title in exactly one place: the top picks first, then each
 * "Because you liked" row with only titles not shown yet (rows left with too few are dropped),
 * then everything that's still left.
 */
@Immutable
private class RecommendationsLayout(
    val top: List<RecommendedMedia>,
    val rows: List<Pair<RecommendationGroup, List<RecommendedMedia>>>,
    val more: List<RecommendedMedia>,
)

private fun ForYouResult.toLayout(): RecommendationsLayout {
    val top = items.take(TOP_PICKS)
    val shown = top.mapTo(mutableSetOf()) { it.id }
    val rows = groups.mapNotNull { group ->
        val rowItems = itemsOf(group).filterNot { it.id in shown }
        if (rowItems.size < RecommendationEngine.MIN_GROUP_SIZE) {
            null
        } else {
            rowItems.forEach { shown += it.id }
            group to rowItems
        }
    }
    val more = items.filterNot { it.id in shown }
    return RecommendationsLayout(top = top, rows = rows, more = more)
}

@Composable
private fun RecommendationsList(
    uiState: RecommendationsUiState,
    result: ForYouResult,
    event: RecommendationsEvent?,
    navActionManager: NavActionManager,
) {
    val mediaType = uiState.mediaType
    val layout = remember(result) { result.toLayout() }

    fun openDetails(item: RecommendedMedia, posterKey: String) {
        navActionManager.toMediaDetails(
            item.mediaType,
            item.id,
            posterUrl = item.picture,
            posterKey = posterKey,
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // As many poster columns as fit (at least 3), for the More picks grid
        val columns = ((maxWidth - GridHorizontalPadding * 2 + GridSpacing) /
            (MEDIA_POSTER_SMALL_WIDTH.dp + GridSpacing)).toInt().coerceAtLeast(3)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
            ),
        ) {
            if (!result.isPersonal) {
                item(key = "hint", contentType = "hint") {
                    HintCard(
                        text = stringResource(
                            if (result.kind == ForYouResult.Kind.POPULAR) R.string.for_you_login_hint
                            else R.string.for_you_starter_hint
                        ),
                        onClick = if (result.kind == ForYouResult.Kind.POPULAR) {
                            { navActionManager.toLogin() }
                        } else null,
                    )
                }
            }

            // ── Top picks: full cards ────────────────────────────────────────
            if (layout.top.isNotEmpty()) {
                item(key = "topTitle", contentType = "title") {
                    ListSectionTitle(
                        text = stringResource(
                            when (result.kind) {
                                ForYouResult.Kind.PERSONAL -> R.string.top_picks_for_you
                                ForYouResult.Kind.STARTER -> R.string.suggested_for_you
                                ForYouResult.Kind.POPULAR -> R.string.popular_right_now
                            }
                        )
                    )
                }
                items(
                    items = layout.top,
                    key = { "top-${it.id}" },
                    contentType = { "pick" },
                ) { item ->
                    val posterKey = posterSharedKey(PosterSection.recommendations("top"), mediaType, item.id)
                    RecommendationListItem(
                        item = item,
                        posterKey = posterKey,
                        status = uiState.statuses[item.id],
                        hideScore = uiState.hideScore,
                        onClick = dropUnlessResumed { openDetails(item, posterKey) },
                        onAddToPlan = { event?.addToPlan(item) },
                        onNotInterested = { event?.notInterested(item) },
                    )
                }
            }

            // ── "Because you liked …": one row per favourite ─────────────────
            layout.rows.forEach { (group, rowItems) ->
                item(key = "group-${group.seedId}", contentType = "group") {
                    val section = PosterSection.recommendations("group_${group.seedId}")
                    Column {
                        GroupTitle(group = group)
                        // No fixed height: the row is exactly as tall as its cards
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = GridHorizontalPadding),
                            horizontalArrangement = Arrangement.spacedBy(GridSpacing),
                        ) {
                            items(
                                items = rowItems,
                                key = { it.id },
                                contentType = { "poster" },
                            ) { item ->
                                val posterKey = posterSharedKey(section, mediaType, item.id)
                                RecommendationPosterCard(
                                    item = item,
                                    posterKey = posterKey,
                                    status = uiState.statuses[item.id],
                                    hideScore = uiState.hideScore,
                                    showReason = false,
                                    onClick = dropUnlessResumed { openDetails(item, posterKey) },
                                    onAddToPlan = { event?.addToPlan(item) },
                                    onNotInterested = { event?.notInterested(item) },
                                )
                            }
                        }
                    }
                }
            }

            // ── More picks: poster grid ──────────────────────────────────────
            if (layout.more.isNotEmpty()) {
                item(key = "moreTitle", contentType = "title") {
                    ListSectionTitle(text = stringResource(R.string.more_picks))
                }
                items(
                    items = layout.more.chunked(columns),
                    key = { row -> "more-${row.first().id}" },
                    contentType = { "moreRow" },
                ) { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = GridHorizontalPadding, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        row.forEach { item ->
                            val posterKey = posterSharedKey(
                                PosterSection.recommendations("more"), mediaType, item.id
                            )
                            RecommendationPosterCard(
                                item = item,
                                posterKey = posterKey,
                                status = uiState.statuses[item.id],
                                hideScore = uiState.hideScore,
                                showReason = true,
                                onClick = dropUnlessResumed { openDetails(item, posterKey) },
                                onAddToPlan = { event?.addToPlan(item) },
                                onNotInterested = { event?.notInterested(item) },
                            )
                        }
                        // Keep a short last row aligned with the columns above
                        repeat(columns - row.size) {
                            Spacer(modifier = Modifier.width(MEDIA_POSTER_SMALL_WIDTH.dp))
                        }
                    }
                }
            }
        }
    }
}

/** Space around the poster rows and grid, and between posters. */
private val GridHorizontalPadding = 16.dp
private val GridSpacing = 12.dp

/** Same spacing above and below every section title. */
private val SectionTitleTop = 20.dp
private val SectionTitleBottom = 10.dp

@Composable
private fun ListSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(
            start = 20.dp,
            end = 20.dp,
            top = SectionTitleTop,
            bottom = SectionTitleBottom
        )
    )
}

/** "Because you liked" + the favourite's title on one line, next to its small poster. */
@Composable
private fun GroupTitle(group: RecommendationGroup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = SectionTitleTop, bottom = SectionTitleBottom),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MediaPoster(
            url = group.seedPicture,
            showShadow = false,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.size(width = 30.dp, height = 45.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.because_you_liked_header),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            Text(
                text = group.seedPreferredTitle(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HintCard(
    text: String,
    onClick: (() -> Unit)?,
) {
    val modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
    val content = @Composable {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(16.dp)
        )
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.secondaryContainer,
            content = content,
        )
    } else {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.secondaryContainer,
            content = content,
        )
    }
}
