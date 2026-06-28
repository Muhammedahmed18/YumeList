package com.axiel7.moelist.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
            profilePicture = profilePicture,
            username = username,
            onStepChange = { currentStep = it },
            onThemeChange = viewModel::setTheme,
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
    profilePicture: String?,
    username: String?,
    onStepChange: (Int) -> Unit,
    onThemeChange: (ThemeStyle) -> Unit,
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
                        onThemeChange = onThemeChange,
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
fun AppearanceStep(
    modifier: Modifier = Modifier,
    theme: ThemeStyle,
    onThemeChange: (ThemeStyle) -> Unit,
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Choose Your Theme",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Personalize your experience",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = show,
            enter = fadeIn(tween(600, 200)) + slideInVertically(tween(600, 200)) { 20 }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThemeOptionCard(
                        label = "System",
                        description = "Follows device",
                        swatchColors = listOf(Color(0xFFE8EAF6), Color(0xFF9FA8DA), Color(0xFF1A1A2E)),
                        selected = theme == ThemeStyle.FOLLOW_SYSTEM,
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeChange(ThemeStyle.FOLLOW_SYSTEM) }
                    )
                    ThemeOptionCard(
                        label = "Light",
                        description = "Always bright",
                        swatchColors = listOf(Color(0xFFFFFFFF), Color(0xFFF5F5F5), Color(0xFF6650A4)),
                        selected = theme == ThemeStyle.LIGHT,
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeChange(ThemeStyle.LIGHT) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThemeOptionCard(
                        label = "Dark",
                        description = "Easy on eyes",
                        swatchColors = listOf(Color(0xFF1C1C2E), Color(0xFF2C2C3E), Color(0xFF9F85F7)),
                        selected = theme == ThemeStyle.DARK,
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeChange(ThemeStyle.DARK) }
                    )
                    ThemeOptionCard(
                        label = "AMOLED",
                        description = "Saves battery",
                        swatchColors = listOf(Color(0xFF000000), Color(0xFF111111), Color(0xFF7B61FF)),
                        selected = theme == ThemeStyle.AMOLED,
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeChange(ThemeStyle.AMOLED) }
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeOptionCard(
    label: String,
    description: String,
    swatchColors: List<Color>,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 1f,
        animationSpec = tween(300),
        label = "theme_card_scale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(300),
        label = "theme_card_border"
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = "theme_card_bg"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "theme_card_label"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(140.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            // Color swatch preview
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                swatchColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(width = 20.dp, height = 32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(color)
                    )
                }
            }

            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = labelColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = labelColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
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
