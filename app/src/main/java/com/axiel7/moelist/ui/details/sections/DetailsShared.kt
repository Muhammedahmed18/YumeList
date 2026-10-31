package com.axiel7.moelist.ui.details.sections

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.KeyboardArrowDown
import com.axiel7.moelist.ui.theme.Motion

/*
 * Small helpers shared by the media details sections.
 */

/** Horizontal margin of every details section. */
internal val DetailsHorizontalPadding: Dp = 16.dp

/** Corner radius of the details section cards (M3 Large). */
internal val DetailsCardCorner: Dp = 16.dp

/** Inner padding of every details section card. */
internal val DetailsCardPadding: Dp = 16.dp

/** Gap between connected tiles (same as the M3 Expressive connected button group). */
internal val DetailsTileGap: Dp = 2.dp

/** Corners on the inside edges of connected tiles. */
internal val DetailsTileInnerCorner: Dp = 4.dp

/**
 * Shape of one tile in a connected grid: [DetailsCardCorner] on the corners that touch the
 * outside of the grid, [DetailsTileInnerCorner] everywhere else, so the tiles read as one card.
 */
internal fun connectedGridShape(row: Int, column: Int, rows: Int, columns: Int): RoundedCornerShape {
    val isTop = row == 0
    val isBottom = row == rows - 1
    val isStart = column == 0
    val isEnd = column == columns - 1
    return RoundedCornerShape(
        topStart = if (isTop && isStart) DetailsCardCorner else DetailsTileInnerCorner,
        topEnd = if (isTop && isEnd) DetailsCardCorner else DetailsTileInnerCorner,
        bottomStart = if (isBottom && isStart) DetailsCardCorner else DetailsTileInnerCorner,
        bottomEnd = if (isBottom && isEnd) DetailsCardCorner else DetailsTileInnerCorner,
    )
}

/**
 * Title above a details section. Every section uses this one, so the gap between sections and
 * the gap between a title and its card are the same all the way down the screen.
 */
@Composable
internal fun DetailsSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .padding(
                start = DetailsHorizontalPadding,
                end = DetailsHorizontalPadding,
                top = 20.dp,
                bottom = 10.dp
            )
            .semantics { heading() }
    )
}

/**
 * Rounded card that holds a details section, in the same style as the header's cards:
 * [DetailsCardCorner] corners on a soft `surfaceContainerLow` fill. Content is clipped to the
 * shape, so horizontally scrolling rows inside it end at the rounded edge.
 */
@Composable
internal fun DetailsSectionCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(DetailsCardPadding),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(DetailsCardCorner),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DetailsHorizontalPadding)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = verticalArrangement,
            content = content
        )
    }
}

/**
 * Tonal pill button that toggles "Show more" / "Show less", with an arrow that flips while
 * expanded. Squishes on press like the other Expressive buttons.
 */
@Composable
internal fun DetailsExpandButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = Motion.fastSpatial(),
        label = "detailsExpandArrow"
    )
    FilledTonalButton(
        onClick = onClick,
        shapes = ButtonDefaults.shapes(),
        contentPadding = PaddingValues(start = 16.dp, end = 12.dp),
        modifier = modifier.heightIn(min = 36.dp),
    ) {
        Text(
            text = if (expanded) stringResource(R.string.show_less)
            else stringResource(R.string.show_more),
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(modifier = Modifier.size(4.dp))
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer { rotationZ = arrowRotation }
        )
    }
}

/**
 * Selectable filter pill (fully rounded). The selected one takes the accent color and a check,
 * like the accent segment of the header's connected pills.
 */
@Composable
internal fun DetailsFilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedIcon: ImageVector? = null,
) {
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .height(32.dp)
                .padding(start = if (selected && selectedIcon != null) 10.dp else 14.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (selected && selectedIcon != null) {
                Icon(
                    imageVector = selectedIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Formats a nullable count as the number, or "-" when null/non-positive. */
internal fun Int?.countOrDash(): String =
    if (this != null && this > 0) this.toString() else "-"

/** Color that represents a media's airing / publishing status. */
@Composable
internal fun MediaStatus.statusColor(): Color = when (this) {
    MediaStatus.AIRING, MediaStatus.PUBLISHING -> MaterialTheme.colorScheme.primary
    MediaStatus.NOT_AIRED -> MaterialTheme.colorScheme.tertiary
    MediaStatus.HIATUS -> MaterialTheme.colorScheme.secondary
    MediaStatus.DISCONTINUED -> MaterialTheme.colorScheme.error
    MediaStatus.FINISHED_AIRING, MediaStatus.FINISHED -> MaterialTheme.colorScheme.outline
}

/**
 * Non-interactive, fully rounded information pill (genre...). A plain [Surface] with no
 * click handling, so it never shows a ripple or reads as a button.
 */
@Composable
internal fun DetailsPill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * M3 Expressive connected pill group: segments sit side by side as one shape, with fully
 * rounded outer ends and small inner corners. The first segment is the accent.
 * e.g. [ TV | 2026 | 12 episodes ]
 */
@Composable
internal fun DetailsConnectedPills(
    segments: List<String>,
    modifier: Modifier = Modifier,
    accentFirst: Boolean = true,
) {
    val last = segments.lastIndex
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        segments.forEachIndexed { index, text ->
            val start = if (index == 0) 50 else 15
            val end = if (index == last) 50 else 15
            val isAccent = accentFirst && index == 0
            Surface(
                shape = RoundedCornerShape(
                    topStartPercent = start,
                    bottomStartPercent = start,
                    topEndPercent = end,
                    bottomEndPercent = end
                ),
                color = if (isAccent) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = if (isAccent) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                // Only the last (longest) segment may shrink, so short ones never get cut
                modifier = if (index == last) Modifier.weight(1f, fill = false) else Modifier
            ) {
                Row(
                    modifier = Modifier.padding(
                        start = if (index == 0) 12.dp else 10.dp,
                        end = if (index == last) 12.dp else 10.dp
                    ).height(30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isAccent) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
