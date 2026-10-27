package com.axiel7.moelist.ui.base

import com.axiel7.moelist.R

/**
 * Where the app's colors come from.
 *
 * - [DYNAMIC]: Material You colors from the wallpaper (Android 12+; older versions fall back to
 *   YumeList's own colors).
 * - [YUME]: YumeList's own built-in palette.
 *
 * The anime palettes were removed. A saved value from them no longer matches, so
 * [valueOfOrNull] returns null and the app falls back to [DYNAMIC] automatically.
 */
enum class ColorPalette {
    DYNAMIC,
    YUME;

    val stringRes
        get() = when (this) {
            DYNAMIC -> R.string.palette_dynamic
            YUME -> R.string.palette_yume_blue
        }

    companion object {
        fun valueOfOrNull(value: String) = try {
            valueOf(value)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
