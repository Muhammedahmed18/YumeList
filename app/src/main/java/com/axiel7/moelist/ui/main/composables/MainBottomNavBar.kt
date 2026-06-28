package com.axiel7.moelist.ui.main.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.axiel7.moelist.ui.base.BottomDestination
import com.axiel7.moelist.ui.base.navigation.Route
import kotlinx.coroutines.launch

@Composable
fun MainBottomNavBar(
    navController: NavController,
    navBackStackEntry: NavBackStackEntry?,
    isVisible: Boolean,
    onItemSelected: (Int) -> Unit,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
) {
    val scope = rememberCoroutineScope()

    AnimatedContent(
        targetState = isVisible,
        label = "BottomBarAnimation",
        transitionSpec = {
            slideInVertically(initialOffsetY = { it }) togetherWith
            slideOutVertically(targetOffsetY = { it })
        }
    ) { isVisible ->
        if (isVisible) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.widthIn(max = 480.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        BottomDestination.values.forEachIndexed { index, dest ->
                            val isSelected = navBackStackEntry?.destination?.hierarchy?.any {
                                it.hasRoute(dest.route::class)
                            } == true

                            FloatingNavItem(
                                destination = dest,
                                isSelected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        if (dest == BottomDestination.More) {
                                            navController.navigate(Route.Settings)
                                        }
                                    } else {
                                        scope.launch { topBarOffsetY.animateTo(0f) }
                                        onItemSelected(index)
                                        navController.navigate(dest.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        } else {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            )
        }
    }
}
