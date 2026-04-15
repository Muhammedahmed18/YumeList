package com.axiel7.moelist.ui.composables.score

import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlin.math.roundToInt

@Composable
fun ScoreSlider(
    score: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    Slider(
        value = score.toFloat(),
        onValueChange = {
            val newValue = it.roundToInt()
            if (newValue != score) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onValueChange(newValue)
            }
        },
        onValueChangeFinished = onValueChangeFinished,
        modifier = modifier,
        valueRange = 0f..10f,
        steps = 9,
    )
}
