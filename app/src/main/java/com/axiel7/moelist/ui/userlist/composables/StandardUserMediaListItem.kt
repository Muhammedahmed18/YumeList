package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
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
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown
import com.axiel7.moelist.utils.UNKNOWN_CHAR

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StandardUserMediaListItem(
    item: BaseUserMediaList<out BaseMediaNode>,
    listStatus: ListStatus?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickPlus: () -> Unit,
) {
    val totalProgress = remember { item.totalProgress() }
    val userProgress = item.userProgress()
    val broadcast = remember { (item.node as? AnimeNode)?.broadcast }
    val isAiring = remember { item.isAiring }
    val currentStatus = item.listStatus?.status
    val progressBarColor = if (currentStatus == ListStatus.DROPPED) {
        currentStatus.primaryColor()
    } else {
        MaterialTheme.colorScheme.primary
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null, // Custom indication or handled by Card
                onLongClick = onLongClick,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(MEDIA_POSTER_SMALL_HEIGHT.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(MEDIA_POSTER_SMALL_WIDTH.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.BottomStart
            ) {
                MediaPoster(
                    url = item.node.mainPicture?.large,
                    showShadow = false,
                    modifier = Modifier.fillMaxHeight()
                )

                // Glass Score Badge
                Surface(
                    modifier = Modifier.padding(6.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = CircleShape,
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if ((item.listStatus?.score ?: 0) == 0) UNKNOWN_CHAR
                            else "${item.listStatus?.score}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Icon(
                            painter = painterResource(R.drawable.ic_round_star_16),
                            contentDescription = "star",
                            modifier = Modifier.size(10.dp),
                            tint = Color(0xFFFFB300) // Gold color for star
                        )
                    }
                }
            }//:Box

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 12.dp, end = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = item.node.userPreferredTitle(),
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                    
                    if (isAiring) {
                        Surface(
                            modifier = Modifier.padding(top = 4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = broadcast?.airingInString() ?: stringResource(R.string.airing),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = item.node.mediaFormat?.localized().orEmpty(),
                            modifier = Modifier.padding(top = 2.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }//:Column

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = "${userProgress ?: 0}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "/${totalProgress.toStringPositiveValueOrUnknown()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                            if ((item as? UserMangaList)?.listStatus?.isUsingVolumeProgress() == true) {
                                Icon(
                                    painter = painterResource(R.drawable.round_bookmark_24),
                                    contentDescription = stringResource(R.string.volumes),
                                    modifier = Modifier
                                        .padding(start = 6.dp)
                                        .size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (item.listStatus?.hasRepeated() == true) {
                                Icon(
                                    painter = painterResource(R.drawable.round_repeat_24),
                                    contentDescription = stringResource(R.string.rewatching),
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (item.listStatus?.hasNotes() == true) {
                                Icon(
                                    painter = painterResource(R.drawable.round_notes_24),
                                    contentDescription = stringResource(R.string.notes),
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (listStatus?.isCurrent() == true) {
                                FilledTonalButton(
                                    onClick = onClickPlus,
                                    modifier = Modifier.height(36.dp),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.plus_one),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }//:Row

                    LinearProgressIndicator(
                        progress = { item.calculateProgressBarValue() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .padding(bottom = 2.dp),
                        color = progressBarColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                }//:Column
            }//:Column
        }//:Row
    }//:Card
}

@Composable
fun StandardUserMediaListItemPlaceholder() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(MEDIA_POSTER_SMALL_HEIGHT.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(
                        width = MEDIA_POSTER_SMALL_WIDTH.dp,
                        height = MEDIA_POSTER_SMALL_HEIGHT.dp
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .defaultPlaceholder(visible = true)
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 12.dp, end = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "This is a loading placeholder for long titles",
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .defaultPlaceholder(visible = true),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2
                    )
                    Text(
                        text = "Placeholder Format",
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .defaultPlaceholder(visible = true),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "??/??",
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .defaultPlaceholder(visible = true),
                            style = MaterialTheme.typography.labelLarge
                        )

                        Box(
                            modifier = Modifier
                                .size(60.dp, 36.dp)
                                .clip(CircleShape)
                                .defaultPlaceholder(visible = true)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .padding(bottom = 2.dp)
                            .defaultPlaceholder(visible = true)
                    )
                }
            }//:Column
        }//:Row
    }
}

@Preview
@Composable
fun StandardUserMediaListItemPreview() {
    MoeListTheme {
        Column {
            StandardUserMediaListItem(
                item = exampleUserAnimeList,
                listStatus = ListStatus.WATCHING,
                onClick = { },
                onLongClick = { },
                onClickPlus = { }
            )
            StandardUserMediaListItemPlaceholder()
        }
    }
}