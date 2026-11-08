package com.axiel7.moelist.ui.details.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.PosterSection
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.posterSharedKey
import com.axiel7.moelist.ui.details.MediaDetailsUiState

/**
 * "If you liked this" picks from MAL users, most recommended first, under Related. Titles
 * already shown in Related are left out. Hidden when there's nothing to show.
 */
@Composable
fun DetailsRecommendations(
    uiState: MediaDetailsUiState,
    navActionManager: NavActionManager,
    modifier: Modifier = Modifier,
) {
    val picks = uiState.visibleRecommendations
    if (picks.isEmpty()) return
    // Own section key: the same title can be in Related and here on the same screen
    val section = PosterSection.detailsRecommendations(uiState.mediaDetails?.id)

    Column(modifier = modifier) {
        DetailsSectionTitle(text = stringResource(R.string.recommendations))
        DetailsSectionCard(contentPadding = PaddingValues(vertical = 12.dp)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = picks,
                    key = { "${it.node.mediaType}-${it.node.id}" }
                ) { recommendation ->
                    val node = recommendation.node
                    val posterKey = posterSharedKey(section, node.mediaType, node.id)
                    val imageUrl = node.mainPicture?.large ?: node.mainPicture?.medium
                    MediaItemVertical(
                        imageUrl = imageUrl,
                        posterKey = posterKey,
                        title = node.userPreferredTitle(),
                        score = node.mean.takeUnless { uiState.hideScore },
                        subtitle = if (recommendation.numRecommendations > 0) {
                            {
                                Text(
                                    text = pluralStringResource(
                                        R.plurals.num_recommended_by,
                                        recommendation.numRecommendations,
                                        recommendation.numRecommendations
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        } else null,
                        minLines = 2,
                        onClick = dropUnlessResumed {
                            navActionManager.toMediaDetails(
                                node.mediaType,
                                node.id,
                                posterUrl = imageUrl,
                                posterKey = posterKey,
                            )
                        }
                    )
                }
            }
        }
    }
}
