package com.axiel7.moelist.ui.details.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.RelationType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.InfoTitle
import com.axiel7.moelist.ui.composables.PosterSection
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.RelationTag
import com.axiel7.moelist.ui.composables.posterSharedKey
import com.axiel7.moelist.ui.details.MediaDetailsUiState
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.Check

/**
 * Normalized UI model so the RelatedAnime and RelatedManga types (which do not share a
 * common supertype here) can be displayed and filtered through one code path.
 */
@Immutable
private data class RelatedRow(
    val id: Int,
    val imageUrl: String?,
    val title: String,
    val relation: String,
    val relationType: RelationType,
    val mediaType: MediaType,
)

/**
 * Franchise timeline (sequels / prequels) and the other related entries, filterable by relation.
 * Each poster is a shared element scoped to this details screen (see [PosterSection.details]).
 */
@Composable
fun DetailsRelated(
    uiState: MediaDetailsUiState,
    navActionManager: NavActionManager,
    modifier: Modifier = Modifier,
) {
    // Keys are scoped to this details screen, so a related poster never shares a key with the
    // hero of the details screen it came from (related lists are often two-way)
    val relatedSection = PosterSection.details(uiState.mediaDetails?.id)
    val all = uiState.relatedAnime.map {
        RelatedRow(
            id = it.node.id,
            imageUrl = it.node.mainPicture?.large,
            title = it.node.userPreferredTitle(),
            relation = it.relationType.localized(),
            relationType = it.relationType,
            mediaType = MediaType.ANIME,
        )
    } + uiState.relatedManga.map {
        RelatedRow(
            id = it.node.id,
            imageUrl = it.node.mainPicture?.large,
            title = it.node.userPreferredTitle(),
            relation = it.relationType.localized(),
            relationType = it.relationType,
            mediaType = MediaType.MANGA,
        )
    }
    if (all.isEmpty()) return

    Column(modifier = modifier) {

        // Timeline focus: Sequels and Prequels
        val timeline = all.filter { it.relationType == RelationType.SEQUEL || it.relationType == RelationType.PREQUEL }
        val others = all.filter { it.relationType != RelationType.SEQUEL && it.relationType != RelationType.PREQUEL }

        if (timeline.isNotEmpty()) {
            InfoTitle(text = stringResource(R.string.franchise_timeline))
            LazyRow(
                modifier = Modifier.padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = DetailsHorizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = timeline,
                    key = { "${it.mediaType}-${it.id}" }
                ) { row ->
                    MediaItemVertical(
                        imageUrl = row.imageUrl,
                        posterKey = posterSharedKey(relatedSection, row.mediaType, row.id),
                        title = row.title,
                        subtitle = { RelationTag(text = row.relation) },
                        minLines = 2,
                        onClick = dropUnlessResumed {
                            navActionManager.toMediaDetails(
                                row.mediaType,
                                row.id,
                                posterUrl = row.imageUrl,
                                posterKey = posterSharedKey(relatedSection, row.mediaType, row.id),
                            )
                        }
                    )
                }
            }
        }

        if (others.isNotEmpty()) {
            InfoTitle(
                text = if (uiState.isAnime) stringResource(R.string.related_anime)
                else stringResource(R.string.related_manga)
            )

            val categories = listOf<String?>(null) + others.map { it.relation }.distinct()
            var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
            val filtered = if (selectedCategory == null) others
            else others.filter { it.relation == selectedCategory }

            var showAll by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(selectedCategory) { showAll = false }
            val displayItems = if (showAll || filtered.size <= 5) filtered else filtered.take(5)

            LazyRow(
                contentPadding = PaddingValues(horizontal = DetailsHorizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(categories) { category ->
                    val selected = selectedCategory == category
                    InputChip(
                        selected = selected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(text = category ?: stringResource(R.string.all))
                        },
                        leadingIcon = if (selected) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null
                    )
                }
            }

            LazyRow(
                modifier = Modifier.padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = DetailsHorizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = displayItems,
                    key = { "${it.mediaType}-${it.id}" }
                ) { row ->
                    MediaItemVertical(
                        imageUrl = row.imageUrl,
                        posterKey = posterSharedKey(relatedSection, row.mediaType, row.id),
                        title = row.title,
                        subtitle = { RelationTag(text = row.relation) },
                        minLines = 2,
                        onClick = dropUnlessResumed {
                            navActionManager.toMediaDetails(
                                row.mediaType,
                                row.id,
                                posterUrl = row.imageUrl,
                                posterKey = posterSharedKey(relatedSection, row.mediaType, row.id),
                            )
                        }
                    )
                }
            }

            if (filtered.size > 5) {
                Text(
                    text = if (showAll) stringResource(R.string.show_less)
                    else stringResource(R.string.show_more),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 4.dp, bottom = 8.dp)
                        .clickable { showAll = !showAll }
                )
            }
        }
    }
}
