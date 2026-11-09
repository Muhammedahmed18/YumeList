package com.axiel7.moelist.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.LocalSnackbarHostState
import com.axiel7.moelist.ui.composables.SnackbarType
import com.axiel7.moelist.ui.composables.showAppSnackbar
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.MenuBook
import com.axiel7.moelist.ui.icons.rounded.Movie
import com.axiel7.moelist.ui.profile.composables.LibraryRing
import com.axiel7.moelist.ui.profile.composables.LibraryRingLegend
import com.axiel7.moelist.ui.profile.composables.RingSegment
import com.axiel7.moelist.ui.profile.composables.UserStatsView
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.DateUtils.parseDateAndLocalize
import com.axiel7.moelist.utils.MAL_PROFILE_URL
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

@Composable
fun ProfileView(
    navActionManager: NavActionManager
) {
    val viewModel: ProfileViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileViewContent(
        uiState = uiState,
        event = viewModel,
        navActionManager = navActionManager
    )
}

/** Height of the soft gradient behind the top of the screen. */
private val GradientHeight = 320.dp

/** Landscape / wide and short screens: identity on the left, stats on the right. */
private val TwoPaneMinWidth = 560.dp

/**
 * Profile, laid out to fit on one screen (no scrolling on a normal phone):
 *
 * - Identity: avatar in the library ring, name, and one row of chips (location, birthday,
 *   joined date)
 * - Stats: Anime / Manga toggle, the ring's legend (Watching / Completed / Plan to), and two
 *   compact rows of the other stats
 *
 * Wide and short screens (a phone in landscape) put the two side by side. If the content still
 * doesn't fit (very small screen, large font or display size), it scrolls rather than being cut
 * off. Pull to refresh works either way.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProfileViewContent(
    uiState: ProfileUiState,
    event: ProfileEvent?,
    navActionManager: NavActionManager
) {
    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHostState.current
    val scrollState = rememberScrollState()
    // Only matters in the scroll fallback: the bar gets a background once content is under it
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var selectedStatsTab by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            snackbarHostState.showAppSnackbar(uiState.message, type = SnackbarType.ERROR)
            event?.onMessageDisplayed()
        }
    }

    val openMalProfile: () -> Unit = {
        val name = uiState.user?.name
        if (name != null) {
            context.openLink(MAL_PROFILE_URL + name)
        }
    }

    val mediaType = if (selectedStatsTab == 0) MediaType.ANIME else MediaType.MANGA
    val ringSegments = ringSegmentsFor(uiState = uiState, mediaType = mediaType)
    val ringLoading = uiState.isRefreshing ||
        if (mediaType == MediaType.ANIME) uiState.isLoading else uiState.isLoadingManga
    val ringError = if (mediaType == MediaType.ANIME) uiState.isProfileError
    else uiState.isMangaError

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                // The name is always on screen below, so the bar has no title
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = navActionManager::goBack,
                        shapes = IconButtonDefaults.shapes(),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.title_home),
                        )
                    }
                },
                // Settings and Log out are in the Account sheet (avatar on Home)
                actions = {
                    IconButton(
                        onClick = openMalProfile,
                        enabled = uiState.user?.name != null,
                        shapes = IconButtonDefaults.shapes(),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_language_24),
                            contentDescription = stringResource(R.string.view_profile_mal),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = topAppBarScrollBehavior,
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Soft accent wash behind the top bar and the avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(GradientHeight)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            val pullToRefreshState = rememberPullToRefreshState()

            // Pull down to fetch the profile again; the ring replays its loading wave meanwhile
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { event?.refresh() },
                state = pullToRefreshState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding()),
                indicator = {
                    PullToRefreshDefaults.LoadingIndicator(
                        state = pullToRefreshState,
                        isRefreshing = uiState.isRefreshing,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val viewportHeight = maxHeight
                    val twoPane = maxWidth > maxHeight && maxWidth >= TwoPaneMinWidth
                    val avatarSize = when {
                        twoPane -> 88.dp
                        maxWidth >= 600.dp && maxHeight >= 800.dp -> 120.dp
                        else -> 92.dp
                    }
                    val horizontalPadding = if (maxWidth >= 600.dp) 32.dp else 20.dp

                    // Fills exactly one screen when the content fits (nothing to scroll), and
                    // scrolls only when it doesn't. The scroll container also lets pull to refresh
                    // work on a screen that doesn't move.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .heightIn(min = viewportHeight)
                            .padding(horizontal = horizontalPadding)
                            .padding(top = 4.dp, bottom = padding.calculateBottomPadding() + 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val identity: @Composable (Modifier) -> Unit = { modifier ->
                            ProfileIdentity(
                                uiState = uiState,
                                ringSegments = ringSegments,
                                ringLoading = ringLoading,
                                ringError = ringError,
                                avatarSize = avatarSize,
                                onAvatarClick = {
                                    uiState.profilePictureUrl?.let {
                                        navActionManager.toFullPoster(listOf(it))
                                    }
                                },
                                modifier = modifier,
                            )
                        }
                        val stats: @Composable (Modifier) -> Unit = { modifier ->
                            ProfileStats(
                                uiState = uiState,
                                mediaType = mediaType,
                                selectedStatsTab = selectedStatsTab,
                                onStatsTabSelected = { selectedStatsTab = it },
                                ringSegments = ringSegments,
                                ringError = ringError && !ringLoading,
                                onRefreshManga = { event?.refreshMangaStats() },
                                modifier = modifier,
                            )
                        }

                        if (twoPane) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                identity(Modifier.weight(0.42f))
                                stats(Modifier.weight(0.58f))
                            }
                        } else {
                            identity(Modifier.fillMaxWidth())
                            stats(
                                Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 600.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Avatar in the library ring, name, and the chips row. */
@Composable
private fun ProfileIdentity(
    uiState: ProfileUiState,
    ringSegments: List<RingSegment>,
    ringLoading: Boolean,
    ringError: Boolean,
    avatarSize: Dp,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LibraryRing(
            segments = ringSegments,
            isLoading = ringLoading,
            isError = ringError,
            avatarSize = avatarSize,
        ) {
            ProfileAvatar(
                url = uiState.profilePictureUrl,
                size = avatarSize,
                onAvatarClick = onAvatarClick,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = uiState.user?.name ?: stringResource(R.string.loading),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = (-0.5).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(8.dp))

        ProfileChips(uiState = uiState)
    }
}

/**
 * Location, birthday and joined date as small chips in one row (they wrap if needed).
 * Empty ones are skipped.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileChips(uiState: ProfileUiState) {
    val location = uiState.user?.location?.takeIf { it.isNotBlank() }
    val birthday = uiState.user?.birthday?.parseDateAndLocalize()?.takeIf { it.isNotBlank() }
    val joined = uiState.user?.joinedAt?.parseDateAndLocalize(
        inputFormat = DateTimeFormatter.ISO_DATE_TIME
    )?.takeIf { it.isNotBlank() }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        location?.let {
            ProfileChip(
                text = it,
                icon = R.drawable.ic_round_location_on_24,
                iconTint = MaterialTheme.colorScheme.primary,
            )
        }
        birthday?.let {
            ProfileChip(
                text = it,
                icon = R.drawable.ic_round_cake_24,
                iconTint = MaterialTheme.colorScheme.secondary,
            )
        }
        joined?.let {
            ProfileChip(
                text = it,
                icon = R.drawable.ic_round_access_time_24,
                iconTint = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun ProfileChip(
    text: String,
    icon: Int,
    iconTint: Color,
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = CircleShape,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = iconTint
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
            )
        }
    }
}

/** Anime / Manga toggle, the ring's legend, and the two rows of stats. */
@Composable
private fun ProfileStats(
    uiState: ProfileUiState,
    mediaType: MediaType,
    selectedStatsTab: Int,
    onStatsTabSelected: (Int) -> Unit,
    ringSegments: List<RingSegment>,
    ringError: Boolean,
    onRefreshManga: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StatsTypeButtonGroup(
            selectedIndex = selectedStatsTab,
            onSelected = onStatsTabSelected
        )

        Spacer(modifier = Modifier.height(4.dp))

        // What the ring's colours mean, with the counts
        LibraryRingLegend(
            segments = ringSegments,
            isError = ringError,
        )

        UserStatsView(
            uiState = uiState,
            mediaType = mediaType,
            showSectionTitle = false,
            compactStats = true,
            onRefreshManga = onRefreshManga
        )
    }
}

/**
 * The ring's three parts for [mediaType]: Watching / Completed / Plan to Watch for anime,
 * Reading / Completed / Plan to Read for manga. On Hold and Dropped are in the stats below.
 */
@Composable
private fun ringSegmentsFor(
    uiState: ProfileUiState,
    mediaType: MediaType,
): List<RingSegment> {
    val stats = if (mediaType == MediaType.ANIME) uiState.animeStats else uiState.mangaStats
    val statuses = if (mediaType == MediaType.ANIME) {
        listOf(ListStatus.WATCHING, ListStatus.COMPLETED, ListStatus.PLAN_TO_WATCH)
    } else {
        listOf(ListStatus.READING, ListStatus.COMPLETED, ListStatus.PLAN_TO_READ)
    }
    return statuses.map { status ->
        RingSegment(
            label = status.localized(),
            value = stats.find { it.type == status }?.value?.toInt() ?: 0,
            color = status.primaryColor(),
        )
    }
}

/**
 * Avatar in an M3 Expressive scalloped shape (12-sided cookie) with a surface-colored ring.
 * Tap opens the full picture.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProfileAvatar(
    url: String?,
    size: Dp,
    onAvatarClick: () -> Unit
) {
    val shape = MaterialShapes.Cookie12Sided.toShape()
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(4.dp, MaterialTheme.colorScheme.surface, shape)
            .clickable(enabled = !url.isNullOrEmpty(), onClick = onAvatarClick),
        contentAlignment = Alignment.Center
    ) {
        if (url.isNullOrEmpty()) {
            Image(
                painter = painterResource(R.drawable.ic_round_account_circle_24),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/** Anime / Manga as two connected toggle buttons (round outside, small inner corners). */
@Composable
private fun StatsTypeButtonGroup(
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    val options = listOf(
        stringResource(R.string.anime) to Icons.Rounded.Movie,
        stringResource(R.string.manga) to Icons.AutoMirrored.Rounded.MenuBook,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        options.forEachIndexed { index, (label, icon) ->
            ToggleButton(
                checked = selectedIndex == index,
                onCheckedChange = { onSelected(index) },
                shapes = if (index == 0) ButtonGroupDefaults.connectedLeadingButtonShapes()
                else ButtonGroupDefaults.connectedTrailingButtonShapes(),
                icon = { Icon(imageVector = icon, contentDescription = null) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Text(text = label, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview
@Composable
fun ProfilePreview() {
    MoeListTheme {
        Surface {
            ProfileViewContent(
                uiState = ProfileUiState(),
                event = null,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}
