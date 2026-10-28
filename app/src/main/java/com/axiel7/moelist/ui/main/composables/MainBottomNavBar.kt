package com.axiel7.moelist.ui.main.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.axiel7.moelist.ui.base.BottomDestination
import com.axiel7.moelist.ui.base.navigation.Route
import com.axiel7.moelist.ui.theme.Motion

private val BarHeight = 56.dp
private val ItemHeight = 40.dp
private val IconSize = 22.dp
private val BarBottomMargin = 12.dp
private val BarTopMargin = 8.dp

/**
 * Space the floating bar covers above the system navigation bar. Screens under the bar add this
 * (plus the navigation bar inset) as bottom *content* padding, so lists scroll behind the bar
 * and their last item can still scroll above it.
 */
val FloatingNavBarHeight: Dp = BarTopMargin + BarHeight + BarBottomMargin

/**
 * Phone navigation, drawn on top of the content: a compact, centered floating pill with the main
 * tabs, plus a separate circular "More" button beside it.
 *
 * - Unselected tabs are icon-only; the selected tab expands into a tinted pill with its label.
 * - Every element springs on press (Material 3 Expressive spatial motion).
 * - When hidden (scrolling, search, keyboard) it slides down out of view and isn't readable by
 *   TalkBack. It never changes the layout, so nothing jumps.
 */
@Composable
fun MainBottomNavBar(
    navController: NavController,
    navBackStackEntry: NavBackStackEntry?,
    isVisible: Boolean,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val hidden by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = Motion.spatial(),
        label = "bottomBarSlide",
    )

    fun isSelected(dest: BottomDestination) =
        navBackStackEntry?.destination?.hierarchy?.any { it.hasRoute(dest.route::class) } == true

    fun onClick(dest: BottomDestination) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (isSelected(dest)) {
            // Tapping "More" again opens Settings (kept from the old bar)
            if (dest == BottomDestination.More) navController.navigate(Route.Settings)
        } else {
            onItemSelected(BottomDestination.values.indexOf(dest))
            navController.navigate(dest.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Row(
        modifier = modifier
            .graphicsLayer { translationY = size.height * hidden }
            .then(if (isVisible) Modifier else Modifier.clearAndSetSemantics {})
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(top = BarTopMargin, bottom = BarBottomMargin),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // ── Main tabs pill (hugs its content, centered by the caller) ──
        Surface(
            modifier = Modifier.height(BarHeight),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 2.dp,
            shadowElevation = 4.dp,
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .selectableGroup(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                BottomDestination.values
                    .filter { it != BottomDestination.More }
                    .forEach { dest ->
                        FloatingNavItem(
                            destination = dest,
                            selected = isSelected(dest),
                            onClick = { onClick(dest) },
                        )
                    }
            }
        }

        // ── Separate circular "More" button ──
        MoreButton(
            selected = isSelected(BottomDestination.More),
            onClick = { onClick(BottomDestination.More) },
        )
    }
}

@Composable
private fun FloatingNavItem(
    destination: BottomDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val title = stringResource(destination.title)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = Motion.fastSpatial(),
        label = "navItemPress",
    )
    val container by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        animationSpec = Motion.effects(),
        label = "navItemContainer",
    )
    val content by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = Motion.effects(),
        label = "navItemContent",
    )

    Row(
        modifier = Modifier
            .height(ItemHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(container)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = ripple(),
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (selected) destination.iconSelected else destination.icon,
            // When the label is hidden, the icon has to name the tab for TalkBack
            contentDescription = if (selected) null else title,
            tint = content,
            modifier = Modifier.size(IconSize),
        )
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(Motion.spatial()) + fadeIn(Motion.effects()),
            exit = shrinkHorizontally(Motion.spatial()) + fadeOut(Motion.fastEffects()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(6.dp))
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

@Composable
private fun MoreButton(
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = Motion.fastSpatial(),
        label = "morePress",
    )
    val container by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = Motion.effects(),
        label = "moreContainer",
    )
    val content by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = Motion.effects(),
        label = "moreContent",
    )

    Surface(
        selected = selected,
        onClick = onClick,
        modifier = Modifier
            .size(BarHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = CircleShape,
        color = container,
        contentColor = content,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        interactionSource = interactionSource,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = BottomDestination.More.icon,
                contentDescription = stringResource(BottomDestination.More.title),
                modifier = Modifier.size(IconSize),
            )
        }
    }
}
