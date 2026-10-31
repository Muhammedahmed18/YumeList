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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.details.MediaDetailsUiState
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.MenuBook
import com.axiel7.moelist.ui.icons.rounded.Add
import com.axiel7.moelist.ui.icons.rounded.Bookmark
import com.axiel7.moelist.ui.icons.rounded.Event
import com.axiel7.moelist.ui.icons.rounded.History
import com.axiel7.moelist.ui.icons.rounded.Info
import com.axiel7.moelist.ui.icons.rounded.LiveTv
import com.axiel7.moelist.ui.icons.rounded.Movie
import com.axiel7.moelist.ui.icons.rounded.RssFeed
import com.axiel7.moelist.utils.DateUtils.parseDateAndLocalize

/** One fact in the "More info" grid. */
private data class InfoEntry(
    val title: String,
    val value: String?,
    val icon: ImageVector,
)

/**
 * "More info": studios / source / dates / season / broadcast (anime) or authors /
 * serialization / dates / status / volumes (manga), as a 2-column grid of **connected tiles**
 * (same style as the stat tiles: 2dp apart, large outer corners, small inner corners).
 */
@Composable
fun DetailsInfo(
    uiState: MediaDetailsUiState,
    modifier: Modifier = Modifier,
) {
    val details = uiState.mediaDetails
    val entries = if (uiState.isAnime) {
        val anime = details as? AnimeDetails
        listOf(
            InfoEntry(stringResource(R.string.studios), uiState.studiosJoined, Icons.Rounded.Movie),
            InfoEntry(stringResource(R.string.source), anime?.source?.localized(), Icons.Rounded.History),
            InfoEntry(stringResource(R.string.start_date), details?.startDate?.parseDateAndLocalize(), Icons.Rounded.Add),
            InfoEntry(stringResource(R.string.end_date), details?.endDate?.parseDateAndLocalize(), Icons.Rounded.Event),
            InfoEntry(stringResource(R.string.season), anime?.startSeason?.seasonYearText(), Icons.Rounded.RssFeed),
            InfoEntry(
                stringResource(R.string.broadcast),
                anime?.broadcast?.timeText(isAiring = false),
                Icons.Rounded.LiveTv
            ),
        )
    } else {
        val manga = details as? MangaDetails
        listOf(
            InfoEntry(
                stringResource(R.string.authors),
                manga?.authors?.joinToString { "${it.node.firstName} ${it.node.lastName}" },
                Icons.Rounded.Movie
            ),
            InfoEntry(stringResource(R.string.serialization), uiState.serializationJoined, Icons.Rounded.Bookmark),
            InfoEntry(stringResource(R.string.start_date), details?.startDate?.parseDateAndLocalize(), Icons.Rounded.Add),
            InfoEntry(stringResource(R.string.end_date), details?.endDate?.parseDateAndLocalize(), Icons.Rounded.Event),
            InfoEntry(stringResource(R.string.status), details?.status?.localized(), Icons.Rounded.Info),
            InfoEntry(
                stringResource(R.string.volumes),
                manga?.numVolumes.countOrDash(),
                Icons.AutoMirrored.Rounded.MenuBook
            ),
        )
    }
    val rows = entries.chunked(INFO_COLUMNS)

    Column(modifier = modifier) {
        DetailsSectionTitle(text = stringResource(R.string.more_info))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DetailsHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(DetailsTileGap)
        ) {
            rows.forEachIndexed { rowIndex, rowEntries ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Both tiles in a row get the height of the taller one
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(DetailsTileGap)
                ) {
                    rowEntries.forEachIndexed { columnIndex, entry ->
                        InfoTile(
                            entry = entry,
                            isLoading = uiState.isLoading,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(
                                    connectedGridShape(
                                        row = rowIndex,
                                        column = columnIndex,
                                        rows = rows.size,
                                        columns = INFO_COLUMNS
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

private const val INFO_COLUMNS = 2

@Composable
private fun InfoTile(
    entry: InfoEntry,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val value = entry.value ?: stringResource(R.string.unknown)
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .defaultPlaceholder(visible = isLoading)
            // Read as one item by TalkBack: "Studios, Wit Studio"
            .clearAndSetSemantics { contentDescription = "${entry.title}, $value" }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            Icon(
                imageVector = entry.icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(7.dp)
                    .size(18.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
