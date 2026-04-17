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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.graphics.Brush
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
                MediaDetailsSections(
                    uiState = uiState,
                    event = event,
                    navActionManager = navActionManager,
                    isSynopsisExpanded = isSynopsisExpanded,
                    maxLinesSynopsis = maxLinesSynopsis,
                    onToggleSynopsis = { isSynopsisExpanded = !isSynopsisExpanded },
                )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaDetailsSections(
    uiState: MediaDetailsUiState,
    event: MediaDetailsEvent?,
    navActionManager: NavActionManager,
    isSynopsisExpanded: Boolean,
    maxLinesSynopsis: Int,
    onToggleSynopsis: () -> Unit,
) {
    val context = LocalContext.current

    MediaHeaderSection(
        uiState = uiState,
        navActionManager = navActionManager,
    )

    Spacer(modifier = Modifier.height(16.dp))

    MediaGenresSection(uiState = uiState)

    MediaSynopsisSection(
        uiState = uiState,
        isSynopsisExpanded = isSynopsisExpanded,
        maxLinesSynopsis = maxLinesSynopsis,
        onToggleSynopsis = onToggleSynopsis,
        onCopySynopsis = {
            uiState.mediaDetails?.synopsis?.let { context.copyToClipBoard(it) }
        },
    )

    MediaStatsSection(uiState = uiState)

    MediaMoreInfoSection(uiState = uiState)

    MediaAlternativeTitlesSection(uiState = uiState)

    MediaCharactersSection(
        uiState = uiState,
        event = event,
    )

    MediaThemesSection(uiState = uiState)

    MediaRelatedMediaSection(
        uiState = uiState,
        navActionManager = navActionManager,
    )

    MediaStatusDistributionSection(uiState = uiState)
}

@Composable
private fun MediaHeaderSection(
    uiState: MediaDetailsUiState,
    navActionManager: NavActionManager,
) {
    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding + 72.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ElevatedCard(
            modifier = Modifier
                .size(
                    width = (MEDIA_POSTER_BIG_WIDTH * 0.85).dp,
                    height = (MEDIA_POSTER_BIG_HEIGHT * 0.85).dp
                )
                .defaultPlaceholder(visible = uiState.isLoading)
                .clickable(onClick = dropUnlessResumed {
                    if (uiState.picturesUrls.isNotEmpty()) {
                        navActionManager.toFullPoster(uiState.picturesUrls)
                    }
                }),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
        ) {
            MediaPoster(
                url = uiState.mediaDetails?.mainPicture?.large,
                showShadow = false,
                modifier = Modifier.fillMaxSize()
            )
        }

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

            val totalCount = if (uiState.mediaDetails is AnimeDetails) {
                uiState.mediaDetails.numEpisodes?.takeIf { it > 0 }?.toString()
            } else if (uiState.mediaDetails is MangaDetails) {
                uiState.mediaDetails.numChapters?.takeIf { it > 0 }?.toString()
            } else null

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text(text = uiState.mediaDetails?.mediaFormat?.localized() ?: "??") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = null,
                )

                AssistChip(
                    onClick = { },
                    label = {
                        val countLabel = if (uiState.isAnime) {
                            stringResource(if (totalCount == "1") R.string.episode else R.string.episodes)
                        } else {
                            stringResource(if (totalCount == "1") R.string.chapter else R.string.chapters)
                        }
                        Text(
                            text = "$countLabel (${totalCount ?: "-"})"
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = null,
                )
            }

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

            AssistChip(
                onClick = { },
                modifier = Modifier.align(Alignment.Start),
                label = { Text(text = uiState.mediaDetails?.status?.localized() ?: "Loading") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(
                            if (uiState.isAnime) R.drawable.ic_round_rss_feed_24
                            else R.drawable.round_drive_file_rename_outline_24
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    leadingIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ),
                border = null,
            )
        }
    }
}

@Composable
private fun MediaGenresSection(uiState: MediaDetailsUiState) {
    LazyRow(
        modifier = Modifier.padding(bottom = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(uiState.mediaDetails?.genres.orEmpty()) {
            AssistChip(
                onClick = { },
                label = { Text(text = it.localized()) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = null
            )
        }
    }
}

@Composable
private fun MediaSynopsisSection(
    uiState: MediaDetailsUiState,
    isSynopsisExpanded: Boolean,
    maxLinesSynopsis: Int,
    onToggleSynopsis: () -> Unit,
    onCopySynopsis: () -> Unit,
) {
    val synopsisAndBackground = uiState.mediaDetails?.synopsisAndBackground()
    if (uiState.isLoading || !synopsisAndBackground.isNullOrEmpty()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.synopsis),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                IconButton(
                    onClick = onCopySynopsis,
                    enabled = uiState.mediaDetails?.synopsis?.isNotBlank() == true,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_content_copy_24),
                        contentDescription = stringResource(R.string.copied),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                SelectionContainer {
                    Text(
                        text = synopsisAndBackground
                            ?: AnnotatedString(stringResource(R.string.lorem_ipsun)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleSynopsis() }
                            .animateContentSize()
                            .defaultPlaceholder(visible = uiState.isLoading),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 26.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = maxLinesSynopsis
                    )
                }

                if (!isSynopsisExpanded) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(36.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onToggleSynopsis() }) {
                Text(
                    text = if (isSynopsisExpanded) "Show Less"
                    else "Read Full Synopsis",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MediaStatsSection(uiState: MediaDetailsUiState) {
    InfoTitle(text = stringResource(R.string.stats))
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .defaultPlaceholder(visible = uiState.isLoading),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatValue(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.top_ranked),
                    value = uiState.mediaDetails?.rankText().orEmpty(),
                    icon = R.drawable.ic_round_bar_chart_24,
                )
                StatValue(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.users_scores),
                    value = uiState.mediaDetails?.numScoringUsers?.format() ?: UNKNOWN_CHAR,
                    icon = R.drawable.ic_round_thumbs_up_down_24,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatValue(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.members),
                    value = uiState.mediaDetails?.numListUsers?.format() ?: UNKNOWN_CHAR,
                    icon = R.drawable.ic_round_group_24,
                )
                StatValue(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.popularity),
                    value = "# ${uiState.mediaDetails?.popularity}",
                    icon = R.drawable.ic_round_trending_up_24,
                )
            }
        }
    }
}

@Composable
private fun StatValue(
    label: String,
    value: String,
    icon: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.padding(10.dp).size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MediaMoreInfoSection(uiState: MediaDetailsUiState) {
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
}

@Composable
private fun MediaAlternativeTitlesSection(uiState: MediaDetailsUiState) {
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaCharactersSection(
    uiState: MediaDetailsUiState,
    event: MediaDetailsEvent?,
) {
    val context = LocalContext.current

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
                        modifier = Modifier.width(140.dp),
                        subtitle = {
                            Text(
                                text = item.role?.localized().orEmpty(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
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
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        } else {
            FilledTonalButton(
                onClick = { event?.getCharacters() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = stringResource(R.string.view_characters), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MediaThemesSection(uiState: MediaDetailsUiState) {
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
}

@Composable
private fun MediaRelatedMediaSection(
    uiState: MediaDetailsUiState,
    navActionManager: NavActionManager,
) {
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
                            modifier = Modifier.padding(top = 4.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = item.relationType.localized(),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
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
}

@Composable
private fun MediaStatusDistributionSection(uiState: MediaDetailsUiState) {
    (uiState.mediaDetails as? AnimeDetails)?.statistics?.status?.toStats()?.let { stats ->
        InfoTitle(text = stringResource(R.string.status_distribution))
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                HorizontalStatsBar(stats = stats)
            }
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
