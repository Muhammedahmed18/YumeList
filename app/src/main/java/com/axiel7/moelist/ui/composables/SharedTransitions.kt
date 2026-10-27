package com.axiel7.moelist.ui.composables

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_BIG_WIDTH
import kotlin.math.abs

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shape
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.theme.Motion
import com.axiel7.moelist.ui.theme.ShapePoster

/**
 * Shared element ("hero") transitions between screens.
 *
 * [MainNavigation] wraps the NavHost in a `SharedTransitionLayout` and provides
 * [LocalSharedTransitionScope]; each destination that takes part provides its
 * [LocalNavAnimatedVisibilityScope] via [ProvideNavAnimatedScope]. Anything outside that
 * setup (previews, dialogs, the widget) simply gets no transition.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/** Which navigation motion is running, set by the NavHost transitions in MainNavigation. */
enum class NavMotionKind { Push, Pop, Fade }

class NavMotionState {
    var kind by mutableStateOf(NavMotionKind.Push)
}

val LocalNavMotion = compositionLocalOf<NavMotionState?> { null }

/** How dark the page underneath gets during the depth slide. */
private const val DEPTH_SCRIM_ALPHA = 0.32f

/**
 * Wraps a navigation destination:
 * - makes its animation scope available to shared elements inside it;
 * - draws the "depth slide" dim: the page underneath (the one being covered on push, or
 *   uncovered on back) is darkened, and clears as it comes back. A real black overlay instead of
 *   transparency, so the empty window never shows through and it looks right in light theme;
 * - gives the page a soft edge shadow, only while it's moving.
 */
@Composable
fun AnimatedVisibilityScope.ProvideNavAnimatedScope(content: @Composable () -> Unit) {
    val motion = LocalNavMotion.current
    val scrimSpec = Motion.effects<Float>()
    val scrim = transition.animateFloat(
        transitionSpec = { scrimSpec },
        label = "depthScrim",
    ) { state ->
        when (state) {
            EnterExitState.Visible -> 0f
            // Uncovered on back: starts dimmed
            EnterExitState.PreEnter -> if (motion?.kind == NavMotionKind.Pop) DEPTH_SCRIM_ALPHA else 0f
            // Covered on push: ends dimmed
            EnterExitState.PostExit -> if (motion?.kind == NavMotionKind.Push) DEPTH_SCRIM_ALPHA else 0f
        }
    }
    val moving = transition.currentState != transition.targetState
    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (moving) Modifier.shadow(12.dp) else Modifier)
                .drawWithContent {
                    drawContent()
                    val alpha = scrim.value
                    if (alpha > 0f) drawRect(color = Color.Black, alpha = alpha)
                }
        ) {
            content()
        }
    }
}

/**
 * Where a poster lives. Part of the shared key, so the same anime shown in two places at once
 * (e.g. Home's "Today" row and "This season" row, or a details screen and the related row of
 * the next details screen) never produces two posters with the same key.
 */
object PosterSection {
    const val HOME_TODAY = "home_today"
    const val HOME_SEASON = "home_season"
    const val USER_LIST = "user_list"
    const val SEARCH = "search"
    const val RANKING = "ranking"
    const val SEASON_CHART = "season_chart"
    const val CALENDAR = "calendar"

    /** Related / franchise rows on the details screen of [mediaId]. */
    fun details(mediaId: Int?) = "details_$mediaId"
}

/**
 * Shared-transition key for a poster: section + media type + id.
 *
 * Matching by media (not by image URL) keeps the transition working when the two screens show
 * different image sizes (the Home airing row uses MAL's `medium` image, details uses `large`).
 * The source screen passes the same key to [com.axiel7.moelist.ui.base.navigation.NavActionManager.toMediaDetails],
 * so the details hero uses exactly the key of the poster that was tapped.
 */
fun posterSharedKey(section: String, mediaType: MediaType, mediaId: Int): String =
    "poster:$section:${mediaType.name}:$mediaId"

/**
 * Turns a poster into a shared element: the poster with the same [key] on the source screen and
 * on the details screen fly between each other. A null key (or no navigation scope, e.g. in a
 * preview) means the poster just renders normally.
 *
 * Must be placed *before* the poster's clip (see [com.axiel7.moelist.ui.composables.media.MediaPoster]):
 * during the flight only what's inside this modifier is drawn in the overlay, so a clip placed
 * outside it would be left behind and the poster would fly with square corners.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedPoster(
    key: String?,
    shape: Shape = ShapePoster,
    // false = this poster only flies in (details hero). Once it has landed it stays part of the
    // page for good: on back it slides away with the page, and a cancelled back swipe can't make it
    // match the list poster again (no flash / jump).
    flyOnExit: Boolean = true,
): Modifier {
    if (key == null) return this
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val animatedScope = LocalNavAnimatedVisibilityScope.current ?: return this
    val landed = remember { LandedFlag() }
    if (!flyOnExit) {
        val t = animatedScope.transition
        if (t.currentState == EnterExitState.Visible && t.targetState == EnterExitState.Visible) {
            landed.value = true
        }
        if (landed.value) return this
    }
    // Opening details (landing on the hero poster) keeps the expressive bounce. Going back uses a
    // non-overshooting spec, so a predictive-back swipe tracks the finger and lands without a wobble.
    val openSpec = Motion.spatial<Rect>()
    val backSpec = Motion.effects<Rect>()
    val heroWidthPx = with(LocalDensity.current) { MEDIA_POSTER_BIG_WIDTH.dp.toPx() }

    return with(sharedScope) {
        this@sharedPoster.sharedElement(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedScope,
            boundsTransform = BoundsTransform { _, target ->
                if (abs(target.width - heroWidthPx) < 1f) openSpec else backSpec
            },
            // Same shape as the poster itself, so nothing outside the poster (a list's edge,
            // the top bar) cuts it off while it's flying in the overlay
            clipInOverlayDuringTransition = OverlayClip(shape),
        )
    }
}

/** Plain holder (not state): flipping it must not trigger a recomposition by itself. */
private class LandedFlag {
    var value = false
}
