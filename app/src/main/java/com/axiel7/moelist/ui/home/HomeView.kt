package com.axiel7.moelist.ui.home

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterVintage
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.ErrorState
import com.axiel7.moelist.ui.composables.HeaderHorizontalList
import com.axiel7.moelist.ui.composables.media.MEDIA_ITEM_VERTICAL_HEIGHT
import com.axiel7.moelist.ui.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.MediaItemVerticalPlaceholder
import com.axiel7.moelist.ui.composables.score.SmallScoreIndicator
import com.axiel7.moelist.ui.home.composables.AiringAnimeHorizontalItem
import com.axiel7.moelist.ui.home.composables.HomeCard
import androidx.compose.foundation.shape.CircleShape
import coil3.compose.AsyncImage
import com.axiel7.moelist.ui.search.SearchViewContent
import com.axiel7.moelist.ui.search.SearchViewModel
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.SeasonCalendar
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    val searchViewModel: SearchViewModel = koinViewModel()
    val searchUiState by searchViewModel.uiState.collectAsStateWithLifecycle()

    var query by rememberSaveable { mutableStateOf("") }
    // Tracks whether the current input has been "committed" (Enter pressed or history clicked).
    // While false, we show history instead of stale results — see recommendation R1 in the plan.
    var hasCommittedSearch by rememberSaveable { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Auto-focus + show keyboard the moment the bar expands (kills the double-tap bug)
    LaunchedEffect(searchActive) {
        if (searchActive) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    BackHandler(enabled = searchActive) {
        onSearchActiveChange(false)
        keyboardController?.hide()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        // Top row: SearchBar (left, fills available width) + profile icon (right).
        // When the SearchBar expands, M3 overlays the full screen — the profile icon
        // is naturally hidden behind it. The Row only shows in collapsed state visually.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (searchActive) Modifier
                    else Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = {
                            // Enter-only search: typing only updates the input, never fires a request
                            query = it
                            // Any edit invalidates the committed state → show history again
                            if (hasCommittedSearch) hasCommittedSearch = false
                        },
                        onSearch = {
                            // The actual search fires here (Enter)
                            if (it.isNotBlank()) {
                                searchViewModel.search(it)
                                searchViewModel.onSaveSearchHistory(it)
                                hasCommittedSearch = true
                            }
                            keyboardController?.hide()
                        },
                        expanded = searchActive,
                        onExpandedChange = onSearchActiveChange,
                        placeholder = { Text(text = stringResource(R.string.search)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            if (searchActive) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "clear",
                                    modifier = Modifier.clickable {
                                        if (query.isNotEmpty()) {
                                            // Clear (X): just clear the input.
                                            // Per product decision: keep last results visible.
                                            query = ""
                                        } else {
                                            onSearchActiveChange(false)
                                            keyboardController?.hide()
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.focusRequester(focusRequester)
                    )
                },
                expanded = searchActive,
                onExpandedChange = onSearchActiveChange,
                modifier = if (searchActive) Modifier.fillMaxWidth() else Modifier.weight(1f),
                colors = SearchBarDefaults.colors(
                    containerColor = if (searchActive)
                        MaterialTheme.colorScheme.surface
                    else
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                shape = if (searchActive) SearchBarDefaults.fullScreenShape
                else MaterialTheme.shapes.extraLarge,
            ) {
                SearchViewContent(
                    uiState = searchUiState,
                    event = searchViewModel,
                    query = query,
                    isCompactScreen = isCompactScreen,
                    navActionManager = navActionManager,
                    // While typing without committing, show history (R1)
                    showHistory = query.isEmpty() || !hasCommittedSearch,
                    onHistoryItemClick = {
                        query = it
                        searchViewModel.search(it)
                        hasCommittedSearch = true
                        keyboardController?.hide()
                    }
                )
            }

            if (!searchActive) {
                if (isLoggedIn && profilePicture != null) {
                    AsyncImage(
                        model = profilePicture,
                        contentDescription = "profile",
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
                        contentDescription = "profile",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(40.dp)
                            .clickable { navActionManager.toProfile() }
                    )
                }
            }
        }

        HomeViewContent(
            uiState = uiState,
            event = viewModel,
            isLoggedIn = isLoggedIn,
            navActionManager = navActionManager,
        )
    }
}

@Composable
private fun HomeViewContent(
    uiState: HomeUiState,
    event: HomeEvent?,
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
) {
    val context = LocalContext.current
    val airingListState = rememberLazyListState()
    val seasonalListState = rememberLazyListState()
    val scrollState = rememberScrollState()

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
    ) {
        // Hero Section Header - Reduced top padding to minimize empty space
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp)
        ) {
            Text(
                text = "Discover",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-1).sp
            )
            Text(
                text = "What will you watch today?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }

        // Asymmetric Bento Grid - Color OS Influence
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Main Highlight Card
                HomeCard(
                    text = stringResource(R.string.anime_ranking),
                    icon = Icons.AutoMirrored.Rounded.TrendingUp,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(160.dp),
                    onClick = dropUnlessResumed {
                        navActionManager.toMediaRanking(MediaType.ANIME)
                    },
                )

                // Side Cards Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val seasonalIcon = when (SeasonCalendar.currentSeason) {
                        Season.WINTER -> Icons.Rounded.AcUnit
                        Season.SPRING -> Icons.Rounded.LocalFlorist
                        Season.SUMMER -> Icons.Rounded.WbSunny
                        Season.FALL -> Icons.Rounded.FilterVintage
                    }

                    HomeCard(
                        text = stringResource(R.string.seasonal_chart),
                        icon = seasonalIcon,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(74.dp),
                        onClick = dropUnlessResumed {
                            navActionManager.toSeasonChart()
                        },
                    )

                    HomeCard(
                        text = stringResource(R.string.calendar),
                        icon = Icons.Rounded.CalendarMonth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(74.dp),
                        onClick = dropUnlessResumed {
                            navActionManager.toCalendar()
                        },
                    )
                }
            }

            // Secondary Broad Card
            HomeCard(
                text = stringResource(R.string.manga_ranking),
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                onClick = dropUnlessResumed {
                    navActionManager.toMediaRanking(MediaType.MANGA)
                },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Airing Today Section
        HeaderHorizontalList(
            text = stringResource(R.string.today),
            onClick = dropUnlessResumed { navActionManager.toCalendar() }
        )

        if (!isLoggedIn) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = dropUnlessResumed { navActionManager.toLogin() },
                shape = RoundedCornerShape(20.dp),
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 12.dp))
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
                        .height(80.dp),
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
                        .padding(top = 8.dp)
                        .height(160.dp),
                    state = airingListState,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    flingBehavior = rememberSnapFlingBehavior(lazyListState = airingListState),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
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

        Spacer(modifier = Modifier.height(24.dp))

        // Seasonal Section
        HeaderHorizontalList(
            text = stringResource(R.string.this_season),
            onClick = dropUnlessResumed { navActionManager.toSeasonChart() }
        )

        if (uiState.message != null && uiState.seasonalAnimes.isEmpty()) {
            ErrorState(
                modifier = Modifier.height(MEDIA_ITEM_VERTICAL_HEIGHT.dp),
                message = uiState.message,
                onAction = { event?.initRequestChain(isLoggedIn) }
            )
        } else {
            LazyRow(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .height(MEDIA_ITEM_VERTICAL_HEIGHT.dp),
                state = seasonalListState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                flingBehavior = rememberSnapFlingBehavior(lazyListState = seasonalListState),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                                    fontSize = 12.sp
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
                    items(5) {
                        MediaItemVerticalPlaceholder()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}
