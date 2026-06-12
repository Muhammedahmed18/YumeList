package com.axiel7.moelist.ui.main.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.widthIn(max = 420.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(0.dp)
            )
        }
    }
}

@Composable
private fun FloatingNavItem(
    destination: BottomDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val pillColor = MaterialTheme.colorScheme.secondaryContainer
    val selectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

    val pillAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "PillAlpha",
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) selectedContentColor else unselectedColor,
        animationSpec = tween(durationMillis = 200),
        label = "IconColor",
    )

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(pillColor.copy(alpha = pillAlpha))
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isSelected) destination.iconSelected else destination.icon,
                contentDescription = stringResource(destination.title),
                tint = iconColor,
                modifier = Modifier.size(22.dp),
            )
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(tween(200)) + expandHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                    expandFrom = Alignment.Start,
                ),
                exit = fadeOut(tween(150)) + shrinkHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    shrinkTowards = Alignment.Start,
                ),
            ) {
                Row {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(destination.title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = selectedContentColor,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}