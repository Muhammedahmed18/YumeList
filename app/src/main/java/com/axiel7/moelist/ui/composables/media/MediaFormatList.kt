package com.axiel7.moelist.ui.composables.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaFormat
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.MenuBook
import com.axiel7.moelist.ui.icons.rounded.Apps
import com.axiel7.moelist.ui.icons.rounded.AutoAwesome
import com.axiel7.moelist.ui.icons.rounded.AutoStories
import com.axiel7.moelist.ui.icons.rounded.Book
import com.axiel7.moelist.ui.icons.rounded.Bookmark
import com.axiel7.moelist.ui.icons.rounded.Brush
import com.axiel7.moelist.ui.icons.rounded.Check
import com.axiel7.moelist.ui.icons.rounded.Draw
import com.axiel7.moelist.ui.icons.rounded.Language
import com.axiel7.moelist.ui.icons.rounded.LiveTv
import com.axiel7.moelist.ui.icons.rounded.Movie
import com.axiel7.moelist.ui.icons.rounded.Tv
import com.axiel7.moelist.ui.icons.rounded.VideoLibrary
import com.axiel7.moelist.ui.icons.rounded.ViewDay

/*
 * Format picker shared by the season chart and the anime / manga lists, so both sheets look
 * and behave the same.
 */

/** Formats offered for anime, in display order (null = All). */
val AnimeFormatOptions: List<MediaFormat?> = listOf(
    null,
    MediaFormat.TV,
    MediaFormat.MOVIE,
    MediaFormat.ONA,
    MediaFormat.OVA,
    MediaFormat.SPECIAL,
    MediaFormat.TV_SPECIAL,
)

/** Formats offered for manga, in display order (null = All). */
val MangaFormatOptions: List<MediaFormat?> = listOf(
    null,
    MediaFormat.MANGA,
    MediaFormat.ONE_SHOT,
    MediaFormat.MANHWA,
    MediaFormat.MANHUA,
    MediaFormat.NOVEL,
    MediaFormat.LIGHT_NOVEL,
)

/** Icon of a format in the picker (null = All). */
val MediaFormat?.pickerIcon: ImageVector
    get() = when (this) {
        null -> Icons.Rounded.Apps
        MediaFormat.TV -> Icons.Rounded.Tv
        MediaFormat.MOVIE -> Icons.Rounded.Movie
        MediaFormat.ONA -> Icons.Rounded.Language
        MediaFormat.OVA -> Icons.Rounded.VideoLibrary
        MediaFormat.SPECIAL -> Icons.Rounded.AutoAwesome
        MediaFormat.TV_SPECIAL -> Icons.Rounded.LiveTv
        MediaFormat.MANGA -> Icons.AutoMirrored.Rounded.MenuBook
        MediaFormat.ONE_SHOT -> Icons.Rounded.Bookmark
        MediaFormat.MANHWA -> Icons.Rounded.ViewDay
        MediaFormat.MANHUA -> Icons.Rounded.Brush
        MediaFormat.NOVEL -> Icons.Rounded.Book
        MediaFormat.LIGHT_NOVEL -> Icons.Rounded.AutoStories
        MediaFormat.DOUJINSHI -> Icons.Rounded.Draw
        else -> Icons.Rounded.Apps
    }

/**
 * Whole content of a format bottom sheet: header, then the grouped single-choice list.
 *
 * - One row per format with its icon and count (badge on the right, so numbers line up).
 * - The selected row is tinted and bold, with a check instead of the icon.
 * - Formats with no entries are greyed out and can't be picked; "All" and the current
 *   choice always stay enabled, so you can never get stuck on an empty list.
 *
 * @param context optional label shown next to the title, e.g. the list status the counts are for
 * @param onSelect called with the tapped format (even when it's already selected), with a tick
 */
@Composable
fun MediaFormatSheetContent(
    options: List<MediaFormat?>,
    selected: MediaFormat?,
    counts: Map<MediaFormat?, Int>,
    onSelect: (MediaFormat?) -> Unit,
    modifier: Modifier = Modifier,
    context: String? = null,
    bottomPadding: Dp = 0.dp,
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp + bottomPadding),
    ) {
        // Header: title + optional context chip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.format),
                style = MaterialTheme.typography.titleLargeEmphasized,
            )
            if (context != null) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ) {
                    Text(
                        text = context,
                        style = MaterialTheme.typography.labelMediumEmphasized,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            options.forEachIndexed { index, format ->
                val count = counts[format] ?: 0
                val isSelected = selected == format
                MediaFormatItem(
                    label = format?.localized() ?: stringResource(R.string.all),
                    icon = format.pickerIcon,
                    count = count,
                    selected = isSelected,
                    enabled = isSelected || format == null || count > 0,
                    index = index,
                    itemCount = options.size,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onSelect(format)
                    },
                )
            }
        }
    }
}

@Composable
private fun MediaFormatItem(
    label: String,
    icon: ImageVector,
    count: Int,
    selected: Boolean,
    enabled: Boolean,
    index: Int,
    itemCount: Int,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    SegmentedListItem(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = itemCount),
        colors = ListItemDefaults.segmentedColors(
            containerColor = colors.surfaceContainer,
            selectedContainerColor = colors.secondaryContainer,
            selectedContentColor = colors.onSecondaryContainer,
            selectedLeadingContentColor = colors.primary,
            selectedTrailingContentColor = colors.onSecondaryContainer,
        ),
        leadingContent = {
            Icon(
                imageVector = if (selected) Icons.Rounded.Check else icon,
                contentDescription = null,
            )
        },
        trailingContent = {
            Surface(
                shape = CircleShape,
                color = if (selected) colors.primary else colors.surfaceContainerHighest,
                contentColor = if (selected) colors.onPrimary else colors.onSurfaceVariant,
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelMediumEmphasized,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                )
            }
        },
    ) {
        Text(
            text = label,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
