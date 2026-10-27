package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.Sort
import com.axiel7.moelist.ui.icons.rounded.ArrowDropDown
import com.axiel7.moelist.ui.theme.Motion
import com.axiel7.moelist.ui.userlist.UserMediaListEvent
import com.axiel7.moelist.ui.userlist.UserMediaListUiState

/** Comfortable touch height for the two controls (M3 small buttons are 40dp). */
private val ControlHeight = 48.dp

/**
 * Format filter + sort, as an M3 Expressive *connected button group*: two tonal buttons that
 * sit 2dp apart, with fully round outer corners and small inner corners. Each button squishes
 * its corners while pressed.
 */
@Composable
fun UserMediaListControlBar(
    uiState: UserMediaListUiState,
    event: UserMediaListEvent?,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    // AnimatedContent's transitionSpec isn't @Composable, so read the motion specs here
    val labelSlide = Motion.fastSpatial<IntOffset>()
    val labelFade = Motion.fastEffects<Float>()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        // Leading: format filter, e.g. "TV (24)"
        FilledTonalButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                event?.toggleFormatSheet(true)
            },
            shapes = ButtonShapes(
                shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                pressedShape = ButtonGroupDefaults.connectedLeadingButtonPressShape,
            ),
            modifier = Modifier
                .weight(1f)
                .heightIn(min = ControlHeight),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            AnimatedContent(
                targetState = uiState.selectedFormat to (uiState.formatCounts[uiState.selectedFormat] ?: 0),
                transitionSpec = {
                    (slideInVertically(labelSlide) { height -> height } + fadeIn(labelFade)) togetherWith
                        (slideOutVertically(labelSlide) { height -> -height } + fadeOut(labelFade))
                },
                modifier = Modifier.weight(1f, fill = false),
                label = "formatLabel"
            ) { (format, count) ->
                val formatText = format?.localized() ?: stringResource(R.string.all)
                Text(
                    text = "$formatText ($count)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Trailing: current sort, e.g. "Last updated"
        FilledTonalButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                event?.toggleSortDialog(true)
            },
            shapes = ButtonShapes(
                shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                pressedShape = ButtonGroupDefaults.connectedTrailingButtonPressShape,
            ),
            modifier = Modifier.heightIn(min = ControlHeight),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Sort,
                contentDescription = stringResource(R.string.sort_by),
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
            Text(
                text = uiState.listSort?.localized().orEmpty(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
