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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
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
                    },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        painter = painterResource(
                            if (uiState.isNewEntry) R.drawable.ic_round_add_24
                            else R.drawable.ic_round_edit_24
                        ),
                        contentDescription = "edit"
                    )
                    Text(
                        text = if (uiState.isNewEntry) stringResource(R.string.add)
                        else uiState.mediaDetails?.myListStatus?.status?.localized()
                            ?: stringResource(R.string.edit),
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
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
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 112.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Poster
                    MediaPoster(
                        url = uiState.mediaDetails?.mainPicture?.large,
                        showShadow = false,
                        modifier = Modifier
                            .size(
                                width = (MEDIA_POSTER_BIG_WIDTH * 0.85).dp,
                                height = (MEDIA_POSTER_BIG_HEIGHT * 0.85).dp
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .defaultPlaceholder(visible = uiState.isLoading)
                            .clickable(onClick = dropUnlessResumed {
                                if (uiState.picturesUrls.isNotEmpty())
                                    navActionManager.toFullPoster(uiState.picturesUrls)
                            })
                    )

                    // Right Side Info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = uiState.mediaDetails?.userPreferredTitle().orEmpty(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Format & Episode/Chapter Count Row
                        val totalCount = if (uiState.mediaDetails is AnimeDetails) {
                            uiState.mediaDetails.numEpisodes?.takeIf { it > 0 }?.toString()
                        } else if (uiState.mediaDetails is MangaDetails) {
                            uiState.mediaDetails.numChapters?.takeIf { it > 0 }?.toString()
                        } else null

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = uiState.mediaDetails?.mediaFormat?.localized() ?: "??",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = buildString {
                                        append(totalCount ?: stringResource(R.string.unknown))
                                        append(" ")
                                        append(
                                            if (uiState.isAnime) stringResource(R.string.episodes)
                                            else stringResource(R.string.chapters)
                                        )
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        // Score
                        if (!uiState.hideScore) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_round_details_star_24),
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = uiState.mediaDetails?.mean.toStringOrNull() ?: "??",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }

                        // Status Chip
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                            shape = CircleShape,
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            TextIconHorizontal(
                                text = uiState.mediaDetails?.status?.localized() ?: "Loading",
                                icon = if (uiState.isAnime) R.drawable.ic_round_rss_feed_24
                                else R.drawable.round_drive_file_rename_outline_24,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Modern Pill Genres
                LazyRow(
                    modifier = Modifier.padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.mediaDetails?.genres.orEmpty()) {
                        AssistChip(
                            onClick = { },
                            label = { Text(text = it.localized()) },
                            shape = CircleShape,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            border = null
                        )
                    }
                }

                // Synopsis with Fade Effect
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
                            lineHeight = 26.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = maxLinesSynopsis
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        TextButton(
                            onClick = { isSynopsisExpanded = !isSynopsisExpanded }
                        ) {
                            Text(
                                text = if (isSynopsisExpanded) "Show Less"
                                else "Read Full Synopsis",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                uiState.mediaDetails?.synopsis?.let { context.copyToClipBoard(it) }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.round_content_copy_24),
                                contentDescription = stringResource(R.string.copied),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Modern Stats Dashboard
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
                            icon = R.drawable.ic_round_bar_chart_24,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.users_scores),
                            value = uiState.mediaDetails?.numScoringUsers?.format() ?: UNKNOWN_CHAR,
                            icon = R.drawable.ic_round_thumbs_up_down_24,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
                            icon = R.drawable.ic_round_group_24,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.popularity),
                            value = "# ${uiState.mediaDetails?.popularity}",
                            icon = R.drawable.ic_round_trending_up_24,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    }
                }

                //Info Section - Tonal Column Grid
                InfoTitle(text = stringResource(R.string.more_info))
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            if (uiState.mediaDetails is AnimeDetails) {
                                MediaInfoView(
                                    title = stringResource(R.string.duration),
                                    info = uiState.mediaDetails.episodeDurationLocalized(),
                                    icon = R.drawable.ic_round_access_time_24,
                                    modifier = Modifier.weight(1f)
                                )
                                MediaInfoView(
                                    title = stringResource(R.string.source),
                                    info = uiState.mediaDetails.source?.localized()
                                        ?: stringResource(R.string.unknown),
                                    icon = R.drawable.ic_round_menu_book_24,
                                    modifier = Modifier.weight(1f)
                                )
                            } else if (uiState.mediaDetails is MangaDetails) {
                                val volumes = uiState.mediaDetails.numVolumes
                                MediaInfoView(
                                    title = stringResource(R.string.volumes),
                                    info = if (volumes == null || volumes == 0) "-" else volumes.toString(),
                                    icon = R.drawable.ic_round_book_24,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            MediaInfoView(
                                title = stringResource(R.string.start_date),
                                info = uiState.mediaDetails?.startDate?.parseDateAndLocalize(),
                                icon = R.drawable.round_calendar_today_24,
                                modifier = Modifier.weight(1f)
                            )
                            MediaInfoView(
                                title = stringResource(R.string.end_date),
                                info = uiState.mediaDetails?.endDate?.parseDateAndLocalize(),
                                icon = R.drawable.round_calendar_today_24,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (uiState.mediaDetails is AnimeDetails) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                MediaInfoView(
                                    title = stringResource(R.string.season),
                                    info = uiState.mediaDetails.startSeason?.seasonYearText(),
                                    icon = R.drawable.ic_spring_24,
                                    modifier = Modifier.weight(1f)
                                )
                                MediaInfoView(
                                    title = stringResource(R.string.studios),
                                    info = uiState.studiosJoined,
                                    icon = R.drawable.ic_round_movie_24,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else if (uiState.mediaDetails is MangaDetails) {
                            MediaInfoView(
                                title = stringResource(R.string.authors),
                                info = uiState.mediaDetails.authors
                                    ?.joinToString { "${it.node.firstName} ${it.node.lastName}" },
                                icon = R.drawable.ic_round_person_24,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Alternative Titles Section - Tonal Column Grid
                val englishTitle = uiState.mediaDetails?.alternativeTitles?.en
                val japaneseTitle = uiState.mediaDetails?.alternativeTitles?.ja
                val synonyms = uiState.mediaDetails?.alternativeTitles?.synonyms

                if (!englishTitle.isNullOrBlank() || !japaneseTitle.isNullOrBlank() || !synonyms.isNullOrEmpty()) {
                    InfoTitle(text = stringResource(R.string.title_language))
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!englishTitle.isNullOrBlank()) {
                                MediaInfoView(
                                    title = stringResource(R.string.english),
                                    info = englishTitle,
                                    icon = R.drawable.ic_round_language_24,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (!japaneseTitle.isNullOrBlank()) {
                                MediaInfoView(
                                    title = stringResource(R.string.japanese),
                                    info = japaneseTitle,
                                    icon = R.drawable.ic_outline_translate_24,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (!synonyms.isNullOrEmpty()) {
                                MediaInfoView(
                                    title = stringResource(R.string.synonyms),
                                    info = synonyms.joinToString(", "),
                                    icon = R.drawable.round_title_24,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                //Characters - Immersive Carousel
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
                                    modifier = Modifier,
                                    subtitle = {
                                        Text(
                                            text = item.role?.localized().orEmpty(),
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
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
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(text = stringResource(R.string.view_characters), fontWeight = FontWeight.Bold)
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
                    InfoTitle(
                        text = stringResource(
                            if (uiState.isAnime) R.string.related_anime
                            else R.string.related_manga
                        )
                    )
                    LazyRow(
                        modifier = Modifier.padding(top = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(relatedMedia) { item ->
                            val mediaType = if (item is RelatedAnime) MediaType.ANIME else MediaType.MANGA
                            MediaItemVertical(
                                imageUrl = item.node.mainPicture?.medium,
                                title = item.node.title,
                                subtitle = {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        shape = CircleShape,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = item.relationType.localized(),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
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
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        HorizontalStatsBar(
                            stats = stats
                        )
                    }
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
    }
}

@Composable
fun ThemeSummaryCard(
    title: String,
    count: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
        contentWindowInsets = { WindowInsets.statusBars },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
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
                fontWeight = FontWeight.ExtraBold
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
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
