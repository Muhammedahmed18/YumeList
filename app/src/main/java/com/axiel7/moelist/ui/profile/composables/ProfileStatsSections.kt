package com.axiel7.moelist.ui.profile.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.composables.RollingNumberText
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.details.sections.DetailsTileGap
import com.axiel7.moelist.ui.details.sections.connectedGridShape
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.MenuBook
import com.axiel7.moelist.ui.icons.rounded.Event
import com.axiel7.moelist.ui.icons.rounded.PlayCircleOutline
import com.axiel7.moelist.ui.icons.rounded.Repeat
import com.axiel7.moelist.ui.icons.rounded.Star
import com.axiel7.moelist.ui.theme.ConnectedGap
import com.axiel7.moelist.ui.theme.Motion
import com.axiel7.moelist.ui.theme.ShapeFull
import com.axiel7.moelist.ui.theme.ShapeLarge
import com.axiel7.moelist.ui.theme.ShapeMedium
import com.axiel7.moelist.ui.theme.connectedRowShape
import com.axiel7.moelist.utils.UNKNOWN_CHAR
import kotlin.math.max

/*
 * Profile stats, replacing the old library ring:
 * - [ProfileLibrarySection]: one stacked bar of all five list statuses + a tappable legend
 *   (each status opens that list).
 * - [ProfileActivitySection]: days, episodes / chapters, mean score and rewatches as connected
 *   tiles, in the same style as the details stat tiles.
 */

/** Height of the stacked status bar. */
private val StatusBarHeight = 12.dp

/** Smallest share a non-empty status gets in the bar, so 2 out of 300 is still visible. */
private const val MinSegmentShare = 0.02f

/**
 * The statuses in display order. Watching and On hold (primary / secondary, which can look alike
 * in some palettes) are kept apart, with Completed between them.
 */
fun profileStatuses(mediaType: MediaType): List<ListStatus> =
    if (mediaType == MediaType.ANIME) {
        listOf(
            ListStatus.WATCHING, ListStatus.COMPLETED, ListStatus.ON_HOLD,
            ListStatus.DROPPED, ListStatus.PLAN_TO_WATCH
        )
    } else {
        listOf(
            ListStatus.READING, ListStatus.COMPLETED, ListStatus.ON_HOLD,
            ListStatus.DROPPED, ListStatus.PLAN_TO_READ
        )
    }

/** Section title with an optional trailing element (entry count, retry...). */
@Composable
private fun ProfileSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            .padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        trailing()
    }
}

/**
 * "Library": the whole list split by status.
 *
 * - Loading: a wavy progress indicator where the bar goes.
 * - Error: an empty track, "─" counts and a Retry button.
 * - Loaded: the stacked bar (each status sized by its share) and the legend. Tapping a legend
 *   row opens that status in the list.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileLibrarySection(
    mediaType: MediaType,
    counts: Map<ListStatus, Int>,
    isLoading: Boolean,
    isError: Boolean,
    onRetry: () -> Unit,
    onStatusClick: (ListStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    val statuses = profileStatuses(mediaType)
    val total = statuses.sumOf { counts[it] ?: 0 }

    Column(modifier = modifier.fillMaxWidth()) {
        ProfileSectionTitle(text = stringResource(R.string.profile_library)) {
            when {
                isError && !isLoading -> TextButton(onClick = onRetry) {
                    Text(text = stringResource(R.string.retry))
                }
                !isLoading -> Text(
                    text = pluralStringResource(R.plurals.profile_entries, total, total),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = ShapeLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Fixed height, so the card doesn't jump between loading / bar / error
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading -> LinearWavyProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                        isError || total == 0 -> EmptyStatusTrack()
                        else -> StatusBar(statuses = statuses, counts = counts, total = total)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Two columns: Watching / Completed, On hold / Dropped, Plan to...
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    statuses.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { status ->
                                StatusLegendItem(
                                    status = status,
                                    count = counts[status] ?: 0,
                                    isLoading = isLoading,
                                    isError = isError,
                                    onClick = { onStatusClick(status) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/** One bar split into the statuses, in the app's connected style (2dp gaps, pill ends). */
@Composable
private fun StatusBar(
    statuses: List<ListStatus>,
    counts: Map<ListStatus, Int>,
    total: Int,
) {
    val shown = statuses.filter { (counts[it] ?: 0) > 0 }
    val shareSpec = Motion.effects<Float>()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(StatusBarHeight),
        horizontalArrangement = Arrangement.spacedBy(ConnectedGap)
    ) {
        shown.forEachIndexed { index, status ->
            key(status) {
                // Effects spec (no overshoot): a weight must never dip to 0 or below
                val share by animateFloatAsState(
                    targetValue = max((counts[status] ?: 0).toFloat() / total, MinSegmentShare),
                    animationSpec = shareSpec,
                    label = "statusShare",
                )
                Box(
                    modifier = Modifier
                        .weight(share)
                        .fillMaxHeight()
                        .clip(
                            connectedRowShape(
                                isFirst = index == 0,
                                isLast = index == shown.lastIndex,
                                outer = CornerSize(50),
                            )
                        )
                        .background(status.primaryColor())
                )
            }
        }
    }
}

/** The bar's empty track (error, or nothing in the list yet). */
@Composable
private fun EmptyStatusTrack() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(StatusBarHeight)
            .clip(ShapeFull)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    )
}

/**
 * Status icon (in its colour), name and count. The whole row is a 48dp touch target that opens
 * the list on this status.
 */
@Composable
private fun StatusLegendItem(
    status: ListStatus,
    count: Int,
    isLoading: Boolean,
    isError: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(ShapeMedium)
            .clickable(enabled = !isLoading, onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = status.icon,
            contentDescription = null,
            tint = status.primaryColor(),
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = status.localized(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        when {
            isLoading -> Box(
                modifier = Modifier
                    .size(width = 24.dp, height = 16.dp)
                    .defaultPlaceholder(visible = true)
            )
            isError -> Text(
                text = UNKNOWN_CHAR,
                style = MaterialTheme.typography.titleSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
            )
            else -> RollingNumberText(
                targetValue = count,
                style = MaterialTheme.typography.titleSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/** One value of the Activity grid. */
private data class ActivityTile(
    val value: String,
    val label: String,
    val icon: ImageVector,
)

/**
 * "Activity": days, episodes / chapters, mean score and rewatches / rereads as a 2×2 grid of
 * connected tiles (same corners and gaps as the details stat tiles).
 */
@Composable
fun ProfileActivitySection(
    mediaType: MediaType,
    days: String,
    progress: String,
    meanScore: String,
    repeats: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val tiles = listOf(
        ActivityTile(days, stringResource(R.string.days), Icons.Rounded.Event),
        if (mediaType == MediaType.ANIME) {
            ActivityTile(progress, stringResource(R.string.episodes), Icons.Rounded.PlayCircleOutline)
        } else {
            ActivityTile(progress, stringResource(R.string.chapters), Icons.AutoMirrored.Rounded.MenuBook)
        },
        ActivityTile(meanScore, stringResource(R.string.mean_score), Icons.Rounded.Star),
        ActivityTile(
            repeats,
            if (mediaType == MediaType.ANIME) stringResource(R.string.rewatched)
            else stringResource(R.string.rereads),
            Icons.Rounded.Repeat
        ),
    )

    Column(modifier = modifier.fillMaxWidth()) {
        ProfileSectionTitle(text = stringResource(R.string.profile_activity))
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(DetailsTileGap)) {
            tiles.chunked(2).forEachIndexed { row, pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(DetailsTileGap)
                ) {
                    pair.forEachIndexed { column, tile ->
                        ActivityTileView(
                            tile = tile,
                            isLoading = isLoading,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(connectedGridShape(row = row, column = column, rows = 2, columns = 2))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityTileView(
    tile: ActivityTile,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = tile.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = tile.value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.defaultPlaceholder(visible = isLoading)
        )
        Text(
            text = tile.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
