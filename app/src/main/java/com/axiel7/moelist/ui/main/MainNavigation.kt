package com.axiel7.moelist.ui.main

import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.axiel7.moelist.ui.composables.LocalNavMotion
import com.axiel7.moelist.ui.composables.NavMotionKind
import com.axiel7.moelist.ui.composables.NavMotionState
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.BottomDestination
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.base.navigation.Route
import com.axiel7.moelist.ui.calendar.CalendarView
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.composables.LocalSharedTransitionScope
import com.axiel7.moelist.ui.composables.ProvideNavAnimatedScope
import com.axiel7.moelist.ui.details.MediaDetailsView
import com.axiel7.moelist.ui.fullposter.FullPosterView
import com.axiel7.moelist.ui.home.HomeView
import com.axiel7.moelist.ui.login.LoginView
import com.axiel7.moelist.ui.more.MoreView
import com.axiel7.moelist.ui.more.about.AboutView
import com.axiel7.moelist.ui.more.credits.CreditsView
import com.axiel7.moelist.ui.more.notifications.NotificationsView
import com.axiel7.moelist.ui.more.settings.SettingsView
import com.axiel7.moelist.ui.more.settings.appearance.AppearanceView
import com.axiel7.moelist.ui.more.settings.content.ContentSettingsView
import com.axiel7.moelist.ui.more.settings.list.ListStyleSettingsView
import com.axiel7.moelist.ui.more.settings.navigation.NavigationSettingsView
import com.axiel7.moelist.ui.profile.ProfileView
import com.axiel7.moelist.ui.ranking.MediaRankingView
import com.axiel7.moelist.ui.season.SeasonChartView
import com.axiel7.moelist.ui.theme.Motion
import com.axiel7.moelist.ui.userlist.UserMediaListWithTabsView
import kotlin.reflect.typeOf

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainNavigation(
    navController: NavHostController,
    navActionManager: NavActionManager,
    lastTabOpened: Int,
    isLoggedIn: Boolean,
    isCompactScreen: Boolean,
    useListTabs: Boolean,
    modifier: Modifier,
    padding: PaddingValues,
    // True when the bottom bar is pinned (shown on every screen, not only the tabs)
    barOnAllScreens: Boolean = false,
    onSortClickTrigger: (() -> Unit) -> Unit = {}, // Added
    searchActive: Boolean = false,
    onSearchActiveChange: (Boolean) -> Unit = {},
    profilePicture: String? = null,
) {
    // NavHost transition lambdas aren't @Composable, so read the theme's motion specs here.
    // Screen slides use the *effects* spring (no overshoot) so full-screen pages never bounce
    // past the screen edge; the expressive bounce is kept for components.
    val screenSlide = Motion.effects<IntOffset>()
    val screenFade = Motion.effects<Float>()
    val tabFade = Motion.fastEffects<Float>()
    val navMotion = remember { NavMotionState() }

    // Reopen the last used tab on top of Home, once per launch (not after rotation / recreation).
    // Skipped if something else already navigated (e.g. a widget opening a details screen).
    var restoredLastTab by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (restoredLastTab) return@LaunchedEffect
        restoredLastTab = true
        val lastTab = BottomDestination.values.getOrNull(lastTabOpened)
        val stillOnHome = navController.currentBackStackEntry?.destination
            ?.hasRoute<Route.Tab.Home>() == true
        if (lastTab != null && lastTab != BottomDestination.Home && stillOnHome) {
            navController.navigate(lastTab.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    // Shared element (hero) transitions, e.g. a poster flying from a list into details.
    // Destinations that take part provide their animation scope via ProvideNavAnimatedScope.
    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this,
            LocalNavMotion provides navMotion,
        ) {
            NavHost(
                navController = navController,
                // Home is always the base of the stack, so back always ends on Home and only Home
                // closes the app. The last used tab is opened on top of it (see below).
                startDestination = BottomDestination.Home.route,
                modifier = modifier,
                // One "depth slide" for every screen: the new page slides in over the old one, which
                // drifts a quarter width under a dim (drawn by ProvideNavAnimatedScope). Back is the
                // exact reverse and follows the finger (predictive back). No fades, so nothing is
                // ever half transparent. Switching tabs (and the full-screen poster viewer) is a
                // crossfade where the screen underneath stays opaque.
                enterTransition = {
                    if (isFadeTransition()) {
                        navMotion.kind = NavMotionKind.Fade
                        fadeIn(animationSpec = tabFade)
                    } else {
                        navMotion.kind = NavMotionKind.Push
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = screenSlide,
                        )
                    }
                },
                exitTransition = {
                    if (isFadeTransition()) ExitTransition.KeepUntilTransitionsFinished
                    else slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = screenSlide,
                        targetOffset = { fullWidth -> fullWidth / 4 },
                    )
                },
                popEnterTransition = {
                    if (isFadeTransition()) {
                        navMotion.kind = NavMotionKind.Fade
                        EnterTransition.None
                    } else {
                        navMotion.kind = NavMotionKind.Pop
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = screenSlide,
                            initialOffset = { fullWidth -> fullWidth / 4 },
                        )
                    }
                },
                popExitTransition = {
                    if (isFadeTransition()) fadeOut(animationSpec = tabFade)
                    else slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = screenSlide,
                    )
                },
            ) {
                composable<Route.Tab.Home> {
                    ProvideNavAnimatedScope {
                        HomeView(
                            isLoggedIn = isLoggedIn,
                            isCompactScreen = isCompactScreen,
                            navActionManager = navActionManager,
                            padding = padding,
                            searchActive = searchActive,
                            onSearchActiveChange = onSearchActiveChange,
                            profilePicture = profilePicture,
                        )
                    }
                }

                composable<Route.Tab.Anime>(
                    typeMap = mapOf(typeOf<MediaType>() to MediaType.navType),
                ) {
                    ProvideNavAnimatedScope {
                        if (!isLoggedIn) {
                            LoginView()
                        } else {
                            UserMediaListWithTabsView(
                                mediaType = MediaType.ANIME,
                                isCompactScreen = isCompactScreen,
                                navActionManager = navActionManager,
                                padding = padding,
                                useListTabs = useListTabs,
                                onSortClickTrigger = onSortClickTrigger
                            )
                        }
                    }
                }

                composable<Route.Tab.Manga>(
                    typeMap = mapOf(typeOf<MediaType>() to MediaType.navType),
                ) {
                    ProvideNavAnimatedScope {
                        if (!isLoggedIn) {
                            LoginView()
                        } else {
                            UserMediaListWithTabsView(
                                mediaType = MediaType.MANGA,
                                isCompactScreen = isCompactScreen,
                                navActionManager = navActionManager,
                                padding = padding,
                                useListTabs = useListTabs,
                                onSortClickTrigger = onSortClickTrigger
                            )
                        }
                    }
                }

                composable<Route.Tab.More> {
                    ProvideNavAnimatedScope {
                        MoreView(
                            navActionManager = navActionManager,
                            padding = padding,
                            isLoggedIn = isLoggedIn
                        )
                    }
                }

                composable<Route.MediaRanking>(
                    typeMap = mapOf(typeOf<MediaType>() to MediaType.navType)
                ) {
                    ProvideNavAnimatedScope {
                        val args = it.toRoute<Route.MediaRanking>()

                        MediaRankingView(
                            mediaType = args.mediaType,
                            isCompactScreen = isCompactScreen,
                            navActionManager = navActionManager,
                        )
                    }
                }

                composable<Route.Calendar> {
                    ProvideNavAnimatedScope {
                        CalendarView(
                            navActionManager = navActionManager
                        )
                    }
                }

                composable<Route.SeasonChart> {
                    ProvideNavAnimatedScope {
                        SeasonChartView(
                            navActionManager = navActionManager
                        )
                    }
                }

                composable<Route.Settings> {
                    ProvideNavAnimatedScope {
                        SettingsView(
                            navActionManager = navActionManager
                        )
                    }
                }

                composable<Route.Appearance> {
                    ProvideNavAnimatedScope {
                        AppearanceView(
                            navActionManager = navActionManager
                        )
                    }
                }

                composable<Route.ListStyleSettings> {
                    ProvideNavAnimatedScope {
                        ListStyleSettingsView(
                            navActionManager = navActionManager
                        )
                    }
                }

                composable<Route.NavigationSettings> {
                    ProvideNavAnimatedScope {
                        NavigationSettingsView(
                            navActionManager = navActionManager
                        )
                    }
                }

                composable<Route.ContentSettings> {
                    ProvideNavAnimatedScope {
                        ContentSettingsView(
                            navActionManager = navActionManager
                        )
                    }
                }

                composable<Route.Notifications> {
                    ProvideNavAnimatedScope {
                        NotificationsView(
                            navActionManager = navActionManager
                        )
                    }
                }

                composable<Route.About> {
                    ProvideNavAnimatedScope {
                        AboutView(
                            navActionManager = navActionManager
                        )
                    }
                }

                composable<Route.Credits> {
                    ProvideNavAnimatedScope {
                        CreditsView(
                            navActionManager = navActionManager
                        )
                    }
                }

                composable<Route.MediaDetails>(
                    typeMap = mapOf(typeOf<MediaType>() to MediaType.navType)
                ) {
                    ProvideNavAnimatedScope {
                        val args = it.toRoute<Route.MediaDetails>()
                        MediaDetailsView(
                            isLoggedIn = isLoggedIn,
                            navActionManager = navActionManager,
                            // The poster tapped on the previous screen (shared poster transition)
                            initialPosterUrl = args.posterUrl,
                            initialPosterKey = args.posterKey,
                        )
                    }
                }

                composable<Route.UserList>(
                    typeMap = mapOf(
                        typeOf<MediaType>() to MediaType.navType,
                        typeOf<ListStatus?>() to ListStatus.navType
                    )
                ) {
                    ProvideNavAnimatedScope {
                        val args = it.toRoute<Route.UserList>()
                        UserMediaListWithTabsView(
                            mediaType = args.mediaType,
                            isCompactScreen = isCompactScreen,
                            navActionManager = navActionManager,
                            padding = if (barOnAllScreens) padding else padding.withBottom(
                                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                            ),
                            useListTabs = useListTabs,
                            onSortClickTrigger = onSortClickTrigger
                        )
                    }
                }

                composable<Route.FullPoster> {
                    val args = it.toRoute<Route.FullPoster>()

                    FullPosterView(
                        pictures = args.pictures,
                        navActionManager = navActionManager
                    )
                }

                composable<Route.Profile> {
                    ProvideNavAnimatedScope {
                        if (!isLoggedIn) {
                            DefaultScaffoldWithTopAppBar(
                                title = stringResource(R.string.title_profile),
                                navigateBack = { navController.popBackStack() }
                            ) { padding ->
                                LoginView(modifier = Modifier.padding(padding))
                            }
                        } else {
                            ProfileView(
                                navActionManager = navActionManager
                            )
                        }
                    }
                }
            }
        }
    }//:NavHost
}

/** Tab switches and the full-screen poster viewer crossfade; everything else uses the depth slide. */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.isFadeTransition(): Boolean {
    val from = initialState.destination
    val to = targetState.destination
    return (from.isTab() && to.isTab()) ||
        from.hasRoute<Route.FullPoster>() || to.hasRoute<Route.FullPoster>()
}

private fun NavDestination.isTab(): Boolean =
    hasRoute<Route.Tab.Home>() || hasRoute<Route.Tab.Anime>() ||
        hasRoute<Route.Tab.Manga>() || hasRoute<Route.Tab.More>()

@Composable
private fun PaddingValues.withBottom(bottom: Dp): PaddingValues {
    val dir = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(dir),
        top = calculateTopPadding(),
        end = calculateEndPadding(dir),
        bottom = bottom,
    )
}
