package com.axiel7.moelist.ui.profile.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.MenuBook
import com.axiel7.moelist.ui.icons.automirrored.rounded.ViewList
import com.axiel7.moelist.ui.icons.outlined.Book
import com.axiel7.moelist.ui.icons.rounded.Event
import com.axiel7.moelist.ui.icons.rounded.PlayCircleOutline
import com.axiel7.moelist.ui.icons.rounded.Refresh
import com.axiel7.moelist.ui.icons.rounded.Repeat
import com.axiel7.moelist.ui.icons.rounded.Star
import com.axiel7.moelist.ui.profile.ProfileUiState
import com.axiel7.moelist.utils.NumExtensions.toStringOrZero
import com.axiel7.moelist.utils.UNKNOWN_CHAR

@Composable
fun UserStatsView(
    uiState: ProfileUiState,
    mediaType: MediaType,
    showSectionTitle: Boolean = true,
    compactStats: Boolean = false,
    onRefreshManga: () -> Unit = {}
) {
    val isLoading = if (mediaType == MediaType.MANGA) uiState.isLoadingManga else uiState.isLoading
    val isError = if (mediaType == MediaType.MANGA) uiState.isMangaError else false
    val stats = remember(uiState, mediaType) {
        if (mediaType == MediaType.ANIME) uiState.animeStats else uiState.mangaStats
    }

    val onHold = stats.find { it.type == ListStatus.ON_HOLD }?.value?.toInt()
    val dropped = stats.find { it.type == ListStatus.DROPPED }?.value?.toInt()

    val totalProgress = if (mediaType == MediaType.ANIME)
        uiState.user?.animeStatistics?.numEpisodes
    else uiState.userMangaStats?.chaptersRead

    val days = if (mediaType == MediaType.ANIME)
        uiState.user?.animeStatistics?.numDays
    else uiState.userMangaStats?.days

    val meanScore = if (mediaType == MediaType.ANIME)
        uiState.user?.animeStatistics?.meanScore
    else uiState.userMangaStats?.meanScore

    val rewatched = if (mediaType == MediaType.ANIME)
        uiState.user?.animeStatistics?.numTimesRewatched
    else uiState.userMangaStats?.repeat

    val progressLabel = if (mediaType == MediaType.ANIME) stringResource(R.string.episodes)
    else stringResource(R.string.chapters)
    
    val progressIcon = if (mediaType == MediaType.ANIME) Icons.Rounded.PlayCircleOutline
    else Icons.AutoMirrored.Rounded.MenuBook

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showSectionTitle || (mediaType == MediaType.MANGA && isError && !isLoading)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (showSectionTitle) {
                    Text(
                        text = if (mediaType == MediaType.ANIME) stringResource(R.string.anime_stats)
                        else stringResource(R.string.manga_stats),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (mediaType == MediaType.MANGA && isError && !isLoading) {
                    IconButton(
                        onClick = onRefreshManga,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.refresh),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Two compact rows so the whole Profile fits on one screen. Watching, Completed and
        // Plan to Watch/Read are in the ring's legend above, so they aren't repeated here.

        // Row 1: totals. Minimum height, not a fixed one: larger fonts make the row taller
        // instead of cutting the labels off. IntrinsicSize keeps all tiles the same height.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .heightIn(min = 72.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BentoStatCard(
                title = stringResource(R.string.days),
                // Manga "days read" only comes from Jikan: unknown ("─") rather than a fake 0
                value = if (isError || (mediaType == MediaType.MANGA && days == null)) UNKNOWN_CHAR
                else days.toStringOrZero(),
                icon = Icons.Rounded.Event,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1.4f),
                isLoading = isLoading,
                isHero = true
            )
            BentoStatCard(
                title = stringResource(R.string.mean_score),
                value = if (isError) UNKNOWN_CHAR else meanScore.toStringOrZero(),
                icon = Icons.Rounded.Star,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f),
                isLoading = isLoading
            )
            BentoStatCard(
                title = progressLabel,
                value = if (isError) UNKNOWN_CHAR else totalProgress?.toString() ?: "0",
                icon = progressIcon,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f),
                isLoading = isLoading
            )
        }

        // Row 2: the other statuses and extras
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .heightIn(min = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BentoStatCard(
                title = stringResource(R.string.on_hold),
                value = if (isError) UNKNOWN_CHAR else onHold?.toString() ?: "0",
                icon = ListStatus.ON_HOLD.icon,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = ListStatus.ON_HOLD.primaryColor(),
                modifier = Modifier.weight(1f),
                isLoading = isLoading
            )
            BentoStatCard(
                title = stringResource(R.string.dropped),
                value = if (isError) UNKNOWN_CHAR else dropped?.toString() ?: "0",
                icon = ListStatus.DROPPED.icon,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = ListStatus.DROPPED.primaryColor(),
                modifier = Modifier.weight(1f),
                isLoading = isLoading
            )
            BentoStatCard(
                title = if (mediaType == MediaType.ANIME) stringResource(R.string.rewatched)
                else stringResource(R.string.rereads),
                value = if (isError) UNKNOWN_CHAR else rewatched.toStringOrZero(),
                icon = Icons.Rounded.Repeat,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.outline,
                modifier = Modifier.weight(1f),
                isLoading = isLoading
            )
            if (mediaType == MediaType.ANIME) {
                BentoStatCard(
                    title = stringResource(R.string.total_entries),
                    value = uiState.user?.animeStatistics?.numItems.toStringOrZero(),
                    icon = Icons.AutoMirrored.Rounded.ViewList,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f),
                    isLoading = isLoading
                )
            } else {
                BentoStatCard(
                    title = stringResource(R.string.volumes),
                    value = if (isError) UNKNOWN_CHAR
                    else uiState.userMangaStats?.volumesRead.toStringOrZero(),
                    icon = Icons.Outlined.Book,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f),
                    isLoading = isLoading
                )
            }
        }
    }
}

@Composable
fun BentoStatCard(
    title: String,
    value: String,
    icon: Any,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isHero: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = if (isHero) Alignment.Start else Alignment.CenterHorizontally
        ) {
            if (isHero) {
                // Hero: icon + title on top, big number below
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatIcon(icon = icon, size = 20.dp, tint = contentColor.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    modifier = Modifier.defaultPlaceholder(visible = isLoading),
                    color = contentColor
                )
            } else {
                // Small tile: icon next to the number (saves a line), label below
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatIcon(icon = icon, size = 16.dp, tint = contentColor.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        modifier = Modifier.defaultPlaceholder(visible = isLoading),
                        color = contentColor
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun StatIcon(icon: Any, size: Dp, tint: Color) {
    when (icon) {
        is ImageVector -> Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size),
            tint = tint
        )
        is Int -> Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(size),
            tint = tint
        )
    }
}
