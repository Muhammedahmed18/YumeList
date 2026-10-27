package com.axiel7.moelist.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

/**
 * App typography.
 *
 * Starts from the library's M3 type scale (sizes, line heights, letter spacing, and in
 * Material 3 Expressive also the matching *Emphasized styles), and only overrides what
 * YumeList does differently: bolder titles.
 */
val SystemTypography = Typography().run {
    copy(
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
}
