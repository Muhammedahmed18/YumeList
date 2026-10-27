package com.axiel7.moelist.ui.details.sections

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/** Height of the blurred backdrop behind the top app bar and the hero. */
private val BackdropHeight: Dp = 440.dp

/**
 * Blurred, color-tinted copy of the poster drawn behind the top app bar and the hero.
 *
 * - Uses the small `medium` image: it's blurred anyway, so it's cheap to load and draw.
 * - Blur needs Android 12+ (RenderEffect). On older versions a soft primary-container tint is
 *   shown instead, so there's never an unblurred, noisy image behind the text.
 * - Fades out as the hero scrolls away. The scroll position is read inside `graphicsLayer`, so
 *   scrolling only redraws this layer and never recomposes the screen.
 * - Ends in the surface color, so it blends into the page (pure black in AMOLED mode).
 */
@Composable
fun DetailsBackdrop(
    imageUrl: String?,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val surface = MaterialTheme.colorScheme.surface
    val tint = MaterialTheme.colorScheme.primaryContainer
    val canBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BackdropHeight)
            .graphicsLayer {
                val scrolled = if (listState.firstVisibleItemIndex == 0) {
                    listState.firstVisibleItemScrollOffset.toFloat()
                } else {
                    size.height
                }
                alpha = (1f - scrolled / size.height).coerceIn(0f, 1f)
            }
    ) {
        if (canBlur && imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .blur(36.dp)
                    .graphicsLayer { alpha = 0.6f }
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(tint.copy(alpha = 0.35f))
            )
        }
        // Tint + fade into the page color
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to surface.copy(alpha = 0.25f),
                        0.55f to surface.copy(alpha = 0.7f),
                        1.0f to surface
                    )
                )
        )
    }
}
