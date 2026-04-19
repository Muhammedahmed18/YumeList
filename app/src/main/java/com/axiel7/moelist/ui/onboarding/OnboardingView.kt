package com.axiel7.moelist.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.login.openLoginUrl
import com.axiel7.moelist.ui.main.MainViewModel
import com.axiel7.moelist.ui.main.SessionStatus
import com.axiel7.moelist.ui.theme.MoeListTheme
import kotlinx.coroutines.launch

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

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val theme by viewModel.theme.collectAsState()
    val useBlackColors by viewModel.useBlackColors.collectAsState(false)
    val useMonochrome by viewModel.useMonochrome.collectAsState(false)
    
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

    if (currentStep == -1 || (isLoggingIn && currentStep != 3)) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        OnboardingContent(
            currentStep = currentStep,
            isLoggedIn = isLoggedIn,
            isLoggingIn = isLoggingIn,
            theme = theme,
            useBlackColors = useBlackColors,
            useMonochrome = useMonochrome,
            profilePicture = profilePicture,
            username = username,
            onStepChange = { currentStep = it },
            onThemeChange = viewModel::setTheme,
            onBlackColorsChange = viewModel::setUseBlackColors,
            onMonochromeChange = viewModel::setUseMonochrome,
            onLoginClick = {
                scope.launch {
                    val url = viewModel.generateLoginUrl()
                    context.openLoginUrl(url, false)
                }
            },
            onFinished = onFinished
        )
    }
}

@Composable
fun OnboardingContent(
    currentStep: Int,
    isLoggedIn: Boolean,
    isLoggingIn: Boolean,
    theme: ThemeStyle,
    useBlackColors: Boolean,
    useMonochrome: Boolean,
    profilePicture: String?,
    username: String?,
    onStepChange: (Int) -> Unit,
    onThemeChange: (ThemeStyle) -> Unit,
    onBlackColorsChange: (Boolean) -> Unit,
    onMonochromeChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onFinished: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (currentStep < 3) {
                        FilledIconButton(
                            onClick = { onStepChange(currentStep + 1) },
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_round_arrow_forward_24),
                                contentDescription = "Next",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = onFinished,
                            modifier = Modifier.height(56.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            enabled = !isLoggingIn
                        ) {
                            Text(if (isLoggedIn) "Finished" else "Skip")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(
                                    if (isLoggedIn) R.drawable.round_check_24 else R.drawable.ic_round_arrow_forward_24
                                ),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        ) { padding ->
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    1 -> WelcomeStep(
                        modifier = Modifier.padding(padding)
                    )
                    2 -> AppearanceStep(
                        modifier = Modifier.padding(padding),
                        theme = theme,
                        useBlackColors = useBlackColors,
                        useMonochrome = useMonochrome,
                        onThemeChange = onThemeChange,
                        onBlackColorsChange = onBlackColorsChange,
                        onMonochromeChange = onMonochromeChange
                    )
                    3 -> LoginStep(
                        modifier = Modifier.padding(padding),
                        isLoggedIn = isLoggedIn,
                        isLoggingIn = isLoggingIn,
                        profilePicture = profilePicture,
                        username = username,
                        onLoginClick = onLoginClick
                    )
                }
            }
        }

        if (currentStep > 1 && !isLoggedIn && !isLoggingIn) {
            Surface(
                modifier = Modifier.statusBarsPadding()
                    .padding(16.dp)
                    .size(48.dp)
                    .align(Alignment.TopStart),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                onClick = { onStepChange(currentStep - 1) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeStep(
    modifier: Modifier = Modifier
) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { show = true }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = show,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_yumelist_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxSize()
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "YumeList",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Your ultimate anime & manga companion",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        val features = listOf(
            Triple("Track Your List", "Manage your anime and manga list effortlessly with MyAnimeList integration.", MaterialTheme.colorScheme.primaryContainer),
            Triple("Discover New Favorites", "Explore seasonal anime, top-rated manga, and get personalized recommendations.", MaterialTheme.colorScheme.secondaryContainer),
            Triple("Beautiful UI", "Enjoy a clean, modern interface with customizable themes and OLED support.", MaterialTheme.colorScheme.tertiaryContainer)
        )

        features.forEachIndexed { index, feature ->
            AnimatedVisibility(
                visible = show,
                enter = fadeIn(tween(600, delayMillis = 200 + (index * 100))) +
                        slideInHorizontally(tween(600, delayMillis = 200 + (index * 100))) { 40 }
            ) {
                FeatureItem(
                    title = feature.first,
                    description = feature.second,
                    containerColor = feature.third,
                    contentColor = if (index == 0) MaterialTheme.colorScheme.onPrimaryContainer
                                  else if (index == 1) MaterialTheme.colorScheme.onSecondaryContainer
                                  else MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun FeatureItem(
    title: String,
    description: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ThemePreviewMockup(
    theme: ThemeStyle,
    useBlackColors: Boolean,
    useMonochrome: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = when (theme) {
        ThemeStyle.LIGHT -> false
        ThemeStyle.DARK -> true
        ThemeStyle.FOLLOW_SYSTEM -> isSystemInDarkTheme()
    }

    Surface(
        modifier = modifier
            .width(160.dp)
            .height(280.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        MoeListTheme(
            darkTheme = isDark,
            useBlackColors = useBlackColors,
            useMonochrome = useMonochrome,
            dynamicColor = false
        ) {
            Scaffold(
                topBar = {
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(20.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Box(modifier = Modifier.width(40.dp).height(8.dp).background(MaterialTheme.colorScheme.outlineVariant, CircleShape))
                        }
                    }
                },
                bottomBar = {
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(3) {
                                Box(modifier = Modifier.size(16.dp).background(if (it == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, CircleShape))
                            }
                        }
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(4) { index ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(if (index == 0) 60.dp else 34.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.width(if (index == 0) 80.dp else 40.dp).height(6.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), CircleShape))
                                Box(modifier = Modifier.width(if (index == 0) 60.dp else 30.dp).height(6.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), CircleShape))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppearanceStep(
    modifier: Modifier = Modifier,
    theme: ThemeStyle,
    useBlackColors: Boolean,
    useMonochrome: Boolean,
    onThemeChange: (ThemeStyle) -> Unit,
    onBlackColorsChange: (Boolean) -> Unit,
    onMonochromeChange: (Boolean) -> Unit
) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { show = true }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = show,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -20 }
        ) {
            ThemePreviewMockup(
                theme = theme,
                useBlackColors = useBlackColors,
                useMonochrome = useMonochrome,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        AnimatedVisibility(
            visible = show,
            enter = fadeIn(tween(600, 200)) + slideInVertically(tween(600, 200)) { 20 }
        ) {
            Column {
                SectionHeader(icon = Icons.Default.Settings, title = "Appearance")

                Text(
                    text = "Personalize your experience",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThemeCard(
                        label = "System",
                        iconRes = R.drawable.ic_round_settings_24,
                        selected = theme == ThemeStyle.FOLLOW_SYSTEM,
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeChange(ThemeStyle.FOLLOW_SYSTEM) }
                    )
                    ThemeCard(
                        label = "Light",
                        iconRes = R.drawable.ic_round_home_24,
                        selected = theme == ThemeStyle.LIGHT,
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeChange(ThemeStyle.LIGHT) }
                    )
                    ThemeCard(
                        label = "Dark",
                        iconRes = R.drawable.ic_round_color_lens_24,
                        selected = theme == ThemeStyle.DARK,
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeChange(ThemeStyle.DARK) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(
            visible = show,
            enter = fadeIn(tween(600, 400)) + slideInVertically(tween(600, 400)) { 20 }
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_round_refresh_24),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("OLED / Black Variant", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text("Pure black background", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = useBlackColors, onCheckedChange = onBlackColorsChange)
                    }

                    AnimatedVisibility(visible = useBlackColors) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_round_color_lens_24),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Monochrome Theme", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text("Black and white style", style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(checked = useMonochrome, onCheckedChange = onMonochromeChange)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeCard(
    label: String,
    iconRes: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = tween(300),
        label = "theme_card_scale"
    )

    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(100.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun LoginStep(
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean,
    isLoggingIn: Boolean = false,
    profilePicture: String?,
    username: String?,
    onLoginClick: () -> Unit
) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { show = true }

    val targetState = when {
        isLoggedIn -> "success"
        isLoggingIn -> "loading"
        else -> "login"
    }

    AnimatedContent(
        targetState = targetState,
        label = "login_state_transition",
        transitionSpec = {
            fadeIn(tween(500)) + scaleIn(initialScale = 0.92f, animationSpec = tween(500)) togetherWith
                    fadeOut(tween(500))
        }
    ) { state ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state == "success" || state == "loading") {
                // Success/Loading UI
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(160.dp)
                            .padding(8.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(4.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        if (state == "success") {
                            val context = LocalContext.current
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(profilePicture)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.ic_round_person_24),
                                error = painterResource(R.drawable.ic_round_person_24)
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-8).dp, y = (-8).dp)
                    ) {
                        if (state == "success") {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(8.dp)
                            )
                        } else {
                            CircularProgressIndicator(
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (state == "success") "You're all set!" else "Syncing your list...",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (state == "success") {
                    Text(
                        text = "Welcome back, $username",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (state == "success")
                        "Your list is synced and ready. Let's start tracking your favorites!"
                        else "Fetching your profile and updating your local database.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

            } else {
                // Login UI
                AnimatedVisibility(
                    visible = show,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -20 }
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = show,
                    enter = fadeIn(tween(600, 100)) + slideInVertically(tween(600, 100)) { 20 }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Sync Your World",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Connect your MyAnimeList account to unlock the full potential of YumeList.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val benefits = listOf(
                        R.drawable.ic_round_refresh_24 to "Auto-sync your list updates",
                        R.drawable.ic_round_star_16 to "Sync your scores and progress",
                        R.drawable.ic_round_person_24 to "Access your profile and stats"
                    )
                    benefits.forEachIndexed { index, benefit ->
                        AnimatedVisibility(
                            visible = show,
                            enter = fadeIn(tween(600, 300 + (index * 100))) +
                                    slideInHorizontally(tween(600, 300 + (index * 100))) { 20 }
                        ) {
                            LoginBenefitItem(icon = benefit.first, text = benefit.second)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                AnimatedVisibility(
                    visible = show,
                    enter = fadeIn(tween(600, 600)) + slideInVertically(tween(600, 600)) { 20 }
                ) {
                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_open_in_browser), contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Login with MyAnimeList",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginBenefitItem(icon: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingWelcomePreview() {
    MoeListTheme {
        OnboardingContent(
            currentStep = 1,
            isLoggedIn = false,
            isLoggingIn = false,
            theme = ThemeStyle.FOLLOW_SYSTEM,
            useBlackColors = false,
            useMonochrome = false,
            profilePicture = null,
            username = null,
            onStepChange = {},
            onThemeChange = {},
            onBlackColorsChange = {},
            onMonochromeChange = {},
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
            useBlackColors = false,
            useMonochrome = false,
            profilePicture = null,
            username = null,
            onStepChange = {},
            onThemeChange = {},
            onBlackColorsChange = {},
            onMonochromeChange = {},
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
            useBlackColors = false,
            useMonochrome = false,
            profilePicture = null,
            username = null,
            onStepChange = {},
            onThemeChange = {},
            onBlackColorsChange = {},
            onMonochromeChange = {},
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
            useBlackColors = false,
            useMonochrome = false,
            profilePicture = "https://myanimelist.net/images/userimages/1.jpg",
            username = "Axiel7",
            onStepChange = {},
            onThemeChange = {},
            onBlackColorsChange = {},
            onMonochromeChange = {},
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
            useBlackColors = true,
            useMonochrome = false,
            profilePicture = null,
            username = null,
            onStepChange = {},
            onThemeChange = {},
            onBlackColorsChange = {},
            onMonochromeChange = {},
            onLoginClick = {},
            onFinished = {}
        )
    }
}
