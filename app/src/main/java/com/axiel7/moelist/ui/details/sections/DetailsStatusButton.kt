package com.axiel7.moelist.ui.details.sections

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.Add
import com.axiel7.moelist.ui.icons.rounded.KeyboardArrowDown
import com.axiel7.moelist.ui.theme.Motion

/** Comfortable touch height for the status button (M3 small split buttons are 40dp). */
private val StatusButtonHeight: Dp = 48.dp

/**
 * The user's list status as an M3 Expressive **split button**.
 *
 * - Leading button: shows the current status (or "Add") and opens the full edit sheet.
 * - Trailing button: opens a menu to change the status in one tap; its arrow flips while open.
 *   The current status is highlighted in the menu.
 * - Both halves squish their inner corners while pressed (Expressive pressed shape).
 * - Filled when the entry isn't in the list yet (primary action "Add"), tonal once it is.
 *
 * @param currentStatus null when the entry isn't in the user's list
 * @param statuses the statuses for this media type, in menu order
 */
// Opt-in kept for safety: some Expressive menu APIs were experimental in earlier 1.5 builds
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DetailsStatusButton(
    currentStatus: ListStatus?,
    statuses: Array<ListStatus>,
    onEditClick: () -> Unit,
    onStatusSelected: (ListStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val colors = if (currentStatus == null) ButtonDefaults.buttonColors()
    else ButtonDefaults.filledTonalButtonColors()
    val changeStatusLabel = stringResource(R.string.change_status)

    Box(modifier = modifier) {
        SplitButtonLayout(
            modifier = Modifier.fillMaxWidth(),
            leadingButton = {
                SplitButtonDefaults.LeadingButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = StatusButtonHeight),
                    shapes = SplitButtonDefaults.leadingButtonShapesFor(StatusButtonHeight),
                    colors = colors,
                    contentPadding = SplitButtonDefaults.leadingButtonContentPaddingFor(StatusButtonHeight),
                ) {
                    Icon(
                        imageVector = currentStatus?.icon ?: Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(SplitButtonDefaults.leadingButtonIconSizeFor(StatusButtonHeight))
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = currentStatus?.localized() ?: stringResource(R.string.add),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            trailingButton = {
                SplitButtonDefaults.TrailingButton(
                    checked = menuOpen,
                    onCheckedChange = { menuOpen = it },
                    modifier = Modifier
                        .heightIn(min = StatusButtonHeight)
                        .semantics {
                            contentDescription = changeStatusLabel
                            stateDescription = if (menuOpen) "Expanded" else "Collapsed"
                        },
                    shapes = SplitButtonDefaults.trailingButtonShapesFor(StatusButtonHeight),
                    colors = colors,
                    contentPadding = SplitButtonDefaults.trailingButtonContentPaddingFor(StatusButtonHeight),
                ) {
                    val rotation by animateFloatAsState(
                        targetValue = if (menuOpen) 180f else 0f,
                        animationSpec = Motion.fastSpatial(),
                        label = "statusMenuArrow"
                    )
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier
                            .size(SplitButtonDefaults.trailingButtonIconSizeFor(StatusButtonHeight))
                            .graphicsLayer { rotationZ = rotation }
                    )
                }
            },
        )

        // M3 Expressive menu: rounded group with selectable rows. The current status is
        // highlighted (tinted, rounded background, bold text) instead of showing a tick.
        DropdownMenuPopup(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(index = 0, count = 1),
            ) {
                statuses.forEachIndexed { index, status ->
                    val selected = status == currentStatus
                    SelectableDropdownMenuItem(
                        selected = selected,
                        onClick = {
                            menuOpen = false
                            if (!selected) onStatusSelected(status)
                        },
                        text = {
                            Text(
                                text = status.localized(),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shapes = MenuDefaults.itemShape(index = index, count = statuses.size),
                        leadingIcon = {
                            Icon(
                                imageVector = status.icon,
                                contentDescription = null,
                                modifier = Modifier.size(MenuDefaults.LeadingIconSize)
                            )
                        },
                        // Same icon when selected: the highlight already marks it
                        selectedLeadingIcon = {
                            Icon(
                                imageVector = status.icon,
                                contentDescription = null,
                                modifier = Modifier.size(MenuDefaults.LeadingIconSize)
                            )
                        },
                        colors = MenuDefaults.selectableItemColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }
        }
    }
}
