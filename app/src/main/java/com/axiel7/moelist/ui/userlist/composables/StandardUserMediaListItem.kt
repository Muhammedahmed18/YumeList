package com.axiel7.moelist.ui.userlist.composables

import com.axiel7.moelist.ui.composables.score.MyScoreBadge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.anime.exampleUserAnimeList
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaFormat
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.ui.composables.RollingNumberText
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_MEDIUM_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_MEDIUM_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.MenuBook
import com.axiel7.moelist.ui.icons.rounded.Movie
import com.axiel7.moelist.ui.icons.rounded.MusicNote
import com.axiel7.moelist.ui.icons.rounded.PlayArrow
import com.axiel7.moelist.ui.icons.rounded.Schedule
import com.axiel7.moelist.ui.icons.rounded.Tv
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.ShapeFull
import com.axiel7.moelist.ui.theme.ShapePoster
import com.axiel7.moelist.ui.theme.ShapeSmall
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown

// Fix #7: minimum reserved height for the metadata badge row so cards with
// zero, one, or multiple badges keep a consistent title-to-content rhythm.
private val BadgeRowMinHeight = 22.dp

// Connected badge group (M3 Expressive connected button group style)
private val ConnectedGap = 2.dp
private val ConnectedInnerCorner = 4.dp

/** Outer ends fully rounded (50%), inner corners small. Both ends → full pill. */
private fun connectedShape(isFirst: Boolean, isLast: Boolean): Shape {
    val full = CornerSize(50)
    val inner = CornerSize(ConnectedInnerCorner)
    return RoundedCornerShape(
        topStart = if (isFirst) full else inner,
        bottomStart = if (isFirst) full else inner,
        topEnd = if (isLast) full else inner,
        bottomEnd = if (isLast) full else inner,
    )
}

@Composable
private fun ConnectedMetaPill(
    text: String,
    shape: Shape,
    containerColor: Color,
    contentColor: Color,
    icon: ImageVector? = null,
) {
    Row(
        modifier = Modifier
            .clip(shape)
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun StandardUserMediaListItem(
    item: BaseUserMediaList<out BaseMediaNode>,
    listStatus: ListStatus?,
    // Shared-transition key of the poster (see posterSharedKey); null = no shared transition
    posterKey: String? = null,
    // Position in the list, for the segmented shape (index 0 / count 1 = standalone card)
    index: Int = 0,
    count: Int = 1,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickPlus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalProgress = item.totalProgress()
    val userProgress = item.userProgress()
    val score = item.listStatus?.score?.takeIf { it > 0 }
    val broadcast = (item.node as? AnimeNode)?.broadcast
    val isAiring = item.isAiring
    val isNotYetAired = (item.node as? AnimeNode)?.status == MediaStatus.NOT_AIRED
    val showQuickAdd = listStatus?.isCurrent() == true ||
        (listStatus?.isPlanning() == true && item.hasStarted)
    val haptic = LocalHapticFeedback.current

    // Fixed primary color rather than the status color, so a dropped/on-hold item doesn't
    // show a misleading "error red" progress bar just because of its list status.
    val progressBarColor = MaterialTheme.colorScheme.primary

    // Segmented row: grouped corners, pressed-shape morph and a ripple clipped to the shape.
    // Inner clickables (the +1 button) still receive their own taps first.
    ListSegment(
        index = index,
        count = count,
        modifier = modifier,
        onClick = onClick,
        onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onLongClick()
        },
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Poster Column ---
            Box {
                // MediaPoster clips itself (ShapePoster), so the corners stay rounded in the transition
                MediaPoster(
                    url = item.node.mainPicture?.large,
                    sharedKey = posterKey,
                    showShadow = true,
                    modifier = Modifier
                        .height(MEDIA_POSTER_MEDIUM_HEIGHT.dp)
                        .width(MEDIA_POSTER_MEDIUM_WIDTH.dp)
                )

                // Score badge — bottom-left overlay (only when a score is given)
                MyScoreBadge(
                    score = score,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                )
            }

            // --- Info Column ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
                    .fillMaxHeight(),
            ) {
                // Title
                Text(
                    text = item.node.userPreferredTitle(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Metadata badges — static, non-interactive (fix #1: no click, no ripple).
                // Fix #7: reserve a minimum height for this row so cards with zero, one,
                // or multiple badges keep a consistent title-to-content rhythm across
                // the list, instead of some cards looking visually "sparser" than others.
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = BadgeRowMinHeight)
                ) {
                    val formatText = item.node.mediaFormat?.localized()
                    // Airing and "Not yet aired" are mutually exclusive, so the group
                    // is at most two segments: format + one temporal-state badge.
                    val temporalText = when {
                        isAiring -> broadcast?.airingInString() ?: stringResource(R.string.airing)
                        isNotYetAired -> stringResource(R.string.not_yet_aired)
                        else -> null
                    }

                    // M3 Expressive-style connected group: one FlowRow item, so the
                    // pair never splits across lines. Outer ends fully rounded, inner
                    // corners small. Single badge → standalone, fully rounded pill.
                    if (formatText != null || temporalText != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(ConnectedGap)) {
                            if (formatText != null) {
                                ConnectedMetaPill(
                                    text = formatText,
                                    shape = connectedShape(
                                        isFirst = true,
                                        isLast = temporalText == null
                                    ),
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            if (temporalText != null) {
                                ConnectedMetaPill(
                                    text = temporalText,
                                    icon = Icons.Rounded.Schedule,
                                    shape = connectedShape(
                                        isFirst = formatText == null,
                                        isLast = true
                                    ),
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // One UI-style bottom pill: progress info on the left and +1 action on
                // the right, with the full-width container preserved even when the
                // action is unavailable for the current list state.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeFull)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .border(
                            BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            ),
                            ShapeFull
                        )
                        .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RollingNumberText(
                            targetValue = userProgress ?: 0,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = " / ${totalProgress.toStringPositiveValueOrUnknown()}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (showQuickAdd) {
                        // Morphs from a pill to a rounded square while pressed
                        PlusOneButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                onClickPlus()
                            }
                        )
                    } else {
                        // Keep the pill's height so every card has the same rhythm
                        Spacer(modifier = Modifier.height(36.dp))
                    }
                }

                // Wavy progress — only when the total is known, so unknown-progress entries
                // don't keep an empty gap under the pill. Fixed primary color (not the
                // status color), so a dropped item isn't drawn in the error color.
                if ((totalProgress ?: 0) > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ListProgressBar(
                        progress = item.calculateProgressBarValue(),
                        color = progressBarColor,
                    )
                }
            }
        }
    }
}


@Composable
fun StandardUserMediaListItemPlaceholder(
    index: Int = 0,
    count: Int = 1,
) {
    // Same segmented container as the real row, so loading -> loaded doesn't jump
    ListSegment(index = index, count = count) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster placeholder
            Box(
                modifier = Modifier
                    .height(MEDIA_POSTER_MEDIUM_HEIGHT.dp)
                    .width(MEDIA_POSTER_MEDIUM_WIDTH.dp)
                    .clip(ShapePoster)
                    .defaultPlaceholder(visible = true)
            )

            // Info column placeholder — mirrors real item structure
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
                    .fillMaxHeight()
            ) {
                // Title line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(ShapeSmall)
                        .defaultPlaceholder(visible = true)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Second title line (short)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(18.dp)
                        .clip(ShapeSmall)
                        .defaultPlaceholder(visible = true)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Badge row placeholder
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(20.dp)
                        .clip(ShapeSmall)
                        .defaultPlaceholder(visible = true)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Full-width progress/action pill placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(ShapeFull)
                        .defaultPlaceholder(visible = true)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar placeholder — inside info column, same as real item
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(ShapeFull)
                        .defaultPlaceholder(visible = true)
                )
            }
        }
    }
}

@Preview
@Composable
fun StandardUserMediaListItemPreview() {
    MoeListTheme {
        StandardUserMediaListItem(
            item = exampleUserAnimeList,
            listStatus = ListStatus.WATCHING,
            onClick = {},
            onLongClick = {},
            onClickPlus = {}
        )
    }
}
