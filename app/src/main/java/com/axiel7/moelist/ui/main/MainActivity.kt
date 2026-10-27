package com.axiel7.moelist.ui.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.BottomDestination.Companion.isBottomDestination
import com.axiel7.moelist.ui.base.BottomDestination.Companion.toBottomDestinationIndex
import com.axiel7.moelist.ui.base.TabletMode
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.base.navigation.NavActionManager.Companion.rememberNavActionManager
import com.axiel7.moelist.ui.base.navigation.Route
import com.axiel7.moelist.ui.composables.LocalSnackbarHostState
import com.axiel7.moelist.ui.composables.AppSnackbarHost
import com.axiel7.moelist.ui.composables.LocalSnackbarOffset
import com.axiel7.moelist.ui.composables.SnackbarOffsetState
import com.axiel7.moelist.ui.main.composables.MainBottomNavBar
import com.axiel7.moelist.ui.main.composables.MainNavigationRail
import com.axiel7.moelist.ui.onboarding.OnboardingView
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.MOELIST_PAGELINK
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : AppCompatActivity() {

    val viewModel by viewModel<MainViewModel>()

    /** Null until preferences are loaded. The splash screen stays on screen while it's null. */
    private var startupState by mutableStateOf<StartupState?>(null)
    private var lastTabOpened = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { startupState == null }

        checkLoginIntent(intent)

        val mediaIdAndType = findMediaIdAndTypeFromIntent()
        val mediaId = mediaIdAndType?.first
        val mediaTypeArg = mediaIdAndType?.second

        val cached = viewModel.cachedStartupState
        if (cached != null) {
            // Activity recreated (e.g. dark mode toggled): reuse what the ViewModel already loaded
            lastTabOpened = findLastTabOpened(cached)
            startupState = cached
        } else {
            // Cold start: load preferences in a coroutine (no main-thread blocking before the first frame)
            lifecycleScope.launch {
                val state = viewModel.loadStartupState()
                lastTabOpened = findLastTabOpened(state)
                startupState = state
            }
        }

        setContent {
            val startup = startupState
            if (startup != null) {
                AppRoot(
                    startup = startup,
                    mediaId = mediaId,
                    mediaTypeArg = mediaTypeArg,
                )
            }
        }
    }

    @Composable
    private fun AppRoot(
        startup: StartupState,
        mediaId: Int?,
        mediaTypeArg: String?,
    ) {
        val theme by viewModel.theme.collectAsStateWithLifecycle(initialValue = startup.theme)
        val pureBlack by viewModel.pureBlack.collectAsStateWithLifecycle(initialValue = startup.pureBlack)
        val colorPalette by viewModel.colorPalette.collectAsStateWithLifecycle(
            initialValue = startup.colorPalette
        )
        val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle(
            initialValue = startup.isOnboardingCompleted
        )
        val isDark = when (theme) {
            ThemeStyle.FOLLOW_SYSTEM -> isSystemInDarkTheme()
            ThemeStyle.LIGHT -> false
            ThemeStyle.DARK -> true
        }

        val navController = rememberNavController()
        val navActionManager = rememberNavActionManager(navController)

        val tabletMode by viewModel.tabletMode.collectAsStateWithLifecycle(startup.tabletMode)
        val windowSizeClass = calculateWindowSizeClass(this)
        val isCompactScreen = when (tabletMode) {
            TabletMode.AUTO -> windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
            TabletMode.ALWAYS -> false
            TabletMode.LANDSCAPE -> LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE
            TabletMode.NEVER -> true
        }

        val accessToken by viewModel.accessToken.collectAsStateWithLifecycle(App.accessToken)
        val isLoggedIn = !accessToken.isNullOrEmpty()
        val useListTabs by viewModel.useListTabs.collectAsStateWithLifecycle()
        val profilePicture by viewModel.profilePicture.collectAsStateWithLifecycle()
        val pinnedNavBar by viewModel.pinnedNavBar.collectAsStateWithLifecycle(false)

        MoeListTheme(
            darkTheme = isDark,
            // Pure black whenever the app is dark (Dark mode, or System at night)
            useBlackColors = isDark && pureBlack,
            colorPalette = colorPalette,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                if (!isOnboardingCompleted) {
                    OnboardingView(
                        viewModel = viewModel,
                        onFinished = viewModel::completeOnboarding
                    )
                } else {
                    MainView(
                        isCompactScreen = isCompactScreen,
                        isLoggedIn = isLoggedIn,
                        useListTabs = useListTabs,
                        navController = navController,
                        navActionManager = navActionManager,
                        lastTabOpened = lastTabOpened,
                        saveLastTab = viewModel::saveLastTab,
                        pinnedNavBar = pinnedNavBar,
                        profilePicture = profilePicture,
                    )
                }

                DisposableEffect(isDark) {
                    this@MainActivity.enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT,
                        ) { isDark },
                        navigationBarStyle = SystemBarStyle.auto(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT,
                        ) { isDark },
                    )
                    onDispose {}
                }
            }
        }

        LaunchedEffect(mediaId) {
            if (mediaId != null && mediaTypeArg != null) {
                val mediaType = MediaType.valueOf(mediaTypeArg)
                navActionManager.toMediaDetails(mediaType, mediaId)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkLoginIntent(intent)
    }

    private fun checkLoginIntent(intent: Intent?) {
        if (intent?.data?.toString()?.startsWith(MOELIST_PAGELINK) == true) {
            intent.data?.let { viewModel.parseIntentData(it) }
        }
    }

    private fun findLastTabOpened(startup: StartupState): Int {
        val fromIntentOrSetting =
            intent.action?.toBottomDestinationIndex()
                ?: startup.startTab?.value?.toBottomDestinationIndex()

        return if (fromIntentOrSetting == null) {
            startup.lastTab
        } else { // opened from intent or start tab setting
            viewModel.saveLastTab(fromIntentOrSetting)
            fromIntentOrSetting
        }
    }

    private fun findMediaIdAndTypeFromIntent(): Pair<Int, String>? {
        if (intent.action == "details") {
            val mediaId = intent.getIntExtra("media_id", 0)
            val mediaType = intent.getStringExtra("media_type")?.uppercase()
            if (mediaId != 0 && mediaType != null) return mediaId to mediaType
        } else if (intent.data != null) {
            val malSchemeIndex = intent.dataString?.indexOf("myanimelist.net")
            if (malSchemeIndex != null && malSchemeIndex != -1) {
                val isMainDetails = intent.data?.pathSegments?.any {
                    when (it) {
                        "character", "episode", "video", "reviews", "stacks", "news", "forum",
                        "clubs", "moreinfo" -> true
                        else -> false
                    }
                } == false
                if (!isMainDetails) {
                    intent.dataString?.let { openLink(it) }
                    return null
                }

                val linkSplit = intent.dataString?.substring(malSchemeIndex)?.split('/').orEmpty()
                val mediaType = linkSplit.getOrNull(1)?.uppercase()
                val mediaId = linkSplit.getOrNull(2)?.toIntOrNull()
                if (mediaType != null && mediaId != null) return mediaId to mediaType
            }
        }
        return null
    }
}

@Composable
fun MainView(
    isCompactScreen: Boolean,
    isLoggedIn: Boolean,
    useListTabs: Boolean,
    navController: NavHostController,
    navActionManager: NavActionManager,
    lastTabOpened: Int,
    saveLastTab: (Int) -> Unit,
    pinnedNavBar: Boolean,
    profilePicture: String?,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    // Screens can lift snackbars above their own bottom controls (see ProvideSnackbarOffset)
    val snackbarOffset = remember { SnackbarOffsetState() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val isBottomDestination by remember {
        derivedStateOf { navBackStackEntry?.isBottomDestination() == true }
    }

    // Step 1: Logic to show sort icon and handle clicks
    val isUserListTab by remember {
        derivedStateOf {
            navBackStackEntry?.destination?.run {
                hasRoute<Route.Tab.Anime>() || hasRoute<Route.Tab.Manga>() || hasRoute<Route.UserList>()
            } == true
        }
    }
    var sortTrigger by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Hoisted in-place search state for HomeView. Lives here so the bottom nav can hide
    // while expanded and BackHandler/system back work consistently.
    var searchActive by rememberSaveable { mutableStateOf(false) }

    // Hide bottom nav while the Home search bar is expanded
    val isBottomBarVisible = (isBottomDestination || pinnedNavBar) && !searchActive

    CompositionLocalProvider(
        LocalSnackbarHostState provides snackbarHostState,
        LocalSnackbarOffset provides snackbarOffset,
    ) {
    Scaffold(
        bottomBar = {
            if (isCompactScreen) {
                MainBottomNavBar(
                    navController = navController,
                    navBackStackEntry = navBackStackEntry,
                    // Hide bottom nav while the Home search bar is expanded
                    isVisible = isBottomBarVisible,
                    onItemSelected = saveLastTab,
                )
            }
        },
        snackbarHost = {
            // Above the bottom bar when it's shown; otherwise clear the system navigation bar
            // ourselves (this Scaffold only applies horizontal insets)
            AppSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .then(
                        if (isCompactScreen && isBottomBarVisible) Modifier
                        else Modifier.navigationBarsPadding()
                    )
                    .padding(bottom = snackbarOffset.bottom)
            )
        },
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { padding ->
        if (!isCompactScreen) {
            Row(
                modifier = Modifier
                    .padding(padding)
                    // The rail applies its own system-bar insets; don't let them double up
                    .consumeWindowInsets(padding)
            ) {
                MainNavigationRail(
                    navController = navController,
                    onItemSelected = saveLastTab,
                )
                MainNavigation(
                    navController = navController,
                    navActionManager = navActionManager,
                    lastTabOpened = lastTabOpened,
                    isLoggedIn = isLoggedIn,
                    isCompactScreen = false,
                    useListTabs = useListTabs,
                    modifier = Modifier.statusBarsPadding(),
                    padding = PaddingValues(),
                    onSortClickTrigger = { sortTrigger = it },
                    searchActive = searchActive,
                    onSearchActiveChange = { searchActive = it },
                    // Was missing on tablets, so Home showed the default avatar there
                    profilePicture = profilePicture,
                )
            }
        } else {
            MainNavigation(
                navController = navController,
                navActionManager = navActionManager,
                lastTabOpened = lastTabOpened,
                isLoggedIn = isLoggedIn,
                isCompactScreen = true,
                useListTabs = useListTabs,
                modifier = Modifier.padding(
                    start = padding.calculateStartPadding(LocalLayoutDirection.current),
                    end = padding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = if (pinnedNavBar) padding.calculateBottomPadding() else 0.dp,
                ),
                padding = PaddingValues(
                    start = padding.calculateStartPadding(LocalLayoutDirection.current),
                    end = padding.calculateEndPadding(LocalLayoutDirection.current),
                    top = if (isUserListTab) 0.dp else padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                ),
                barOnAllScreens = pinnedNavBar,
                onSortClickTrigger = { sortTrigger = it },
                searchActive = searchActive,
                onSearchActiveChange = { searchActive = it },
                profilePicture = profilePicture,
            )
        }
    }
    } // CompositionLocalProvider
}