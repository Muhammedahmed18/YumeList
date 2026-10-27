package com.axiel7.moelist.ui.composables.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.Brightness4
import com.axiel7.moelist.ui.icons.rounded.DarkMode
import com.axiel7.moelist.ui.icons.rounded.LightMode

/*
 * Theme-mode preview shared by the Appearance settings and the onboarding theme step,
 * so both pickers look the same.
 */

/** Icon for each theme mode. */
val ThemeStyle.icon: ImageVector
    get() = when (this) {
        ThemeStyle.FOLLOW_SYSTEM -> Icons.Rounded.Brightness4
        ThemeStyle.LIGHT -> Icons.Rounded.LightMode
        ThemeStyle.DARK -> Icons.Rounded.DarkMode
    }

// Fixed sample colours for the mini screens, so each card shows what that mode looks like
// whatever theme is active right now
private val PreviewLightBackground = Color(0xFFFDF7FF)
private val PreviewLightContent = Color(0xFF1D1B20)
private val PreviewDarkBackground = Color(0xFF211F26)
private val PreviewDarkContent = Color(0xFFE6E0E9)

/**
 * A tiny screen in the given mode, accented with [accent] (usually the current primary).
 * "System" shows light and dark side by side; with [pureBlack] the dark side is true black.
 */
@Composable
fun ThemeMiniPreview(
    themeStyle: ThemeStyle,
    accent: Color,
    modifier: Modifier = Modifier,
    height: Dp = 72.dp,
    pureBlack: Boolean = false,
) {
    val darkBackground = if (pureBlack) Color.Black else PreviewDarkBackground
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
    ) {
        when (themeStyle) {
            ThemeStyle.FOLLOW_SYSTEM -> {
                MiniScreen(PreviewLightBackground, PreviewLightContent, accent, Modifier.weight(1f))
                MiniScreen(darkBackground, PreviewDarkContent, accent, Modifier.weight(1f))
            }
            ThemeStyle.LIGHT ->
                MiniScreen(PreviewLightBackground, PreviewLightContent, accent, Modifier.weight(1f))
            ThemeStyle.DARK ->
                MiniScreen(darkBackground, PreviewDarkContent, accent, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MiniScreen(
    background: Color,
    content: Color,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(background)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(8.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(5.dp)
                .clip(CircleShape)
                .background(content.copy(alpha = 0.25f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(5.dp)
                .clip(CircleShape)
                .background(content.copy(alpha = 0.25f))
        )
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(accent)
                .align(Alignment.End)
        )
    }
}
