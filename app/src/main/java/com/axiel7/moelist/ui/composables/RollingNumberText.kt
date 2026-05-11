package com.axiel7.moelist.ui.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun RollingNumberText(
    targetValue: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified
) {
    var lastValue by remember { mutableIntStateOf(targetValue) }
    val isIncreasing = targetValue >= lastValue
    
    SideEffect {
        lastValue = targetValue
    }

    val targetString = targetValue.toString()
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        targetString.forEachIndexed { index, char ->
            // Use the index and total length as a key logic to keep digits aligned from the right
            // This ensures that when 1157 becomes 1158, only the last digit's state changes
            val key = targetString.length - index
            AnimatedContent(
                targetState = char to key,
                transitionSpec = {
                    if (isIncreasing) {
                        (slideInVertically { height -> height } + fadeIn())
                            .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                    } else {
                        (slideInVertically { height -> -height } + fadeIn())
                            .togetherWith(slideOutVertically { height -> height } + fadeOut())
                    }
                },
                label = "digitAnimation"
            ) { (animatedDigit, _) ->
                Text(
                    text = animatedDigit.toString(),
                    style = style,
                    fontWeight = fontWeight,
                    color = color
                )
            }
        }
    }
}
