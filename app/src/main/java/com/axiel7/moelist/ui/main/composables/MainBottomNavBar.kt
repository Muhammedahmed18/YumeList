package com.axiel7.moelist.ui.main.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.axiel7.moelist.ui.base.BottomDestination
import com.axiel7.moelist.ui.theme.Motion

/** Top corners of the navigation bar. The bottom stays square: it runs under the gesture area. */
private val NavBarShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

/**
 * Phone navigation bar: the Material 3 Expressive [ShortNavigationBar] (pill-shaped active
 * indicator, spring motion, proper touch targets, ripple and accessibility semantics), with its
 * top corners rounded.
 *
 * The bar keeps its size and only slides down out of view when hidden, so the screen's bottom
 * padding never changes and content doesn't jump.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainBottomNavBar(
    navController: NavController,
    navBackStackEntry: NavBackStackEntry?,
    isVisible: Boolean,
    onItemSelected: (Int) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val hidden by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = Motion.spatial(),
        label = "bottomBarSlide",
    )

    ShortNavigationBar(
        modifier = Modifier
            .graphicsLayer { translationY = size.height * hidden }
            .clip(NavBarShape)
            // Hidden bar: not focusable / readable by TalkBack
            .then(if (isVisible) Modifier else Modifier.clearAndSetSemantics {}),
    ) {
        BottomDestination.values.forEachIndexed { index, dest ->
            val isSelected = navBackStackEntry?.destination?.hierarchy?.any {
                it.hasRoute(dest.route::class)
            } == true

            ShortNavigationBarItem(
                selected = isSelected,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (!isSelected) {
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
                        // Outlined when unselected, filled when selected (same for every tab)
                        imageVector = if (isSelected) dest.iconSelected else dest.icon,
                        // The label below already names the item for TalkBack
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
