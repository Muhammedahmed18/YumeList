package com.axiel7.moelist.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * One place for app motion, backed by the Material 3 Expressive [MaterialTheme.motionScheme]
 * set in [MoeListTheme].
 *
 * Two families, as defined by M3 Expressive:
 * - **Spatial**: things that move or change size/shape (offset, scale, expand/shrink, rotation).
 *   These are springs with a little bounce, which is the "expressive" feel.
 * - **Effects**: things that should never overshoot (alpha, color, crossfades).
 *
 * Each comes in fast / default / slow. Use `fast` for small elements (press feedback, icons),
 * `default` for most component changes, `slow` for large/full-screen changes.
 *
 * These read the theme, so call them from a @Composable. For non-composable lambdas
 * (NavHost transitions, AnimatedContent transitionSpec) read the spec into a val first.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object Motion {

    @Composable
    fun <T> fastSpatial(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.fastSpatialSpec()

    @Composable
    fun <T> spatial(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.defaultSpatialSpec()

    @Composable
    fun <T> slowSpatial(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.slowSpatialSpec()

    @Composable
    fun <T> fastEffects(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.fastEffectsSpec()

    @Composable
    fun <T> effects(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.defaultEffectsSpec()

    @Composable
    fun <T> slowEffects(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.slowEffectsSpec()
}
