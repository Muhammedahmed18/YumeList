package com.axiel7.moelist.ui.details.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.details.MediaDetailsUiState
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.TrendingUp
import com.axiel7.moelist.ui.icons.rounded.EmojiEvents
import com.axiel7.moelist.ui.icons.rounded.Group
import com.axiel7.moelist.ui.icons.rounded.Star
import com.axiel7.moelist.utils.NumExtensions.format
import com.axiel7.moelist.utils.StringExtensions.toStringOrNull
import com.axiel7.moelist.utils.UNKNOWN_CHAR
import androidx.compose.foundation.shape.CornerSize
import com.axiel7.moelist.ui.theme.connectedRowShape

/**
 * Score, rank, popularity and members as four **connected tiles**: they sit 2dp apart, with
 * large corners on the outside of the row and small corners between tiles, so they read as
 * one group (the M3 Expressive connected style).
 *
 * Values shrink to fit (auto-size), so a long number like "1,234,567" never wraps or clips.
 */
@Composable
fun DetailsStatTiles(
    uiState: MediaDetailsUiState,
    modifier: Modifier = Modifier,
) {
    val details = uiState.mediaDetails
    val tiles = listOf(
        StatTile(
            value = details?.mean.toStringOrNull() ?: UNKNOWN_CHAR,
            label = stringResource(R.string.score),
            icon = Icons.Rounded.Star,
            // Same star colour as every other score in the app (PosterScoreChip, SmallScoreIndicator)
            iconTint = MaterialTheme.colorScheme.tertiary,
        ),
        StatTile(
            value = details?.rankText() ?: UNKNOWN_CHAR,
            label = stringResource(R.string.top_ranked),
            icon = Icons.Rounded.EmojiEvents,
        ),
        StatTile(
            value = details?.popularity?.let { "#$it" } ?: UNKNOWN_CHAR,
            label = stringResource(R.string.popularity),
            icon = Icons.AutoMirrored.Rounded.TrendingUp,
        ),
        StatTile(
            value = details?.numListUsers?.format() ?: UNKNOWN_CHAR,
            label = stringResource(R.string.members),
            icon = Icons.Rounded.Group,
        ),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            // Equal tile heights even if one label wraps differently
            .height(IntrinsicSize.Min)
            .padding(horizontal = DetailsHorizontalPadding, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(DetailsTileGap)
    ) {
        tiles.forEachIndexed { index, tile ->
            val isFirst = index == 0
            val isLast = index == tiles.lastIndex
            val shape = connectedRowShape(
                isFirst = isFirst,
                isLast = isLast,
                outer = CornerSize(DetailsCardCorner),
            )
            StatTileView(
                tile = tile,
                isLoading = uiState.isLoading,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(shape)
            )
        }
    }
}

private data class StatTile(
    val value: String,
    val label: String,
    val icon: ImageVector,
    val iconTint: Color? = null,
)

@Composable
private fun StatTileView(
    tile: StatTile,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .defaultPlaceholder(visible = isLoading)
            // Read as one item by TalkBack: "Score, 8.61"
            .clearAndSetSemantics { contentDescription = "${tile.label}, ${tile.value}" }
            .padding(horizontal = 6.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = tile.icon,
            contentDescription = null,
            tint = tile.iconTint ?: MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = tile.value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 11.sp,
                maxFontSize = MaterialTheme.typography.titleMedium.fontSize
            )
        )
        Text(
            text = tile.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
