package com.axiel7.moelist.ui.composables.media

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.axiel7.moelist.ui.composables.sharedPoster
import com.axiel7.moelist.ui.composables.skeleton
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.ShapePoster

const val MEDIA_POSTER_RATIO = 2f / 3f

const val MEDIA_POSTER_SMALL_WIDTH = 100
const val MEDIA_POSTER_SMALL_HEIGHT = 150

const val MEDIA_POSTER_MEDIUM_WIDTH = 110
const val MEDIA_POSTER_MEDIUM_HEIGHT = 165

const val MEDIA_POSTER_BIG_WIDTH = 150
const val MEDIA_POSTER_BIG_HEIGHT = 225

/**
 * Media poster image. Shows a pulsing [skeleton] while the image is downloading, then the image;
 * if it fails (or there's no url) it shows a plain surface color.
 *
 * The poster owns its [shape]: it's applied *inside* the shared element, so the rounded corners
 * travel with the poster during the shared transition (callers must not clip it themselves).
 *
 * @param showShadow unused, kept so existing call sites don't need to change
 * @param shape corner shape of the poster; every poster uses [ShapePoster] so the corners stay
 * identical from the source screen to the details screen
 * @param sharedKey shared-transition key (see `posterSharedKey`); null = no shared transition
 * @param lowResUrl an already-loaded smaller image of the same poster (e.g. the one tapped on the
 * previous screen). It's shown underneath while [url] loads, so the poster never flashes empty
 * or swaps to a skeleton when a sharper image replaces it.
 */
@Composable
fun MediaPoster(
    url: String?,
    showShadow: Boolean = true,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = ShapePoster,
    sharedKey: String? = null,
    lowResUrl: String? = null,
    // false = no flight back to the list when this poster's screen leaves (see sharedPoster)
    flyOnExit: Boolean = true,
    modifier: Modifier
) {
    // Starts false so images already in Coil's memory cache appear instantly with no skeleton flash
    var isLoading by remember(url) { mutableStateOf(false) }
    val hasLowRes = lowResUrl != null && lowResUrl != url
    val emptyPainter = ColorPainter(MaterialTheme.colorScheme.surfaceContainerHighest)

    Box(
        modifier = modifier
            // Order matters: shared element first, then the clip, so the clip is part of what flies
            .sharedPoster(key = sharedKey, shape = shape, flyOnExit = flyOnExit)
            .clip(shape)
            // No skeleton when the low-res image is already covering the poster
            .skeleton(visible = isLoading && !hasLowRes)
    ) {
        if (hasLowRes) {
            AsyncImage(
                model = lowResUrl,
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.matchParentSize()
            )
        }
        AsyncImage(
            model = url,
            contentDescription = "poster",
            // Transparent while loading when the low-res image is underneath
            placeholder = if (hasLowRes) null else emptyPainter,
            error = if (hasLowRes) null else emptyPainter,
            fallback = if (hasLowRes) null else emptyPainter,
            contentScale = contentScale,
            // Skeleton only while downloading
            onLoading = { isLoading = true },
            onSuccess = { isLoading = false },
            onError = { isLoading = false },
            modifier = Modifier.matchParentSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MediaPosterPreview() {
    MoeListTheme {
        MediaPoster(
            url = "https://cdn.myanimelist.net/images/anime/1170/124312l.jpg",
            modifier = Modifier
                .size(width = MEDIA_POSTER_SMALL_WIDTH.dp, height = MEDIA_POSTER_SMALL_HEIGHT.dp)
        )
    }
}
