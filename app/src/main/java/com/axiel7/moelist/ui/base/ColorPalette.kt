package com.axiel7.moelist.ui.base

import com.axiel7.moelist.R

enum class ColorPalette {
    DYNAMIC,
    ONE_PIECE,
    DEMON_SLAYER,
    ATTACK_ON_TITAN,
    NARUTO,
    BLEACH;

    val stringRes
        get() = when (this) {
            DYNAMIC -> R.string.palette_dynamic
            ONE_PIECE -> R.string.palette_one_piece
            DEMON_SLAYER -> R.string.palette_demon_slayer
            ATTACK_ON_TITAN -> R.string.palette_attack_on_titan
            NARUTO -> R.string.palette_naruto
            BLEACH -> R.string.palette_bleach
        }

    companion object {
        fun valueOfOrNull(value: String) = try {
            valueOf(value)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
