package com.axiel7.moelist.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import coil3.compose.AsyncImage
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.EmptyState
import com.axiel7.moelist.ui.composables.ErrorState
import com.axiel7.moelist.ui.composables.PosterSection
import com.axiel7.moelist.ui.composables.isSessionExpiredMessage
import com.axiel7.moelist.ui.composables.media.MEDIA_ITEM_VERTICAL_HEIGHT
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.MediaItemVerticalPlaceholder
import com.axiel7.moelist.ui.composables.posterSharedKey
import com.axiel7.moelist.ui.composables.rememberLoadErrorState
import com.axiel7.moelist.ui.home.composables.TodayCarousel
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.MenuBook
import com.axiel7.moelist.ui.icons.automirrored.rounded.TrendingUp
import com.axiel7.moelist.ui.icons.outlined.CloudOff
import com.axiel7.moelist.ui.icons.outlined.EventBusy
import com.axiel7.moelist.ui.icons.outlined.LockReset
import com.axiel7.moelist.ui.icons.rounded.AccountCircle
import com.axiel7.moelist.ui.icons.rounded.CalendarMonth
import com.axiel7.moelist.ui.icons.rounded.Close
import com.axiel7.moelist.ui.icons.rounded.Search
import com.axiel7.moelist.ui.search.SearchViewContent
import com.axiel7.moelist.ui.search.SearchViewModel
import com.axiel7.moelist.ui.theme.Motion
import com.axiel7.moelist.utils.SeasonCalendar
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeView(
    isLoggedIn: Boolean,
    isCompactScreen: Boolean,
    navActionManager: NavActionManager,
    padding: PaddingValues,
    searchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    profilePicture: String?,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // The search ViewModel is NOT created here: it lives in HomeSearchResults, which only exists
    // while search is expanded, so opening Home doesn't start the search pipeline. A search typed
    // before the results were composed is kept in pendingSearch and run as soon as they are.
    //
    // Material 3's SearchBarState + TextFieldState API (the old query/expanded SearchBar is
    // deprecated). The results are shown inline in Home rather than in the library's
    // full-screen dialog (ExpandedFullScreenSearchBar): a dialog would stay on top while a
    // result's details screen opens, and would break the poster shared-element transition.
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()
    val query = textFieldState.text.toString()
    val expanded = searchBarState.targetValue == SearchBarValue.Expanded
    val scope = rememberCoroutineScope()

    // The query whose results are on screen; typing anything else shows the history again
    var committedQuery by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingSearch by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingSaveHistory by rememberSaveable { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current

    fun collapse() {
        keyboardController?.hide()
        scope.launch { searchBarState.animateToCollapsed() }
    }

    // Report the state up (MainActivity hides the bottom bar while searching), and follow it
    // if the parent closes search
    LaunchedEffect(expanded) { onSearchActiveChange(expanded) }
    LaunchedEffect(searchActive) {
        if (!searchActive && searchBarState.targetValue == SearchBarValue.Expanded) {
            searchBarState.animateToCollapsed()
        }
    }

    BackHandler(enabled = expanded) { collapse() }

    val barColor by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.surfaceContainerHighest
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = Motion.fastEffects(),
        label = "searchBarColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = barColor,
                modifier = Modifier.weight(1f),
            ) {
                SearchBarDefaults.InputField(
                    textFieldState = textFieldState,
                    searchBarState = searchBarState,
                    onSearch = {
                        if (it.isNotBlank()) {
                            pendingSearch = it
                            pendingSaveHistory = true
                            committedQuery = it
                        }
                        keyboardController?.hide()
                    },
                    placeholder = { Text(text = stringResource(R.string.search)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (expanded) {
                            IconButton(
                                onClick = {
                                    if (query.isNotEmpty()) textFieldState.clearText()
                                    else collapse()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.clear),
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Package-qualified: the plain (non-scoped) overload, so the outer Column's
            // ColumnScope.AnimatedVisibility isn't picked up by the implicit receiver
            androidx.compose.animation.AnimatedVisibility(
                visible = !expanded,
                enter = fadeIn(Motion.fastEffects()) + scaleIn(Motion.fastSpatial()),
                exit = fadeOut(Motion.fastEffects()) + scaleOut(Motion.fastSpatial()),
            ) {
                if (isLoggedIn && profilePicture != null) {
                    AsyncImage(
                        model = profilePicture,
                        contentDescription = stringResource(R.string.title_profile),
                        placeholder = painterResource(R.drawable.ic_round_account_circle_24),
                        error = painterResource(R.drawable.ic_round_account_circle_24),
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(40.dp)
                            .clickable { navActionManager.toProfile() }
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_round_account_circle_24),
                        contentDescription = stringResource(R.string.title_profile),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(40.dp)
                            .clickable { navActionManager.toProfile() }
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            // Home stays composed underneath, so its scroll position and data survive a search
            HomeViewContent(
                uiState = uiState,
                event = viewModel,
                isLoggedIn = isLoggedIn,
                navActionManager = navActionManager,
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(Motion.effects()),
                exit = fadeOut(Motion.fastEffects()),
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxSize()
                ) {
                    HomeSearchResults(
                        query = query,
                        isCompactScreen = isCompactScreen,
                        navActionManager = navActionManager,
                        showHistory = query.isEmpty() || committedQuery != query,
                        pendingSearch = pendingSearch,
                        pendingSaveHistory = pendingSaveHistory,
                        onPendingSearchHandled = {
                            pendingSearch = null
                            pendingSaveHistory = false
                        },
                        onHistoryItemClick = {
                            textFieldState.setTextAndPlaceCursorAtEnd(it)
                            // History items are already saved, so only search
                            pendingSearch = it
                            pendingSaveHistory = false
                            committedQuery = it
                            keyboardController?.hide()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Search results inside the expanded search bar. Owns the search ViewModel, so it's only
 * created when search is first opened (the search bar composes its content only while expanded).
 */
@Composable
private fun HomeSearchResults(
    query: String,
    isCompactScreen: Boolean,
    navActionManager: NavActionManager,
    showHistory: Boolean,
    pendingSearch: String?,
    pendingSaveHistory: Boolean,
    onPendingSearchHandled: () -> Unit,
    onHistoryItemClick: (String) -> Unit,
) {
    val searchViewModel: SearchViewModel = koinViewModel()
    val searchUiState by searchViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(pendingSearch) {
        val pending = pendingSearch ?: return@LaunchedEffect
        searchViewModel.search(pending)
        if (pendingSaveHistory) searchViewModel.onSaveSearchHistory(pending)
        onPendingSearchHandled()
    }

    SearchViewContent(
        uiState = searchUiState,
        event = searchViewModel,
        query = query,
        isCompactScreen = isCompactScreen,
        navActionManager = navActionManager,
        showHistory = showHistory,
        onHistoryItemClick = onHistoryItemClick
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HomeViewContent(
    uiState: HomeUiState,
    event: HomeEvent?,
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
) {
    val seasonalListState = rememberLazyListState()

    // Full-screen (section) errors stay until Retry. Only when both sections already have
    // content is a new error shown as a snackbar instead.
    val loadError = rememberLoadErrorState(
        message = uiState.message,
        hasContent = uiState.todayAnimes.isNotEmpty() && uiState.seasonalAnimes.isNotEmpty(),
        onMessageConsumed = { event?.onMessageDisplayed() },
    )

    LaunchedEffect(isLoggedIn) {
        event?.initRequestChain(isLoggedIn)
    }

    // Uses the app's own season artwork (same icons as the season chart), picked from the
    // current anime season, so the tile updates automatically every season
    val seasonalIcon = ImageVector.vectorResource(SeasonCalendar.currentSeason.icon)

    @Composable
    fun SectionError(height: androidx.compose.ui.unit.Dp) {
        val sessionExpired = isSessionExpiredMessage(loadError.value)
        ErrorState(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            icon = if (sessionExpired) Icons.Outlined.LockReset else Icons.Outlined.CloudOff,
            message = loadError.value,
            actionLabel = if (sessionExpired) stringResource(R.string.sign_in_again)
            else stringResource(R.string.retry),
            onAction = {
                if (sessionExpired) navActionManager.toLogin()
                else {
                    loadError.value = null
                    event?.initRequestChain(isLoggedIn)
                }
            },
            compact = true,
        )
    }

    // Lazy root: sections are composed as they scroll into view (the old Column +
    // verticalScroll built everything up front)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item(key = "title", contentType = "title") {
            Text(
                text = stringResource(R.string.discover),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp)
            )
        }

        item(key = "shortcuts", contentType = "shortcuts") {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeNavTile(
                        title = stringResource(R.string.anime_ranking),
                        subtitle = stringResource(R.string.top_rated),
                        icon = Icons.AutoMirrored.Rounded.TrendingUp,
                        iconShape = MaterialShapes.Sunny.toShape(),
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = dropUnlessResumed {
                            navActionManager.toMediaRanking(MediaType.ANIME)
                        }
                    )
                    HomeNavTile(
                        title = stringResource(R.string.seasonal_chart),
                        subtitle = SeasonCalendar.currentStartSeason.seasonYearText(),
                        icon = seasonalIcon,
                        iconShape = MaterialShapes.Flower.toShape(),
                        iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = dropUnlessResumed {
                            navActionManager.toSeasonChart()
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeNavTile(
                        title = stringResource(R.string.calendar),
                        subtitle = stringResource(R.string.weekly_schedule),
                        icon = Icons.Rounded.CalendarMonth,
                        iconShape = MaterialShapes.Cookie4Sided.toShape(),
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = dropUnlessResumed {
                            navActionManager.toCalendar()
                        }
                    )
                    HomeNavTile(
                        title = stringResource(R.string.manga_ranking),
                        subtitle = stringResource(R.string.top_manga),
                        icon = Icons.AutoMirrored.Rounded.MenuBook,
                        iconShape = MaterialShapes.Clover4Leaf.toShape(),
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = dropUnlessResumed {
                            navActionManager.toMediaRanking(MediaType.MANGA)
                        }
                    )
                }
            }
        }

        item(key = "todayHeader", contentType = "header") {
            SectionHeader(
                title = stringResource(R.string.today),
                onSeeAll = dropUnlessResumed { navActionManager.toCalendar() },
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        item(key = "today", contentType = "today") {
            when {
                !isLoggedIn -> LoginPrompt(
                    onClick = dropUnlessResumed { navActionManager.toLogin() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )

                loadError.value != null && uiState.todayAnimes.isEmpty() && !uiState.isLoading ->
                    SectionError(height = 140.dp)

                !uiState.isLoading && uiState.todayAnimes.isEmpty() -> EmptyState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    icon = Icons.Outlined.EventBusy,
                    title = stringResource(R.string.nothing_airing_today),
                    description = stringResource(R.string.nothing_airing_today_desc),
                    compact = true,
                )

                else -> TodayCarousel(
                    items = uiState.todayAnimes,
                    isLoading = uiState.isLoading,
                    hideScore = uiState.hideScore,
                    posterKey = { posterSharedKey(PosterSection.HOME_TODAY, MediaType.ANIME, it.node.id) },
                    onClick = { item, posterKey ->
                        navActionManager.toMediaDetails(
                            MediaType.ANIME,
                            item.node.id,
                            posterUrl = item.node.mainPicture?.large ?: item.node.mainPicture?.medium,
                            posterKey = posterKey,
                        )
                    },
                )
            }
        }

        item(key = "seasonHeader", contentType = "header") {
            SectionHeader(
                title = stringResource(R.string.this_season),
                onSeeAll = dropUnlessResumed { navActionManager.toSeasonChart() },
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item(key = "season", contentType = "season") {
            when {
                loadError.value != null && uiState.seasonalAnimes.isEmpty() && !uiState.isLoading ->
                    SectionError(height = MEDIA_ITEM_VERTICAL_HEIGHT.dp)

                !uiState.isLoading && uiState.seasonalAnimes.isEmpty() -> EmptyState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MEDIA_ITEM_VERTICAL_HEIGHT.dp),
                    icon = Icons.Outlined.EventBusy,
                    title = stringResource(R.string.no_anime_this_season),
                    description = stringResource(R.string.no_anime_this_season_desc),
                    compact = true,
                )

                else -> LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MEDIA_ITEM_VERTICAL_HEIGHT.dp),
                    state = seasonalListState,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    flingBehavior = rememberSnapFlingBehavior(lazyListState = seasonalListState),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.seasonalAnimes,
                        key = { it.node.id },
                        contentType = { "media" }
                    ) {
                        val score = it.node.mean
                        val posterKey = posterSharedKey(PosterSection.HOME_SEASON, MediaType.ANIME, it.node.id)
                        MediaItemVertical(
                            imageUrl = it.node.mainPicture?.large,
                            posterKey = posterKey,
                            title = it.node.userPreferredTitle(),
                            status = it.node.myListStatus?.status,
                            score = score.takeUnless { uiState.hideScore },
                            minLines = 2,
                            onClick = dropUnlessResumed {
                                navActionManager.toMediaDetails(
                                    MediaType.ANIME,
                                    it.node.id,
                                    posterUrl = it.node.mainPicture?.large,
                                    posterKey = posterKey,
                                )
                            }
                        )
                    }
                    if (uiState.isLoading) {
                        items(5, contentType = { "placeholder" }) {
                            MediaItemVerticalPlaceholder()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Shortcut tile. The icon sits in an M3 Expressive shape (sunny, flower, cookie, clover), each
 * tile with its own, and the tile's corners tighten while pressed (Expressive pressed shape).
 */
@Composable
private fun HomeNavTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconShape: Shape,
    iconContainerColor: Color,
    iconContentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val corner by animateDpAsState(
        targetValue = if (isPressed) TilePressedCorner else TileCorner,
        animationSpec = Motion.fastSpatial(),
        label = "tileCorner"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(corner),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(iconShape)
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconContentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Shortcut tile corners: M3 Large, tightening to Medium while pressed. */
private val TileCorner = 24.dp
private val TilePressedCorner = 12.dp

@Composable
private fun SectionHeader(
    title: String,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(onClick = onSeeAll, shapes = ButtonDefaults.shapes()) {
            Text(text = stringResource(R.string.see_all))
        }
    }
}

@Composable
private fun LoginPrompt(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxSize(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                text = stringResource(R.string.please_login_to_use_this_feature),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
