package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.ui.graphics.vector.ImageVector
import com.axiel7.moelist.data.model.media.MediaFormat
import com.axiel7.moelist.data.model.media.MediaStatus
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
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
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseUserMediaList
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.ui.composables.RollingNumberText
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_MEDIUM_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_MEDIUM_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.ShapeExtraLarge
import com.axiel7.moelist.ui.theme.ShapeFull
import com.axiel7.moelist.ui.theme.ShapeMedium
import com.axiel7.moelist.ui.theme.ShapePoster
import com.axiel7.moelist.ui.theme.ShapeSmall
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StandardUserMediaListItem(
    item: BaseUserMediaList<out BaseMediaNode>,
    listStatus: ListStatus?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickPlus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalProgress = remember { item.totalProgress() }
    val userProgress = item.userProgress()
    val score = item.listStatus?.score?.takeIf { it > 0 }
    val broadcast = remember { (item.node as? AnimeNode)?.broadcast }
    val isAiring = remember { item.isAiring }
    val isNotYetAired = remember { (item.node as? AnimeNode)?.status == MediaStatus.NOT_AIRED }
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .combinedClickable(
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
                onClick = onClick
            ),
        shape = ShapeExtraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Poster Column ---
            Box {
                MediaPoster(
                    url = item.node.mainPicture?.large,
                    showShadow = true,
                    modifier = Modifier
                        .height(MEDIA_POSTER_MEDIUM_HEIGHT.dp)
                        .width(MEDIA_POSTER_MEDIUM_WIDTH.dp)
                        .clip(ShapePoster)
                )

                // Score Badge — bottom-left overlay (only when score is given)
                if (score != null) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp)
                            .clip(ShapeSmall)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = "star",
                            modifier = Modifier.size(11.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = score.toString(),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- Info Column ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
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

                // Metadata chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item.node.mediaFormat?.localized()?.let { format ->
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = format,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = item.node.mediaFormat?.toIcon() ?: Icons.Rounded.Tv,
                                    contentDescription = null,
                                    modifier = Modifier.size(11.dp)
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                iconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = null,
                            elevation = null
                        )
                    }

                    if (isAiring) {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = broadcast?.airingInString() ?: stringResource(R.string.airing),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(11.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                leadingIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ),
                            border = null,
                            elevation = null
                        )
                    }

                    if (isNotYetAired) {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = stringResource(R.string.not_yet_aired),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(11.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            border = null,
                            elevation = null
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Progress numbers row + action button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RollingNumberText(
                            targetValue = userProgress ?: 0,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = " / ${totalProgress.toStringPositiveValueOrUnknown()}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if ((item as? UserMangaList)?.listStatus?.isUsingVolumeProgress() == true) {
                            Icon(
                                imageVector = Icons.Rounded.Bookmark,
                                contentDescription = stringResource(R.string.volumes),
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (listStatus?.isCurrent() == true ||
                        (listStatus?.isPlanning() == true && item.hasStarted)
                    ) {
                        FilledIconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                onClickPlus()
                            },
                            modifier = Modifier.size(36.dp),
                            shape = ShapeMedium,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = stringResource(R.string.plus_one),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Progress bar — scoped to info column only
                if ((totalProgress ?: 0) > 0) {
                    val progressBarColor = when (listStatus) {
                        ListStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
                        ListStatus.ON_HOLD -> MaterialTheme.colorScheme.outline
                        ListStatus.DROPPED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }

                    val animatedProgress by animateFloatAsState(
                        targetValue = item.calculateProgressBarValue(),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "progressBarAnimation"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(ShapeFull),
                        color = progressBarColor,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}


@Composable
fun StandardUserMediaListItemPlaceholder() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = ShapeExtraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Max)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        .padding(start = 16.dp)
                        .fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .clip(ShapeSmall)
                            .defaultPlaceholder(visible = true)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(16.dp)
                            .clip(ShapeSmall)
                            .defaultPlaceholder(visible = true)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(ShapeMedium)
                            .defaultPlaceholder(visible = true)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .defaultPlaceholder(visible = true)
            )
        }
    }
}

private fun MediaFormat.toIcon(): ImageVector = when (this) {
    MediaFormat.TV, MediaFormat.TV_SPECIAL -> Icons.Rounded.Tv
    MediaFormat.MOVIE -> Icons.Rounded.Movie
    MediaFormat.OVA, MediaFormat.ONA, MediaFormat.SPECIAL,
    MediaFormat.CM, MediaFormat.PV -> Icons.Rounded.PlayArrow
    MediaFormat.MUSIC -> Icons.Rounded.MusicNote
    MediaFormat.MANGA, MediaFormat.ONE_SHOT, MediaFormat.MANHWA,
    MediaFormat.MANHUA, MediaFormat.DOUJINSHI,
    MediaFormat.NOVEL, MediaFormat.LIGHT_NOVEL -> Icons.AutoMirrored.Rounded.MenuBook
    MediaFormat.UNKNOWN -> Icons.Rounded.Tv
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
