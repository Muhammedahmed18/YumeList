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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.ListStatus
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
import com.axiel7.moelist.ui.icons.rounded.CalendarToday
import com.axiel7.moelist.ui.icons.rounded.MoreVert
import com.axiel7.moelist.ui.icons.rounded.Movie
import com.axiel7.moelist.ui.icons.rounded.Share
import com.axiel7.moelist.ui.profile.composables.ProfileStatusCards
import com.axiel7.moelist.ui.theme.Motion
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.TwoPaneMaxWidth
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.DateUtils.parseDateAndLocalize
import com.axiel7.moelist.utils.MAL_PROFILE_URL
import kotlinx.coroutines.launch
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

/** Landscape / wide and short screens: identity on the left, tabs and cards on the right. */
private val TwoPaneMinWidth = 560.dp

/** Widest the single-column content gets (tablets in portrait). */
private val ContentMaxWidth = 600.dp

/** How far the page has to scroll before the name moves into the top bar (large fonts only). */
private val TitleRevealOffset = 120.dp

/** Above this font scale the layout uses its smallest sizes and is allowed to scroll. */
private const val LargeFontScale = 1.3f

/**
 * Sizes of the screen's blocks, worked out from the space the screen actually has. Small phones
 * get the minimums (everything fits without scrolling); tall phones get bigger cards and a
 * bigger avatar, and whatever is left over becomes even spacing between the blocks.
 */
private data class ProfileSizes(
    val avatar: Dp,
    val largeCard: Dp,
    val smallCard: Dp,
)

private fun profileSizes(height: Dp, twoPane: Boolean, largeFont: Boolean): ProfileSizes = when {
    largeFont -> ProfileSizes(avatar = 64.dp, largeCard = 112.dp, smallCard = 76.dp)
    twoPane -> ProfileSizes(
        avatar = (height * 0.28f).coerceIn(64.dp, 112.dp),
        largeCard = (height * 0.32f).coerceIn(104.dp, 180.dp),
        smallCard = (height * 0.2f).coerceIn(72.dp, 112.dp),
    )
    else -> ProfileSizes(
        avatar = (height * 0.15f).coerceIn(64.dp, 120.dp),
        largeCard = (height * 0.22f).coerceIn(112.dp, 200.dp),
        smallCard = (height * 0.13f).coerceIn(76.dp, 116.dp),
    )
}

/**
 * Profile, on one screen:
 *
 * - Identity: avatar, name, and a row with location, birthday and joined date
 * - Anime / Manga tabs (tap or swipe the cards), each showing its total
 * - Status cards: Watching and Plan to watch large, Completed / On hold / Dropped smaller.
 *   Each card opens that list.
 *
 * The block sizes follow the screen height (see [profileSizes]), so it fills a tall phone and
 * still fits a small one without scrolling. With a very large system font it scrolls instead of
 * cutting text off. Pull to refresh works everywhere.
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
    val pagerState = rememberPagerState(pageCount = { 2 })
    var showOverflowMenu by remember { mutableStateOf(false) }
    val revealPx = with(LocalDensity.current) { TitleRevealOffset.toPx() }
    val showTitle by remember { derivedStateOf { scrollState.value > revealPx } }
    val fontScale = LocalDensity.current.fontScale

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            snackbarHostState.showAppSnackbar(uiState.message, type = SnackbarType.ERROR)
            event?.onMessageDisplayed()
        }
    }

    val userName = uiState.user?.name
    val profileUrl = userName?.let { MAL_PROFILE_URL + it }
    val animeCounts = remember(uiState.animeStats) {
        uiState.animeStats.associate { it.type to it.value.toInt() }
    }
    val mangaCounts = remember(uiState.mangaStats) {
        uiState.mangaStats.associate { it.type to it.value.toInt() }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                // The name is on screen below; it only moves up here if the page has scrolled
                // (which only happens with a very large font)
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
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = topAppBarScrollBehavior,
            )
        }
    ) { padding ->
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
                val bottomPadding = padding.calculateBottomPadding()
                val viewportHeight = maxHeight
                val twoPane = maxWidth > maxHeight && maxWidth >= TwoPaneMinWidth
                val sizes = profileSizes(
                    // The cards share the height left after the vertical padding
                    height = viewportHeight - bottomPadding,
                    twoPane = twoPane,
                    largeFont = fontScale > LargeFontScale,
                )
                val horizontalPadding = if (maxWidth >= 600.dp) 32.dp else 16.dp

                val identity: @Composable (Modifier) -> Unit = { modifier ->
                    ProfileIdentity(
                        uiState = uiState,
                        avatarSize = sizes.avatar,
                        onAvatarClick = {
                            uiState.profilePictureUrl?.let {
                                navActionManager.toFullPoster(listOf(it))
                            }
                        },
                        modifier = modifier,
                    )
                }
                val stats: @Composable (Modifier) -> Unit = { modifier ->
                    ProfileStatsPager(
                        pagerState = pagerState,
                        animeCounts = animeCounts,
                        mangaCounts = mangaCounts,
                        uiState = uiState,
                        largeCardHeight = sizes.largeCard,
                        smallCardHeight = sizes.smallCard,
                        onRetry = { mediaType ->
                            if (mediaType == MediaType.ANIME) event?.refresh()
                            else event?.refreshMangaStats()
                        },
                        onStatusClick = { mediaType, status ->
                            navActionManager.toUserList(mediaType, status)
                        },
                        modifier = modifier,
                    )
                }

                // At least the screen's height: the spacers share whatever is left over, and the
                // whole area stays draggable for pull to refresh. Only scrolls if it can't fit.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .heightIn(min = viewportHeight)
                        .padding(horizontal = horizontalPadding)
                        .padding(top = 8.dp, bottom = bottomPadding + 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (twoPane) {
                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                            // Capped on very large windows so the two panes don't drift apart
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = TwoPaneMaxWidth),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            identity(Modifier.weight(0.4f))
                            stats(Modifier.weight(0.6f))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        Spacer(modifier = Modifier.weight(0.5f))
                        identity(
                            Modifier
                                .fillMaxWidth()
                                .widthIn(max = ContentMaxWidth)
                        )
                        // 24dp minimum between identity and the tabs, more on tall screens
                        Spacer(modifier = Modifier.height(24.dp))
                        Spacer(modifier = Modifier.weight(1f))
                        stats(
                            Modifier
                                .fillMaxWidth()
                                .widthIn(max = ContentMaxWidth)
                        )
                        Spacer(modifier = Modifier.weight(0.5f))
                    }
                }
            }
        }
    }
}

/** Avatar, name, and the location / birthday / joined row. */
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
        val isLoadingUser = uiState.user == null && uiState.isLoading
        if (isLoadingUser) {
            Box(
                modifier = Modifier
                    .size(width = 160.dp, height = 28.dp)
                    .defaultPlaceholder(visible = true)
            )
        } else {
            Text(
                text = name ?: "",
                style = MaterialTheme.typography.headlineSmallEmphasized,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        ProfileInfoRow(uiState = uiState, isLoading = isLoadingUser)
    }
}

/** One value of the info row. */
private data class ProfileInfo(
    val icon: Painter,
    val value: String,
    val label: String,
)

/**
 * Location, birthday and joined date as three equal columns (icon, value, label). Empty ones are
 * left out and the rest re-centre.
 */
@Composable
private fun ProfileInfoRow(
    uiState: ProfileUiState,
    isLoading: Boolean,
) {
    val items = buildList {
        uiState.user?.location?.takeIf { it.isNotBlank() }?.let {
            add(
                ProfileInfo(
                    icon = painterResource(R.drawable.ic_round_location_on_24),
                    value = it,
                    label = stringResource(R.string.profile_location),
                )
            )
        }
        uiState.user?.birthday?.parseDateAndLocalize()?.takeIf { it.isNotBlank() }?.let {
            add(
                ProfileInfo(
                    icon = painterResource(R.drawable.ic_round_cake_24),
                    value = it,
                    label = stringResource(R.string.profile_birthday),
                )
            )
        }
        uiState.user?.joinedAt?.parseDateAndLocalize(
            inputFormat = DateTimeFormatter.ISO_DATE_TIME
        )?.takeIf { it.isNotBlank() }?.let {
            add(
                ProfileInfo(
                    icon = rememberVectorPainter(Icons.Rounded.CalendarToday),
                    value = it,
                    label = stringResource(R.string.profile_joined),
                )
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        if (isLoading) {
            repeat(3) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .defaultPlaceholder(visible = true)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 72.dp, height = 16.dp)
                            .defaultPlaceholder(visible = true)
                    )
                }
            }
        } else {
            items.forEach { info ->
                ProfileInfoItem(info = info, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ProfileInfoItem(
    info: ProfileInfo,
    modifier: Modifier = Modifier,
) {
    Column(
        // One TalkBack stop per item: "Lahore, Location"
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            painter = info.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = 2.dp)
                .size(20.dp),
        )
        Text(
            text = info.value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = info.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
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
            .clickable(
                enabled = !url.isNullOrEmpty(),
                onClickLabel = stringResource(R.string.view_profile_picture),
                role = Role.Image,
                onClick = onAvatarClick
            ),
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

/**
 * Anime / Manga tabs over the status cards. The cards are a pager: tap a tab or swipe the cards
 * to switch.
 */
@Composable
private fun ProfileStatsPager(
    pagerState: PagerState,
    animeCounts: Map<ListStatus, Int>,
    mangaCounts: Map<ListStatus, Int>,
    uiState: ProfileUiState,
    largeCardHeight: Dp,
    smallCardHeight: Dp,
    onRetry: (MediaType) -> Unit,
    onStatusClick: (MediaType, ListStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val animeTotal = if (uiState.isLoading || uiState.isProfileError) null
    else animeCounts.values.sum()
    val mangaTotal = if (uiState.isLoadingManga || uiState.isMangaError) null
    else mangaCounts.values.sum()

    Column(modifier = modifier) {
        StatsTypeButtonGroup(
            selectedIndex = pagerState.targetPage,
            totals = listOf(animeTotal, mangaTotal),
            onSelected = { index -> scope.launch { pagerState.animateScrollToPage(index) } }
        )
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val isAnime = page == 0
            val mediaType = if (isAnime) MediaType.ANIME else MediaType.MANGA
            ProfileStatusCards(
                mediaType = mediaType,
                counts = if (isAnime) animeCounts else mangaCounts,
                isLoading = if (isAnime) uiState.isLoading else uiState.isLoadingManga,
                isError = if (isAnime) uiState.isProfileError else uiState.isMangaError,
                largeCardHeight = largeCardHeight,
                smallCardHeight = smallCardHeight,
                onRetry = { onRetry(mediaType) },
                onStatusClick = { status -> onStatusClick(mediaType, status) },
            )
        }
    }
}

/** Anime / Manga as two connected toggle buttons, each with its total once loaded. */
@Composable
private fun StatsTypeButtonGroup(
    selectedIndex: Int,
    totals: List<Int?>,
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
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = label, fontWeight = FontWeight.Bold, maxLines = 1)
                totals[index]?.let { total ->
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = total.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = LocalContentColor.current.copy(alpha = 0.7f),
                        maxLines = 1,
                    )
                }
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
