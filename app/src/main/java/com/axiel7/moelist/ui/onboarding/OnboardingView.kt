package com.axiel7.moelist.ui.onboarding

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.composables.preferences.SettingsChoiceCard
import com.axiel7.moelist.ui.composables.preferences.SettingsGroup
import com.axiel7.moelist.ui.composables.preferences.SettingsIcon
import com.axiel7.moelist.ui.composables.preferences.SettingsSwitchItem
import com.axiel7.moelist.ui.composables.preferences.ThemeMiniPreview
import com.axiel7.moelist.ui.composables.preferences.icon
import com.axiel7.moelist.ui.composables.preferences.settingsItemColors
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.PlaylistAdd
import com.axiel7.moelist.ui.icons.rounded.Contrast
import com.axiel7.moelist.ui.icons.rounded.Explore
import com.axiel7.moelist.ui.icons.rounded.Palette
import com.axiel7.moelist.ui.icons.rounded.Person
import com.axiel7.moelist.ui.login.LoginButton
import com.axiel7.moelist.ui.login.openLoginUrl
import com.axiel7.moelist.ui.main.MainViewModel
import com.axiel7.moelist.ui.main.SessionStatus
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.Motion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val STEP_COUNT = 3

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingView(
    viewModel: MainViewModel,
    onFinished: () -> Unit
) {
    val sessionStatus by viewModel.sessionStatus.collectAsState()
    val isLoggedIn = sessionStatus == SessionStatus.LOGGED_IN
    val isLoggingIn = sessionStatus == SessionStatus.LOADING

    // Use -1 as a pending state to avoid defaulting to step 1 while loading
    var currentStep by rememberSaveable { mutableIntStateOf(-1) }
    // True while the login link is being prepared, so the button can't be tapped twice
    var isOpeningLogin by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val theme by viewModel.theme.collectAsState()
    val pureBlack by viewModel.pureBlack.collectAsState()
    val profilePicture by viewModel.profilePicture.collectAsState()
    val username by viewModel.username.collectAsState()

    // Initialize the step once the session status is known
    LaunchedEffect(sessionStatus) {
        if (currentStep == -1 && sessionStatus != SessionStatus.LOADING) {
            currentStep = if (sessionStatus == SessionStatus.LOGGED_IN) 3 else 1
        }
    }

    // Handle jump to success if login happens while onboarding
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && currentStep != 3 && currentStep != -1) {
            currentStep = 3
        }
    }

    // ── Predictive back between steps ──
    // The gesture drives the content's scale/offset live; releasing commits to the previous
    // step, cancelling springs back. Without this, system back would leave onboarding entirely.
    val backProgress = remember { Animatable(0f) }
    var backSwipeEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }
    val backSettleSpec = Motion.spatial<Float>()
    val canGoBack = currentStep > 1 && !isLoggedIn && !isLoggingIn

    PredictiveBackHandler(enabled = canGoBack) { events ->
        try {
            events.collect { event ->
                backSwipeEdge = event.swipeEdge
                backProgress.snapTo(event.progress)
            }
            currentStep -= 1
            scope.launch { backProgress.animateTo(0f, backSettleSpec) }
        } catch (e: CancellationException) {
            scope.launch { backProgress.animateTo(0f, backSettleSpec) }
            throw e
        }
    }

    if (currentStep == -1 || (isLoggingIn && currentStep != 3)) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ContainedLoadingIndicator(modifier = Modifier.size(96.dp))
        }
    } else {
        OnboardingContent(
            currentStep = currentStep,
            isLoggedIn = isLoggedIn,
            isLoggingIn = isLoggingIn,
            isOpeningLogin = isOpeningLogin,
            theme = theme,
            profilePicture = profilePicture,
            username = username,
            onStepChange = { currentStep = it },
            onThemeChange = viewModel::setTheme,
            pureBlack = pureBlack,
            onPureBlackChange = viewModel::setPureBlack,
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
            onFinished = onFinished,
            backProgress = { backProgress.value },
            backSwipeEdge = backSwipeEdge,
        )
    }
}

@Composable
fun OnboardingContent(
    currentStep: Int,
    isLoggedIn: Boolean,
    isLoggingIn: Boolean,
    theme: ThemeStyle,
    profilePicture: String?,
    username: String?,
    onStepChange: (Int) -> Unit,
    onThemeChange: (ThemeStyle) -> Unit,
    onLoginClick: () -> Unit,
    onFinished: () -> Unit,
    isOpeningLogin: Boolean = false,
    pureBlack: Boolean = false,
    onPureBlackChange: (Boolean) -> Unit = {},
    backProgress: () -> Float = { 0f },
    backSwipeEdge: Int = BackEventCompat.EDGE_LEFT,
) {
    // Read here: AnimatedContent's transitionSpec isn't @Composable
    val slideSpec = Motion.spatial<IntOffset>()
    val fadeSpec = Motion.effects<Float>()
    val sizeSpec = Motion.spatial<IntSize>()

    Scaffold(
        bottomBar = {
            OnboardingBottomBar(
                currentStep = currentStep,
                showBack = currentStep > 1 && !isLoggedIn && !isLoggingIn,
                isLoggedIn = isLoggedIn,
                isLoggingIn = isLoggingIn,
                onBack = { onStepChange(currentStep - 1) },
                onNext = { onStepChange(currentStep + 1) },
                onFinished = onFinished,
            )
        }
    ) { padding ->
        StepColumn(
            modifier = Modifier
                .padding(padding)
                .graphicsLayer {
                    // Predictive back: shrink slightly and follow the finger
                    val progress = backProgress()
                    val scale = 1f - 0.08f * progress
                    scaleX = scale
                    scaleY = scale
                    val direction = if (backSwipeEdge == BackEventCompat.EDGE_LEFT) 1f else -1f
                    translationX = direction * 24.dp.toPx() * progress
                }
        ) {
            // One hero that stays on screen and morphs between steps
            OnboardingHero(
                step = currentStep,
                isLoggedIn = isLoggedIn,
                isLoggingIn = isLoggingIn,
                profilePicture = profilePicture,
                modifier = Modifier.revealOnEnter(0)
            )

            Spacer(modifier = Modifier.height(28.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    // M3 shared-axis: a short slide (a third of the width) plus a crossfade.
                    // The size springs too, so the hero glides as the content height changes.
                    val direction = if (targetState > initialState) 1 else -1
                    ((slideInHorizontally(slideSpec) { it * direction / 3 } + fadeIn(fadeSpec)) togetherWith
                        (slideOutHorizontally(slideSpec) { -it * direction / 3 } + fadeOut(fadeSpec)))
                        .using(SizeTransform(clip = false) { _, _ -> sizeSpec })
                },
                contentAlignment = Alignment.TopCenter,
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    1 -> WelcomeStep()
                    2 -> AppearanceStep(
                        theme = theme,
                        onThemeChange = onThemeChange,
                        pureBlack = pureBlack,
                        onPureBlackChange = onPureBlackChange,
                    )
                    3 -> LoginStep(
                        isLoggedIn = isLoggedIn,
                        isLoggingIn = isLoggingIn,
                        isOpeningLogin = isOpeningLogin,
                        username = username,
                        onLoginClick = onLoginClick
                    )
                }
            }
        }
    }
}

// ── Hero ──────────────────────────────────────────────────────────────────────

private enum class HeroContent { LOGO, PALETTE, PERSON, AVATAR }
private enum class HeroBadge { NONE, LOADING, DONE }

/**
 * The persistent hero above every step. Its container morphs Cookie9 → Sunny → Cookie12,
 * spins a little on each change, and resizes/recolours with springs. The content inside stays
 * upright (counter-rotated) and crossfades: logo, palette, person, then the user's avatar.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun OnboardingHero(
    step: Int,
    isLoggedIn: Boolean,
    isLoggingIn: Boolean,
    profilePicture: String?,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val polygons = remember {
        listOf(
            MaterialShapes.Cookie9Sided,
            MaterialShapes.Sunny,
            MaterialShapes.Cookie12Sided,
        ).map { it.normalized() }
    }
    val shape = rememberMorphingShape(polygons, (step - 1).coerceIn(0, polygons.lastIndex))

    val inResult = step == STEP_COUNT && (isLoggedIn || isLoggingIn)
    val content = when {
        step == 1 -> HeroContent.LOGO
        step == 2 -> HeroContent.PALETTE
        isLoggedIn && profilePicture != null -> HeroContent.AVATAR
        else -> HeroContent.PERSON
    }
    val badge = when {
        step != STEP_COUNT -> HeroBadge.NONE
        isLoggedIn -> HeroBadge.DONE
        isLoggingIn -> HeroBadge.LOADING
        else -> HeroBadge.NONE
    }

    val size by animateDpAsState(
        targetValue = when {
            step == 1 -> 120.dp
            step == 2 -> 112.dp
            inResult -> 144.dp
            else -> 104.dp
        },
        animationSpec = Motion.spatial(),
        label = "heroSize"
    )
    val containerColor by animateColorAsState(
        targetValue = when {
            step == 1 -> colors.primary
            step == 2 -> colors.tertiaryContainer
            inResult -> colors.primaryContainer
            else -> colors.secondaryContainer
        },
        animationSpec = Motion.effects(),
        label = "heroColor"
    )
    // Animate the colour rather than the width: a 0.dp border still draws a hairline
    val borderColor by animateColorAsState(
        targetValue = if (inResult) colors.primary else Color.Transparent,
        animationSpec = Motion.effects(),
        label = "heroBorder"
    )
    val rotation by animateFloatAsState(
        targetValue = (step - 1) * 120f,
        animationSpec = Motion.spatial(),
        label = "heroRotation"
    )
    val fadeSpec = Motion.effects<Float>()
    val scaleSpec = Motion.spatial<Float>()

    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotation }
                .clip(shape)
                .background(containerColor)
                .border(4.dp, borderColor, shape),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = content,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = -rotation },
                contentAlignment = Alignment.Center,
                transitionSpec = {
                    (fadeIn(fadeSpec) + scaleIn(scaleSpec, initialScale = 0.8f)) togetherWith fadeOut(fadeSpec)
                },
                label = "heroContent"
            ) { heroContent ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    when (heroContent) {
                        HeroContent.LOGO -> Image(
                            painter = painterResource(R.drawable.ic_yumelist_logo),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(26.dp)
                        )
                        HeroContent.PALETTE -> Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = colors.onTertiaryContainer
                        )
                        HeroContent.PERSON -> Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.size(if (inResult) 64.dp else 52.dp),
                            tint = if (inResult) colors.onPrimaryContainer.copy(alpha = 0.6f)
                            else colors.onSecondaryContainer
                        )
                        HeroContent.AVATAR -> AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(profilePicture)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.ic_round_person_24),
                            error = painterResource(R.drawable.ic_round_person_24)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = badge != HeroBadge.NONE,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-4).dp, y = (-4).dp),
            enter = scaleIn(scaleSpec) + fadeIn(fadeSpec),
            exit = scaleOut(scaleSpec) + fadeOut(fadeSpec),
        ) {
            Surface(
                color = colors.primary,
                contentColor = colors.onPrimary,
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                AnimatedContent(
                    targetState = badge == HeroBadge.DONE,
                    contentAlignment = Alignment.Center,
                    transitionSpec = {
                        (fadeIn(fadeSpec) + scaleIn(scaleSpec, initialScale = 0.6f)) togetherWith fadeOut(fadeSpec)
                    },
                    label = "heroBadge"
                ) { done ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (done) {
                            Icon(
                                painter = painterResource(R.drawable.round_check_24),
                                contentDescription = null,
                            )
                        } else {
                            LoadingIndicator(
                                modifier = Modifier.size(32.dp),
                                color = colors.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Springs between [polygons] whenever [index] changes. If a new target arrives mid-morph,
 * it continues from whichever end it was closer to.
 */
@Composable
private fun rememberMorphingShape(polygons: List<RoundedPolygon>, index: Int): Shape {
    var fromIndex by remember { mutableIntStateOf(index) }
    var toIndex by remember { mutableIntStateOf(index) }
    val progress = remember { Animatable(1f) }
    val spec = Motion.spatial<Float>()

    LaunchedEffect(index) {
        if (index != toIndex) {
            fromIndex = if (progress.value >= 0.5f) toIndex else fromIndex
            toIndex = index
            progress.snapTo(0f)
            progress.animateTo(1f, spec)
        }
    }

    val morph = remember(fromIndex, toIndex) { Morph(polygons[fromIndex], polygons[toIndex]) }
    // A new Shape per frame so clip/border pick up the outline change; the spring's overshoot
    // is left to size and rotation, so the outline itself never over-extrapolates.
    return MorphShape(morph, progress.value.coerceIn(0f, 1f))
}

/** A [Morph] at a fixed [progress], scaled from its normalized 0..1 bounds to the layout size. */
private class MorphShape(
    private val morph: Morph,
    private val progress: Float,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val cubics = morph.asCubics(progress)
        if (cubics.isNotEmpty()) {
            val w = size.width
            val h = size.height
            path.moveTo(cubics.first().anchor0X * w, cubics.first().anchor0Y * h)
            for (cubic in cubics) {
                path.cubicTo(
                    cubic.control0X * w, cubic.control0Y * h,
                    cubic.control1X * w, cubic.control1Y * h,
                    cubic.anchor1X * w, cubic.anchor1Y * h,
                )
            }
            path.close()
        }
        return Outline.Generic(path)
    }
}

// ── Bottom bar ────────────────────────────────────────────────────────────────

private enum class PrimaryAction { NEXT, SKIP, FINISH }

/** Back (text button) · step indicator · one primary button that grows from Next into Finish / Skip. */
@Composable
private fun OnboardingBottomBar(
    currentStep: Int,
    showBack: Boolean,
    isLoggedIn: Boolean,
    isLoggingIn: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onFinished: () -> Unit,
) {
    val fadeSpec = Motion.effects<Float>()
    val isLastStep = currentStep >= STEP_COUNT

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            // Fully qualified: inside Row { Box { } } the RowScope overload is picked otherwise,
            // and Compose's scope marker forbids using the outer Row's receiver here
            androidx.compose.animation.AnimatedVisibility(
                visible = showBack,
                enter = fadeIn(fadeSpec),
                exit = fadeOut(fadeSpec),
            ) {
                TextButton(onClick = onBack, shapes = ButtonDefaults.shapes()) {
                    Text(text = stringResource(R.string.back))
                }
            }
        }

        StepIndicator(currentStep = currentStep)

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd
        ) {
            OnboardingPrimaryButton(
                action = when {
                    !isLastStep -> PrimaryAction.NEXT
                    isLoggedIn -> PrimaryAction.FINISH
                    else -> PrimaryAction.SKIP
                },
                enabled = !(isLastStep && isLoggingIn),
                onClick = if (isLastStep) onFinished else onNext,
            )
        }
    }
}

/**
 * A single button: a round arrow on steps 1–2 that springs wider into a labelled pill on the
 * last step. The pill shape plus press squish come from [ButtonDefaults.shapes]; the size comes
 * from the medium button tokens instead of hard-coded dp.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun OnboardingPrimaryButton(
    action: PrimaryAction,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val height = ButtonDefaults.MediumContainerHeight
    val fadeSpec = Motion.effects<Float>()
    val sizeSpec = Motion.spatial<IntSize>()

    Button(
        onClick = onClick,
        enabled = enabled,
        shapes = ButtonDefaults.shapes(),
        // A non-zero min width also skips Button's own 58dp minimum, so Next is a true circle
        modifier = Modifier
            .height(height)
            .widthIn(min = height),
        contentPadding = PaddingValues(0.dp),
    ) {
        AnimatedContent(
            targetState = action,
            contentAlignment = Alignment.Center,
            transitionSpec = {
                (fadeIn(fadeSpec) togetherWith fadeOut(fadeSpec))
                    .using(SizeTransform { _, _ -> sizeSpec })
            },
            label = "primaryAction"
        ) { target ->
            if (target == PrimaryAction.NEXT) {
                Box(modifier = Modifier.size(height), contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_round_arrow_forward_24),
                        contentDescription = stringResource(R.string.onboarding_next),
                        modifier = Modifier.size(ButtonDefaults.iconSizeFor(height))
                    )
                }
            } else {
                Row(
                    modifier = Modifier.padding(ButtonDefaults.contentPaddingFor(height, hasEndIcon = true)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            if (target == PrimaryAction.FINISH) R.string.finished else R.string.skip
                        ),
                        style = ButtonDefaults.textStyleFor(height),
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.width(ButtonDefaults.iconSpacingFor(height)))
                    Icon(
                        painter = painterResource(
                            if (target == PrimaryAction.FINISH) R.drawable.round_check_24
                            else R.drawable.ic_round_arrow_forward_24
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.iconSizeFor(height))
                    )
                }
            }
        }
    }
}

/** Three dots; the current step stretches into a pill (expressive spring). */
@Composable
private fun StepIndicator(currentStep: Int) {
    val stepDescription = stringResource(R.string.onboarding_step, currentStep, STEP_COUNT)
    Row(
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = stepDescription
        },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..STEP_COUNT) {
            val selected = step == currentStep
            val width by animateDpAsState(
                targetValue = if (selected) 24.dp else 8.dp,
                animationSpec = Motion.fastSpatial(),
                label = "stepWidth"
            )
            val color by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                animationSpec = Motion.fastEffects(),
                label = "stepColor"
            )
            Box(
                modifier = Modifier
                    .size(width = width, height = 8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
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
        delay(index * 70L)
        progress.animateTo(1f, spec)
    }
    return graphicsLayer {
        alpha = progress.value.coerceIn(0f, 1f)
        translationY = (1f - progress.value) * 24.dp.toPx()
    }
}

/** Scrollable, centred column holding the hero and the current step, capped in width for tablets. */
@Composable
private fun StepColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

/** The body of a single step, below the hero. */
@Composable
private fun StepBody(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

/** A non-clickable grouped item: shaped tonal icon, title and description. */
@Composable
private fun InfoItem(
    title: String,
    description: String?,
    icon: Painter,
    iconShape: Shape,
    iconContainer: Color,
    iconContent: Color,
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
) {
    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        modifier = modifier,
        colors = settingsItemColors(),
        leadingContent = {
            SettingsIcon(
                painter = icon,
                containerColor = iconContainer,
                contentColor = iconContent,
                shape = iconShape,
            )
        },
        supportingContent = description?.let { { Text(text = it) } },
    ) {
        Text(text = title, style = MaterialTheme.typography.titleSmallEmphasized)
    }
}

// ── Step 1: Welcome ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WelcomeStep() {
    val colors = MaterialTheme.colorScheme
    val features = listOf(
        FeatureInfo(
            title = stringResource(R.string.onboarding_track_title),
            description = stringResource(R.string.onboarding_track_desc),
            icon = rememberVectorPainter(Icons.AutoMirrored.Rounded.PlaylistAdd),
            shape = MaterialShapes.Cookie4Sided.toShape(),
            container = colors.primaryContainer,
            content = colors.onPrimaryContainer,
        ),
        FeatureInfo(
            title = stringResource(R.string.onboarding_discover_title),
            description = stringResource(R.string.onboarding_discover_desc),
            icon = rememberVectorPainter(Icons.Rounded.Explore),
            shape = MaterialShapes.Flower.toShape(),
            container = colors.secondaryContainer,
            content = colors.onSecondaryContainer,
        ),
        FeatureInfo(
            title = stringResource(R.string.onboarding_ui_title),
            description = stringResource(R.string.onboarding_ui_desc),
            icon = rememberVectorPainter(Icons.Rounded.Palette),
            shape = MaterialShapes.Sunny.toShape(),
            container = colors.tertiaryContainer,
            content = colors.onTertiaryContainer,
        ),
    )

    StepBody {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmallEmphasized,
            textAlign = TextAlign.Center,
            modifier = Modifier.revealOnEnter(1)
        )
        Text(
            text = stringResource(R.string.onboarding_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.revealOnEnter(1)
        )

        Spacer(modifier = Modifier.height(36.dp))

        SettingsGroup {
            features.forEachIndexed { index, feature ->
                InfoItem(
                    title = feature.title,
                    description = feature.description,
                    icon = feature.icon,
                    iconShape = feature.shape,
                    iconContainer = feature.container,
                    iconContent = feature.content,
                    index = index,
                    count = features.size,
                    modifier = Modifier.revealOnEnter(2 + index)
                )
            }
        }
    }
}

private class FeatureInfo(
    val title: String,
    val description: String,
    val icon: Painter,
    val shape: Shape,
    val container: Color,
    val content: Color,
)

// ── Step 2: Appearance ────────────────────────────────────────────────────────

@get:StringRes
private val ThemeStyle.onboardingDescription: Int
    get() = when (this) {
        ThemeStyle.FOLLOW_SYSTEM -> R.string.onboarding_theme_system_desc
        ThemeStyle.LIGHT -> R.string.onboarding_theme_light_desc
        ThemeStyle.DARK -> R.string.onboarding_theme_dark_desc
    }

@Composable
fun AppearanceStep(
    theme: ThemeStyle,
    onThemeChange: (ThemeStyle) -> Unit,
    pureBlack: Boolean = false,
    onPureBlackChange: (Boolean) -> Unit = {},
) {
    StepBody {
        Text(
            text = stringResource(R.string.onboarding_theme_title),
            style = MaterialTheme.typography.headlineSmallEmphasized,
            textAlign = TextAlign.Center,
            modifier = Modifier.revealOnEnter(1)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.onboarding_theme_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.revealOnEnter(1)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // System / Light / Dark side by side, then Pure black as its own switch (same as settings)
        Row(
            modifier = Modifier
                .revealOnEnter(2)
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OnboardingThemeModes.forEach { style ->
                ThemeOptionCard(
                    themeStyle = style,
                    selected = theme == style,
                    pureBlack = pureBlack,
                    onClick = { onThemeChange(style) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val isLight = theme == ThemeStyle.LIGHT
        SettingsSwitchItem(
            title = stringResource(R.string.pure_black_mode),
            subtitle = stringResource(
                if (isLight) R.string.pure_black_dark_only else R.string.pure_black_summary
            ),
            icon = Icons.Rounded.Contrast,
            checked = pureBlack,
            onCheckedChange = onPureBlackChange,
            enabled = !isLight,
            index = 0,
            count = 1,
            modifier = Modifier.revealOnEnter(3),
        )
    }
}

private val OnboardingThemeModes = listOf(ThemeStyle.FOLLOW_SYSTEM, ThemeStyle.LIGHT, ThemeStyle.DARK)

/** Same look as the theme cards in Appearance settings, plus a one-line description. */
@Composable
private fun ThemeOptionCard(
    themeStyle: ThemeStyle,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
) {
    SettingsChoiceCard(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ThemeMiniPreview(
                themeStyle = themeStyle,
                accent = MaterialTheme.colorScheme.primary,
                pureBlack = pureBlack,
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = themeStyle.icon,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(themeStyle.stringRes),
                        style = if (selected) MaterialTheme.typography.labelLargeEmphasized
                        else MaterialTheme.typography.labelLarge,
                        color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                }
                Text(
                    text = stringResource(themeStyle.onboardingDescription),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Step 3: Login ─────────────────────────────────────────────────────────────

private enum class LoginStepState { LOGIN, LOADING, SUCCESS }

/** The avatar, loading badge and check now live in the hero; this is the text and actions below it. */
@Composable
fun LoginStep(
    isLoggedIn: Boolean,
    username: String?,
    onLoginClick: () -> Unit,
    isLoggingIn: Boolean = false,
    isOpeningLogin: Boolean = false,
) {
    val targetState = when {
        isLoggedIn -> LoginStepState.SUCCESS
        isLoggingIn -> LoginStepState.LOADING
        else -> LoginStepState.LOGIN
    }
    val fadeSpec = Motion.effects<Float>()
    val scaleSpec = Motion.spatial<Float>()

    AnimatedContent(
        targetState = targetState,
        contentAlignment = Alignment.TopCenter,
        label = "login_state_transition",
        transitionSpec = {
            (fadeIn(fadeSpec) + scaleIn(scaleSpec, initialScale = 0.92f)) togetherWith fadeOut(fadeSpec)
        }
    ) { state ->
        StepBody {
            if (state == LoginStepState.LOGIN) {
                LoginPrompt(
                    isOpeningLogin = isOpeningLogin,
                    onLoginClick = onLoginClick
                )
            } else {
                LoginResult(
                    success = state == LoginStepState.SUCCESS,
                    username = username
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoginPrompt(
    isOpeningLogin: Boolean,
    onLoginClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Text(
        text = stringResource(R.string.onboarding_login_title),
        style = MaterialTheme.typography.headlineMediumEmphasized,
        textAlign = TextAlign.Center,
        modifier = Modifier.revealOnEnter(1)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.onboarding_login_desc),
        style = MaterialTheme.typography.bodyLarge,
        color = colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .revealOnEnter(1)
            .padding(horizontal = 8.dp)
    )

    Spacer(modifier = Modifier.height(32.dp))

    val benefits = listOf(
        R.drawable.ic_round_refresh_24 to stringResource(R.string.onboarding_benefit_sync),
        R.drawable.ic_round_star_16 to stringResource(R.string.onboarding_benefit_scores),
        R.drawable.ic_round_person_24 to stringResource(R.string.onboarding_benefit_profile)
    )
    SettingsGroup {
        benefits.forEachIndexed { index, (icon, text) ->
            InfoItem(
                title = text,
                description = null,
                icon = painterResource(icon),
                iconShape = CircleShape,
                iconContainer = colors.primaryContainer,
                iconContent = colors.onPrimaryContainer,
                index = index,
                count = benefits.size,
                modifier = Modifier.revealOnEnter(2 + index)
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    LoginButton(
        isLoading = isOpeningLogin,
        onClick = onLoginClick,
        modifier = Modifier.revealOnEnter(5)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoginResult(
    success: Boolean,
    username: String?,
) {
    val colors = MaterialTheme.colorScheme

    Text(
        text = stringResource(if (success) R.string.onboarding_all_set else R.string.onboarding_syncing),
        style = MaterialTheme.typography.headlineMediumEmphasized,
        color = colors.primary,
        textAlign = TextAlign.Center
    )

    if (success) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            // The name can arrive a moment after the token; don't show "null"
            text = if (username.isNullOrBlank()) stringResource(R.string.onboarding_welcome_back)
            else stringResource(R.string.onboarding_welcome_back_name, username),
            style = MaterialTheme.typography.titleLargeEmphasized,
            textAlign = TextAlign.Center
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(
            if (success) R.string.onboarding_synced_desc else R.string.onboarding_syncing_desc
        ),
        style = MaterialTheme.typography.bodyLarge,
        color = colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun OnboardingWelcomePreview() {
    MoeListTheme {
        OnboardingContent(
            currentStep = 1,
            isLoggedIn = false,
            isLoggingIn = false,
            theme = ThemeStyle.FOLLOW_SYSTEM,
            profilePicture = null,
            username = null,
            onStepChange = {},
            onThemeChange = {},
            onLoginClick = {},
            onFinished = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingAppearancePreview() {
    MoeListTheme {
        OnboardingContent(
            currentStep = 2,
            isLoggedIn = false,
            isLoggingIn = false,
            theme = ThemeStyle.FOLLOW_SYSTEM,
            profilePicture = null,
            username = null,
            onStepChange = {},
            onThemeChange = {},
            onLoginClick = {},
            onFinished = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingLoginPreview() {
    MoeListTheme {
        OnboardingContent(
            currentStep = 3,
            isLoggedIn = false,
            isLoggingIn = false,
            theme = ThemeStyle.FOLLOW_SYSTEM,
            profilePicture = null,
            username = null,
            onStepChange = {},
            onThemeChange = {},
            onLoginClick = {},
            onFinished = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingSuccessPreview() {
    MoeListTheme {
        OnboardingContent(
            currentStep = 3,
            isLoggedIn = true,
            isLoggingIn = false,
            theme = ThemeStyle.FOLLOW_SYSTEM,
            profilePicture = "https://myanimelist.net/images/userimages/1.jpg",
            username = "Axiel7",
            onStepChange = {},
            onThemeChange = {},
            onLoginClick = {},
            onFinished = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingDarkThemePreview() {
    MoeListTheme(darkTheme = true) {
        OnboardingContent(
            currentStep = 2,
            isLoggedIn = false,
            isLoggingIn = false,
            theme = ThemeStyle.DARK,
            profilePicture = null,
            username = null,
            onStepChange = {},
            onThemeChange = {},
            onLoginClick = {},
            onFinished = {}
        )
    }
}
