package com.axiel7.moelist.ui.profile.composables

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.composables.RollingNumberText
import com.axiel7.moelist.ui.theme.Motion
import com.axiel7.moelist.utils.UNKNOWN_CHAR
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/** One part of the [LibraryRing]: a list status, its count and its colour. */
@Immutable
data class RingSegment(
    val label: String,
    val value: Int,
    val color: Color,
)

/** Thickness of the ring, and the space between the ring and the avatar. */
private val RingStroke = 8.dp
private val RingGap = 6.dp

/** Visible gap between two segments, in degrees (the rounded caps are accounted for). */
private const val SegmentGapDegrees = 12f

/** One full loading wave, and how far each segment trails the one before it. */
private const val WaveDurationMs = 1400
private const val WaveStagger = 0.16f

/** Delay between segments when they grow in. */
private const val RevealStaggerMs = 110L

/** Total size of a [LibraryRing] around an avatar of [avatarSize]. */
fun libraryRingSize(avatarSize: Dp): Dp = avatarSize + (RingStroke + RingGap) * 2

/**
 * A 3-segment ring around the profile avatar showing how the user's list is split
 * (e.g. Watching / Completed / Plan to Watch). Each segment's length is its share of the three.
 *
 * - **Loading** ([isLoading]): the segments pulse one after another in a looping wave.
 * - **Loaded**: they grow in one by one with a spring; when that follows a load, one
 *   confirm haptic and a small avatar bounce mark that the data is in.
 * - **Switching data** (Anime <-> Manga, both loaded): the segments morph to the new shares.
 * - **Error / empty list**: only the faint track is shown.
 *
 * With system animations turned off there is no wave and the ring just appears.
 */
@Composable
fun LibraryRing(
    segments: List<RingSegment>,
    isLoading: Boolean,
    isError: Boolean,
    avatarSize: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    require(segments.size == 3) { "LibraryRing shows exactly 3 segments" }

    val haptic = LocalHapticFeedback.current
    val reduceMotion = rememberReduceMotion()

    val total = segments.sumOf { it.value.coerceAtLeast(0) }
    val hasData = total > 0

    // Shares morph with a spring when the numbers change (e.g. Anime -> Manga)
    val fractionSpec = Motion.slowSpatial<Float>()
    val fractions = List(3) { i ->
        animateFloatAsState(
            targetValue = if (hasData) segments[i].value.coerceAtLeast(0) / total.toFloat() else 0f,
            animationSpec = fractionSpec,
            label = "ringFraction$i"
        )
    }

    // How far each segment has grown in (0 = hidden, 1 = full)
    val reveal = remember { List(3) { Animatable(0f) } }
    val avatarScale = remember { Animatable(1f) }
    var sawLoading by remember { mutableStateOf(false) }
    val revealSpec = Motion.slowSpatial<Float>()
    val bounceSpec = Motion.fastSpatial<Float>()
    val currentIsError by rememberUpdatedState(isError)

    LaunchedEffect(isLoading, hasData) {
        if (isLoading) {
            sawLoading = true
            reveal.forEach { it.snapTo(0f) }
            return@LaunchedEffect
        }
        if (reduceMotion) {
            reveal.forEach { it.snapTo(1f) }
        } else {
            // Cascade: each segment starts a little after the previous one
            coroutineScope {
                reveal.forEachIndexed { index, progress ->
                    launch {
                        delay(index * RevealStaggerMs)
                        progress.animateTo(1f, revealSpec)
                    }
                }
            }
        }
        // Completion feedback, only when this follows a real load
        if (sawLoading && hasData && !currentIsError) {
            sawLoading = false
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            if (!reduceMotion) {
                avatarScale.animateTo(1.06f, bounceSpec)
                avatarScale.animateTo(1f, bounceSpec)
            }
        }
    }

    // The wave only runs while loading, so an idle profile doesn't redraw every frame
    val wavePhase: State<Float> = if (isLoading && !reduceMotion) {
        rememberInfiniteTransition(label = "ringWave").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(WaveDurationMs, easing = LinearEasing)),
            label = "ringWavePhase"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }
    val waveAlpha by animateFloatAsState(
        targetValue = if (isLoading) 1f else 0f,
        animationSpec = Motion.effects(),
        label = "ringWaveAlpha"
    )

    val trackColor = if (isError) MaterialTheme.colorScheme.outlineVariant
    else MaterialTheme.colorScheme.surfaceContainerHighest
    val colors = segments.map { it.color }

    val loadingDescription = stringResource(R.string.loading)
    val description = if (isLoading) loadingDescription
    else segments.joinToString(", ") { "${it.label} ${it.value}" }

    Box(
        modifier = modifier
            .size(libraryRingSize(avatarSize))
            .semantics { contentDescription = description }
            .drawBehind {
                val stroke = RingStroke.toPx()
                val arcSize = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(stroke / 2, stroke / 2)
                val radius = arcSize.width / 2
                // Angle taken by one rounded cap, so gaps look the same at any size
                val capDegrees = Math.toDegrees((stroke / 2 / radius).toDouble()).toFloat()

                // Track
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke)
                )

                // Loading wave: three pills in equal slots, swelling one after another
                if (waveAlpha > 0f) {
                    val slot = 120f
                    for (i in 0 until 3) {
                        val t = ((wavePhase.value - i * WaveStagger) % 1f + 1f) % 1f
                        val wave = ((sin(t * 2 * PI) + 1) / 2).toFloat()
                        val sweep = ((slot - SegmentGapDegrees) * (0.25f + 0.75f * wave) - 2 * capDegrees)
                            .coerceAtLeast(0.1f)
                        val center = -90f + slot * i + slot / 2
                        drawArc(
                            color = colors[i].copy(alpha = waveAlpha * (0.4f + 0.6f * wave)),
                            startAngle = center - sweep / 2,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                }

                // Data: segments one after another, clockwise from the top
                if (!isLoading && hasData) {
                    val angles = fractions.map { it.value * 360f }
                    val visibleCount = angles.count { it > 0.5f }
                    val gap = if (visibleCount > 1) SegmentGapDegrees else 0f
                    var start = -90f
                    for (i in 0 until 3) {
                        val angle = angles[i]
                        if (angle > 0.5f) {
                            val full = if (visibleCount == 1) 360f
                            else (angle - gap - 2 * capDegrees).coerceAtLeast(0.1f)
                            // A little spring overshoot is fine, but not into the next segment
                            val sweep = full * reveal[i].value.coerceIn(0f, 1.04f)
                            if (sweep > 0.05f) {
                                drawArc(
                                    color = colors[i],
                                    startAngle = if (visibleCount == 1) start
                                    else start + gap / 2 + capDegrees,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(
                                        width = stroke,
                                        cap = if (visibleCount == 1) StrokeCap.Butt else StrokeCap.Round
                                    )
                                )
                            }
                        }
                        start += angle
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = avatarScale.value
                scaleY = avatarScale.value
            }
        ) {
            content()
        }
    }
}

/**
 * The ring's key: count and colour of each segment. The numbers roll up when they load.
 */
@Composable
fun LibraryRingLegend(
    segments: List<RingSegment>,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            segments.forEach { segment ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isError) {
                        Text(
                            text = UNKNOWN_CHAR,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    } else {
                        RollingNumberText(
                            targetValue = segment.value,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(segment.color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = segment.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

/** True when the system "Animator duration scale" is off (Remove animations). */
@Composable
private fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
}
