package com.axiel7.moelist.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.LocalSnackbarHostState
import com.axiel7.moelist.ui.composables.SnackbarType
import com.axiel7.moelist.ui.composables.showAppSnackbar
import com.axiel7.moelist.ui.details.composables.MediaDetailsTopAppBar
import com.axiel7.moelist.ui.details.composables.MusicStreamingSheet
import com.axiel7.moelist.ui.details.sections.DetailsBackdrop
import com.axiel7.moelist.ui.details.sections.DetailsCharacters
import com.axiel7.moelist.ui.details.sections.DetailsGenres
import com.axiel7.moelist.ui.details.sections.DetailsHero
import com.axiel7.moelist.ui.details.sections.DetailsInfo
import com.axiel7.moelist.ui.details.sections.DetailsRelated
import com.axiel7.moelist.ui.details.sections.DetailsStatTiles
import com.axiel7.moelist.ui.details.sections.DetailsStatusDistribution
import com.axiel7.moelist.ui.details.sections.DetailsSynopsis
import com.axiel7.moelist.ui.details.sections.DetailsThemes
import com.axiel7.moelist.ui.details.sections.ThemesSheet
import com.axiel7.moelist.ui.editmedia.EditMediaSheet
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openLink
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun MediaDetailsView(
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
    initialPosterUrl: String? = null,
    initialPosterKey: String? = null,
) {
    val viewModel: MediaDetailsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MediaDetailsContent(
        uiState = uiState,
        event = viewModel,
        isLoggedIn = isLoggedIn,
        navActionManager = navActionManager,
        initialPosterUrl = initialPosterUrl,
        initialPosterKey = initialPosterKey,
    )
}

/**
 * Media details: a blurred poster backdrop behind an M3 Expressive large flexible top app bar,
 * then a lazy list of sections (see the `sections` package). Sections far down the screen are
 * only composed when scrolled into view.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaDetailsContent(
    uiState: MediaDetailsUiState,
    event: MediaDetailsEvent?,
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
    // Poster from the screen we came from: shown right away (and used as the landing spot of
    // the shared poster transition) until the full details have loaded
    initialPosterUrl: String? = null,
    // Shared-transition key of that poster, so the hero flies from exactly the poster tapped
    initialPosterKey: String? = null,
) {
    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Edit sheet
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    fun hideSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
    }

    // Theme sheets (kept here, not in the section, so they survive the section scrolling away)
    var showThemesSheet by remember { mutableStateOf(false) }
    var themesSheetTitle by remember { mutableStateOf("") }
    var themesSheetItems by remember { mutableStateOf<List<String>>(emptyList()) }
    val themesSheetState = rememberModalBottomSheetState()

    // Music streaming (per-song)
    var showMusicSheet by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<String?>(null) }

    // Pre-calculate strings to avoid Context.getString lint in Composable
    val pleaseLoginMessage = stringResource(R.string.please_login_to_use_this_feature)

    fun requireLogin(action: () -> Unit) {
        if (isLoggedIn) action()
        else scope.launch { snackbarHostState.showAppSnackbar(pleaseLoginMessage, type = SnackbarType.INFO) }
    }

    val openEditSheet: () -> Unit = { requireLogin { showSheet = true } }
    val malUrl: () -> String? = {
        uiState.mediaDetails?.let { details ->
            val type = if (uiState.isAnime) "anime" else "manga"
            "https://myanimelist.net/$type/${details.id}"
        }
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
            snackbarHostState.showAppSnackbar(uiState.message, type = SnackbarType.ERROR)
            event?.onMessageDisplayed()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Behind the (transparent while expanded) top app bar and the hero
        DetailsBackdrop(
            imageUrl = uiState.mediaDetails?.mainPicture?.medium ?: initialPosterUrl,
            listState = listState,
        )

        Scaffold(
            modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                MediaDetailsTopAppBar(
                    uiState = uiState,
                    event = event,
                    navigateBack = dropUnlessResumed { navActionManager.goBack() },
                    scrollBehavior = topAppBarScrollBehavior,
                    onOpenClick = { malUrl()?.let { context.openLink(it) } },
                    onShareClick = { malUrl()?.let { context.shareText(it) } },
                    onEditClick = openEditSheet,
                )
            },
        ) { padding ->
            // synopsisAndBackground() is @Composable, so it must be read here and not inside
            // the LazyColumn builder (which isn't a composable scope)
            val hasSynopsis = uiState.isLoading ||
                !uiState.mediaDetails?.synopsisAndBackground().isNullOrEmpty()

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 24.dp
                ),
            ) {
                item(key = "hero", contentType = "hero") {
                    DetailsHero(
                        uiState = uiState,
                        initialPosterUrl = initialPosterUrl,
                        initialPosterKey = initialPosterKey,
                        onPosterClick = {
                            if (uiState.picturesUrls.isNotEmpty())
                                navActionManager.toFullPoster(uiState.picturesUrls)
                        },
                        onEditClick = openEditSheet,
                        onStatusSelected = { status: ListStatus ->
                            requireLogin { event?.setListStatus(status) }
                        },
                    )
                }

                item(key = "stats", contentType = "stats") {
                    DetailsStatTiles(uiState = uiState)
                }

                val genres = uiState.mediaDetails?.genres
                if (!genres.isNullOrEmpty()) {
                    item(key = "genres", contentType = "genres") {
                        DetailsGenres(genres = genres)
                    }
                }

                if (hasSynopsis) {
                    item(key = "synopsis", contentType = "synopsis") {
                        DetailsSynopsis(uiState = uiState)
                    }
                }

                item(key = "info", contentType = "info") {
                    DetailsInfo(uiState = uiState)
                }

                if (uiState.relatedAnime.isNotEmpty() || uiState.relatedManga.isNotEmpty()) {
                    item(key = "related", contentType = "related") {
                        DetailsRelated(uiState = uiState, navActionManager = navActionManager)
                    }
                }

                if (uiState.isAnime) {
                    item(key = "characters", contentType = "characters") {
                        DetailsCharacters(
                            uiState = uiState,
                            onLoadCharacters = { event?.getCharacters() }
                        )
                    }
                }

                (uiState.mediaDetails as? AnimeDetails)?.let { anime ->
                    val openings = anime.openingThemes?.map { it.text }.orEmpty()
                    val endings = anime.endingThemes?.map { it.text }.orEmpty()
                    if (openings.isNotEmpty() || endings.isNotEmpty()) {
                        item(key = "themes", contentType = "themes") {
                            DetailsThemes(
                                openings = openings,
                                endings = endings,
                                onOpenThemes = { title, songs ->
                                    themesSheetTitle = title
                                    themesSheetItems = songs
                                    showThemesSheet = true
                                }
                            )
                        }
                    }
                }

                item(key = "distribution", contentType = "distribution") {
                    DetailsStatusDistribution(uiState = uiState)
                }
            }
        }
    }
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
