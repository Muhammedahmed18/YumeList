package com.axiel7.moelist.ui.composables.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.ui.composables.TextIconHorizontal
import com.axiel7.moelist.ui.composables.score.MyScoreBadge
import com.axiel7.moelist.ui.composables.skeleton
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.ShapeCard
import com.axiel7.moelist.ui.theme.ShapeChip
import com.axiel7.moelist.ui.theme.ShapeExtraSmall
import com.axiel7.moelist.ui.theme.ShapePoster

/**
 * The one horizontal media card, used by Search and Ranking.
 *
 * Poster: optional [topBadge] at the top-start (the rank) and the user's own score (heart) at
 * the bottom-start, like My List. Text side: title, metadata tags, then a stats row with the
 * community mean score, optional member count and the list status in its own colour.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaItemRow(
    title: String,
    imageUrl: String?,
    tags: List<String>,
    meanScore: String?,
    personalScore: Int,
    status: ListStatus?,
    onClick: () -> Unit,
    // Shared-transition key of the poster (see posterSharedKey); null = no shared transition
    posterKey: String? = null,
    members: String? = null,
    topBadge: (@Composable () -> Unit)? = null,
) {
    Card(
        onClick = onClick,
        shape = ShapeCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(modifier = Modifier.height(MEDIA_POSTER_SMALL_HEIGHT.dp)) {
            Box(
                modifier = Modifier.size(
                    width = MEDIA_POSTER_SMALL_WIDTH.dp,
                    height = MEDIA_POSTER_SMALL_HEIGHT.dp
                )
            ) {
                // MediaPoster clips itself (ShapePoster), so the corners stay rounded in the transition
                MediaPoster(
                    url = imageUrl,
                    showShadow = false,
                    sharedKey = posterKey,
                    modifier = Modifier.fillMaxSize()
                )
                if (topBadge != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) { topBadge() }
                }
                MyScoreBadge(
                    score = personalScore,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )

                if (tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        maxLines = 1,
                    ) {
                        tags.forEach { MetaTag(text = it) }
                    }
                }

                if (meanScore != null || members != null || status != null) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        itemVerticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (meanScore != null) {
                            TextIconHorizontal(
                                text = meanScore,
                                icon = R.drawable.ic_round_details_star_24,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                iconSize = 16.dp
                            )
                        }
                        if (members != null) {
                            TextIconHorizontal(
                                text = members,
                                icon = R.drawable.ic_round_group_24,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                iconSize = 16.dp
                            )
                        }
                        if (status != null) {
                            MediaStatusIndicator(status = status)
                        }
                    }
                }
            }
        }
    }
}

/** Loading skeleton with the same layout as [MediaItemRow] (title, tags, stats). */
@Composable
fun MediaItemRowPlaceholder() {
    Card(
        shape = ShapeCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(modifier = Modifier.height(MEDIA_POSTER_SMALL_HEIGHT.dp)) {
            Box(
                modifier = Modifier
                    .size(width = MEDIA_POSTER_SMALL_WIDTH.dp, height = MEDIA_POSTER_SMALL_HEIGHT.dp)
                    .clip(ShapePoster)
                    .skeleton()
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
            ) {
                Bar(width = 180, height = 16)
                Bar(width = 120, height = 16)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { Bar(width = 48, height = 20, chip = true) }
                }
                Bar(width = 90, height = 14)
            }
        }
    }
}

@Composable
private fun Bar(width: Int, height: Int, chip: Boolean = false) {
    Box(
        modifier = Modifier
            .size(width = width.dp, height = height.dp)
            .clip(if (chip) ShapeChip else ShapeExtraSmall)
            .skeleton()
    )
}

@Preview
@Composable
private fun MediaItemRowPreview() {
    MoeListTheme {
        Column {
            MediaItemRow(
                title = "Frieren: Beyond Journey's End and a very long title to preview wrapping",
                imageUrl = null,
                tags = listOf("TV", "28 Episodes", "Fall 2023"),
                meanScore = "9.31",
                members = "1,024,512",
                personalScore = 8,
                status = ListStatus.WATCHING,
                onClick = {}
            )
            MediaItemRowPlaceholder()
        }
    }
}
