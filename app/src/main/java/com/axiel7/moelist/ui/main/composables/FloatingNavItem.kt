package com.axiel7.moelist.ui.main.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.ui.base.BottomDestination

/**
 * A single navigation item rendered as a horizontal pill: icon with the label revealed
 * beside it while selected. The label slides in/out with a smooth (non-bouncy) motion and
 * the whole item sits inside a rounded "pill" indicator. Shared by both the phone bottom
 * bar ([MainBottomNavBar]) and the tablet rail ([MainNavigationRail]) so they stay identical.
 */
@Composable
internal fun FloatingNavItem(
    destination: BottomDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pillColor = MaterialTheme.colorScheme.secondaryContainer
    val selectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val haptic = LocalHapticFeedback.current

    val pillAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "PillAlpha",
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) selectedContentColor else unselectedColor,
        animationSpec = tween(durationMillis = 200),
        label = "IconColor",
    )

    Row(
        modifier = modifier
            .clip(CircleShape)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .background(pillColor.copy(alpha = pillAlpha))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .semantics {
                role = Role.Tab
                selected = isSelected
            },
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
                animationSpec = tween(durationMillis = 250),
                expandFrom = Alignment.Start,
            ),
            exit = fadeOut(tween(150)) + shrinkHorizontally(
                animationSpec = tween(durationMillis = 200),
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
