package com.axiel7.moelist.ui.main.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.axiel7.moelist.ui.base.BottomDestination
import com.axiel7.moelist.ui.base.navigation.Route
import com.axiel7.moelist.ui.theme.Motion

private val BarHeight = 64.dp
private val ItemHeight = 48.dp

/**
 * Phone navigation: a floating pill with the main tabs plus a separate circular "More" button.
 *
 * - Unselected tabs are icon-only; the selected tab expands into a highlighted pill with its
 *   icon and label (spring motion from the theme's motion scheme).
 * - The bar keeps its size and only slides down out of view when hidden (search open, or
 *   scrolling down), so the screen's bottom padding never changes and content doesn't jump.
 */
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

    fun isSelected(dest: BottomDestination) =
        navBackStackEntry?.destination?.hierarchy?.any { it.hasRoute(dest.route::class) } == true

    fun onClick(index: Int, dest: BottomDestination) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (isSelected(dest)) {
            // Tapping "More" again opens Settings (kept from the old bar)
            if (dest == BottomDestination.More) navController.navigate(Route.Settings)
        } else {
            onItemSelected(index)
            navController.navigate(dest.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val tabs = BottomDestination.values.filter { it != BottomDestination.More }
    val moreIndex = BottomDestination.values.indexOf(BottomDestination.More)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = size.height * hidden }
            // Hidden bar: not focusable / readable by TalkBack
            .then(if (isVisible) Modifier else Modifier.clearAndSetSemantics {})
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // ── Main tabs pill ──
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(BarHeight),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 6.dp,
            tonalElevation = 3.dp,
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .selectableGroup(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                tabs.forEach { dest ->
                    FloatingNavItem(
                        destination = dest,
                        selected = isSelected(dest),
                        onClick = { onClick(BottomDestination.values.indexOf(dest), dest) },
                    )
                }
            }
        }

        // ── Separate circular "More" button ──
        val moreSelected = isSelected(BottomDestination.More)
        val moreContainer by animateColorAsState(
            targetValue = if (moreSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHigh,
            animationSpec = Motion.effects(),
            label = "moreContainer",
        )
        val moreContent by animateColorAsState(
            targetValue = if (moreSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
            animationSpec = Motion.effects(),
            label = "moreContent",
        )
        Surface(
            selected = moreSelected,
            onClick = { onClick(moreIndex, BottomDestination.More) },
            modifier = Modifier.size(BarHeight),
            shape = CircleShape,
            color = moreContainer,
            contentColor = moreContent,
            shadowElevation = 6.dp,
            tonalElevation = 3.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = BottomDestination.More.icon,
                    contentDescription = stringResource(BottomDestination.More.title),
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    destination: BottomDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val title = stringResource(destination.title)
    val container by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = Motion.effects(),
        label = "navItemContainer",
    )
    val content by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = Motion.effects(),
        label = "navItemContent",
    )

    Row(
        modifier = Modifier
            .height(ItemHeight)
            .clip(CircleShape)
            .background(container)
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (selected) destination.iconSelected else destination.icon,
            // When the label is hidden, the icon has to name the tab for TalkBack
            contentDescription = if (selected) null else title,
            tint = content,
        )
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(Motion.spatial()) + fadeIn(Motion.effects()),
            exit = shrinkHorizontally(Motion.spatial()) + fadeOut(Motion.effects()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    color = content,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
