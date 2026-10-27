package com.axiel7.moelist.ui.details.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.composables.InfoTitle
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.details.MediaDetailsUiState
import com.axiel7.moelist.utils.NumExtensions.format

/**
 * Community status distribution (watching / completed / on hold / dropped / planned), as
 * colored bars with percentages. Rank, popularity and members moved up to [DetailsStatTiles].
 */
@Composable
fun DetailsStatusDistribution(
    uiState: MediaDetailsUiState,
    modifier: Modifier = Modifier,
) {
    if (!uiState.isLoadingStatusDistribution && uiState.statusDistribution == null) return
    Column(modifier = modifier) {
        InfoTitle(text = stringResource(R.string.status_distribution))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DetailsHorizontalPadding),
            shape = RoundedCornerShape(DetailsCardCorner),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val stats = uiState.statusDistribution
                if (uiState.isLoadingStatusDistribution || stats == null) {
                    repeat(5) { StatusDistributionShimmerRow() }
                } else {
                    val total = remember(stats) { stats.sumOf { it.value.toDouble() } }
                    stats.forEach { stat ->
                        StatusBarRow(
                            color = stat.type.primaryColor(),
                            label = stat.type.localized(),
                            value = stat.value.toDouble(),
                            total = total
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusDistributionShimmerRow() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .defaultPlaceholder(visible = true)
            )
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .defaultPlaceholder(visible = true)
            )
            Spacer(
                modifier = Modifier
                    .width(52.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .defaultPlaceholder(visible = true)
            )
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .defaultPlaceholder(visible = true)
        )
    }
}

/**
 * A single status-distribution row: colored dot + label, a progress bar, and a
 * trailing percentage with raw count.
 */
@Composable
private fun StatusBarRow(
    color: Color,
    label: String,
    value: Double,
    total: Double,
) {
    val fraction = if (total > 0) (value / total).toFloat() else 0f
    val percentString = (fraction * 100).format() ?: "0"
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = color,
                modifier = Modifier.size(8.dp)
            ) {}
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            )
            Text(
                text = "$percentString%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = " (${value.toInt().format() ?: value.toInt().toString()})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            gapSize = (-1).dp,
            drawStopIndicator = {}
        )
    }
}
