package com.axiel7.moelist.ui.details.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.ui.theme.ShapeSmall

/*
 * Small helpers shared by the media details sections.
 */

/** Horizontal margin of every details section. */
internal val DetailsHorizontalPadding: Dp = 16.dp

/** Corner radius of the details section cards (M3 Large). */
internal val DetailsCardCorner: Dp = 16.dp

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
 * Non-interactive information pill (format, episode count, genre...). A plain [Surface] with no
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
        shape = ShapeSmall,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
