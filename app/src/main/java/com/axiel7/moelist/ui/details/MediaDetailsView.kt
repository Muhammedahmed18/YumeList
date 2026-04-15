package com.axiel7.moelist.ui.details

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import coil3.compose.AsyncImage
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.anime.RelatedAnime
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.RelationType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.InfoTitle
import com.axiel7.moelist.ui.composables.TextIconHorizontal
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_BIG_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_BIG_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.composables.stats.HorizontalStatsBar
import com.axiel7.moelist.ui.details.composables.AnimeThemeItem
import com.axiel7.moelist.ui.details.composables.MediaDetailsTopAppBar
import com.axiel7.moelist.ui.details.composables.MediaInfoView
import com.axiel7.moelist.ui.details.composables.MusicStreamingSheet
import com.axiel7.moelist.ui.editmedia.EditMediaSheet
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    fun hideSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
    }

    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var isSynopsisExpanded by remember { mutableStateOf(false) }
    val maxLinesSynopsis by remember {
        derivedStateOf { if (isSynopsisExpanded) Int.MAX_VALUE else 5 }
    }

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

    if (uiState.message != null) {
        LaunchedEffect(uiState.message) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            if (isLoggedIn) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (uiState.mediaDetails != null) {
                            showSheet = true
                        } else {
                            context.showToast(context.getString(R.string.please_login_to_use_this_feature))
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(
                            if (uiState.isNewEntry) R.drawable.ic_round_add_24
                            else R.drawable.ic_round_edit_24
                        ),
                        contentDescription = "edit"
                    )
                    @Suppress("DEPRECATION")
                    Text(
                        text = if (uiState.isNewEntry) stringResource(R.string.add)
                        else uiState.mediaDetails?.myListStatus?.status?.localized()
                            ?: stringResource(R.string.edit),
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(bottom = padding.calculateBottomPadding() + 80.dp)
            ) {
                // Hero Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    // Blurred Banner
                    AsyncImage(
                        model = uiState.mediaDetails?.mainPicture?.large,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(20.dp),
                        contentScale = ContentScale.Crop,
                        alpha = 0.6f
                    )
                    // Gradient for top bar blend
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.4f),
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                    )

                    // Overlapping Poster with Border
                    val posterShape = RoundedCornerShape(8.dp)
                    MediaPoster(
                        url = uiState.mediaDetails?.mainPicture?.large,
                        showShadow = false,
                        modifier = Modifier
                            .padding(start = 24.dp)
                            .size(
                                width = (MEDIA_POSTER_BIG_WIDTH * 0.85).dp,
                                height = (MEDIA_POSTER_BIG_HEIGHT * 0.85).dp
                            )
                            .align(Alignment.BottomStart)
                            .offset { IntOffset(0, 30.dp.roundToPx()) }
                            .clip(posterShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                shape = posterShape
                            )
                            .defaultPlaceholder(visible = uiState.isLoading)
                            .clickable(onClick = dropUnlessResumed {
                                if (uiState.picturesUrls.isNotEmpty())
                                    navActionManager.toFullPoster(uiState.picturesUrls)
                            })
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {


                    Spacer(modifier = Modifier.height(8.dp))

                    // Format & Status Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextIconHorizontal(
                            text = uiState.mediaDetails?.mediaFormatWithYear() ?: "Loading",
                            icon = if (uiState.isAnime) R.drawable.ic_round_local_movies_24
                            else R.drawable.ic_round_book_24,
                            modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading),
                            fontSize = 14.sp
                        )
                        VerticalDivider(modifier = Modifier.height(12.dp))
                        TextIconHorizontal(
                            text = uiState.mediaDetails?.status?.localized() ?: "Loading",
                            icon = if (uiState.isAnime) R.drawable.ic_round_rss_feed_24
                            else R.drawable.round_drive_file_rename_outline_24,
                            modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading),
                            fontSize = 14.sp
                        )
                    }

                    if (!uiState.hideScore) {
                        TextIconHorizontal(
                            text = uiState.mediaDetails?.mean.toStringOrNull() ?: "??",
                            icon = R.drawable.ic_round_details_star_24,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .defaultPlaceholder(visible = uiState.isLoading),
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                //Genres
                LazyRow(
                    modifier = Modifier.padding(vertical = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.mediaDetails?.genres.orEmpty()) {
                        AssistChip(
                            onClick = { },
                            label = { Text(text = it.localized()) },
                        )
                    }
                }

                //Synopsis
                val synopsisAndBackground = uiState.mediaDetails?.synopsisAndBackground()
                if (uiState.isLoading || !synopsisAndBackground.isNullOrEmpty()) {
                    SelectionContainer {
                        Text(
                            text = synopsisAndBackground
                                ?: AnnotatedString(stringResource(R.string.lorem_ipsun)),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clickable { isSynopsisExpanded = !isSynopsisExpanded }
                                .animateContentSize()
                                .defaultPlaceholder(visible = uiState.isLoading),
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = maxLinesSynopsis
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { isSynopsisExpanded = !isSynopsisExpanded }
                        ) {
                            Text(
                                text = if (isSynopsisExpanded) stringResource(R.string.back)
                                else stringResource(R.string.more)
                            )
                        }

                        IconButton(
                            onClick = {
                                uiState.mediaDetails?.synopsis?.let { context.copyToClipBoard(it) }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.round_content_copy_24),
                                contentDescription = stringResource(R.string.copied)
                            )
                        }
                    }
                }

                //Stats Grid
                InfoTitle(text = stringResource(R.string.stats))
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .defaultPlaceholder(visible = uiState.isLoading),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.top_ranked),
                            value = uiState.mediaDetails?.rankText().orEmpty(),
                            icon = R.drawable.ic_round_bar_chart_24
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.users_scores),
                            value = uiState.mediaDetails?.numScoringUsers?.format() ?: UNKNOWN_CHAR,
                            icon = R.drawable.ic_round_thumbs_up_down_24
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.members),
                            value = uiState.mediaDetails?.numListUsers?.format() ?: UNKNOWN_CHAR,
                            icon = R.drawable.ic_round_group_24
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.popularity),
                            value = "# ${uiState.mediaDetails?.popularity}",
                            icon = R.drawable.ic_round_trending_up_24
                        )
                    }
                }

                //Info Section
                InfoTitle(text = stringResource(R.string.more_info))
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (uiState.mediaDetails is AnimeDetails) {
                            MediaInfoView(
                                title = stringResource(R.string.duration),
                                info = uiState.mediaDetails.episodeDurationLocalized(),
                                modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                            )
                        } else if (uiState.mediaDetails is MangaDetails) {
                            MediaInfoView(
                                title = stringResource(R.string.authors),
                                info = uiState.mediaDetails.authors
                                    ?.joinToString { "${it.node.firstName} ${it.node.lastName}" },
                                modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                            )
                        }
                        MediaInfoView(
                            title = stringResource(R.string.source),
                            info = (uiState.mediaDetails as? AnimeDetails)?.source?.localized()
                                ?: stringResource(R.string.unknown),
                            modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        MediaInfoView(
                            title = stringResource(R.string.start_date),
                            info = uiState.mediaDetails?.startDate?.parseDateAndLocalize(),
                            modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                        )
                        MediaInfoView(
                            title = stringResource(R.string.end_date),
                            info = uiState.mediaDetails?.endDate?.parseDateAndLocalize(),
                            modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                        )
                    }

                    if (uiState.mediaDetails is AnimeDetails) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            MediaInfoView(
                                title = stringResource(R.string.season),
                                info = uiState.mediaDetails.startSeason?.seasonYearText(),
                                modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                            )
                            MediaInfoView(
                                title = stringResource(R.string.studios),
                                info = uiState.studiosJoined,
                                modifier = Modifier.weight(1f).defaultPlaceholder(visible = uiState.isLoading)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    MediaInfoView(
                        title = stringResource(R.string.jp_title),
                        info = uiState.mediaDetails?.alternativeTitles?.ja,
                        modifier = Modifier.fillMaxWidth().defaultPlaceholder(visible = uiState.isLoading)
                    )
                    MediaInfoView(
                        title = stringResource(R.string.english),
                        info = uiState.mediaDetails?.alternativeTitles?.en,
                        modifier = Modifier.fillMaxWidth().defaultPlaceholder(visible = uiState.isLoading)
                    )
                }

                //Characters
                if (uiState.isAnime) {
                    InfoTitle(text = stringResource(R.string.characters))
                    if (uiState.characters.isNotEmpty() || uiState.isLoadingCharacters) {
                        LazyRow(
                            modifier = Modifier.padding(top = 8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(
                                items = uiState.characters,
                                contentType = { it }
                            ) { item ->
                                MediaItemVertical(
                                    imageUrl = item.node.mainPicture?.medium,
                                    title = item.fullName(),
                                    modifier = Modifier.padding(end = 8.dp),
                                    subtitle = {
                                        Text(
                                            text = item.role?.localized().orEmpty(),
                                            color = MaterialTheme.colorScheme.outline,
                                            fontSize = 13.sp
                                        )
                                    },
                                    minLines = 2,
                                    onClick = {
                                        context.openLink(CHARACTER_URL + item.node.id)
                                    }
                                )
                            }
                            if (uiState.isLoadingCharacters) {
                                item {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    } else {
                        TextButton(
                            onClick = {
                                event?.getCharacters()
                            },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(text = stringResource(R.string.view_characters))
                        }
                    }
                }

                //Themes Section
                if (uiState.mediaDetails is AnimeDetails) {
                    var showOpeningSheet by remember { mutableStateOf(false) }
                    var showEndingSheet by remember { mutableStateOf(false) }
                    var selectedSong by remember { mutableStateOf<String?>(null) }
                    var showMusicSheet by remember { mutableStateOf(false) }

                    if (showMusicSheet && selectedSong != null) {
                        MusicStreamingSheet(
                            songTitle = selectedSong.orEmpty(),
                            bottomPadding = WindowInsets.navigationBars.asPaddingValues()
                                .calculateBottomPadding(),
                            onDismiss = {
                                showMusicSheet = false
                                selectedSong = null
                            }
                        )
                    }

                    InfoTitle(text = stringResource(R.string.music_themes))
                    
                    val openingThemes = uiState.mediaDetails.openingThemes.orEmpty()
                    if (openingThemes.isNotEmpty()) {
                        ThemeSummaryCard(
                            title = stringResource(R.string.opening),
                            count = openingThemes.size,
                            onClick = { showOpeningSheet = true }
                        )
                    }

                    val endingThemes = uiState.mediaDetails.endingThemes.orEmpty()
                    if (endingThemes.isNotEmpty()) {
                        ThemeSummaryCard(
                            title = stringResource(R.string.ending),
                            count = endingThemes.size,
                            onClick = { showEndingSheet = true }
                        )
                    }

                    if (showOpeningSheet) {
                        ThemeListSheet(
                            title = stringResource(R.string.opening),
                            themes = openingThemes.map { it.text },
                            onDismiss = { showOpeningSheet = false },
                            onThemeClick = { 
                                selectedSong = it
                                showMusicSheet = true
                            }
                        )
                    }

                    if (showEndingSheet) {
                        ThemeListSheet(
                            title = stringResource(R.string.ending),
                            themes = endingThemes.map { it.text },
                            onDismiss = { showEndingSheet = false },
                            onThemeClick = { 
                                selectedSong = it
                                showMusicSheet = true
                            }
                        )
                    }
                }

                // Related Media Section
                val relatedMedia = (uiState.relatedAnime + uiState.relatedManga)
                    .filter { it.relationType == RelationType.PREQUEL || it.relationType == RelationType.SEQUEL }

                if (relatedMedia.isNotEmpty()) {
                    InfoTitle(text = stringResource(R.string.related_anime))
                    LazyRow(
                        modifier = Modifier.padding(top = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(relatedMedia) { item ->
                            val mediaType = if (item is RelatedAnime) MediaType.ANIME else MediaType.MANGA
                            MediaItemVertical(
                                imageUrl = item.node.mainPicture?.medium,
                                title = item.node.title,
                                subtitle = {
                                    Text(
                                        text = item.relationType.localized(),
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                onClick = {
                                    navActionManager.toMediaDetails(mediaType, item.node.id)
                                }
                            )
                        }
                    }
                }

                (uiState.mediaDetails as? AnimeDetails)?.statistics?.status?.toStats()?.let { stats ->
                    InfoTitle(text = stringResource(R.string.status_distribution))
                    HorizontalStatsBar(
                        stats = stats
                    )
                }
            }

            // Top App Bar placed on top for immersive effect
            MediaDetailsTopAppBar(
                uiState = uiState,
                event = event,
                navigateBack = dropUnlessResumed { navActionManager.goBack() },
                scrollBehavior = topAppBarScrollBehavior,
            )
        }
    }//:Scaffold
}

@Composable
fun ThemeSummaryCard(
    title: String,
    count: Int,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$count tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeListSheet(
    title: String,
    themes: List<String>,
    onDismiss: () -> Unit,
    onThemeClick: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        contentWindowInsets = { WindowInsets.statusBars }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(themes) { theme ->
                    AnimeThemeItem(
                        text = theme,
                        onClick = { onThemeClick(theme) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: Int,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}
