package com.axiel7.moelist.ui.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val PULSE_DURATION_MS = 900

/**
 * Lightweight loading skeleton: covers the element with a softly pulsing surface color.
 *
 * Replaces the `placeholder-material3` shimmer library and the old `composed {}` shimmer
 * modifiers. It's a [Modifier.Node], so the pulse only re-draws (no recomposition every
 * frame, no per-modifier composition), and it stops animating as soon as [visible] is false.
 *
 * @param visible when true the content is hidden behind the pulsing skeleton
 * @param shape outline of the skeleton (use [RectangleShape] if the element is already clipped)
 */
@Composable
fun Modifier.skeleton(
    visible: Boolean = true,
    shape: Shape = RectangleShape,
): Modifier = this then SkeletonElement(
    visible = visible,
    baseColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    highlightColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    shape = shape,
)

private data class SkeletonElement(
    val visible: Boolean,
    val baseColor: Color,
    val highlightColor: Color,
    val shape: Shape,
) : ModifierNodeElement<SkeletonNode>() {
    override fun create() = SkeletonNode(visible, baseColor, highlightColor, shape)

    override fun update(node: SkeletonNode) {
        node.update(visible, baseColor, highlightColor, shape)
    }
}

private class SkeletonNode(
    private var visible: Boolean,
    private var baseColor: Color,
    private var highlightColor: Color,
    private var shape: Shape,
) : Modifier.Node(), DrawModifierNode {

    private val pulse = Animatable(0f)
    private var pulseJob: Job? = null

    override fun onAttach() {
        if (visible) startPulse()
    }

    override fun onDetach() {
        // The node's coroutineScope is cancelled automatically on detach
        pulseJob = null
    }

    fun update(visible: Boolean, baseColor: Color, highlightColor: Color, shape: Shape) {
        val wasVisible = this.visible
        this.visible = visible
        this.baseColor = baseColor
        this.highlightColor = highlightColor
        this.shape = shape
        if (visible && !wasVisible) startPulse()
        if (!visible) {
            pulseJob?.cancel()
            pulseJob = null
        }
        invalidateDraw()
    }

    private fun startPulse() {
        if (!isAttached || pulseJob?.isActive == true) return
        pulseJob = coroutineScope.launch {
            pulse.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(PULSE_DURATION_MS, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            )
        }
    }

    override fun ContentDrawScope.draw() {
        if (!visible) {
            drawContent()
            return
        }
        // Reading pulse.value here only invalidates the draw phase
        val color = lerp(baseColor, highlightColor, pulse.value)
        if (shape === RectangleShape) {
            drawRect(color)
        } else {
            drawOutline(shape.createOutline(size, layoutDirection, this), color)
        }
    }
}
