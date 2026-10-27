package com.axiel7.moelist.ui.season

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberFloatingToolbarState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.BackIconButton
import com.axiel7.moelist.ui.composables.EmptyState
import com.axiel7.moelist.ui.composables.ErrorState
import com.axiel7.moelist.ui.composables.LoadingState
import com.axiel7.moelist.ui.composables.LocalSnackbarHostState
import com.axiel7.moelist.ui.composables.PosterSection
import com.axiel7.moelist.ui.composables.ProvideSnackbarOffset
import com.axiel7.moelist.ui.composables.SnackbarType
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.posterSharedKey
import com.axiel7.moelist.ui.composables.rememberLoadErrorState
import com.axiel7.moelist.ui.composables.showAppSnackbar
import com.axiel7.moelist.ui.editmedia.EditMediaSheet
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.outlined.CloudOff
import com.axiel7.moelist.ui.icons.outlined.EventBusy
import com.axiel7.moelist.ui.icons.rounded.Add
import com.axiel7.moelist.ui.icons.rounded.Check
import com.axiel7.moelist.ui.icons.rounded.ChevronLeft
import com.axiel7.moelist.ui.icons.rounded.ChevronRight
import com.axiel7.moelist.ui.icons.rounded.Tune
import com.axiel7.moelist.ui.season.composables.SeasonChartFilterSheet
import com.axiel7.moelist.ui.season.composables.SeasonChartFormatSheet
import com.axiel7.moelist.ui.season.composables.SeasonChartSeasonSheet
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.Motion
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SeasonChartView(
    navActionManager: NavActionManager
) {
    val viewModel: SeasonChartViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SeasonChartViewContent(
        uiState = uiState,
        event = viewModel,
        navActionManager = navActionManager
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeasonChartViewContent(
    uiState: SeasonChartUiState,
    event: SeasonChartEvent?,
    navActionManager: NavActionManager?
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showFilterSheet by remember { mutableStateOf(false) }
    fun hideFilterSheet() {
        scope.launch { filterSheetState.hide() }.invokeOnCompletion { showFilterSheet = false }
    }

    val formatSheetState = rememberModalBottomSheetState()
    var showFormatSheet by remember { mutableStateOf(false) }
    fun hideFormatSheet() {
        scope.launch { formatSheetState.hide() }.invokeOnCompletion { showFormatSheet = false }
    }

    val seasonSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSeasonSheet by remember { mutableStateOf(false) }
    fun hideSeasonSheet() {
        scope.launch { seasonSheetState.hide() }.invokeOnCompletion { showSeasonSheet = false }
    }

    val editSheetState = rememberModalBottomSheetState()
    var selectedAnimeForEdit by remember { mutableStateOf<AnimeSeasonal?>(null) }
    fun hideEditSheet() {
        scope.launch { editSheetState.hide() }.invokeOnCompletion { selectedAnimeForEdit = null }
    }

    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val toolbarState = rememberFloatingToolbarState()
    val toolbarScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom,
        state = toolbarState,
    )

    if (showFilterSheet) {
        SeasonChartFilterSheet(
            uiState = uiState,
            event = event,
            onApply = {
                hideFilterSheet()
                event?.onApplyFilters()
            },
            onDismiss = { hideFilterSheet() },
            sheetState = filterSheetState,
            bottomPadding = bottomBarPadding
        )
    }

    if (showFormatSheet) {
        SeasonChartFormatSheet(
            uiState = uiState,
            event = event,
            onDismiss = { hideFormatSheet() },
            sheetState = formatSheetState,
            bottomPadding = bottomBarPadding
        )
    }

    if (showSeasonSheet) {
        SeasonChartSeasonSheet(
            current = uiState.season,
            onSelect = { season ->
                hideSeasonSheet()
                if (season != uiState.season) {
                    event?.setSeason(season = season.season, year = season.year)
                    event?.onApplyFilters()
                }
            },
            onDismiss = { hideSeasonSheet() },
            sheetState = seasonSheetState,
            bottomPadding = bottomBarPadding
        )
    }

    selectedAnimeForEdit?.let { anime ->
        EditMediaSheet(
            sheetState = editSheetState,
            mediaInfo = anime.node,
            myListStatus = event?.getMyListStatusOf(anime.node.id),
            onEdited = { _, _ -> hideEditSheet() },
            onDismissed = { hideEditSheet() }
        )
    }

    // Quick-add feedback through the app's shared snackbar (lifted above the floating toolbar
    // by ProvideSnackbarOffset below)
    val snackbarHostState = LocalSnackbarHostState.current
    val addedText = stringResource(R.string.added_to_plan_to_watch)
    val undoText = stringResource(R.string.undo)
    LaunchedEffect(uiState.quickAddResult) {
        val result = uiState.quickAddResult ?: return@LaunchedEffect
        val title = uiState.animes.find { it.node.id == result.animeId }?.node?.userPreferredTitle()
        event?.onQuickAddResultShown()
        haptic.performHapticFeedback(
            if (result.success) HapticFeedbackType.Confirm else HapticFeedbackType.Reject
        )
        // Separate scope: clearing the result restarts this effect, which must not cancel the
        // snackbar that's showing
        scope.launch {
            if (result.success) {
                val action = snackbarHostState.showAppSnackbar(
                    message = addedText,
                    type = SnackbarType.SUCCESS,
                    highlight = title,
                    actionLabel = undoText,
                )
                if (action == SnackbarResult.ActionPerformed) {
                    event?.onUndoQuickAdd(result.animeId)
                }
            } else {
                snackbarHostState.showAppSnackbar(
                    message = "Failed to add to list",
                    type = SnackbarType.ERROR,
                )
            }
        }
    }

    // Snackbars sit above the floating toolbar on this screen
    ProvideSnackbarOffset(bottom = SnackbarAboveToolbar)

    // Full-screen error stays until Retry (snackbar only when content is already showing)
    val loadError = rememberLoadErrorState(
        message = uiState.message,
        hasContent = uiState.animes.isNotEmpty(),
        onMessageConsumed = { event?.onMessageDisplayed() },
        onRetry = { event?.onApplyFilters() },
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = stringResource(R.string.seasonal_chart),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Season + count stay readable while the toolbar is hidden
                        Text(
                            text = buildString {
                                append(uiState.season.seasonYearText())
                                if (uiState.animes.isNotEmpty()) {
                                    append(" · ")
                                    append(stringResource(R.string.anime_count, uiState.animes.size))
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    BackIconButton(onClick = { navActionManager?.goBack() })
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                // The toolbar hides while scrolling down and comes back on scroll up
                .nestedScroll(toolbarScrollBehavior)
        ) {
            // Animate only when the *kind* of content changes (loading / error / empty / grid).
            // Keying AnimatedContent on the whole uiState re-created the grid on every state
            // update (filter change, quick-add...), crossfading the screen and resetting scroll.
            val contentKind = when {
                uiState.isLoading && uiState.animes.isEmpty() -> SeasonChartContentKind.LOADING
                loadError.value != null && uiState.animes.isEmpty() -> SeasonChartContentKind.ERROR
                !uiState.isLoading && uiState.animes.isEmpty() -> SeasonChartContentKind.EMPTY
                else -> SeasonChartContentKind.GRID
            }
            // Bring the toolbar back whenever the grid is replaced (new season, retry...)
            LaunchedEffect(contentKind) {
                if (contentKind != SeasonChartContentKind.GRID) toolbarState.offset = 0f
            }
            // Read here: AnimatedContent's transitionSpec isn't @Composable
            val contentFade = Motion.effects<Float>()

            AnimatedContent(
                targetState = contentKind,
                transitionSpec = {
                    fadeIn(animationSpec = contentFade) togetherWith fadeOut(animationSpec = contentFade)
                },
                label = "SeasonChartContent"
            ) { kind ->
                when (kind) {
                    SeasonChartContentKind.LOADING -> {
                        LoadingState(modifier = Modifier.fillMaxSize())
                    }
                    SeasonChartContentKind.ERROR -> {
                        ErrorState(
                            modifier = Modifier.fillMaxSize(),
                            icon = Icons.Outlined.CloudOff,
                            message = loadError.value,
                            onAction = {
                                loadError.value = null
                                event?.onApplyFilters()
                            }
                        )
                    }
                    SeasonChartContentKind.EMPTY -> {
                        EmptyState(
                            modifier = Modifier.fillMaxSize(),
                            icon = Icons.Outlined.EventBusy,
                            title = stringResource(R.string.no_anime_found),
                            description = stringResource(R.string.no_anime_found_desc),
                        )
                    }
                    SeasonChartContentKind.GRID -> {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Adaptive(minSize = MEDIA_POSTER_SMALL_WIDTH.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 12.dp,
                                top = 8.dp,
                                end = 12.dp,
                                bottom = bottomBarPadding + ToolbarClearance
                            ),
                            verticalItemSpacing = 16.dp,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.filteredAnimes,
                                key = { it.node.id },
                                contentType = { "media" }
                            ) { item ->
                                val score = item.node.mean
                                val listStatus = item.node.myListStatus?.status
                                val quickAddState = uiState.quickAdd[item.node.id]
                                val posterKey = posterSharedKey(PosterSection.SEASON_CHART, MediaType.ANIME, item.node.id)
                                MediaItemVertical(
                                    imageUrl = item.node.mainPicture?.large,
                                    posterKey = posterKey,
                                    title = item.node.userPreferredTitle(),
                                    // Hidden while the quick-add check is still showing
                                    status = listStatus?.takeIf { quickAddState == null },
                                    // Respects the "hide scores" setting like every other screen
                                    score = score.takeUnless { uiState.hideScore },
                                    minLines = 2,
                                    posterOverlay = {
                                        // Stays while adding / just added, even though the entry is
                                        // already in the list by then
                                        if (listStatus == null || quickAddState != null) {
                                            QuickAddButton(
                                                state = quickAddState,
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    event?.onQuickAddPlanToWatch(item.node.id)
                                                },
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .align(Alignment.TopEnd)
                                            )
                                        }
                                    },
                                    onClick = dropUnlessResumed {
                                        navActionManager?.toMediaDetails(
                                            MediaType.ANIME,
                                            item.node.id,
                                            posterUrl = item.node.mainPicture?.large,
                                            posterKey = posterKey,
                                        )
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedAnimeForEdit = item
                                    }
                                )
                            }
                        }
                    }
                }
            }

            SeasonChartToolbar(
                uiState = uiState,
                scrollBehavior = toolbarScrollBehavior,
                onPreviousSeason = {
                    val season = uiState.season.previous()
                    event?.setSeason(season = season.season, year = season.year)
                    event?.onApplyFilters()
                },
                onNextSeason = {
                    val season = uiState.season.next()
                    event?.setSeason(season = season.season, year = season.year)
                    event?.onApplyFilters()
                },
                onSeasonClick = { showSeasonSheet = true },
                onFormatClick = { showFormatSheet = true },
                onFiltersClick = { showFilterSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomBarPadding)
                    .offset(y = -FloatingToolbarDefaults.ScreenOffset)
                    .zIndex(1f)
            )
        }
    }
}

/** Snackbar lift on this screen: the floating toolbar (64dp) plus its 16dp margin. */
private val SnackbarAboveToolbar = 80.dp

/**
 * The "+" on a poster. While the request runs it shows an M3 Expressive loading indicator (and
 * ignores taps); on success it pops into a check (fast spatial spring) before the normal status
 * badge takes over.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun QuickAddButton(
    state: QuickAddState?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val addLabel = stringResource(R.string.add)
    val pop = Motion.fastSpatial<Float>()
    val fade = Motion.fastEffects<Float>()

    Surface(
        onClick = onClick,
        enabled = state == null,
        modifier = modifier
            .size(24.dp)
            .semantics { contentDescription = addLabel },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                (scaleIn(pop, initialScale = 0.4f) + fadeIn(fade)) togetherWith fadeOut(fade)
            },
            contentAlignment = Alignment.Center,
            label = "quickAdd"
        ) { current ->
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                when (current) {
                    null -> Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    QuickAddState.ADDING -> LoadingIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    QuickAddState.ADDED -> Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/** Space under the grid so the last row can scroll above the floating toolbar. */
private val ToolbarClearance = 96.dp

/**
 * M3 Expressive floating toolbar for the season chart: step to the previous / next season,
 * jump to any season from the season label, pick a format, or open the filters (show / sort).
 * It slides away while scrolling down and returns on scroll up.
 */
@Composable
private fun SeasonChartToolbar(
    uiState: SeasonChartUiState,
    scrollBehavior: FloatingToolbarScrollBehavior,
    onPreviousSeason: () -> Unit,
    onNextSeason: () -> Unit,
    onSeasonClick: () -> Unit,
    onFormatClick: () -> Unit,
    onFiltersClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatText = uiState.selectedFormat?.localized() ?: stringResource(R.string.all)
    val count = uiState.formatCounts[uiState.selectedFormat] ?: 0

    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier,
        scrollBehavior = scrollBehavior,
    ) {
        IconButton(
            onClick = onPreviousSeason,
            enabled = !uiState.isLoading,
            shapes = IconButtonDefaults.shapes(),
        ) {
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = stringResource(R.string.previous_season)
            )
        }
        TextButton(
            onClick = onSeasonClick,
            shapes = ButtonDefaults.shapes(),
            contentPadding = PaddingValues(horizontal = 8.dp),
        ) {
            Text(
                text = uiState.season.seasonYearText(),
                style = MaterialTheme.typography.labelLargeEmphasized,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 120.dp)
            )
        }
        IconButton(
            onClick = onNextSeason,
            enabled = !uiState.isLoading,
            shapes = IconButtonDefaults.shapes(),
        ) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = stringResource(R.string.next_season)
            )
        }

        Spacer(Modifier.width(4.dp))

        FilledTonalButton(
            onClick = onFormatClick,
            shapes = ButtonDefaults.shapes(),
            contentPadding = PaddingValues(horizontal = 12.dp),
        ) {
            Text(
                text = formatText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 72.dp)
            )
            Spacer(Modifier.width(6.dp))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }

        Spacer(Modifier.width(4.dp))

        FilledIconButton(
            onClick = onFiltersClick,
            shapes = IconButtonDefaults.shapes(),
        ) {
            Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = stringResource(R.string.filters)
            )
        }
    }
}

/** What the season chart is currently showing; AnimatedContent animates between these. */
private enum class SeasonChartContentKind { LOADING, ERROR, EMPTY, GRID }

@Preview
@Composable
fun SeasonChartViewPreview() {
    MoeListTheme {
        SeasonChartViewContent(
            uiState = SeasonChartUiState(),
            event = null,
            navActionManager = null
        )
    }
}
