package com.axiel7.moelist.ui.profile

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.platform.LocalDensity
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
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.BackIconButton
import com.axiel7.moelist.ui.composables.LocalSnackbarHostState
import com.axiel7.moelist.ui.composables.SnackbarType
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.composables.showAppSnackbar
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.MenuBook
import com.axiel7.moelist.ui.icons.automirrored.rounded.OpenInNew
import com.axiel7.moelist.ui.icons.rounded.MoreVert
import com.axiel7.moelist.ui.icons.rounded.Movie
import com.axiel7.moelist.ui.icons.rounded.Share
import com.axiel7.moelist.ui.profile.composables.ProfileActivitySection
import com.axiel7.moelist.ui.profile.composables.ProfileLibrarySection
import com.axiel7.moelist.ui.theme.Motion
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.TwoPaneMaxWidth
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.DateUtils.parseDateAndLocalize
import com.axiel7.moelist.utils.MAL_PROFILE_URL
import com.axiel7.moelist.utils.NumExtensions.toStringOrZero
import com.axiel7.moelist.utils.UNKNOWN_CHAR
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

/** How far the page scrolls before the name appears in the top bar. */
private val TitleRevealOffset = 160.dp

/**
 * Profile:
 *
 * - Identity: avatar, name, and one quiet row of chips (location, birthday, joined date)
 * - Anime / Manga switch, right under the identity
 * - Library: one stacked bar of all five list statuses and a tappable legend (each status opens
 *   that list)
 * - Activity: days, episodes / chapters, mean score and rewatches as connected tiles
 *
 * Content starts at the top, so switching Anime / Manga never shifts the page. Wide and short
 * screens (a phone in landscape) put identity and stats side by side. Pull to refresh works
 * everywhere; the name appears in the top bar once the page has scrolled.
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
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var selectedStatsTab by rememberSaveable { mutableIntStateOf(0) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    val revealPx = with(LocalDensity.current) { TitleRevealOffset.toPx() }
    val showTitle by remember { derivedStateOf { scrollState.value > revealPx } }

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            snackbarHostState.showAppSnackbar(uiState.message, type = SnackbarType.ERROR)
            event?.onMessageDisplayed()
        }
    }

    val userName = uiState.user?.name
    val profileUrl = userName?.let { MAL_PROFILE_URL + it }

    val mediaType = if (selectedStatsTab == 0) MediaType.ANIME else MediaType.MANGA
    val isAnime = mediaType == MediaType.ANIME
    val statsLoading = if (isAnime) uiState.isLoading else uiState.isLoadingManga
    val statsError = if (isAnime) uiState.isProfileError else uiState.isMangaError
    val counts = (if (isAnime) uiState.animeStats else uiState.mangaStats)
        .associate { it.type to it.value.toInt() }

    // Activity values (same sources as before). Manga "days read" only comes from Jikan, so it's
    // unknown ("─") rather than a fake 0 when missing.
    val animeStatistics = uiState.user?.animeStatistics
    val mangaStatistics = uiState.userMangaStats
    val days = when {
        statsError -> UNKNOWN_CHAR
        isAnime -> animeStatistics?.numDays.toStringOrZero()
        mangaStatistics?.days == null -> UNKNOWN_CHAR
        else -> mangaStatistics?.days.toStringOrZero()
    }
    val progress = when {
        statsError -> UNKNOWN_CHAR
        isAnime -> animeStatistics?.numEpisodes?.toString() ?: "0"
        else -> mangaStatistics?.chaptersRead?.toString() ?: "0"
    }
    val meanScore = when {
        statsError -> UNKNOWN_CHAR
        isAnime -> animeStatistics?.meanScore.toStringOrZero()
        else -> mangaStatistics?.meanScore.toStringOrZero()
    }
    val repeats = when {
        statsError -> UNKNOWN_CHAR
        isAnime -> animeStatistics?.numTimesRewatched.toStringOrZero()
        else -> mangaStatistics?.repeat.toStringOrZero()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                // The name is on screen below; it only moves up here once that has scrolled away
                title = {
                    AnimatedVisibility(
                        visible = showTitle && userName != null,
                        enter = fadeIn(Motion.fastEffects()),
                        exit = fadeOut(Motion.fastEffects()),
                    ) {
                        Text(
                            text = userName.orEmpty(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = { BackIconButton(onClick = navActionManager::goBack) },
                // Settings and Log out are in the Account sheet (avatar on Home)
                actions = {
                    Box {
                        IconButton(
                            onClick = { showOverflowMenu = true },
                            enabled = profileUrl != null,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = stringResource(R.string.more_options)
                            )
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.view_profile_mal)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    profileUrl?.let { context.openLink(it) }
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.share)) },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Rounded.Share, contentDescription = null)
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    profileUrl?.let { context.shareText(it) }
                                },
                            )
                        }
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
                        else -> 104.dp
                    }
                    val horizontalPadding = if (maxWidth >= 600.dp) 32.dp else 16.dp

                    // Top-aligned. The minimum height keeps the whole screen draggable, so pull
                    // to refresh works even when nothing needs to scroll.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .heightIn(min = viewportHeight)
                            .padding(horizontal = horizontalPadding)
                            .padding(top = 8.dp, bottom = padding.calculateBottomPadding() + 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val identity: @Composable (Modifier) -> Unit = { modifier ->
                            ProfileIdentity(
                                uiState = uiState,
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
                            Column(
                                modifier = modifier,
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                            ) {
                                StatsTypeButtonGroup(
                                    selectedIndex = selectedStatsTab,
                                    onSelected = { selectedStatsTab = it }
                                )
                                ProfileLibrarySection(
                                    mediaType = mediaType,
                                    counts = counts,
                                    isLoading = statsLoading,
                                    isError = statsError,
                                    onRetry = {
                                        if (isAnime) event?.refresh() else event?.refreshMangaStats()
                                    },
                                    onStatusClick = { status ->
                                        navActionManager.toUserList(mediaType, status)
                                    },
                                )
                                ProfileActivitySection(
                                    mediaType = mediaType,
                                    days = days,
                                    progress = progress,
                                    meanScore = meanScore,
                                    repeats = repeats,
                                    isLoading = statsLoading,
                                )
                            }
                        }

                        if (twoPane) {
                            Row(
                                // Capped on very large windows so the two panes don't drift apart
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = TwoPaneMaxWidth),
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                identity(Modifier.weight(0.4f))
                                stats(Modifier.weight(0.6f))
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

/** Avatar, name, and the chips row. */
@Composable
private fun ProfileIdentity(
    uiState: ProfileUiState,
    avatarSize: Dp,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileAvatar(
            url = uiState.profilePictureUrl,
            size = avatarSize,
            onAvatarClick = onAvatarClick,
        )

        Spacer(modifier = Modifier.height(12.dp))

        val name = uiState.user?.name
        if (name == null && uiState.isLoading) {
            // Placeholder shape while loading, like the rest of the app
            Box(
                modifier = Modifier
                    .size(width = 160.dp, height = 28.dp)
                    .defaultPlaceholder(visible = true)
            )
        } else {
            Text(
                text = name ?: UNKNOWN_CHAR,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.5).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        ProfileChips(uiState = uiState)
    }
}

/**
 * Location, birthday and joined date as small, quiet chips in one row (they wrap if needed).
 * One neutral tint for all three, so they don't compete with the stats. Empty ones are skipped.
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
        location?.let { ProfileChip(text = it, icon = R.drawable.ic_round_location_on_24) }
        birthday?.let { ProfileChip(text = it, icon = R.drawable.ic_round_cake_24) }
        joined?.let { ProfileChip(text = it, icon = R.drawable.ic_round_access_time_24) }
    }
}

@Composable
private fun ProfileChip(
    text: String,
    icon: Int,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
    }
}

/**
 * Avatar in an M3 Expressive scalloped shape (12-sided cookie). Tap opens the full picture.
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

/** Fires a system share sheet for the given text. */
private fun Context.shareText(text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, null))
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
