package com.axiel7.moelist.ui.composables.media

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.ui.composables.score.PosterScoreChip
import com.axiel7.moelist.ui.composables.skeleton
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.Motion
import com.axiel7.moelist.ui.theme.ShapePoster

const val MEDIA_ITEM_VERTICAL_HEIGHT = 220

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaItemVertical(
    title: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    // Shared-transition key of the poster (see posterSharedKey); null = no shared transition
    posterKey: String? = null,
    posterOverlay: @Composable (BoxScope.() -> Unit)? = null,
    // List status, shown as a coloured icon chip at the top-end corner of the poster
    status: ListStatus? = null,
    // Community mean score, shown as a chip at the bottom-start corner (null or 0 = hidden)
    score: Float? = null,
    subtitle: @Composable (() -> Unit)? = null,
    subtitle2: @Composable (() -> Unit)? = null,
    minLines: Int = 1,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    // Press feedback: fast expressive spring so the poster "squishes" and bounces back
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = Motion.fastSpatial(),
        label = "scale"
    )

    Column(
        modifier = modifier
            .width(MEDIA_POSTER_SMALL_WIDTH.dp)
            .sizeIn(minHeight = MEDIA_ITEM_VERTICAL_HEIGHT.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier.padding(bottom = 8.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            // MediaPoster clips itself (ShapePoster), so the corners stay rounded in the transition
            MediaPoster(
                url = imageUrl,
                showShadow = false,
                sharedKey = posterKey,
                modifier = Modifier
                    .size(
                        width = MEDIA_POSTER_SMALL_WIDTH.dp,
                        height = MEDIA_POSTER_SMALL_HEIGHT.dp
                    )
            )

            if (score != null && score > 0f) {
                PosterScoreChip(
                    score = score,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.BottomStart),
                )
            }

            posterOverlay?.invoke(this)

            if (status != null) {
                PosterStatusBadge(
                    status = status,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd),
                )
            }
        }

        Text(
            text = title,
            modifier = Modifier
                .width(MEDIA_POSTER_SMALL_WIDTH.dp)
                .padding(horizontal = 4.dp),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            minLines = minLines
        )

        Column(
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
        ) {
            subtitle?.let { it() }
            subtitle2?.let { it() }
        }
    }
}

/**
 * Pulsing loading skeleton. Was a `composed {}` shimmer that rebuilt a gradient brush and
 * recomposed every frame for every placeholder; now delegates to the lightweight [skeleton]
 * modifier node, which only re-draws.
 */
@Composable
fun Modifier.shimmerEffect(): Modifier = skeleton()

@Composable
fun MediaItemVerticalPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(MEDIA_POSTER_SMALL_WIDTH.dp)
            .sizeIn(minHeight = MEDIA_ITEM_VERTICAL_HEIGHT.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .size(
                    width = MEDIA_POSTER_SMALL_WIDTH.dp,
                    height = MEDIA_POSTER_SMALL_HEIGHT.dp
                )
                .clip(ShapePoster)
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(8.dp))

        repeat(2) { line ->
            Box(
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp, bottom = 6.dp)
                    .size(width = if (line == 0) 88.dp else 56.dp, height = 12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
        }
    }
}

@Preview
@Composable
fun MediaItemVerticalPreview() {
    MoeListTheme {
        Surface {
            Row(modifier = Modifier.padding(16.dp)) {
                MediaItemVertical(
                    imageUrl = null,
                    title = "Modern Anime Title",
                    onClick = {}
                )
            }
        }
    }
}
