package com.axiel7.moelist.ui.composables.score

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.media.scoreText
import kotlin.math.roundToInt

private const val MAX_SCORE = 10f

/** Converts a 0f..1f slider position to a whole 0..10 score. */
private fun Float.toScore() = (this * MAX_SCORE).roundToInt()

@Composable
fun ScoreSlider(
    score: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val scoreTextLabel = score.scoreText()

    // material3 1.5's SliderState no longer takes a valueRange, so the slider works on the
    // default 0f..1f range and we map it to the 0..10 score (score / 10f <-> value * 10).
    // 9 steps = a stop at every whole score between 0 and 10.
    val sliderState = remember {
        SliderState(
            value = score / MAX_SCORE,
            steps = 9,
        )
    }

    // Keep the slider in sync when the score changes from outside (e.g. reset / loaded entry)
    LaunchedEffect(score) {
        if (sliderState.value.toScore() != score) {
            sliderState.value = score / MAX_SCORE
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val colors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
        inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // High-end Value Display (Compact Row)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = scoreTextLabel.ifEmpty { "-" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (score > 0) score.toString() else "-",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // M3 Expressive slider: the thumb is a vertical handle that narrows while dragged,
        // the track is split around it with a gap, and a small dot marks every whole score.
        Slider(
            state = sliderState,
            onValueChange = {
                sliderState.value = it
                val newValue = it.toScore()
                if (newValue != score) {
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    onValueChange(newValue)
                }
            },
            onValueChangeFinished = onValueChangeFinished,
            colors = colors,
            interactionSource = interactionSource,
            thumb = {
                // The sliderState overload is hidden in material3 1.5 (binary compatibility
                // only); this one takes the orientation directly. Same expressive handle thumb.
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    isVertical = false,
                    colors = colors,
                )
            },
            track = { state ->
                SliderDefaults.Track(
                    sliderState = state,
                    colors = colors,
                )
            }
        )
    }
}
