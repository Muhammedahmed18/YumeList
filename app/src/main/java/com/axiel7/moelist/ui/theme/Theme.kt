package com.axiel7.moelist.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import com.axiel7.moelist.ui.base.ColorPalette
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
    surfaceContainerLowest = md_theme_light_surfaceContainerLowest,
    surfaceContainerLow = md_theme_light_surfaceContainerLow,
    surfaceContainer = md_theme_light_surfaceContainer,
    surfaceContainerHigh = md_theme_light_surfaceContainerHigh,
    surfaceContainerHighest = md_theme_light_surfaceContainerHighest,
    surfaceDim = md_theme_light_surfaceDim,
    surfaceBright = md_theme_light_surfaceBright,
)


private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
    surfaceContainerLowest = md_theme_dark_surfaceContainerLowest,
    surfaceContainerLow = md_theme_dark_surfaceContainerLow,
    surfaceContainer = md_theme_dark_surfaceContainer,
    surfaceContainerHigh = md_theme_dark_surfaceContainerHigh,
    surfaceContainerHighest = md_theme_dark_surfaceContainerHighest,
    surfaceDim = md_theme_dark_surfaceDim,
    surfaceBright = md_theme_dark_surfaceBright,
)

/**
 * Pure black (for AMOLED / OLED screens).
 *
 * - The big areas are true black: page background, surface (top app bars at rest), dim surface,
 *   and the navigation bar (see [LocalPureBlack] in MainBottomNavBar). That's where nearly all
 *   the battery saving is.
 * - Raised containers (cards, list items, sheets, dialogs, menus, snackbar) stay a near-black
 *   grey, so they're still visible as separate layers. Fully black containers would merge into
 *   the page and make OLED black smear worse while scrolling.
 */
private fun ColorScheme.toBlack() = this.copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceVariant = surfaceVariant.copy(alpha = 0.4f).compositeOver(Color.Black),
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = surfaceContainerLow.copy(alpha = 0.5f).compositeOver(Color.Black),
    surfaceContainer = surfaceContainer.copy(alpha = 0.6f).compositeOver(Color.Black),
    surfaceContainerHigh = surfaceContainerHigh.copy(alpha = 0.6f).compositeOver(Color.Black),
    surfaceContainerHighest = surfaceContainerHighest.copy(alpha = 0.7f).compositeOver(Color.Black),
)

/** True while the pure black (AMOLED) theme is active, for parts that need to go fully black. */
val LocalPureBlack = staticCompositionLocalOf { false }

/**
 * The colour scheme of [palette] in light or dark, before pure black is applied. Used by the
 * theme itself and by the colour picker swatches, so a swatch always shows the real colours.
 * [ColorPalette.DYNAMIC] needs Android 12+ (wallpaper colours); older versions get Yume Blue.
 */
fun colorSchemeFor(palette: ColorPalette, darkTheme: Boolean, context: Context): ColorScheme =
    when (palette) {
        ColorPalette.DYNAMIC -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context)
                else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) DarkColors else LightColors
            }
        }
        // YumeList's own palette
        ColorPalette.YUME -> if (darkTheme) DarkColors else LightColors
        // Accent palettes (AccentSchemes.kt)
        ColorPalette.LAVENDER -> if (darkTheme) LavenderDarkColors else LavenderLightColors
        ColorPalette.SAKURA -> if (darkTheme) SakuraDarkColors else SakuraLightColors
        ColorPalette.SUNSET -> if (darkTheme) SunsetDarkColors else SunsetLightColors
        ColorPalette.MATCHA -> if (darkTheme) MatchaDarkColors else MatchaLightColors
        ColorPalette.OCEAN -> if (darkTheme) OceanDarkColors else OceanLightColors
    }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MoeListTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useBlackColors: Boolean = false,
    colorPalette: ColorPalette = ColorPalette.DYNAMIC,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = remember(colorPalette, darkTheme, useBlackColors) {
        val scheme = colorSchemeFor(colorPalette, darkTheme, context)
        if (darkTheme && useBlackColors) scheme.toBlack() else scheme
    }

    // Material 3 Expressive theme:
    // - motionScheme: spring-based "expressive" motion used by every M3 component
    //   (and available to our own animations via MaterialTheme.motionScheme)
    // - shapes: our token scale, so built-in components match custom ones
    // - typography: M3 scale + our bolder titles (includes the Emphasized styles)
    CompositionLocalProvider(LocalPureBlack provides (darkTheme && useBlackColors)) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = MotionScheme.expressive(),
            shapes = AppShapes,
            typography = SystemTypography,
            content = content
        )
    }
}
