package com.axiel7.moelist.ui.details

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.RelationType
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.InfoTitle
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_BIG_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_BIG_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.composables.LocalSnackbarHostState
import com.axiel7.moelist.ui.details.composables.MediaDetailsTopAppBar
import com.axiel7.moelist.ui.details.composables.MediaInfoView
import com.axiel7.moelist.ui.details.composables.MusicStreamingSheet
import com.axiel7.moelist.ui.editmedia.EditMediaSheet
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.CHARACTER_URL
import com.axiel7.moelist.utils.ContextExtensions.copyToClipBoard
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.DateUtils.parseDateAndLocalize
import com.axiel7.moelist.utils.NumExtensions.format
import com.axiel7.moelist.utils.StringExtensions.toStringOrNull
import com.axiel7.moelist.utils.UNKNOWN_CHAR
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun MediaDetailsView(
    isLoggedIn: Boolean,
    navActionManager: NavActionManager
) {
    val viewModel: MediaDetailsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MediaDetailsContent(
        uiState = uiState,
        event = viewModel,
        isLoggedIn = isLoggedIn,
        navActionManager = navActionManager,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun MediaDetailsContent(
    uiState: MediaDetailsUiState,
    event: MediaDetailsEvent?,
    isLoggedIn: Boolean,
    navActionManager: NavActionManager
) {
    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHostState.current

    val scrollState = rememberScrollState()
    val topAppBarScrollBehavior =
        TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val density = LocalDensity.current
    val titleThresholdPx = remember(density) {
        with(density) { (MEDIA_POSTER_BIG_HEIGHT / 2).dp.toPx() }
    }
    val showTitle by remember { derivedStateOf { scrollState.value > titleThresholdPx } }
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    fun hideSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
    }

    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var isSynopsisExpanded by remember { mutableStateOf(false) }
    val maxLinesSynopsis by remember {
        derivedStateOf { if (isSynopsisExpanded) Int.MAX_VALUE else 6 }
    }

    // Theme sheets
    var showThemesSheet by remember { mutableStateOf(false) }
    var themesSheetTitle by remember { mutableStateOf("") }
    var themesSheetItems by remember { mutableStateOf<List<String>>(emptyList()) }
    val themesSheetState = rememberModalBottomSheetState()

    // Music streaming (per-song)
    var showMusicSheet by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<String?>(null) }

    // Pre-calculate strings to avoid Context.getString lint in Composable
    val pleaseLoginMessage = stringResource(R.string.please_login_to_use_this_feature)
    val openingLabel = stringResource(R.string.opening)
    val endingLabel = stringResource(R.string.ending)

    if (showSheet && uiState.mediaInfo != null) {
        EditMediaSheet(
            sheetState = sheetState,
            mediaInfo = uiState.mediaInfo!!,
            myListStatus = uiState.myListStatus,
            bottomPadding = bottomBarPadding,
            onEdited = { status, removed ->
                hideSheet()
                event?.onChangedMyListStatus(status, removed)
            },
            onDismissed = { hideSheet() }
        )
    }

    if (showThemesSheet) {
        ThemesSheet(
            sheetState = themesSheetState,
            title = themesSheetTitle,
            themes = themesSheetItems,
            bottomPadding = bottomBarPadding,
            onThemeClick = { song ->
                selectedSong = song
                showThemesSheet = false
                showMusicSheet = true
            },
            onDismiss = { showThemesSheet = false }
        )
    }

    if (showMusicSheet && selectedSong != null) {
        MusicStreamingSheet(
            songTitle = selectedSong.orEmpty(),
            bottomPadding = bottomBarPadding,
            onDismiss = {
                showMusicSheet = false
                selectedSong = null
            }
        )
    }

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            snackbarHostState.showSnackbar(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            MediaDetailsTopAppBar(
                uiState = uiState,
                event = event,
                showTitle = showTitle,
                navigateBack = dropUnlessResumed { navActionManager.goBack() },
                scrollBehavior = topAppBarScrollBehavior,
                onOpenClick = {
                    uiState.mediaDetails?.let { details ->
                        val type = if (uiState.isAnime) "anime" else "manga"
                        context.openLink("https://myanimelist.net/$type/${details.id}")
                    }
                },
                onShareClick = {
                    uiState.mediaDetails?.let { details ->
                        val type = if (uiState.isAnime) "anime" else "manga"
                        context.shareText("https://myanimelist.net/$type/${details.id}")
                    }
                },
            )
        },
        floatingActionButton = {
            if (uiState.mediaDetails != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (isLoggedIn) {
                            showSheet = true
                        } else {
                            scope.launch { snackbarHostState.showSnackbar(pleaseLoginMessage) }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (uiState.isNewEntry) Icons.Rounded.Add else Icons.Rounded.Edit,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = if (uiState.isNewEntry) stringResource(R.string.add)
                            else uiState.mediaDetails.myListStatus?.status?.localized() ?: stringResource(R.string.edit)
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    expanded = !scrollState.isScrollInProgress
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(bottom = bottomBarPadding + 80.dp) // Room for FAB
        ) {
            // ---------- Hero Section ----------
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MediaPoster(
                    url = uiState.mediaDetails?.mainPicture?.large,
                    modifier = Modifier
                        .size(
                            width = MEDIA_POSTER_BIG_WIDTH.dp,
                            height = MEDIA_POSTER_BIG_HEIGHT.dp
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .defaultPlaceholder(visible = uiState.isLoading)
                        .clickable(onClick = dropUnlessResumed {
                            if (uiState.picturesUrls.isNotEmpty())
                                navActionManager.toFullPoster(uiState.picturesUrls)
                        })
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(MEDIA_POSTER_BIG_HEIGHT.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = uiState.mediaDetails?.userPreferredTitle() ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                    )
                    val altTitle = if (App.titleLanguage == TitleLanguage.ENGLISH) {
                        uiState.mediaDetails?.title
                    } else {
                        uiState.mediaDetails?.alternativeTitles?.en
                    }
                    if (!altTitle.isNullOrBlank()) {
                        Text(
                            text = altTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    uiState.mediaDetails?.status?.let { status ->
                        val statusColor = status.statusColor()
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = statusColor.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 12.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.status),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = statusColor,
                                        modifier = Modifier.size(8.dp)
                                    ) {}
                                    Text(
                                        text = status.localized(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                                if (status == MediaStatus.AIRING) {
                                    val airingText = (uiState.mediaDetails as? AnimeDetails)
                                        ?.broadcast?.airingInWithDayAndTime()
                                    if (!airingText.isNullOrEmpty()) {
                                        Text(
                                            text = airingText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ---------- Stat Strip ----------
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .defaultPlaceholder(visible = uiState.isLoading)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricItem(
                        value = uiState.mediaDetails?.mean.toStringOrNull() ?: "??",
                        label = stringResource(R.string.score),
                        icon = Icons.Rounded.Star,
                        iconTint = Color(0xFFFFC107)
                    )
                    VerticalDivider(
                        modifier = Modifier.height(40.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    MetricItem(
                        value = uiState.mediaDetails?.mediaFormatWithYear() ?: "...",
                        label = stringResource(R.string.format),
                        icon = Icons.Rounded.BarChart
                    )
                    VerticalDivider(
                        modifier = Modifier.height(40.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    MetricItem(
                        value = when (val d = uiState.mediaDetails) {
                            is AnimeDetails -> d.numEpisodes.countOrDash()
                            is MangaDetails -> d.numChapters.countOrDash()
                            else -> "..."
                        },
                        label = if (uiState.isAnime) "Episodes" else "Chapters",
                        icon = if (uiState.isAnime) Icons.Rounded.PlayArrow
                               else Icons.AutoMirrored.Rounded.MenuBook
                    )
                }
            }

            // ---------- Genres ----------
            if (!uiState.mediaDetails?.genres.isNullOrEmpty()) {
                InfoTitle(text = stringResource(R.string.genres))
                LazyRow(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.mediaDetails?.genres.orEmpty()) { genre ->
                        AssistChip(
                            onClick = { },
                            label = { Text(text = genre.localized()) },
                            shape = CircleShape,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            border = null
                        )
                    }
                }
            }

            // ---------- Synopsis ----------
            val synopsisAndBackground = uiState.mediaDetails?.synopsisAndBackground()
            if (uiState.isLoading || !synopsisAndBackground.isNullOrEmpty()) {
                InfoTitle(text = stringResource(R.string.synopsis))
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = synopsisAndBackground
                                ?: AnnotatedString(stringResource(R.string.lorem_ipsun)),
                            modifier = Modifier
                                .animateContentSize(animationSpec = spring())
                                .defaultPlaceholder(visible = uiState.isLoading)
                                .combinedClickable(
                                    onLongClick = {
                                        uiState.mediaDetails?.synopsis?.let {
                                            context.copyToClipBoard(it)
                                        }
                                    },
                                    onClick = { isSynopsisExpanded = !isSynopsisExpanded }
                                ),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 26.sp,
                                letterSpacing = 0.2.sp
                            ),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = maxLinesSynopsis
                        )
                        val gradientAlpha by animateFloatAsState(
                            targetValue = if (isSynopsisExpanded) 0f else 1f,
                            label = "synopsisGradient"
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .graphicsLayer { alpha = gradientAlpha }
                                .background(
                                    Brush.verticalGradient(
                                        0.0f to Color.Transparent,
                                        0.5f to Color.Transparent,
                                        1.0f to MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                )
                        )
                    }
                    val arrowRotation by animateFloatAsState(
                        targetValue = if (isSynopsisExpanded) 180f else 0f,
                        label = "synopsisArrow"
                    )
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = if (isSynopsisExpanded) stringResource(R.string.show_less)
                        else stringResource(R.string.show_more),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 4.dp)
                            .graphicsLayer { rotationZ = arrowRotation }
                            .clickable { isSynopsisExpanded = !isSynopsisExpanded }
                    )
                }
            }

            // ---------- More Info ----------
            InfoTitle(text = stringResource(R.string.more_info))
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (uiState.isAnime) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MediaInfoView(
                                    title = stringResource(R.string.studios),
                                    info = uiState.studiosJoined,
                                    iconVector = Icons.Rounded.Movie,
                                    modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                                )
                                MediaInfoView(
                                    title = stringResource(R.string.source),
                                    info = (uiState.mediaDetails as? AnimeDetails)?.source?.localized(),
                                    iconVector = Icons.Rounded.History,
                                    modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MediaInfoView(
                                    title = stringResource(R.string.start_date),
                                    info = uiState.mediaDetails?.startDate?.parseDateAndLocalize(),
                                    iconVector = Icons.Rounded.Add,
                                    modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                                )
                                MediaInfoView(
                                    title = stringResource(R.string.end_date),
                                    info = uiState.mediaDetails?.endDate?.parseDateAndLocalize(),
                                    iconVector = Icons.Rounded.Event,
                                    modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MediaInfoView(
                                    title = stringResource(R.string.season),
                                    info = (uiState.mediaDetails as? AnimeDetails)?.startSeason?.seasonYearText(),
                                    iconVector = Icons.Rounded.RssFeed,
                                    modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                                )
                                MediaInfoView(
                                    title = stringResource(R.string.broadcast),
                                    info = (uiState.mediaDetails as? AnimeDetails)?.broadcast?.timeText(
                                        isAiring = false
                                    ),
                                    iconVector = Icons.Rounded.LiveTv,
                                    modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MediaInfoView(
                                    title = stringResource(R.string.authors),
                                    info = (uiState.mediaDetails as? MangaDetails)?.authors
                                        ?.joinToString { "${it.node.firstName} ${it.node.lastName}" },
                                    iconVector = Icons.Rounded.Movie,
                                    modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                                )
                                MediaInfoView(
                                    title = stringResource(R.string.serialization),
                                    info = uiState.serializationJoined,
                                    iconVector = Icons.Rounded.Bookmark,
                                    modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MediaInfoView(
                                    title = stringResource(R.string.start_date),
                                    info = uiState.mediaDetails?.startDate?.parseDateAndLocalize(),
                                    iconVector = Icons.Rounded.Add,
                                    modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                                )
                                MediaInfoView(
                                    title = stringResource(R.string.end_date),
                                    info = uiState.mediaDetails?.endDate?.parseDateAndLocalize(),
                                    iconVector = Icons.Rounded.Event,
                                    modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MediaInfoView(
                                    title = stringResource(R.string.status),
                                    info = uiState.mediaDetails?.status?.localized(),
                                    iconVector = Icons.Rounded.Info,
                                    modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                                )
                                MediaInfoView(
                                    title = stringResource(R.string.volumes),
                                    info = (uiState.mediaDetails as? MangaDetails)?.numVolumes.countOrDash(),
                                    iconVector = Icons.AutoMirrored.Rounded.MenuBook,
                                    modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                                )
                            }
                        }
                    }
                }

            }

            // ---------- Related Section ----------
            RelatedSection(
                uiState = uiState,
                navActionManager = navActionManager
            )

            // ---------- Characters ----------
            if (uiState.isAnime) {
                InfoTitle(text = stringResource(R.string.characters))
                if (uiState.characters.isNotEmpty() || uiState.isLoadingCharacters) {
                    LazyRow(
                        modifier = Modifier.padding(top = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.characters,
                            contentType = { it }
                        ) { item ->
                            MediaItemVertical(
                                imageUrl = item.node.mainPicture?.medium,
                                title = item.fullName(),
                                subtitle = {
                                    CharacterRolePill(
                                        role = item.role?.localized().orEmpty(),
                                        isMain = item.role?.name.equals("MAIN", ignoreCase = true)
                                    )
                                },
                                minLines = 2,
                                onClick = {
                                    context.openLink(CHARACTER_URL + item.node.id)
                                }
                            )
                        }
                        if (uiState.isLoadingCharacters && uiState.characters.isEmpty()) {
                            items(4) {
                                CharacterShimmerItem()
                            }
                        }
                    }
                } else {
                    FilledTonalButton(
                        onClick = { event?.getCharacters() },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(text = stringResource(R.string.view_characters))
                    }
                }
            }

            // ---------- Themes ----------
            if (uiState.mediaDetails is AnimeDetails) {
                val openings = uiState.mediaDetails.openingThemes?.map { it.text }.orEmpty()
                val endings = uiState.mediaDetails.endingThemes?.map { it.text }.orEmpty()

                if (openings.isNotEmpty() || endings.isNotEmpty()) {
                    InfoTitle(text = stringResource(R.string.music_themes))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (openings.isNotEmpty()) {
                            Button(
                                onClick = {
                                    themesSheetTitle = openingLabel
                                    themesSheetItems = openings
                                    showThemesSheet = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "$openingLabel (${openings.size})",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                        if (endings.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    themesSheetTitle = endingLabel
                                    themesSheetItems = endings
                                    showThemesSheet = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "$endingLabel (${endings.size})",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ---------- Stats Section ----------
            InfoTitle(text = stringResource(R.string.stats))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultPlaceholder(visible = uiState.isLoading),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetricItem(
                            value = uiState.mediaDetails?.rankText() ?: UNKNOWN_CHAR,
                            label = stringResource(R.string.top_ranked),
                            icon = Icons.Rounded.BarChart
                        )
                        VerticalDivider(modifier = Modifier.height(32.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        MetricItem(
                            value = uiState.mediaDetails?.popularity?.let { "# $it" } ?: UNKNOWN_CHAR,
                            label = stringResource(R.string.popularity),
                            icon = Icons.AutoMirrored.Rounded.TrendingUp
                        )
                        VerticalDivider(modifier = Modifier.height(32.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        MetricItem(
                            value = uiState.mediaDetails?.numListUsers?.format() ?: UNKNOWN_CHAR,
                            label = stringResource(R.string.members),
                            icon = Icons.Rounded.Group
                        )
                    }

                    if (uiState.isLoadingStatusDistribution || uiState.statusDistribution != null) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = stringResource(R.string.status_distribution),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        if (uiState.isLoadingStatusDistribution) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                repeat(5) { StatusDistributionShimmerRow() }
                            }
                        } else {
                            val stats = uiState.statusDistribution!!
                            val total = remember(stats) { stats.sumOf { it.value.toDouble() } }
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                stats.forEach { stat ->
                                    StatusBarRow(
                                        color = stat.type.primaryColor(),
                                        label = stat.type.localized(),
                                        value = stat.value.toDouble(),
                                        total = total
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }//:Column
    }//:Scaffold
}

@Composable
private fun StatusDistributionShimmerRow() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .defaultPlaceholder(visible = true)
            )
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .defaultPlaceholder(visible = true)
            )
            Spacer(
                modifier = Modifier
                    .width(52.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .defaultPlaceholder(visible = true)
            )
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .defaultPlaceholder(visible = true)
        )
    }
}

/**
 * A single status-distribution row: colored dot + label, a progress bar, and a
 * trailing percentage with raw count.
 */
@Composable
private fun StatusBarRow(
    color: Color,
    label: String,
    value: Double,
    total: Double,
) {
    val fraction = if (total > 0) (value / total).toFloat() else 0f
    val percentString = (fraction * 100).format() ?: "0"
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = color,
                modifier = Modifier.size(8.dp)
            ) {}
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            )
            Text(
                text = "$percentString%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = " (${value.toInt().format() ?: value.toInt().toString()})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            gapSize = (-1).dp,
            drawStopIndicator = {}
        )
    }
}

@Composable
private fun MetricItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CharacterRolePill(
    role: String,
    isMain: Boolean,
) {
    if (role.isEmpty()) return
    val container = if (isMain) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceContainerHighest
    val content = if (isMain) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        shape = CircleShape,
        color = container,
        tonalElevation = 2.dp,
    ) {
        Text(
            text = role,
            color = content,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun CharacterShimmerItem(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(width = 100.dp, height = 140.dp)
    ) {
        // Poster placeholder
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .defaultPlaceholder(visible = true)
        )
        // Role pill placeholder, overlaid where the real CharacterRolePill sits
        Spacer(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
                .size(width = 52.dp, height = 18.dp)
                .clip(CircleShape)
                .defaultPlaceholder(visible = true)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemesSheet(
    sheetState: androidx.compose.material3.SheetState,
    title: String,
    themes: List<String>,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onThemeClick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.padding(bottom = bottomPadding + 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            )
            LazyColumn {
                items(themes) { song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeClick(song) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = song,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        )
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Normalized UI model so the RelatedAnime and RelatedManga types (which do not share a
 * common supertype here) can be displayed and filtered through one code path.
 */
@androidx.compose.runtime.Immutable
private data class RelatedRow(
    val id: Int,
    val imageUrl: String?,
    val title: String,
    val relation: String,
    val relationType: RelationType,
    val mediaType: MediaType,
)

@Composable
private fun RelatedSection(
    uiState: MediaDetailsUiState,
    navActionManager: NavActionManager,
) {
    val all = uiState.relatedAnime.map {
        RelatedRow(
            id = it.node.id,
            imageUrl = it.node.mainPicture?.large,
            title = it.node.userPreferredTitle(),
            relation = it.relationType.localized(),
            relationType = it.relationType,
            mediaType = MediaType.ANIME,
        )
    } + uiState.relatedManga.map {
        RelatedRow(
            id = it.node.id,
            imageUrl = it.node.mainPicture?.large,
            title = it.node.userPreferredTitle(),
            relation = it.relationType.localized(),
            relationType = it.relationType,
            mediaType = MediaType.MANGA,
        )
    }
    if (all.isEmpty()) return

    // Timeline focus: Sequels and Prequels
    val timeline = all.filter { it.relationType == RelationType.SEQUEL || it.relationType == RelationType.PREQUEL }
    val others = all.filter { it.relationType != RelationType.SEQUEL && it.relationType != RelationType.PREQUEL }

    if (timeline.isNotEmpty()) {
        InfoTitle(text = stringResource(R.string.franchise_timeline))
        LazyRow(
            modifier = Modifier.padding(top = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = timeline,
                key = { "${it.mediaType}-${it.id}" }
            ) { row ->
                MediaItemVertical(
                    imageUrl = row.imageUrl,
                    title = row.title,
                    subtitle = {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = row.relation,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    },
                    onClick = dropUnlessResumed {
                        navActionManager.toMediaDetails(row.mediaType, row.id)
                    }
                )
            }
        }
    }

    if (others.isNotEmpty()) {
        InfoTitle(
            text = if (uiState.isAnime) stringResource(R.string.related_anime)
            else stringResource(R.string.related_manga)
        )

        val categories = listOf<String?>(null) + others.map { it.relation }.distinct()
        var selectedCategory by remember { mutableStateOf<String?>(null) }
        val filtered = if (selectedCategory == null) others
        else others.filter { it.relation == selectedCategory }

        var showAll by remember { mutableStateOf(false) }
        LaunchedEffect(selectedCategory) { showAll = false }
        val displayItems = if (showAll || filtered.size <= 5) filtered else filtered.take(5)

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            items(categories) { category ->
                val selected = selectedCategory == category
                InputChip(
                    selected = selected,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(text = category ?: stringResource(R.string.all))
                    },
                    leadingIcon = if (selected) {
                        {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null
                )
            }
        }

        LazyRow(
            modifier = Modifier.padding(top = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = displayItems,
                key = { "${it.mediaType}-${it.id}" }
            ) { row ->
                MediaItemVertical(
                    imageUrl = row.imageUrl,
                    title = row.title,
                    subtitle = {
                        Text(
                            text = row.relation,
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    onClick = dropUnlessResumed {
                        navActionManager.toMediaDetails(row.mediaType, row.id)
                    }
                )
            }
        }

        if (filtered.size > 5) {
            Text(
                text = if (showAll) stringResource(R.string.show_less)
                else stringResource(R.string.show_more),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(start = 16.dp, top = 4.dp, bottom = 8.dp)
                    .clickable { showAll = !showAll }
            )
        }
    }
}

/** Formats a nullable count as the number, or "-" when null/non-positive. */
private fun Int?.countOrDash(): String =
    if (this != null && this > 0) this.toString() else "-"

@Composable
private fun MediaStatus.statusColor() = when (this) {
    MediaStatus.AIRING, MediaStatus.PUBLISHING -> MaterialTheme.colorScheme.primary
    MediaStatus.NOT_AIRED -> MaterialTheme.colorScheme.tertiary
    MediaStatus.HIATUS -> MaterialTheme.colorScheme.secondary
    MediaStatus.DISCONTINUED -> MaterialTheme.colorScheme.error
    MediaStatus.FINISHED_AIRING, MediaStatus.FINISHED -> MaterialTheme.colorScheme.outline
}

/** Fires a system share sheet for the given text. */
private fun android.content.Context.shareText(text: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, text)
    }
    startActivity(android.content.Intent.createChooser(intent, null))
}

@Preview
@Composable
fun MediaDetailsPreview() {
    MoeListTheme {
        Surface {
            MediaDetailsContent(
                uiState = MediaDetailsUiState(),
                event = null,
                isLoggedIn = false,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}
