package com.axiel7.moelist.ui.base

import androidx.compose.ui.graphics.Color
import com.axiel7.moelist.R

/**
 * Where the app's colors come from.
 *
 * - [DYNAMIC]: Material You colors from the wallpaper (Android 12+; older versions fall back to
 *   YumeList's own colors).
 * - Every other entry is an accent: a built-in palette made from [seed]. [YUME] is the app's
 *   original blue.
 *
 * The old anime palettes were removed. A saved value from them no longer matches, so
 * [valueOfOrNull] returns null and the app falls back to [DYNAMIC] automatically.
 */
enum class ColorPalette(val seed: Color) {
    DYNAMIC(Color(0xFF5C6BC0)),
    YUME(Color(0xFF5C6BC0)),
    LAVENDER(Color(0xFF8E5CD9)),
    SAKURA(Color(0xFFE91E63)),
    SUNSET(Color(0xFFFF7043)),
    MATCHA(Color(0xFF43A047)),
    OCEAN(Color(0xFF00897B));

    val stringRes
        get() = when (this) {
            DYNAMIC -> R.string.palette_dynamic
            YUME -> R.string.palette_yume_blue
            LAVENDER -> R.string.palette_lavender
            SAKURA -> R.string.palette_sakura
            SUNSET -> R.string.palette_sunset
            MATCHA -> R.string.palette_matcha
            OCEAN -> R.string.palette_ocean
        }

    companion object {
        fun valueOfOrNull(value: String) = try {
            valueOf(value)
        } catch (e: IllegalArgumentException) {
            null
        }

        /** The accents offered in the picker (everything except wallpaper colors). */
        val accents = entries.filter { it != DYNAMIC }
    }
}
