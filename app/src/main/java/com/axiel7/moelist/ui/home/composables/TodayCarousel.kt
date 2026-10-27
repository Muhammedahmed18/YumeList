package com.axiel7.moelist.ui.home.composables

import androidx.compose.animation.EnterExitState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselItemScope
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.ui.composables.LocalNavAnimatedVisibilityScope
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.composables.media.PosterStatusBadge
import com.axiel7.moelist.ui.composables.score.PosterScoreChip
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.Schedule
import com.axiel7.moelist.ui.theme.ShapePoster

/** Height of the Today carousel. */
val TodayCarouselHeight: Dp = 260.dp

/** Width the carousel aims for its large (focused) item. */
private val PreferredItemWidth: Dp = 186.dp

/**
 * "Today" as an M3 Expressive **multi-browse carousel**: one large poster card in focus,
 * with smaller ones peeking next to it that grow as they scroll in.
 *
 * - Each card is the poster itself, with the airing time, title and score over a soft scrim.
 *   The text fades out as a card shrinks (read in the draw phase, so scrolling never recomposes).
 * - Only the focused (large) card is a shared element, so tapping it flies the poster into
 *   details; a half-hidden small card never flies from its full, unmasked bounds.
 * - While loading with nothing to show yet, skeleton cards are shown in the same carousel.
 *
 * @param posterKey shared-transition key for an anime (see posterSharedKey)
 * @param onClick called with the anime and its shared-transition key
 */
@Composable
fun TodayCarousel(
    items: List<AnimeRanking>,
    isLoading: Boolean,
    hideScore: Boolean,
    posterKey: (AnimeRanking) -> String,
    onClick: (item: AnimeRanking, posterKey: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showSkeleton = isLoading && items.isEmpty()
    val itemCount = if (showSkeleton) SKELETON_COUNT else items.size
    val state = rememberCarouselState { itemCount }

    HorizontalMultiBrowseCarousel(
        state = state,
        preferredItemWidth = PreferredItemWidth,
        modifier = modifier
            .fillMaxWidth()
            .height(TodayCarouselHeight),
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) { index ->
        if (showSkeleton) {
            Box(
                modifier = Modifier
                    .height(TodayCarouselHeight)
                    .maskClip(ShapePoster)
                    .defaultPlaceholder(visible = true)
            )
        } else {
            val item = items.getOrNull(index) ?: return@HorizontalMultiBrowseCarousel
            TodayCard(
                item = item,
                hideScore = hideScore,
                posterKey = posterKey(item),
                onClick = onClick,
            )
        }
    }
}

private const val SKELETON_COUNT = 4

@Composable
private fun CarouselItemScope.TodayCard(
    item: AnimeRanking,
    hideScore: Boolean,
    posterKey: String,
    onClick: (item: AnimeRanking, posterKey: String?) -> Unit,
) {
    val info = carouselItemDrawInfo
    // Only recomposes when the card crosses the "focused" threshold, not on every scroll frame
    val isFocused by remember(info) {
        derivedStateOf { info.maxSize > 0f && info.size >= info.maxSize * 0.9f }
    }
    // No shared element while Home is coming back from details. Carousel items are placed by
    // the pager and then shifted into their keyline slots with a graphics-layer translation;
    // the shared-element system targets the un-shifted (layout) position, so the returning
    // poster flew in from the side and then jumped. Forward it's fine (the start bounds are
    // read from what's on screen), so the flight into details is kept.
    val navTransition = LocalNavAnimatedVisibilityScope.current?.transition
    val isReturning = navTransition != null &&
        navTransition.targetState == EnterExitState.Visible &&
        navTransition.currentState != EnterExitState.Visible
    val sharedKey = posterKey.takeIf { isFocused && !isReturning }
    val score = item.node.mean

    Box(
        modifier = Modifier
            .height(TodayCarouselHeight)
            // Same shape as every other poster (and the details hero), so a shared flight
            // keeps its corners
            .maskClip(ShapePoster)
            .clickable { onClick(item, sharedKey) }
    ) {
        MediaPoster(
            url = item.node.mainPicture?.large ?: item.node.mainPicture?.medium,
            sharedKey = sharedKey,
            modifier = Modifier.matchParentSize()
        )

        // Soft scrim so the text stays readable on any poster
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0.45f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.78f)
                    )
                )
        )

        // Fades with the text as the card shrinks (draw phase only)
        val textAlpha: () -> Float = {
            val range = info.maxSize - info.minSize
            if (range > 0f) ((info.size - info.minSize) / range).coerceIn(0f, 1f) else 1f
        }

        // Local airing time ("5:25 PM") as a pill in the top corner: short, so it always fits
        item.node.broadcast?.localStartTime()?.let { time ->
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .graphicsLayer { alpha = textAlpha() }
            ) {
                Row(
                    modifier = Modifier.padding(start = 6.dp, end = 8.dp, top = 3.dp, bottom = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelMediumEmphasized,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        item.node.myListStatus?.status?.let { status ->
            PosterStatusBadge(
                status = status,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                // Fade the text as the card shrinks (draw phase only)
                .graphicsLayer { alpha = textAlpha() }
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // "Airing in 6h" on its own line above the title, at full card width. It used to
            // share one small pill with the clock time ("Airing in 6h (5:25 PM)"), which was cut
            // off on the 186dp card; the time now has its own pill at the top.
            val airingIn = item.node.broadcast?.airingInString().orEmpty()
            if (airingIn.isNotEmpty()) {
                Text(
                    text = airingIn,
                    style = MaterialTheme.typography.labelMediumEmphasized,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = item.node.userPreferredTitle(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!hideScore && score != null && score > 0f) {
                PosterScoreChip(score = score)
            }
        }
    }
}
