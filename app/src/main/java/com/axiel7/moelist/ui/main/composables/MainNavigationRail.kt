package com.axiel7.moelist.ui.main.composables

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.axiel7.moelist.ui.base.BottomDestination
import com.axiel7.moelist.ui.base.navigation.Route

/**
 * Tablet / large-screen navigation: the Material 3 Expressive [WideNavigationRail]
 * (collapsed form), replacing the hand-built floating pill rail. Same destinations and
 * behaviour as [MainBottomNavBar].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainNavigationRail(
    navController: NavController,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val haptic = LocalHapticFeedback.current

    WideNavigationRail(modifier = modifier) {
        BottomDestination.values.forEachIndexed { index, dest ->
            val isSelected = navBackStackEntry?.destination?.hierarchy?.any {
                it.hasRoute(dest.route::class)
            } == true

            WideNavigationRailItem(
                // Collapsed rail: icon on top, label underneath (material3 1.5 requires this)
                railExpanded = false,
                selected = isSelected,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (isSelected) {
                        // Tapping "More" again opens Settings (kept from the old rail)
                        if (dest == BottomDestination.More) {
                            navController.navigate(Route.Settings)
                        }
                    } else {
                        onItemSelected(index)
                        navController.navigate(dest.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) dest.iconSelected else dest.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(
                        text = stringResource(dest.title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}
