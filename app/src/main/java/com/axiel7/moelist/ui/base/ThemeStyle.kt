package com.axiel7.moelist.ui.base

import com.axiel7.moelist.R

enum class ThemeStyle {
    // The old AMOLED mode is now DARK + the separate "Pure black" switch. A value saved by an
    // older version is still read correctly: see DefaultPreferencesRepository.LEGACY_AMOLED.
    FOLLOW_SYSTEM, LIGHT, DARK;

    val stringRes
        get() = when (this) {
            FOLLOW_SYSTEM -> R.string.theme_system
            LIGHT -> R.string.theme_light
            DARK -> R.string.theme_dark
        }

    companion object {
        fun valueOfOrNull(value: String) = try {
            valueOf(value)
        } catch (e: IllegalArgumentException) {
            null
        }

        val entriesLocalized = entries.associateWith { it.stringRes }
    }
}