package com.axiel7.moelist.ui.composables.media

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.ShapePoster

/**
 * Search result card: a taller cover with the list-status shown as a banner over
 * the poster, the metadata broken out into chips, and a stats row with the mean
 * score and the user's personal score.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaItemSearch(
    title: String,
    imageUrl: String?,
    chips: List<String>,
    meanScore: String?,
    personalScore: Int,
    status: ListStatus?,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.height(MEDIA_POSTER_SMALL_HEIGHT.dp)
        ) {
            Box(
                modifier = Modifier.size(
                    width = MEDIA_POSTER_SMALL_WIDTH.dp,
                    height = MEDIA_POSTER_SMALL_HEIGHT.dp
                )
            ) {
                MediaPoster(
                    url = imageUrl,
                    showShadow = false,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(ShapePoster)
                )

                if (status != null) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(status.primaryColor())
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = status.icon,
                            contentDescription = null,
                            tint = status.onPrimaryColor(),
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = status.localized(),
                            modifier = Modifier.padding(start = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = status.onPrimaryColor(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
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

                if (chips.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        chips.forEach { MetaChip(text = it) }
                    }
                }

                if (meanScore != null || personalScore > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                        if (personalScore > 0) {
                            TextIconHorizontal(
                                text = personalScore.toString(),
                                icon = R.drawable.ic_round_person_24,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                                iconSize = 16.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaChip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}

@Preview
@Composable
fun MediaItemSearchPreview() {
    MoeListTheme {
        MediaItemSearch(
            title = "Frieren: Beyond Journey's End and a very long title to preview wrapping",
            imageUrl = null,
            chips = listOf("TV", "28 Episodes", "Fall 2023"),
            meanScore = "9.31",
            personalScore = 8,
            status = ListStatus.WATCHING,
            onClick = {}
        )
    }
}
