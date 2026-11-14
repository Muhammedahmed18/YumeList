package com.axiel7.moelist.ui.onboarding

import android.os.Build
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import com.axiel7.moelist.ui.composables.preferences.ColorPalettePicker
import com.axiel7.moelist.ui.composables.preferences.ThemeModePicker
import com.axiel7.moelist.ui.composables.preferences.PureBlackChip
import com.axiel7.moelist.ui.composables.preferences.ListStylePicker
import com.axiel7.moelist.ui.icons.rounded.Tab
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.ColorPalette
import com.axiel7.moelist.ui.base.StartTab
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.ui.composables.preferences.SettingsSection
import com.axiel7.moelist.ui.composables.preferences.SettingsSwitchItem
import com.axiel7.moelist.ui.composables.preferences.StartTabPicker
import com.axiel7.moelist.ui.composables.preferences.TitleLanguagePicker
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.MenuBook
import com.axiel7.moelist.ui.icons.filled.Tv
import com.axiel7.moelist.ui.icons.rounded.Check
import com.axiel7.moelist.ui.icons.rounded.Contrast
import com.axiel7.moelist.ui.icons.rounded.Info
import com.axiel7.moelist.ui.icons.rounded.LiveTv
import com.axiel7.moelist.ui.icons.rounded.PlayCircle
import com.axiel7.moelist.ui.icons.rounded.PriorityHigh
import com.axiel7.moelist.ui.icons.rounded.Remove
import com.axiel7.moelist.ui.login.LoginButton
import com.axiel7.moelist.ui.login.openLoginUrl
import com.axiel7.moelist.ui.main.MainViewModel
import com.axiel7.moelist.ui.main.SessionStatus
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.Motion
import com.axiel7.moelist.utils.ContextExtensions.openCustomTab
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import com.axiel7.moelist.ui.theme.ShapeExtraLarge
import com.axiel7.moelist.ui.theme.ShapeLargeIncreased

/*
 * Onboarding: Login → Your stats → Make it yours → Home.
 *
 * - Login: a slowly scrolling, tilted collage of this season's covers (from MyAnimeList) fading
 *   into the background, with "Continue with MyAnimeList" and "Continue as guest".
 * - Your stats: the account coming over, row by row, then a few fun numbers.
 * - Make it yours: theme, colours, start tab, title language, pure black and list tabs.
 *
 * A guest goes Login → Make it yours → Home. If they log in later, only Your stats is shown,
 * once (see MainActivity / DefaultPreferencesRepository.welcomePending).
 */

private enum class OnboardingStep { LOGIN, STATS, PERSONALIZE }

private const val MAL_SIGN_UP_URL = "https://myanimelist.net/register.php"

@Composable
fun OnboardingView(
    viewModel: MainViewModel,
    onFinished: () -> Unit,
    onContinueAsGuest: () -> Unit,
    // A guest who just logged in: they already made their choices, so only the stats are shown
    statsOnly: Boolean = false,
) {
    val onboardingViewModel: OnboardingViewModel = koinViewModel()
    val sessionStatus by viewModel.sessionStatus.collectAsState()
    val isLoggedIn = sessionStatus == SessionStatus.LOGGED_IN
    val isLoggingIn = sessionStatus == SessionStatus.LOADING

    // Null until the session is known, so a logged-in user never sees the login screen flash
    var stepName by rememberSaveable { mutableStateOf<String?>(null) }
    val step = stepName?.let { OnboardingStep.valueOf(it) }
    var isOpeningLogin by remember { mutableStateOf(false) }
    // True when "Make it yours" was opened with "Continue as guest" (no stats step before it)
    var asGuest by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(sessionStatus) {
        if (stepName == null && sessionStatus != SessionStatus.LOADING) {
            stepName = if (isLoggedIn) OnboardingStep.STATS.name else OnboardingStep.LOGIN.name
        }
        // Logged in from the login screen (the redirect came back): on to the stats
        if (isLoggedIn && step == OnboardingStep.LOGIN) {
            asGuest = false
            stepName = OnboardingStep.STATS.name
        }
    }

    // Back from "Make it yours" returns to where it was opened from: the login screen for a
    // guest, the stats otherwise. Back on the stats does nothing (the account is already
    // connected), and on the login screen it leaves the app as usual.
    val personalizeBack: () -> Unit = {
        stepName = if (asGuest) OnboardingStep.LOGIN.name else OnboardingStep.STATS.name
    }
    BackHandler(enabled = step == OnboardingStep.PERSONALIZE, onBack = personalizeBack)

    if (step == null) {
        OnboardingLoading()
        return
    }

    val slideSpec = Motion.spatial<IntOffset>()
    val fadeSpec = Motion.effects<Float>()

    AnimatedContent(
        targetState = step,
        transitionSpec = {
            // M3 shared axis: a short slide (a third of the width) plus a crossfade
            val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
            (slideInHorizontally(slideSpec) { it * direction / 3 } + fadeIn(fadeSpec)) togetherWith
                (slideOutHorizontally(slideSpec) { -it * direction / 3 } + fadeOut(fadeSpec))
        },
        label = "onboardingStep",
    ) { current ->
        when (current) {
            OnboardingStep.LOGIN -> {
                val covers by onboardingViewModel.covers.collectAsStateWithLifecycle()
                LoginStep(
                    covers = covers,
                    isLoading = isOpeningLogin || isLoggingIn,
                    onLoginClick = {
                        isOpeningLogin = true
                        scope.launch {
                            try {
                                val url = viewModel.generateLoginUrl()
                                context.openLoginUrl(url, false)
                            } finally {
                                isOpeningLogin = false
                            }
                        }
                    },
                    // Guests pick their look first, then go to Home
                    onGuestClick = {
                        asGuest = true
                        stepName = OnboardingStep.PERSONALIZE.name
                    },
                    onCreateAccountClick = { context.openCustomTab(MAL_SIGN_UP_URL) },
                )
            }

            OnboardingStep.STATS -> {
                val importState by onboardingViewModel.importState.collectAsStateWithLifecycle()
                val username by viewModel.username.collectAsStateWithLifecycle()
                val profilePicture by viewModel.profilePicture.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { onboardingViewModel.startImport() }
                StatsStep(
                    username = username,
                    profilePicture = profilePicture,
                    importState = importState,
                    onContinue = {
                        if (statsOnly) {
                            onFinished()
                        } else {
                            stepName = OnboardingStep.PERSONALIZE.name
                        }
                    },
                )
            }

            OnboardingStep.PERSONALIZE -> {
                val theme by onboardingViewModel.theme.collectAsStateWithLifecycle()
                val pureBlack by onboardingViewModel.pureBlack.collectAsStateWithLifecycle()
                val colorPalette by onboardingViewModel.colorPalette.collectAsStateWithLifecycle()
                val startTab by onboardingViewModel.startTab.collectAsStateWithLifecycle()
                val titleLanguage by onboardingViewModel.titleLanguage.collectAsStateWithLifecycle()
                val titleSample by onboardingViewModel.titleSample.collectAsStateWithLifecycle()
                val useListTabs by onboardingViewModel.useListTabs.collectAsStateWithLifecycle()
                val nsfw by onboardingViewModel.nsfw.collectAsStateWithLifecycle()
                PersonalizeStep(
                    theme = theme,
                    pureBlack = pureBlack,
                    colorPalette = colorPalette,
                    startTab = startTab,
                    titleLanguage = titleLanguage,
                    titleSample = titleSample,
                    useListTabs = useListTabs,
                    nsfw = nsfw,
                    onThemeChange = { onboardingViewModel.setTheme(it) },
                    onPureBlackChange = { onboardingViewModel.setPureBlack(it) },
                    onColorPaletteChange = { onboardingViewModel.setColorPalette(it) },
                    onStartTabChange = { onboardingViewModel.setStartTab(it) },
                    onTitleLanguageChange = { onboardingViewModel.setTitleLanguage(it) },
                    onUseListTabsChange = { onboardingViewModel.setUseListTabs(it) },
                    onNsfwChange = { onboardingViewModel.setNsfw(it) },
                    onBack = personalizeBack,
                    onDone = if (asGuest && !isLoggedIn) onContinueAsGuest else onFinished,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun OnboardingLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        ContainedLoadingIndicator(modifier = Modifier.size(96.dp))
    }
}

// ── Shared bits ───────────────────────────────────────────────────────────────

/**
 * Fades and lifts the element in with an expressive spring, [index] steps after the screen
 * appears. Done in the draw phase, so the layout never shifts while things appear.
 */
@Composable
private fun Modifier.revealOnEnter(index: Int): Modifier {
    val progress = remember { Animatable(0f) }
    val spec = Motion.spatial<Float>()
    LaunchedEffect(Unit) {
        delay(120L + index * 90L)
        progress.animateTo(1f, spec)
    }
    return graphicsLayer {
        alpha = progress.value.coerceIn(0f, 1f)
        translationY = (1f - progress.value) * 24.dp.toPx()
    }
}

/** Centred, width-capped column for tablets and landscape. */
@Composable
private fun CenteredColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth(),
            content = content,
        )
    }
}

// ── Step 1: Login ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoginStep(
    covers: List<String>,
    isLoading: Boolean,
    onLoginClick: () -> Unit,
    onGuestClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
) {
    val background = MaterialTheme.colorScheme.background

    Box(modifier = Modifier.fillMaxSize().background(background)) {
        CoverCollage(
            covers = covers,
            modifier = Modifier.fillMaxSize(),
        )

        // Fade the art into the background: light at the top (so the logo reads), then fully
        // solid before the headline, so nothing moves behind the text and buttons
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to background.copy(alpha = 0.55f),
                        0.15f to background.copy(alpha = 0.05f),
                        0.32f to background.copy(alpha = 0.2f),
                        0.45f to background.copy(alpha = 0.85f),
                        0.52f to background,
                        1f to background,
                    )
                )
        )

        // Logo
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 20.dp)
                .revealOnEnter(0),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // The logo is a white mark made for the launcher icon, so it needs its own background
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(MaterialShapes.Cookie9Sided.toShape())
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_yumelist_logo),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // Headline, buttons and the sign-up link
        CenteredColumn(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Text(
                text = stringResource(R.string.onboarding_headline),
                style = MaterialTheme.typography.displaySmallEmphasized,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .revealOnEnter(1),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.onboarding_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .revealOnEnter(2),
            )
            Spacer(Modifier.height(32.dp))

            LoginButton(
                isLoading = isLoading,
                onClick = onLoginClick,
                text = stringResource(R.string.continue_with_mal),
                modifier = Modifier.revealOnEnter(3),
            )
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                // Stays styled normally while loading (a disabled button turns grey and
                // see-through); taps are just ignored until the login page opens
                onClick = { if (!isLoading) onGuestClick() },
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .revealOnEnter(4),
                contentPadding = ButtonDefaults.contentPaddingFor(56.dp),
            ) {
                Text(
                    text = stringResource(R.string.continue_as_guest),
                    style = ButtonDefaults.textStyleFor(56.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .revealOnEnter(5),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.new_to_mal),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onCreateAccountClick, shapes = ButtonDefaults.shapes()) {
                    Text(
                        text = stringResource(R.string.create_free_account),
                        style = MaterialTheme.typography.labelLargeEmphasized,
                    )
                }
            }
        }
    }
}

/**
 * Tilted columns of cover art drifting slowly and endlessly, alternate columns in opposite
 * directions. With no covers yet (loading / offline) the cards are soft tonal blocks, so the
 * screen never looks empty. Decorative, so TalkBack skips it.
 */
@Composable
private fun CoverCollage(
    covers: List<String>,
    modifier: Modifier = Modifier,
) {
    val columns = 3
    val cardsPerColumn = 6
    val cardHeight = 210.dp
    val gap = 14.dp
    val cycle = (cardHeight + gap) * cardsPerColumn
    val cyclePx = with(LocalDensity.current) { cycle.toPx() }

    val transition = rememberInfiniteTransition(label = "collage")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 48_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "collageDrift",
    )

    val placeholderColors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
    )

    BoxWithConstraints(
        modifier = modifier.clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        // Wider than the screen and tilted, so the cards run off every edge
        Row(
            modifier = Modifier
                // requiredWidth, not width: width() is capped at the screen, so the tilted
                // columns stopped short and the corners showed the background
                .requiredWidth(maxWidth * 1.45f)
                .fillMaxHeight()
                .graphicsLayer { rotationZ = -11f },
            horizontalArrangement = Arrangement.spacedBy(gap),
        ) {
            repeat(columns) { column ->
                val columnCovers = remember(covers, column) {
                    if (covers.isEmpty()) emptyList()
                    else List(cardsPerColumn) { i -> covers[(column + i * columns) % covers.size] }
                }
                val goingUp = column % 2 == 0
                Column(
                    modifier = Modifier
                        .weight(1f)
                        // Taller than the screen: two copies of the column, for a seamless loop
                        .wrapContentHeight(align = Alignment.Top, unbounded = true)
                        .graphicsLayer {
                            val progress = if (goingUp) drift else 1f - drift
                            // Offset the columns a little so the rows don't line up
                            translationY = -progress * cyclePx - column * cyclePx / 7f
                        },
                    verticalArrangement = Arrangement.spacedBy(gap),
                ) {
                    repeat(2) {
                        repeat(cardsPerColumn) { index ->
                            CollageCard(
                                url = columnCovers.getOrNull(index),
                                placeholder = placeholderColors[(index + column) % placeholderColors.size],
                                height = cardHeight,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollageCard(
    url: String?,
    placeholder: Color,
    height: Dp,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(24.dp))
            .background(placeholder.copy(alpha = 0.6f)),
    ) {
        if (url != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

// ── Step 2: Your stats ────────────────────────────────────────────────────────

/**
 * Sized to fit one phone screen: a short blurred header with the avatar and name side by side,
 * a compact import card and one row of stat tiles. Scrolling is only a fallback for very small
 * screens or very large fonts.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatsStep(
    username: String?,
    profilePicture: String?,
    importState: ImportState,
    onContinue: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val finished = importState.isFinished

    Scaffold(
        containerColor = colors.background,
        // The header draws behind the status bar; the bottom bar handles its own inset
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            CenteredColumn(
                modifier = Modifier
                    .background(colors.background)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.onboarding_background_refresh),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
                Button(
                    onClick = onContinue,
                    enabled = finished,
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    contentPadding = ButtonDefaults.contentPaddingFor(56.dp),
                ) {
                    AnimatedContent(targetState = finished, label = "statsButton") { done ->
                        Text(
                            text = stringResource(
                                if (done) R.string.onboarding_continue else R.string.onboarding_syncing
                            ),
                            style = ButtonDefaults.textStyleFor(56.dp),
                        )
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            StatsHeader(
                heroImage = importState.heroImage,
                username = username,
                profilePicture = profilePicture,
                pulsing = !finished,
            )

            CenteredColumn(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = stringResource(R.string.onboarding_import_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.revealOnEnter(2),
                )
                Spacer(Modifier.height(14.dp))
                ImportCard(
                    state = importState,
                    modifier = Modifier.revealOnEnter(3),
                )
                Spacer(Modifier.height(12.dp))
                StatsStrip(
                    stats = importState.stats,
                    finished = finished,
                    modifier = Modifier.revealOnEnter(4),
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/**
 * The user's top cover, blurred, fading into the background (a gradient if there's none), with
 * the avatar and "Okaeri, name" sitting on its lower edge.
 */
@Composable
private fun StatsHeader(
    heroImage: String?,
    username: String?,
    profilePicture: String?,
    pulsing: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(196.dp)
            .background(
                Brush.linearGradient(
                    listOf(colors.primaryContainer, colors.tertiaryContainer, colors.secondaryContainer)
                )
            ),
    ) {
        if (heroImage != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(heroImage)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    // Blur needs Android 12+; older versions get the dimmed picture
                    .blur(28.dp)
                    .graphicsLayer {
                        alpha = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 0.9f else 0.55f
                    },
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to colors.background.copy(alpha = 0.15f),
                        0.5f to colors.background.copy(alpha = 0.45f),
                        1f to colors.background,
                    )
                )
        )
        CenteredColumn(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(
                    picture = profilePicture,
                    pulsing = pulsing,
                    modifier = Modifier.revealOnEnter(0),
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.revealOnEnter(1)) {
                    Text(
                        text = stringResource(R.string.onboarding_okaeri),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = colors.primary,
                    )
                    Text(
                        // The name can arrive a moment after the token
                        text = username?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.onboarding_welcome_back),
                        style = MaterialTheme.typography.headlineMediumEmphasized,
                        color = colors.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** Avatar in a 12-sided cookie with a soft glow ring that pulses while the import runs. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProfileAvatar(
    picture: String?,
    pulsing: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialShapes.Cookie12Sided.toShape()
    val transition = rememberInfiniteTransition(label = "avatarPulse")
    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "avatarPulseScale",
    )
    val glowScale by animateFloatAsState(
        targetValue = if (pulsing) pulse else 1f,
        animationSpec = Motion.effects(),
        label = "avatarGlow",
    )

    Box(modifier = modifier.size(88.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                }
                .background(
                    Brush.radialGradient(
                        listOf(colors.primary.copy(alpha = 0.45f), Color.Transparent)
                    ),
                    CircleShape,
                )
        )
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(shape)
                .background(colors.primary)
                .padding(3.dp)
                .clip(shape)
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(picture)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_round_person_24),
                error = painterResource(R.drawable.ic_round_person_24),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** "Bringing your list over": one row per part of the account, plus a wavy progress line. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ImportCard(
    state: ImportState,
    modifier: Modifier = Modifier,
) {
    val rows = listOf(state.animeList, state.mangaList, state.watching, state.airing)
    val settled = rows.count {
        it.status == ImportRowStatus.DONE || it.status == ImportRowStatus.FAILED
    }
    val progress by animateFloatAsState(
        targetValue = if (state.isFinished) 1f else settled / (rows.size + 1f),
        animationSpec = Motion.slowEffects(),
        label = "importProgress",
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeExtraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text(
                text = stringResource(R.string.onboarding_import_title),
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            ImportRowItem(
                icon = { Icon(Icons.Filled.Tv, contentDescription = null) },
                label = stringResource(R.string.onboarding_import_anime),
                row = state.animeList,
                valueText = { pluralStringResource(R.plurals.import_entries, it, it) },
            )
            ImportRowItem(
                icon = { Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null) },
                label = stringResource(R.string.onboarding_import_manga),
                row = state.mangaList,
                valueText = { pluralStringResource(R.plurals.import_entries, it, it) },
            )
            ImportRowItem(
                icon = { Icon(Icons.Rounded.PlayCircle, contentDescription = null) },
                label = stringResource(R.string.onboarding_import_watching),
                row = state.watching,
                valueText = { pluralStringResource(R.plurals.import_shows, it, it) },
            )
            ImportRowItem(
                icon = { Icon(Icons.Rounded.LiveTv, contentDescription = null) },
                label = stringResource(R.string.onboarding_import_airing),
                row = state.airing,
                valueText = { pluralStringResource(R.plurals.import_this_week, it, it) },
            )
            Spacer(Modifier.height(10.dp))
            LinearWavyProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ImportRowItem(
    icon: @Composable () -> Unit,
    label: String,
    row: ImportRow,
    valueText: @Composable (Int) -> String,
) {
    val colors = MaterialTheme.colorScheme
    // Numbers count up when their row finishes
    val shown by animateIntAsState(
        targetValue = if (row.status == ImportRowStatus.DONE) row.value else 0,
        animationSpec = tween(durationMillis = 900),
        label = "importCount",
    )
    val iconContainer by animateColorAsState(
        targetValue = when (row.status) {
            ImportRowStatus.DONE -> colors.primaryContainer
            ImportRowStatus.FAILED -> colors.errorContainer
            else -> colors.surfaceContainerHighest
        },
        animationSpec = Motion.effects(),
        label = "importIconContainer",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconContainer),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(targetState = row.status, label = "importIcon") { status ->
                Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                    when (status) {
                        ImportRowStatus.LOADING -> LoadingIndicator(modifier = Modifier.size(28.dp))
                        ImportRowStatus.DONE -> Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = colors.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                        ImportRowStatus.FAILED -> Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = null,
                            tint = colors.onErrorContainer,
                            modifier = Modifier.size(20.dp),
                        )
                        ImportRowStatus.WAITING -> Box(Modifier.size(20.dp)) {
                            CompositionLocalProvider(LocalContentColor provides colors.onSurfaceVariant) {
                                icon()
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = when (row.status) {
                ImportRowStatus.DONE -> valueText(shown)
                ImportRowStatus.FAILED -> "—"
                else -> ""
            },
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
        )
    }
}

/**
 * Four small tiles in one row: days watched, episodes, mean score and completed. The space is
 * there from the start (showing "—"), so nothing jumps when the numbers arrive.
 */
@Composable
private fun StatsStrip(
    stats: QuickStats?,
    finished: Boolean,
    modifier: Modifier = Modifier,
) {
    val number = remember { NumberFormat.getIntegerInstance() }
    val show = finished && stats != null
    val locale = java.util.Locale.getDefault()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatTile(
            value = if (show) String.format(locale, "%.1f", stats!!.daysWatched) else "—",
            label = stringResource(R.string.days),
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        StatTile(
            value = if (show) number.format(stats!!.episodes) else "—",
            label = stringResource(R.string.episodes),
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        StatTile(
            value = if (show && stats!!.meanScore > 0f) String.format(locale, "%.2f", stats.meanScore) else "—",
            label = stringResource(R.string.mean_score),
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        StatTile(
            value = if (show) number.format(stats!!.completed) else "—",
            label = stringResource(R.string.completed),
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

@Composable
private fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = ShapeLargeIncreased,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AnimatedContent(targetState = value, label = "statValue") { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    maxLines = 1,
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ── Step 3: Make it yours ─────────────────────────────────────────────────────

/** A compact section title, so the whole step fits on one screen. */
@Composable
private fun StepSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLargeEmphasized,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, top = 10.dp, bottom = 6.dp),
    )
}

/**
 * Theme, colours, start tab, title language, pure black and list tabs, sized to fit one phone
 * screen (scrolling is only a fallback for very small screens or very large fonts).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PersonalizeStep(
    theme: ThemeStyle,
    pureBlack: Boolean,
    colorPalette: ColorPalette,
    startTab: StartTab,
    titleLanguage: TitleLanguage,
    titleSample: TitleSample?,
    useListTabs: Boolean,
    nsfw: Boolean,
    onThemeChange: (ThemeStyle) -> Unit,
    onPureBlackChange: (Boolean) -> Unit,
    onColorPaletteChange: (ColorPalette) -> Unit,
    onStartTabChange: (StartTab) -> Unit,
    onTitleLanguageChange: (TitleLanguage) -> Unit,
    onUseListTabsChange: (Boolean) -> Unit,
    onNsfwChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            CenteredColumn(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onBack, shapes = ButtonDefaults.shapes()) {
                        Text(text = stringResource(R.string.back))
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = onDone,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.heightIn(min = 56.dp),
                        contentPadding = ButtonDefaults.contentPaddingFor(56.dp, hasEndIcon = true),
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_looks_good),
                            style = ButtonDefaults.textStyleFor(56.dp),
                        )
                        Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(56.dp)))
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.iconSizeFor(56.dp)),
                        )
                    }
                }
            }
        },
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // On short phones the two one-line explanations step aside so everything still fits
            val roomy = maxHeight >= 620.dp
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // Only a fallback for very small screens or very large fonts
                    .verticalScroll(rememberScrollState()),
            ) {
                CenteredColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.onboarding_make_it_yours),
                        style = MaterialTheme.typography.headlineSmallEmphasized,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .revealOnEnter(0),
                    )

                    Column(modifier = Modifier.revealOnEnter(1)) {
                        // Theme, with the Pure black chip on the title line (dark / system only)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 40.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            StepSectionTitle(stringResource(R.string.theme_section))
                            Spacer(Modifier.weight(1f))
                            PureBlackChip(
                                theme = theme,
                                pureBlack = pureBlack,
                                onPureBlackChange = onPureBlackChange,
                            )
                        }
                        ThemeModePicker(selected = theme, onSelect = onThemeChange)

                        StepSectionTitle(stringResource(R.string.colors_section))
                        ColorPalettePicker(selected = colorPalette, onSelect = onColorPaletteChange)
                    }

                    Column(modifier = Modifier.revealOnEnter(2)) {
                        StepSectionTitle(stringResource(R.string.start_on))
                        StartTabPicker(selected = startTab, onSelect = onStartTabChange)

                        StepSectionTitle(stringResource(R.string.title_language))
                        if (titleSample != null) {
                            TitleLanguagePicker(
                                selected = titleLanguage,
                                onSelect = onTitleLanguageChange,
                                romajiSample = titleSample.romaji,
                                englishSample = titleSample.english,
                                japaneseSample = titleSample.japanese,
                                showSample = roomy,
                            )
                        } else {
                            TitleLanguagePicker(
                                selected = titleLanguage,
                                onSelect = onTitleLanguageChange,
                                showSample = roomy,
                            )
                        }
                    }

                    Column(modifier = Modifier.revealOnEnter(3)) {
                        StepSectionTitle(stringResource(R.string.list_switch_title))
                        ListStylePicker(
                            useListTabs = useListTabs,
                            onUseListTabsChange = onUseListTabsChange,
                            showDescription = roomy,
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    SettingsSection(modifier = Modifier.revealOnEnter(4)) {
                        item {
                            SettingsSwitchItem(
                                title = stringResource(R.string.show_nsfw),
                                subtitle = stringResource(R.string.nsfw_onboarding_summary),
                                icon = Icons.Rounded.PriorityHigh,
                                checked = nsfw,
                                onCheckedChange = onNsfwChange,
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showSystemUi = true)
@Composable
private fun LoginStepPreview() {
    MoeListTheme(darkTheme = true) {
        LoginStep(
            covers = emptyList(),
            isLoading = false,
            onLoginClick = {},
            onGuestClick = {},
            onCreateAccountClick = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun StatsStepPreview() {
    MoeListTheme(darkTheme = true) {
        StatsStep(
            username = "ahmed02050",
            profilePicture = null,
            importState = ImportState(
                animeList = ImportRow(ImportRowStatus.DONE, 784),
                mangaList = ImportRow(ImportRowStatus.DONE, 96),
                watching = ImportRow(ImportRowStatus.DONE, 14),
                airing = ImportRow(ImportRowStatus.LOADING),
                stats = QuickStats(daysWatched = 128.4f, episodes = 6812, meanScore = 7.8f, completed = 412),
            ),
            onContinue = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PersonalizeStepPreview() {
    MoeListTheme {
        PersonalizeStep(
            theme = ThemeStyle.FOLLOW_SYSTEM,
            pureBlack = false,
            colorPalette = ColorPalette.YUME,
            startTab = StartTab.LAST_USED,
            titleLanguage = TitleLanguage.ROMAJI,
            titleSample = null,
            useListTabs = false,
            nsfw = false,
            onThemeChange = {},
            onPureBlackChange = {},
            onColorPaletteChange = {},
            onStartTabChange = {},
            onTitleLanguageChange = {},
            onUseListTabsChange = {},
            onNsfwChange = {},
            onBack = {},
            onDone = {},
        )
    }
}
