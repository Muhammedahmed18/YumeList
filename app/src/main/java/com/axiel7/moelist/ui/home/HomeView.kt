package com.axiel7.moelist.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.LockReset
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
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import coil3.compose.AsyncImage
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.EmptyState
import com.axiel7.moelist.ui.composables.ErrorState
import com.axiel7.moelist.ui.composables.LocalSnackbarHostState
import com.axiel7.moelist.ui.composables.isSessionExpiredMessage
import com.axiel7.moelist.ui.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.MediaItemVerticalPlaceholder
import com.axiel7.moelist.ui.composables.media.PosterStatusBadge
import com.axiel7.moelist.ui.composables.score.PosterScoreChip
import com.axiel7.moelist.ui.home.composables.AiringAnimeHorizontalItem
import com.axiel7.moelist.ui.search.SearchViewContent
import com.axiel7.moelist.ui.search.SearchViewModel
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
    var hasCommittedSearch by rememberSaveable { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

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
                            query = it
                            if (hasCommittedSearch) hasCommittedSearch = false
                        },
                        onSearch = {
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
    val snackbarHostState = LocalSnackbarHostState.current
    val airingListState = rememberLazyListState()
    val seasonalListState = rememberLazyListState()

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            snackbarHostState.showSnackbar(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    LaunchedEffect(isLoggedIn) {
        event?.initRequestChain(isLoggedIn)
    }

    val seasonalIcon = when (SeasonCalendar.currentSeason) {
        Season.WINTER -> Icons.Rounded.AcUnit
        Season.SPRING -> Icons.Rounded.LocalFlorist
        Season.SUMMER -> Icons.Rounded.WbSunny
        Season.FALL -> Icons.Rounded.FilterVintage
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.discover),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeNavTile(
                    title = stringResource(R.string.anime_ranking),
                    subtitle = stringResource(R.string.top_rated),
                    icon = Icons.AutoMirrored.Rounded.TrendingUp,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    onClick = dropUnlessResumed {
                        navActionManager.toMediaRanking(MediaType.ANIME)
                    }
                )
                HomeNavTile(
                    title = stringResource(R.string.seasonal_chart),
                    subtitle = SeasonCalendar.currentStartSeason.seasonYearText(),
                    icon = seasonalIcon,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    onClick = dropUnlessResumed {
                        navActionManager.toSeasonChart()
                    }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeNavTile(
                    title = stringResource(R.string.calendar),
                    subtitle = stringResource(R.string.weekly_schedule),
                    icon = Icons.Rounded.CalendarMonth,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    onClick = dropUnlessResumed {
                        navActionManager.toCalendar()
                    }
                )
                HomeNavTile(
                    title = stringResource(R.string.manga_ranking),
                    subtitle = stringResource(R.string.top_manga),
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    onClick = dropUnlessResumed {
                        navActionManager.toMediaRanking(MediaType.MANGA)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SectionHeader(
            title = stringResource(R.string.today),
            onSeeAll = dropUnlessResumed { navActionManager.toCalendar() }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            when {
                !isLoggedIn -> LoginPrompt(
                    onClick = dropUnlessResumed { navActionManager.toLogin() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )

                uiState.message != null && uiState.todayAnimes.isEmpty() && !uiState.isLoading -> {
                    val sessionExpired = isSessionExpiredMessage(uiState.message)
                    ErrorState(
                        modifier = Modifier.fillMaxSize(),
                        icon = if (sessionExpired) Icons.Outlined.LockReset else Icons.Outlined.CloudOff,
                        message = uiState.message,
                        actionLabel = if (sessionExpired) stringResource(R.string.sign_in_again)
                        else stringResource(R.string.retry),
                        onAction = {
                            if (sessionExpired) navActionManager.toLogin()
                            else event?.initRequestChain(isLoggedIn)
                        },
                        compact = true,
                    )
                }

                !uiState.isLoading && uiState.todayAnimes.isEmpty() -> EmptyState(
                    modifier = Modifier.fillMaxSize(),
                    icon = Icons.Outlined.EventBusy,
                    title = stringResource(R.string.nothing_airing_today),
                    description = stringResource(R.string.nothing_airing_today_desc),
                    compact = true,
                )

                else -> LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    state = airingListState,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    flingBehavior = rememberSnapFlingBehavior(lazyListState = airingListState),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
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

        Spacer(modifier = Modifier.height(4.dp))

        SectionHeader(
            title = stringResource(R.string.this_season),
            onSeeAll = dropUnlessResumed { navActionManager.toSeasonChart() }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 230.dp)
        ) {
            when {
                uiState.message != null && uiState.seasonalAnimes.isEmpty() && !uiState.isLoading -> {
                    val sessionExpired = isSessionExpiredMessage(uiState.message)
                    ErrorState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp),
                        icon = if (sessionExpired) Icons.Outlined.LockReset else Icons.Outlined.CloudOff,
                        message = uiState.message,
                        actionLabel = if (sessionExpired) stringResource(R.string.sign_in_again)
                        else stringResource(R.string.retry),
                        onAction = {
                            if (sessionExpired) navActionManager.toLogin()
                            else event?.initRequestChain(isLoggedIn)
                        },
                        compact = true,
                    )
                }

                !uiState.isLoading && uiState.seasonalAnimes.isEmpty() -> EmptyState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    icon = Icons.Outlined.EventBusy,
                    title = stringResource(R.string.no_anime_this_season),
                    description = stringResource(R.string.no_anime_this_season_desc),
                    compact = true,
                )

                else -> LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
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
                        val score = it.node.mean
                        MediaItemVertical(
                            imageUrl = it.node.mainPicture?.large,
                            title = it.node.userPreferredTitle(),
                            badgeContent = it.node.myListStatus?.status?.let { status ->
                                { PosterStatusBadge(status) }
                            },
                            posterOverlay = if (!uiState.hideScore && score != null && score > 0f) {
                                {
                                    PosterScoreChip(
                                        score = score,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .align(Alignment.BottomStart)
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
        }
    }
}

@Composable
private fun HomeNavTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
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

@Composable
private fun SectionHeader(
    title: String,
    onSeeAll: () -> Unit,
) {
    Row(
        modifier = Modifier
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
        TextButton(onClick = onSeeAll) {
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
