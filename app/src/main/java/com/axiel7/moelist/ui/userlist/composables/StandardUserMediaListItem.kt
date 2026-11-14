package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.anime.exampleUserAnimeList
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.composables.RollingNumberText
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_MEDIUM_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_MEDIUM_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.composables.score.MyScoreBadge
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.Schedule
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.ShapeFull
import com.axiel7.moelist.ui.theme.ShapePoster
import com.axiel7.moelist.ui.theme.ShapeSmall
import com.axiel7.moelist.utils.UNKNOWN_CHAR

/*
 * The standard list row:
 *
 *   [poster]  Title (2 lines)
 *   [ ★ 8  ]  TV · 2026                      ← plain supporting text
 *             [🕒 Ep 6 · in 2 days]          ← one accent chip, only while airing / not aired
 *
 *             5 / 12 episodes                ← progress, once
 *             ━━━━━━━━━━──────────  [ +1 ]   ← bar and +1 on one row
 *
 * Completed and Plan to watch/read show the length ("12 episodes") instead of a progress bar
 * (it would always be full / empty). With no +1 the bar runs full width: no empty space.
 */

/** Small accent chip for time-sensitive state (airing countdown, not yet aired). */
@Composable
private fun StateChip(
    text: String,
    icon: ImageVector? = null,
) {
    Row(
        modifier = Modifier
            .clip(ShapeFull)
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.labelMediumEmphasized,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** "12 episodes" / "120 chapters" / "18 volumes", or "─" when unknown. */
@Composable
private fun lengthText(total: Int?, isVolumes: Boolean, isManga: Boolean): String = when {
    total == null || total <= 0 -> UNKNOWN_CHAR
    isVolumes -> pluralStringResource(R.plurals.num_volumes, total, total)
    isManga -> pluralStringResource(R.plurals.num_chapters, total, total)
    else -> pluralStringResource(R.plurals.num_episodes, total, total)
}

@OptIn(ExperimentalFoundationApi::class)
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
    val isNotYetAired = item.node.status == MediaStatus.NOT_AIRED
    val showQuickAdd = listStatus?.isCurrent() == true ||
        (listStatus?.isPlanning() == true && item.hasStarted)
    val haptic = LocalHapticFeedback.current

    val isManga = item.node.mediaType == MediaType.MANGA
    val isVolumes = (item.listStatus as? MyMangaListStatus)?.isUsingVolumeProgress() == true
    // Completed: the bar would always be full. Planning: always empty. Show the length instead.
    val showsLength = listStatus == ListStatus.COMPLETED || listStatus?.isPlanning() == true
    val showBar = !showsLength && (totalProgress ?: 0) > 0

    // "TV · 2026": format and start year as quiet supporting text
    val supportingText = listOfNotNull(
        item.node.mediaFormat?.localized(),
        ((item.node as? AnimeNode)?.startSeason?.year?.toString()
            ?: item.node.startDate?.take(4)?.takeIf { it.length == 4 }),
    ).joinToString(" · ")

    // The one time-sensitive chip: airing countdown or not yet aired
    val stateText = when {
        isAiring -> broadcast?.airingInString() ?: stringResource(R.string.airing)
        isNotYetAired -> stringResource(R.string.not_yet_aired)
        else -> null
    }

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
            // --- Poster ---
            Box {
                // MediaPoster clips itself (ShapePoster), so the corners stay rounded in the transition
                MediaPoster(
                    url = item.node.mainPicture?.large,
                    sharedKey = posterKey,
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

            // --- Info ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
                    .fillMaxHeight(),
            ) {
                Text(
                    text = item.node.userPreferredTitle(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )

                if (supportingText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (stateText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StateChip(
                        text = stateText,
                        icon = if (isAiring) Icons.Rounded.Schedule else null,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Progress (or length) and +1. The button sits beside the text and bar, so
                // without it nothing is left empty.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (showsLength) {
                            Text(
                                text = lengthText(totalProgress, isVolumes, isManga),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            // "5 / 12 episodes": the count rolls up after +1
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RollingNumberText(
                                    targetValue = userProgress ?: 0,
                                    style = MaterialTheme.typography.labelLargeEmphasized,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = " / " + lengthText(totalProgress, isVolumes, isManga),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (showBar) {
                            Spacer(modifier = Modifier.height(6.dp))
                            // Fixed primary colour (not the status colour), so a dropped item
                            // isn't drawn in the error colour
                            ListProgressBar(progress = item.calculateProgressBarValue())
                        }
                    }

                    if (showQuickAdd) {
                        // Morphs from a pill to a rounded square while pressed
                        PlusOneButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                onClickPlus()
                            }
                        )
                    }
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
    // Same segmented container and layout as the real row, so loading -> loaded doesn't jump
    ListSegment(index = index, count = count) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster
            Box(
                modifier = Modifier
                    .height(MEDIA_POSTER_MEDIUM_HEIGHT.dp)
                    .width(MEDIA_POSTER_MEDIUM_WIDTH.dp)
                    .clip(ShapePoster)
                    .defaultPlaceholder(visible = true)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
                    .fillMaxHeight()
            ) {
                // Title, two lines
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(ShapeSmall)
                        .defaultPlaceholder(visible = true)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(18.dp)
                        .clip(ShapeSmall)
                        .defaultPlaceholder(visible = true)
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Supporting text
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(14.dp)
                        .clip(ShapeSmall)
                        .defaultPlaceholder(visible = true)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Progress text, then bar + +1
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(16.dp)
                        .clip(ShapeSmall)
                        .defaultPlaceholder(visible = true)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(ShapeFull)
                            .defaultPlaceholder(visible = true)
                    )
                    Box(
                        modifier = Modifier
                            .width(72.dp)
                            .height(36.dp)
                            .clip(ShapeFull)
                            .defaultPlaceholder(visible = true)
                    )
                }
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
