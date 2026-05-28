package com.axiel7.moelist.ui.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Abc
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.ThumbsUpDown
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.anime.RelatedAnime
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.manga.RelatedManga
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.RelationType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.InfoTitle
import com.axiel7.moelist.ui.composables.TextIconVertical
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_BIG_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_BIG_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.details.composables.MediaDetailsTopAppBar
import com.axiel7.moelist.ui.details.composables.MediaInfoView
import com.axiel7.moelist.ui.details.composables.MusicStreamingSheet
import com.axiel7.moelist.ui.editmedia.EditMediaSheet
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.CHARACTER_URL
import com.axiel7.moelist.utils.ContextExtensions.copyToClipBoard
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.ContextExtensions.showToast
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun MediaDetailsContent(
    uiState: MediaDetailsUiState,
    event: MediaDetailsEvent?,
    isLoggedIn: Boolean,
    navActionManager: NavActionManager
) {
    val context = LocalContext.current

    val scrollState = rememberScrollState()
    val topAppBarScrollBehavior =
        TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
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
    val copiedMessage = stringResource(R.string.copied)

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

    if (uiState.message != null) {
        LaunchedEffect(uiState.message) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            MediaDetailsTopAppBar(
                uiState = uiState,
                event = event,
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(bottom = bottomBarPadding + 16.dp)
        ) {
            // ---------- Header ----------
            Row(modifier = Modifier.padding(top = 16.dp)) {
                MediaPoster(
                    url = uiState.mediaDetails?.mainPicture?.large,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .size(
                            width = MEDIA_POSTER_BIG_WIDTH.dp,
                            height = MEDIA_POSTER_BIG_HEIGHT.dp
                        )
                        .defaultPlaceholder(visible = uiState.isLoading)
                        .clickable(onClick = dropUnlessResumed {
                            if (uiState.picturesUrls.isNotEmpty())
                                navActionManager.toFullPoster(uiState.picturesUrls)
                        })
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Title
                    Text(
                        text = uiState.mediaDetails?.userPreferredTitle() ?: "Loading",
                        modifier = Modifier
                            .defaultPlaceholder(visible = uiState.isLoading)
                            .combinedClickable(
                                onLongClick = {
                                    uiState.mediaDetails?.title?.let { context.copyToClipBoard(it) }
                                },
                                onClick = { }
                            ),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        // Status Chip
                        val statusStr = uiState.mediaDetails?.status?.localized() ?: "Loading"
                        val statusColor = if (uiState.mediaDetails?.status == MediaStatus.AIRING
                            || uiState.mediaDetails?.status == MediaStatus.PUBLISHING
                        ) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainer

                        SuggestionChip(
                            onClick = { },
                            label = { Text(text = statusStr) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = statusColor
                            ),
                            border = null,
                            modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                        )

                        // Format & Year Chip
                        SuggestionChip(
                            onClick = { },
                            label = { Text(text = uiState.mediaDetails?.mediaFormatWithYear() ?: "Loading") },
                            modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                        )

                        // Episodes / Chapters
                        val countLabel = when (val d = uiState.mediaDetails) {
                            is AnimeDetails -> "${d.numEpisodes.countOrDash()} Eps"
                            is MangaDetails -> "${d.numChapters.countOrDash()} Chs"
                            else -> "Loading"
                        }
                        SuggestionChip(
                            onClick = { },
                            label = { Text(text = countLabel) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.isAnime) Icons.Rounded.Timer
                                    else Icons.AutoMirrored.Rounded.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                        )

                        // Duration Chip (anime only)
                        if (uiState.isAnime) {
                            SuggestionChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = (uiState.mediaDetails as? AnimeDetails)
                                            ?.episodeDurationLocalized() ?: "Loading"
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                            )
                        }

                        // Score Chip
                        if (!uiState.hideScore) {
                            SuggestionChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = uiState.mediaDetails?.mean.toStringOrNull() ?: "??",
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                            )
                        }
                    }
                }
            }//:Row

            // ---------- Action Dock ----------
            ActionDock(
                isLoggedIn = isLoggedIn,
                isNewEntry = uiState.isNewEntry,
                listStatusLabel = uiState.mediaDetails?.myListStatus?.status?.localized(),
                onEditClick = {
                    if (isLoggedIn) {
                        if (uiState.mediaDetails != null) showSheet = true
                    } else {
                        context.showToast(pleaseLoginMessage)
                    }
                }
            )

            // ---------- Genres ----------
            if (!uiState.mediaDetails?.genres.isNullOrEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.mediaDetails?.genres.orEmpty()) { genre ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(text = genre.localized()) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // ---------- Synopsis ----------
            val synopsisAndBackground = uiState.mediaDetails?.synopsisAndBackground()
            if (uiState.isLoading || !synopsisAndBackground.isNullOrEmpty()) {
                InfoTitle(text = stringResource(R.string.synopsis))
                Text(
                    text = synopsisAndBackground
                        ?: AnnotatedString(stringResource(R.string.lorem_ipsun)),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .animateContentSize(animationSpec = spring())
                        .defaultPlaceholder(visible = uiState.isLoading)
                        .combinedClickable(
                            onLongClick = {
                                uiState.mediaDetails?.synopsis?.let {
                                    context.copyToClipBoard(it)
                                    context.showToast(copiedMessage)
                                }
                            },
                            onClick = { isSynopsisExpanded = !isSynopsisExpanded }
                        )
                        .then(
                            if (!isSynopsisExpanded) {
                                Modifier.drawWithContent {
                                    drawContent()
                                    // Fade the bottom of the collapsed synopsis for a soft cut-off
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.Black, Color.Transparent),
                                            startY = size.height * 0.65f,
                                            endY = size.height
                                        ),
                                        blendMode = BlendMode.DstIn
                                    )
                                }
                            } else Modifier
                        ),
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = maxLinesSynopsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { isSynopsisExpanded = !isSynopsisExpanded }
                    ) {
                        Icon(
                            imageVector = if (isSynopsisExpanded) Icons.Rounded.KeyboardArrowUp
                            else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = if (isSynopsisExpanded) stringResource(R.string.show_less)
                            else stringResource(R.string.show_more)
                        )
                    }
                }
            }

            // ---------- More Info (single card) ----------
            InfoTitle(text = stringResource(R.string.more_info))
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        maxItemsInEachRow = 2
                    ) {
                        val cellModifier = Modifier.weight(1f)

                        if (uiState.isAnime) {
                            // Row 1: Studio + Source
                            MediaInfoView(
                                title = stringResource(R.string.studios),
                                info = uiState.studiosJoined,
                                iconVector = Icons.Rounded.Movie,
                                modifier = cellModifier.defaultPlaceholder(visible = uiState.isLoading)
                            )
                            MediaInfoView(
                                title = stringResource(R.string.source),
                                info = (uiState.mediaDetails as? AnimeDetails)?.source?.localized(),
                                iconVector = Icons.Rounded.History,
                                modifier = cellModifier.defaultPlaceholder(visible = uiState.isLoading)
                            )
                            // Row 2: Start Date + End Date
                            MediaInfoView(
                                title = stringResource(R.string.start_date),
                                info = uiState.mediaDetails?.startDate?.parseDateAndLocalize(),
                                iconVector = Icons.Rounded.Add,
                                modifier = cellModifier.defaultPlaceholder(visible = uiState.isLoading)
                            )
                            MediaInfoView(
                                title = stringResource(R.string.end_date),
                                info = uiState.mediaDetails?.endDate?.parseDateAndLocalize(),
                                iconVector = Icons.Rounded.History,
                                modifier = cellModifier.defaultPlaceholder(visible = uiState.isLoading)
                            )
                            // Row 3: Season + Broadcast
                            MediaInfoView(
                                title = stringResource(R.string.season),
                                info = (uiState.mediaDetails as? AnimeDetails)?.startSeason?.seasonYearText(),
                                iconVector = Icons.Rounded.RssFeed,
                                modifier = cellModifier.defaultPlaceholder(visible = uiState.isLoading)
                            )
                            MediaInfoView(
                                title = stringResource(R.string.broadcast),
                                info = (uiState.mediaDetails as? AnimeDetails)?.broadcast?.timeText(
                                    isAiring = uiState.mediaDetails.status == MediaStatus.AIRING
                                ),
                                iconVector = Icons.Rounded.RssFeed,
                                modifier = cellModifier.defaultPlaceholder(visible = uiState.isLoading)
                            )
                        } else {
                            MediaInfoView(
                                title = stringResource(R.string.authors),
                                info = (uiState.mediaDetails as? MangaDetails)?.authors
                                    ?.joinToString { "${it.node.firstName} ${it.node.lastName}" },
                                iconVector = Icons.Rounded.Movie,
                                modifier = cellModifier.defaultPlaceholder(visible = uiState.isLoading)
                            )
                            MediaInfoView(
                                title = stringResource(R.string.source),
                                info = (uiState.mediaDetails as? AnimeDetails)?.source?.localized(),
                                iconVector = Icons.Rounded.History,
                                modifier = cellModifier.defaultPlaceholder(visible = uiState.isLoading)
                            )
                            MediaInfoView(
                                title = stringResource(R.string.serialization),
                                info = uiState.serializationJoined,
                                iconVector = Icons.Rounded.Bookmark,
                                modifier = cellModifier.defaultPlaceholder(visible = uiState.isLoading)
                            )
                            if (uiState.mediaDetails is MangaDetails) {
                                MediaInfoView(
                                    title = stringResource(R.string.volumes),
                                    info = uiState.mediaDetails.numVolumes.countOrDash(),
                                    iconVector = Icons.Rounded.Bookmark,
                                    modifier = cellModifier.defaultPlaceholder(visible = uiState.isLoading)
                                )
                            }
                            MediaInfoView(
                                title = stringResource(R.string.start_date),
                                info = uiState.mediaDetails?.startDate?.parseDateAndLocalize(),
                                iconVector = Icons.Rounded.Add,
                                modifier = cellModifier.defaultPlaceholder(visible = uiState.isLoading)
                            )
                            MediaInfoView(
                                title = stringResource(R.string.end_date),
                                info = uiState.mediaDetails?.endDate?.parseDateAndLocalize(),
                                iconVector = Icons.Rounded.History,
                                modifier = cellModifier.defaultPlaceholder(visible = uiState.isLoading)
                            )
                        }
                    }
                }
            }

            // ---------- Title Languages ----------
            InfoTitle(text = stringResource(R.string.title_language))
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SelectionContainer {
                        MediaInfoView(
                            title = stringResource(R.string.romaji),
                            info = uiState.mediaDetails?.title,
                            iconVector = Icons.Rounded.Translate,
                            modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                    SelectionContainer {
                        MediaInfoView(
                            title = stringResource(R.string.jp_title),
                            info = uiState.mediaDetails?.alternativeTitles?.ja,
                            iconVector = Icons.Rounded.Language,
                            modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                    SelectionContainer {
                        MediaInfoView(
                            title = stringResource(R.string.english),
                            info = uiState.mediaDetails?.alternativeTitles?.en,
                            iconVector = Icons.Rounded.Abc,
                            modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }
                }
            }

            // ---------- Related Section ----------
            RelatedSection(
                uiState = uiState,
                navActionManager = navActionManager
            )

            // ---------- Characters (anime only) ----------
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
                            Box {
                                MediaItemVertical(
                                    imageUrl = item.node.mainPicture?.medium,
                                    title = item.fullName(),
                                    subtitle = {},
                                    minLines = 2,
                                    onClick = {
                                        context.openLink(CHARACTER_URL + item.node.id)
                                    }
                                )
                                CharacterRolePill(
                                    role = item.role?.localized().orEmpty(),
                                    isMain = item.role?.name.equals("MAIN", ignoreCase = true),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 8.dp, end = 8.dp)
                                )
                            }
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
                            .fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.view_characters))
                    }
                }
            }

            // ---------- Themes / Music (anime only) ----------
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
                            FilledTonalButton(
                                onClick = {
                                    themesSheetTitle = openingLabel
                                    themesSheetItems = openings
                                    showThemesSheet = true
                                },
                                modifier = Modifier.weight(1f)
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
                            FilledTonalButton(
                                onClick = {
                                    themesSheetTitle = endingLabel
                                    themesSheetItems = endings
                                    showThemesSheet = true
                                },
                                modifier = Modifier.weight(1f)
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
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultPlaceholder(visible = uiState.isLoading),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetricItem(
                            value = uiState.mediaDetails?.rankText().orEmpty(),
                            label = stringResource(R.string.top_ranked),
                            icon = Icons.Rounded.BarChart
                        )
                        VerticalDivider(modifier = Modifier.height(40.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        MetricItem(
                            value = "# ${uiState.mediaDetails?.popularity}",
                            label = stringResource(R.string.popularity),
                            icon = Icons.AutoMirrored.Rounded.TrendingUp
                        )
                        VerticalDivider(modifier = Modifier.height(40.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        MetricItem(
                            value = uiState.mediaDetails?.numListUsers?.format() ?: UNKNOWN_CHAR,
                            label = stringResource(R.string.members),
                            icon = Icons.Rounded.Group
                        )
                    }

                    (uiState.mediaDetails as? AnimeDetails)?.statistics?.status?.toStats()?.let { stats ->
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.status_distribution),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        val total = remember(stats) { stats.sumOf { it.value.toDouble() } }
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        }//:Column
    }//:Scaffold
}

@Composable
private fun ActionDock(
    isLoggedIn: Boolean,
    isNewEntry: Boolean,
    listStatusLabel: String?,
    onEditClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onEditClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(
                imageVector = if (isNewEntry) Icons.Rounded.Add else Icons.Rounded.Edit,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isNewEntry) stringResource(R.string.add)
                else listStatusLabel ?: stringResource(R.string.edit),
                style = MaterialTheme.typography.labelLarge
            )
        }
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
    val percent = (fraction * 100).format() ?: "0"
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = color,
                modifier = Modifier.size(10.dp)
            ) {}
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            Text(
                text = "$percent%",
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
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
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
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
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
    modifier: Modifier = Modifier,
) {
    if (role.isEmpty()) return
    val container = if (isMain) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceContainerHighest
    val content = if (isMain) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = container.copy(alpha = 0.9f),
        tonalElevation = 2.dp,
    ) {
        Text(
            text = role,
            color = content,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
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
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = row.relation,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
        var isRelatedExpanded by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isRelatedExpanded = !isRelatedExpanded },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                InfoTitle(text = stringResource(R.string.related_anime))
            }
            Icon(
                imageVector = if (isRelatedExpanded) Icons.Rounded.KeyboardArrowUp
                else Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp)
            )
        }

        val categories = listOf<String?>(null) + others.map { it.relation }.distinct()
        var selectedCategory by remember { mutableStateOf<String?>(null) }
        val filtered = if (selectedCategory == null) others
        else others.filter { it.relation == selectedCategory }

        AnimatedVisibility(
            visible = isRelatedExpanded,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(animationSpec = tween(durationMillis = 220)),
            exit = shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeOut(animationSpec = tween(durationMillis = 150))
        ) {
            Column {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(categories) { category ->
                        val selected = selectedCategory == category
                        FilterChip(
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
                        items = filtered,
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
            }
        }
    }
}

/** Formats a nullable count as the number, or "-" when null/non-positive. */
private fun Int?.countOrDash(): String =
    if (this != null && this > 0) this.toString() else "-"

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