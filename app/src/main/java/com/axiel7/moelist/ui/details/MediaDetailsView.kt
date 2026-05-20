package com.axiel7.moelist.ui.details

import android.content.ClipData
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.anime.RelatedAnime
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.media.BaseRelated
import com.axiel7.moelist.data.model.media.Character
import com.axiel7.moelist.data.model.media.MediaFormat
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.Stat
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_BIG_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_BIG_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.details.composables.MediaDetailsTopAppBar
import com.axiel7.moelist.ui.details.composables.MediaInfoView
import com.axiel7.moelist.ui.details.composables.MusicStreamingSheet
import com.axiel7.moelist.ui.editmedia.EditMediaSheet
import com.axiel7.moelist.utils.CHARACTER_URL
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.DateUtils.parseDateAndLocalize
import com.axiel7.moelist.utils.NumExtensions.format
import com.axiel7.moelist.utils.StringExtensions.toStringOrNull
import com.axiel7.moelist.utils.UNKNOWN_CHAR
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

// ============================================
// SPACING TOKENS
// ============================================
private val PageHorizontalPadding = 24.dp
private val SectionVerticalSpacing = 20.dp
private val CardPadding = 20.dp
private val CardRadius = 24.dp
private val ButtonHeight = 48.dp
private val ButtonRadius = 24.dp

// ============================================
// MAIN ENTRY POINT
// ============================================
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

// ============================================
// ROOT CONTENT
// ============================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    fun hideSheet(onComplete: () -> Unit = {}) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            showSheet = false
            onComplete()
        }
    }

    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    var isSynopsisExpanded by remember { mutableStateOf(false) }
    val maxLinesSynopsis by remember {
        derivedStateOf { if (isSynopsisExpanded) Int.MAX_VALUE else 6 }
    }

    if (showSheet && uiState.mediaInfo != null) {
        EditMediaSheet(
            sheetState = sheetState,
            mediaInfo = uiState.mediaInfo!!,
            myListStatus = uiState.myListStatus,
            bottomPadding = bottomBarPadding,
            onEdited = { status, removed ->
                hideSheet {
                    event?.onChangedMyListStatus(status, removed)
                }
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
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = padding.calculateBottomPadding() + 24.dp),
                verticalArrangement = Arrangement.spacedBy(SectionVerticalSpacing)
            ) {
                MediaDetailsSections(
                    uiState = uiState,
                    event = event,
                    isLoggedIn = isLoggedIn,
                    navActionManager = navActionManager,
                    isSynopsisExpanded = isSynopsisExpanded,
                    maxLinesSynopsis = maxLinesSynopsis,
                    onToggleSynopsis = { isSynopsisExpanded = !isSynopsisExpanded },
                    onShowEditSheet = { showSheet = true }
                )
            }

            MediaDetailsTopAppBar(
                uiState = uiState,
                event = event,
                navigateBack = dropUnlessResumed { navActionManager.goBack() },
                scrollBehavior = topAppBarScrollBehavior,
            )
        }
    }
}

// ============================================
// SECTIONS ORCHESTRATOR
// ============================================
@Composable
private fun MediaDetailsSections(
    uiState: MediaDetailsUiState,
    event: MediaDetailsEvent?,
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
    isSynopsisExpanded: Boolean,
    maxLinesSynopsis: Int,
    onToggleSynopsis: () -> Unit,
    onShowEditSheet: () -> Unit,
) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MediaHeaderSection(
            uiState = uiState,
            navActionManager = navActionManager,
        )

        MediaActionDock(
            uiState = uiState,
            isLoggedIn = isLoggedIn,
            onEditClick = onShowEditSheet,
            onShareClick = { },
            onMalClick = { context.openLink(uiState.mediaDetails?.malUrl.orEmpty()) }
        )

        MediaGenresSection(uiState = uiState)
    }

    MediaSynopsisSection(
        uiState = uiState,
        isSynopsisExpanded = isSynopsisExpanded,
        maxLinesSynopsis = maxLinesSynopsis,
        onToggleSynopsis = onToggleSynopsis,
    )
    MediaDetailsInfoCard(uiState = uiState)
    MediaFranchiseTimelineSection(
        uiState = uiState,
        navActionManager = navActionManager
    )
    MediaCharactersSection(
        uiState = uiState,
        event = event,
    )
    MediaThemesSection(uiState = uiState)
    MediaStatsSection(uiState = uiState)
}

// ============================================
// HEADER SECTION
// ============================================
@Composable
private fun MediaHeaderSection(
    uiState: MediaDetailsUiState,
    navActionManager: NavActionManager,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = topPadding + 64.dp,
                start = PageHorizontalPadding,
                end = PageHorizontalPadding
            ),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(
                    width = (MEDIA_POSTER_BIG_WIDTH * 0.9).dp,
                    height = (MEDIA_POSTER_BIG_HEIGHT * 0.9).dp
                )
                .defaultPlaceholder(visible = uiState.isLoading)
                .clip(RoundedCornerShape(20.dp))
                .clickable(onClick = dropUnlessResumed {
                    if (uiState.picturesUrls.isNotEmpty()) {
                        navActionManager.toFullPoster(uiState.picturesUrls)
                    }
                })
        ) {
            MediaPoster(
                url = uiState.mediaDetails?.mainPicture?.large,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val title = uiState.mediaDetails?.userPreferredTitle().orEmpty()
            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultPlaceholder(visible = uiState.isLoading)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (title.isNotEmpty()) {
                            scope.launch {
                                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("title", title)))
                            }
                            context.showToast(R.string.copied)
                        }
                    },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 28.sp
            )

            val totalCount = when (val media = uiState.mediaDetails) {
                is AnimeDetails -> media.numEpisodes?.takeIf { it > 0 }?.toString()
                is MangaDetails -> media.numChapters?.takeIf { it > 0 }?.toString()
                else -> null
            }

            val countLabel = if (uiState.isAnime) {
                if (totalCount == "1") stringResource(R.string.episode) else stringResource(R.string.episodes)
            } else {
                if (totalCount == "1") stringResource(R.string.chapter) else stringResource(R.string.chapters)
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetadataBadge(
                    text = uiState.mediaDetails?.mediaFormat?.localized() ?: "??",
                    isLoading = uiState.isLoading
                )
                MetadataBadge(
                    text = "$countLabel (${totalCount ?: "-"})",
                    isLoading = uiState.isLoading
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!uiState.hideScore) {
                    ModernScoreDisplay(
                        score = uiState.mediaDetails?.mean ?: 0f,
                        isLoading = uiState.isLoading
                    )
                }
                MetadataBadge(
                    text = uiState.mediaDetails?.status?.localized() ?: "Loading",
                    isLoading = uiState.isLoading,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun ModernScoreDisplay(
    score: Float,
    isLoading: Boolean,
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = if (isLoading) " " else score.toStringOrNull() ?: "??",
            modifier = Modifier.defaultPlaceholder(visible = isLoading),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "/10",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

@Composable
private fun MetadataBadge(
    text: String,
    isLoading: Boolean,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        modifier = Modifier.defaultPlaceholder(visible = isLoading),
        shape = CircleShape,
        color = containerColor,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

// ============================================
// ACTION DOCK
// ============================================
@Composable
private fun MediaActionDock(
    uiState: MediaDetailsUiState,
    isLoggedIn: Boolean,
    onEditClick: () -> Unit,
    onShareClick: () -> Unit,
    onMalClick: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PageHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = {
                if (isLoggedIn) onEditClick()
                else context.showToast(R.string.please_login_to_use_this_feature)
            },
            modifier = Modifier
                .weight(1f)
                .height(ButtonHeight),
            shape = RoundedCornerShape(ButtonRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                painter = painterResource(
                    if (uiState.isNewEntry) R.drawable.ic_round_add_24
                    else R.drawable.ic_round_edit_24
                ),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            val btnText = if (uiState.isNewEntry) stringResource(R.string.add)
            else uiState.mediaDetails?.myListStatus?.status?.localized() ?: stringResource(R.string.edit)
            Text(
                text = btnText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        ActionCircleButton(
            icon = R.drawable.ic_open_in_browser,
            onClick = onMalClick
        )

        ActionCircleButton(
            icon = R.drawable.round_share_24,
            onClick = onShareClick
        )
    }
}

@Composable
private fun ActionCircleButton(
    icon: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(ButtonHeight),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ============================================
// GENRES SECTION
// ============================================
@Composable
private fun MediaGenresSection(uiState: MediaDetailsUiState) {
    val genres = uiState.mediaDetails?.genres ?: return
    if (genres.isEmpty()) return

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PageHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        genres.forEach { genre ->
            Surface(
                onClick = { },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                )
            ) {
                Text(
                    text = genre.localized(),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

// ============================================
// SYNOPSIS SECTION
// ============================================
@Composable
private fun MediaSynopsisSection(
    uiState: MediaDetailsUiState,
    isSynopsisExpanded: Boolean,
    maxLinesSynopsis: Int,
    onToggleSynopsis: () -> Unit,
) {
    val synopsisAndBackground = uiState.mediaDetails?.synopsisAndBackground()
    if (uiState.isLoading || !synopsisAndBackground.isNullOrEmpty()) {
        Column(
            modifier = Modifier
                .padding(horizontal = PageHorizontalPadding)
        ) {
            Text(
                text = stringResource(R.string.synopsis),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                SelectionContainer {
                    Text(
                        text = synopsisAndBackground
                            ?: AnnotatedString(stringResource(R.string.lorem_ipsun)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultPlaceholder(visible = uiState.isLoading)
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 28.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = maxLinesSynopsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }
            }

            if (!uiState.isLoading) {
                TextButton(
                    onClick = { onToggleSynopsis() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = if (isSynopsisExpanded) "Show Less" else "Read More",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ============================================
// MERGED INFO CARD
// ============================================
@Composable
private fun MediaDetailsInfoCard(uiState: MediaDetailsUiState) {
    val englishTitle = uiState.mediaDetails?.alternativeTitles?.en
    val japaneseTitle = uiState.mediaDetails?.alternativeTitles?.ja
    val synonyms = uiState.mediaDetails?.alternativeTitles?.synonyms
    val hasAltTitles = !englishTitle.isNullOrBlank() || !japaneseTitle.isNullOrBlank() || !synonyms.isNullOrEmpty()

    Surface(
        modifier = Modifier
            .padding(horizontal = PageHorizontalPadding)
            .fillMaxWidth(),
        shape = RoundedCornerShape(CardRadius),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(CardPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.more_info),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (uiState.mediaDetails is AnimeDetails) {
                Row(modifier = Modifier.fillMaxWidth()) {
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
                }
            } else if (uiState.mediaDetails is MangaDetails) {
                val volumes = uiState.mediaDetails.numVolumes
                MediaInfoView(
                    title = stringResource(R.string.volumes),
                    info = if (volumes == null || volumes == 0) "-" else volumes.toString(),
                    icon = R.drawable.ic_round_book_24,
                    modifier = Modifier.fillMaxWidth()
                )
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

            if (hasAltTitles) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Text(
                    text = stringResource(R.string.title_language),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

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

// ============================================
// FRANCHISE TIMELINE
// ============================================
@Composable
private fun MediaFranchiseTimelineSection(
    uiState: MediaDetailsUiState,
    navActionManager: NavActionManager
) {
    val coreRelated = uiState.coreRelatedMedia
    if (coreRelated.isNotEmpty()) {
        Column {
            Text(
                text = stringResource(R.string.franchise_timeline),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = PageHorizontalPadding, vertical = 16.dp)
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = PageHorizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(coreRelated) { index, item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TimelineItem(
                            item = item,
                            isCurrent = item.node.id == uiState.mediaDetails?.id,
                            onClick = {
                                val mediaType = if (item is RelatedAnime) MediaType.ANIME else MediaType.MANGA
                                navActionManager.toMediaDetails(mediaType, item.node.id)
                            }
                        )

                        if (index < coreRelated.size - 1) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .size(4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.outlineVariant,
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineItem(
    item: BaseRelated,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            AsyncImage(
                model = item.node.mainPicture?.medium,
                contentDescription = null,
                modifier = Modifier
                    .size(width = 90.dp, height = 120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = if (isCurrent) 2.dp else 1.dp,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentScale = ContentScale.Crop
            )
            if (isCurrent) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                        .size(10.dp)
                ) { }
            }
        }
        Text(
            text = item.node.userPreferredTitle(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = if (isCurrent) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
        ) {
            Text(
                text = item.relationType.localized(),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================
// CHARACTERS SECTION
// ============================================
@Composable
private fun MediaCharactersSection(
    uiState: MediaDetailsUiState,
    event: MediaDetailsEvent?,
) {
    val context = LocalContext.current

    if (uiState.isAnime) {
        Column {
            Text(
                text = stringResource(R.string.characters),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = PageHorizontalPadding, vertical = 16.dp)
            )
            if (uiState.characters.isNotEmpty() || uiState.isLoadingCharacters) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = PageHorizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(
                        items = uiState.characters,
                        key = { it.node.id },
                        contentType = { it }
                    ) { item ->
                        CharacterItem(
                            character = item,
                            onClick = { context.openLink(CHARACTER_URL + item.node.id) }
                        )
                    }
                    if (uiState.isLoadingCharacters) {
                        item {
                            Box(
                                modifier = Modifier.size(96.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            } else {
                FilledTonalButton(
                    onClick = { event?.getCharacters() },
                    modifier = Modifier.padding(horizontal = PageHorizontalPadding),
                    shape = RoundedCornerShape(ButtonRadius)
                ) {
                    Text(
                        text = stringResource(R.string.view_characters),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun CharacterItem(
    character: Character,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(96.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box {
            AsyncImage(
                model = character.node.mainPicture?.medium,
                contentDescription = null,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(16.dp)
                    ),
                contentScale = ContentScale.Crop
            )
            character.role?.let { role ->
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                ) {
                    Text(
                        text = role.localized(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        Text(
            text = character.fullName(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ============================================
// MUSIC THEMES
// ============================================
@Composable
private fun MediaThemesSection(uiState: MediaDetailsUiState) {
    var showOpeningSheet by remember { mutableStateOf(false) }
    var showEndingSheet by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<String?>(null) }
    var showMusicSheet by remember { mutableStateOf(false) }

    if (uiState.mediaDetails is AnimeDetails) {
        Column {
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

            Text(
                text = stringResource(R.string.music_themes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = PageHorizontalPadding, vertical = 16.dp)
            )

            val openingThemes = uiState.mediaDetails.openingThemes.orEmpty()
            if (openingThemes.isNotEmpty()) {
                ThemePlayerCard(
                    title = stringResource(R.string.opening),
                    count = openingThemes.size,
                    onClick = { showOpeningSheet = true }
                )
            }

            val endingThemes = uiState.mediaDetails.endingThemes.orEmpty()
            if (endingThemes.isNotEmpty()) {
                ThemePlayerCard(
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
                    onThemeClick = { song: String ->
                        selectedSong = song
                        showMusicSheet = true
                    }
                )
            }

            if (showEndingSheet) {
                ThemeListSheet(
                    title = stringResource(R.string.ending),
                    themes = endingThemes.map { it.text },
                    onDismiss = { showEndingSheet = false },
                    onThemeClick = { song: String ->
                        selectedSong = song
                        showMusicSheet = true
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemePlayerCard(
    title: String,
    count: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PageHorizontalPadding, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Rounded.MusicNote),
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
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
private fun ThemeListSheet(
    title: String,
    themes: List<String>,
    onDismiss: () -> Unit,
    onThemeClick: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            themes.forEach { theme ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onThemeClick(theme)
                            onDismiss()
                        }
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Rounded.MusicNote),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = theme,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

// ============================================
// STATS SECTION
// ============================================
@Composable
private fun MediaStatsSection(uiState: MediaDetailsUiState) {
    Column {
        Text(
            text = stringResource(R.string.stats),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = PageHorizontalPadding, vertical = 16.dp)
        )

        Surface(
            modifier = Modifier
                .padding(horizontal = PageHorizontalPadding)
                .fillMaxWidth()
                .defaultPlaceholder(visible = uiState.isLoading),
            shape = RoundedCornerShape(CardRadius),
            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(CardPadding),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricItem(
                        label = stringResource(R.string.top_ranked),
                        value = uiState.mediaDetails?.rankText().orEmpty(),
                        icon = R.drawable.ic_round_bar_chart_24
                    )

                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )

                    MetricItem(
                        label = stringResource(R.string.popularity),
                        value = "#${uiState.mediaDetails?.popularity}",
                        icon = R.drawable.ic_round_trending_up_24
                    )

                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )

                    MetricItem(
                        label = stringResource(R.string.members),
                        value = uiState.mediaDetails?.numListUsers?.format() ?: UNKNOWN_CHAR,
                        icon = R.drawable.ic_round_group_24
                    )
                }

                (uiState.mediaDetails as? AnimeDetails)?.statistics?.status?.toStats()?.let { stats ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.status_distribution),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val totalValue = remember(stats) { stats.sumOf { it.value.toDouble() } }.toFloat()

                        stats.forEach { stat ->
                            StatusBarRow(
                                stat = stat,
                                totalValue = totalValue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBarRow(
    stat: Stat<*>,
    totalValue: Float
) {
    val progress = if (totalValue > 0) stat.value / totalValue else 0f
    val percentage = (progress * 100).toInt()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(stat.type.primaryColor(), CircleShape)
        )

        Text(
            text = stat.type.localized(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = stat.type.primaryColor(),
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                drawStopIndicator = {}
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stat.value.toInt().format() ?: stat.value.toInt().toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    icon: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ============================================
// PREVIEW
// ============================================
@Preview(showBackground = true)
@Composable
private fun MediaDetailsPreview() {
    val mockAnime = AnimeDetails(
        id = 1,
        title = "Frieren: Beyond Journey's End",
        mainPicture = null,
        alternativeTitles = null,
        startDate = "2023-09-29",
        endDate = "2024-03-22",
        synopsis = "The adventure is over but life goes on for an elf mage beginning to learn what life is all about.",
        mean = 9.39f,
        rank = 1,
        popularity = 10,
        numListUsers = 1000000,
        numScoringUsers = 800000,
        nsfw = "white",
        genres = emptyList(),
        mediaFormat = MediaFormat.TV,
        status = MediaStatus.FINISHED_AIRING,
        numEpisodes = 28,
        startSeason = null,
        broadcast = null,
        source = null,
        averageEpisodeDuration = 1440,
        rating = "pg_13",
        studios = emptyList()
    )

    MaterialTheme {
        MediaDetailsContent(
            uiState = MediaDetailsUiState(mediaDetails = mockAnime, isLoading = false),
            event = null,
            isLoggedIn = true,
            navActionManager = NavActionManager.rememberNavActionManager(rememberNavController())
        )
    }
}
