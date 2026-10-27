package com.axiel7.moelist.ui.details.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_BIG_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_BIG_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.details.MediaDetailsUiState
import com.axiel7.moelist.ui.theme.ShapePoster

/**
 * Header of the details screen, drawn over [DetailsBackdrop]: the poster (the landing spot of
 * the shared poster transition) and, next to it, quick facts, the airing status and the
 * list-status split button. The title lives in the top app bar, which collapses into it.
 */
@Composable
fun DetailsHero(
    uiState: MediaDetailsUiState,
    initialPosterUrl: String?,
    initialPosterKey: String?,
    onPosterClick: () -> Unit,
    onEditClick: () -> Unit,
    onStatusSelected: (ListStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    val details = uiState.mediaDetails

    Row(
        modifier = modifier.padding(
            start = DetailsHorizontalPadding,
            end = DetailsHorizontalPadding,
            top = 4.dp,
            bottom = 16.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MediaPoster(
            url = details?.mainPicture?.large ?: initialPosterUrl,
            // The image tapped on the previous screen stays underneath until the large
            // one has loaded (the Home airing row passes the smaller `medium` image)
            lowResUrl = initialPosterUrl,
            shape = ShapePoster,
            sharedKey = initialPosterKey,
            // Back swipe: the hero slides away with the page instead of flying to the list
            flyOnExit = false,
            modifier = Modifier
                .size(
                    width = MEDIA_POSTER_BIG_WIDTH.dp,
                    height = MEDIA_POSTER_BIG_HEIGHT.dp
                )
                // Same shape as the poster (MediaPoster clips itself too); kept here so
                // the placeholder and the click ripple are rounded as well
                .clip(ShapePoster)
                // No skeleton over the poster when we already have it from the previous screen
                .defaultPlaceholder(visible = uiState.isLoading && initialPosterUrl == null)
                .clickable(onClick = dropUnlessResumed { onPosterClick() })
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .height(MEDIA_POSTER_BIG_HEIGHT.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Quick facts: [ TV | 2026 ] on top, [ Episodes (12) ] below
            val formatAndYear = details?.mediaFormatWithYear()
                ?.split("•", "·")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?: listOf("…")
            val countText = when (details) {
                is AnimeDetails ->
                    "${stringResource(R.string.episodes).replaceFirstChar { it.uppercase() }} (${details.numEpisodes.countOrDash()})"
                is MangaDetails ->
                    "${stringResource(R.string.chapters).replaceFirstChar { it.uppercase() }} (${details.numChapters.countOrDash()})"
                else -> null
            }
            DetailsConnectedPills(
                segments = formatAndYear,
                modifier = Modifier.defaultPlaceholder(visible = uiState.isLoading)
            )
            if (countText != null) {
                DetailsConnectedPills(
                    segments = listOf(countText),
                    accentFirst = false
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            details?.status?.let { status ->
                AiringStatusCard(
                    status = status,
                    scheduleText = if (status == MediaStatus.AIRING) {
                        (details as? AnimeDetails)?.broadcast?.localDayAndTimeText()
                    } else null
                )
            }

            if (details != null) {
                DetailsStatusButton(
                    currentStatus = uiState.myListStatus?.status,
                    statuses = if (uiState.isAnime) ListStatus.listStatusAnimeValues
                    else ListStatus.listStatusMangaValues,
                    onEditClick = onEditClick,
                    onStatusSelected = onStatusSelected,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** Airing / publishing status with its color dot and, while airing, the local schedule. */
@Composable
private fun AiringStatusCard(
    status: MediaStatus,
    scheduleText: String?,
) {
    val statusColor = status.statusColor()
    Surface(
        shape = RoundedCornerShape(DetailsCardCorner),
        color = statusColor.copy(alpha = 0.12f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = statusColor,
                    modifier = Modifier.size(8.dp)
                ) {}
                Text(
                    text = status.localized(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
            if (!scheduleText.isNullOrEmpty()) {
                Text(
                    text = scheduleText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
