package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_COMPACT_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown
import com.axiel7.moelist.utils.UNKNOWN_CHAR

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactUserMediaListItem(
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
    val progressTextColor = if (currentStatus == ListStatus.DROPPED) {
        currentStatus.primaryColor()
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(onLongClick = onLongClick, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.height(MEDIA_POSTER_COMPACT_HEIGHT.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.BottomStart
            ) {
                MediaPoster(
                    url = item.node.mainPicture?.large,
                    showShadow = false,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(
                            width = MEDIA_POSTER_SMALL_WIDTH.dp,
                            height = MEDIA_POSTER_COMPACT_HEIGHT.dp
                        )
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topEnd = 8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if ((item.listStatus?.score ?: 0) == 0) UNKNOWN_CHAR
                        else "${item.listStatus?.score}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(
                            start = 6.dp,
                            top = 2.dp,
                            end = 2.dp,
                            bottom = 2.dp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_round_star_16),
                        contentDescription = "star",
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(10.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }//:Box

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = item.node.userPreferredTitle(),
                        modifier = Modifier
                            .padding(top = 8.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )

                    Text(
                        text = if (isAiring && broadcast != null) broadcast.airingInString()
                        else if (isAiring) stringResource(R.string.airing)
                        else item.node.mediaFormat?.localized().orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isAiring) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${userProgress ?: 0}/${totalProgress.toStringPositiveValueOrUnknown()}",
                            style = MaterialTheme.typography.labelLarge,
                            color = progressTextColor
                        )
                        if ((item as? UserMangaList)?.listStatus?.isUsingVolumeProgress() == true) {
                            Icon(
                                painter = painterResource(R.drawable.round_bookmark_24),
                                contentDescription = stringResource(R.string.volumes),
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(12.dp),
                                tint = if (currentStatus == ListStatus.DROPPED) progressTextColor else MaterialTheme.colorScheme.onSurfaceVariant
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
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (item.listStatus?.hasNotes() == true) {
                            Icon(
                                painter = painterResource(R.drawable.round_notes_24),
                                contentDescription = stringResource(R.string.notes),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (listStatus?.isCurrent() == true) {
                            OutlinedButton(
                                onClick = onClickPlus,
                                modifier = Modifier.height(28.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.plus_one),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }//:Row
            }//:Column
        }//:Row
    }//:Card
}

@Composable
fun CompactUserMediaListItemPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.height(MEDIA_POSTER_COMPACT_HEIGHT.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(
                        width = MEDIA_POSTER_SMALL_WIDTH.dp,
                        height = MEDIA_POSTER_COMPACT_HEIGHT.dp
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .defaultPlaceholder(visible = true)
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "This is a loading placeholder for long titles",
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .defaultPlaceholder(visible = true),
                        style = MaterialTheme.typography.titleSmall,
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

                Text(
                    text = "??/??",
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .defaultPlaceholder(visible = true),
                    style = MaterialTheme.typography.labelLarge
                )
            }//:Column
        }//:Row
    }//:Column
}

@Preview
@Composable
fun CompactUserMediaListItemPreview() {
    MoeListTheme {
        Column {
            CompactUserMediaListItem(
                item = exampleUserAnimeList,
                listStatus = ListStatus.WATCHING,
                onClick = { },
                onLongClick = { },
                onClickPlus = { }
            )
            CompactUserMediaListItemPlaceholder()
        }
    }
}