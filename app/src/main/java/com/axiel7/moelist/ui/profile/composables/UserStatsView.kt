package com.axiel7.moelist.ui.profile.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.composables.TextIconVertical
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.profile.ProfileUiState
import com.axiel7.moelist.utils.NumExtensions.toStringOrZero
import com.axiel7.moelist.utils.UNKNOWN_CHAR

@OptIn(ExperimentalLayoutApi::class)
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

    val watching = stats.find { it.type == ListStatus.WATCHING || it.type == ListStatus.READING }?.value?.toInt()
    val completed = stats.find { it.type == ListStatus.COMPLETED }?.value?.toInt()
    val onHold = stats.find { it.type == ListStatus.ON_HOLD }?.value?.toInt()
    val dropped = stats.find { it.type == ListStatus.DROPPED }?.value?.toInt()
    val planToWatch = stats.find { it.type == ListStatus.PLAN_TO_WATCH || it.type == ListStatus.PLAN_TO_READ }?.value?.toInt()
    
    val totalProgress = if (mediaType == MediaType.ANIME)
        uiState.user?.animeStatistics?.numEpisodes
    else uiState.userMangaStats?.chaptersRead

    val progressLabel = if (mediaType == MediaType.ANIME) stringResource(R.string.episodes)
    else stringResource(R.string.chapters)
    
    val progressIcon = if (mediaType == MediaType.ANIME) R.drawable.play_circle_outline_24
    else R.drawable.ic_round_menu_book_24

    val cardPadding = if (compactStats) 8.dp else 12.dp
    val statIconSize = if (compactStats) 18.dp else 20.dp

    Column(modifier = Modifier.fillMaxWidth()) {
        if (showSectionTitle) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (mediaType == MediaType.ANIME) stringResource(R.string.anime_stats)
                    else stringResource(R.string.manga_stats),
                    modifier = Modifier.padding(vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (mediaType == MediaType.MANGA && isError && !isLoading) {
                    IconButton(
                        onClick = onRefreshManga,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_round_refresh_24),
                            contentDescription = stringResource(R.string.refresh),
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        } else if (mediaType == MediaType.MANGA && isError && !isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onRefreshManga,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_refresh_24),
                        contentDescription = stringResource(R.string.refresh),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Modern Dashboard Group
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Primary Stats Grid (2x3)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    maxItemsInEachRow = 2
                ) {
                    val itemModifier = Modifier.weight(1f)
                    
                    // Row 1: Watching/Reading & Completed
                    StatCard(
                        title = if (mediaType == MediaType.ANIME) stringResource(R.string.watching) else stringResource(R.string.reading),
                        value = if (isError) UNKNOWN_CHAR else watching?.toString() ?: "0",
                        icon = if (mediaType == MediaType.ANIME) ListStatus.WATCHING.icon else ListStatus.READING.icon,
                        containerColor = (if (mediaType == MediaType.ANIME) ListStatus.WATCHING else ListStatus.READING).primaryColor().copy(alpha = 0.12f),
                        contentColor = (if (mediaType == MediaType.ANIME) ListStatus.WATCHING else ListStatus.READING).primaryColor(),
                        modifier = itemModifier,
                        isLoading = isLoading,
                        contentPadding = cardPadding,
                        iconSize = statIconSize
                    )
                    StatCard(
                        title = stringResource(R.string.completed),
                        value = if (isError) UNKNOWN_CHAR else completed?.toString() ?: "0",
                        icon = ListStatus.COMPLETED.icon,
                        containerColor = ListStatus.COMPLETED.primaryColor().copy(alpha = 0.12f),
                        contentColor = ListStatus.COMPLETED.primaryColor(),
                        modifier = itemModifier,
                        isLoading = isLoading,
                        contentPadding = cardPadding,
                        iconSize = statIconSize
                    )

                    // Row 2: On Hold & Dropped
                    StatCard(
                        title = stringResource(R.string.on_hold),
                        value = if (isError) UNKNOWN_CHAR else onHold?.toString() ?: "0",
                        icon = ListStatus.ON_HOLD.icon,
                        containerColor = ListStatus.ON_HOLD.primaryColor().copy(alpha = 0.12f),
                        contentColor = ListStatus.ON_HOLD.primaryColor(),
                        modifier = itemModifier,
                        isLoading = isLoading,
                        contentPadding = cardPadding,
                        iconSize = statIconSize
                    )
                    StatCard(
                        title = stringResource(R.string.dropped),
                        value = if (isError) UNKNOWN_CHAR else dropped?.toString() ?: "0",
                        icon = ListStatus.DROPPED.icon,
                        containerColor = ListStatus.DROPPED.primaryColor().copy(alpha = 0.12f),
                        contentColor = ListStatus.DROPPED.primaryColor(),
                        modifier = itemModifier,
                        isLoading = isLoading,
                        contentPadding = cardPadding,
                        iconSize = statIconSize
                    )

                    // Row 3: Plan to Watch/Read & Total Progress
                    StatCard(
                        title = if (mediaType == MediaType.ANIME) stringResource(R.string.ptw) else stringResource(R.string.ptr),
                        value = if (isError) UNKNOWN_CHAR else planToWatch?.toString() ?: "0",
                        icon = if (mediaType == MediaType.ANIME) ListStatus.PLAN_TO_WATCH.icon else ListStatus.PLAN_TO_READ.icon,
                        containerColor = (if (mediaType == MediaType.ANIME) ListStatus.PLAN_TO_WATCH else ListStatus.PLAN_TO_READ).primaryColor().copy(alpha = 0.12f),
                        contentColor = (if (mediaType == MediaType.ANIME) ListStatus.PLAN_TO_WATCH else ListStatus.PLAN_TO_READ).primaryColor(),
                        modifier = itemModifier,
                        isLoading = isLoading,
                        contentPadding = cardPadding,
                        iconSize = statIconSize
                    )
                    StatCard(
                        title = progressLabel,
                        value = if (isError) UNKNOWN_CHAR else totalProgress?.toString() ?: "0",
                        icon = progressIcon,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = itemModifier,
                        isLoading = isLoading,
                        contentPadding = cardPadding,
                        iconSize = statIconSize
                    )
                }

                // Divider-less secondary info
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextIconVertical(
                        text = if (isError) UNKNOWN_CHAR else if (mediaType == MediaType.ANIME)
                            uiState.user?.animeStatistics?.numDays.toStringOrZero()
                        else uiState.userMangaStats?.days.toStringOrZero(),
                        icon = R.drawable.ic_round_event_24,
                        tooltip = stringResource(R.string.days),
                        isLoading = isLoading,
                        style = MaterialTheme.typography.labelMedium
                    )
                    
                    TextIconVertical(
                        text = if (isError) UNKNOWN_CHAR else if (mediaType == MediaType.ANIME)
                            uiState.user?.animeStatistics?.meanScore.toStringOrZero()
                        else uiState.userMangaStats?.meanScore.toStringOrZero(),
                        icon = R.drawable.ic_round_details_star_24,
                        tooltip = stringResource(R.string.mean_score),
                        isLoading = isLoading,
                        style = MaterialTheme.typography.labelMedium
                    )
                    TextIconVertical(
                        text = if (isError) UNKNOWN_CHAR else if (mediaType == MediaType.ANIME)
                            uiState.user?.animeStatistics?.numTimesRewatched.toStringOrZero()
                        else uiState.userMangaStats?.repeat.toStringOrZero(),
                        icon = R.drawable.round_repeat_24,
                        tooltip = if (mediaType == MediaType.ANIME) stringResource(R.string.rewatched)
                        else stringResource(R.string.total_rereads),
                        isLoading = isLoading,
                        style = MaterialTheme.typography.labelMedium
                    )
                    
                    if (mediaType == MediaType.MANGA) {
                        TextIconVertical(
                            text = if (isError) UNKNOWN_CHAR else uiState.userMangaStats?.volumesRead.toStringOrZero(),
                            icon = R.drawable.ic_outline_book_24,
                            tooltip = stringResource(R.string.volumes),
                            isLoading = isLoading,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: Int,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    contentPadding: Dp = 12.dp,
    iconSize: Dp = 20.dp
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.defaultPlaceholder(visible = isLoading)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
