package com.axiel7.moelist.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.axiel7.moelist.ui.theme.ShapeSmall

/**
 * Hides the element behind a pulsing skeleton while [visible] is true.
 *
 * Same name and usage as before, but now backed by our own [skeleton] modifier instead of the
 * `placeholder-material3` library (which used a `composed {}` modifier + shimmer).
 */
@Composable
fun Modifier.defaultPlaceholder(
    visible: Boolean
): Modifier = skeleton(visible = visible, shape = ShapeSmall)
