package com.axiel7.moelist.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.axiel7.moelist.ui.base.ColorPalette

// ── Demon Slayer ──────────────────────────────────────────────────────────────
// Brand palette: #232323 (charcoal), #3C2328 (dark burgundy), #8B3A42 (muted crimson),
//               #EFEFED (off-white), #4CAF78 (sage green / Tanjiro haori)

val DemonSlayerLightColors = lightColorScheme(
    primary = Color(0xFF8B3A42),        // Muted crimson — exact palette
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDADC),
    onPrimaryContainer = Color(0xFF3D0009),
    secondary = Color(0xFF4CAF78),      // Sage green — exact palette (Tanjiro haori)
    onSecondary = Color(0xFF00210F),    // Dark for contrast on medium green
    secondaryContainer = Color(0xFFC0EDD6),
    onSecondaryContainer = Color(0xFF00210F),
    tertiary = Color(0xFF3C2328),       // Dark burgundy — exact palette
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF5D6DA),
    onTertiaryContainer = Color(0xFF2C0009),
)

val DemonSlayerDarkColors = darkColorScheme(
    primary = Color(0xFFCF8890),        // Crimson softened for dark bg
    onPrimary = Color(0xFF510016),
    primaryContainer = Color(0xFF6B2030),
    onPrimaryContainer = Color(0xFFFFDADC),
    secondary = Color(0xFF4CAF78),      // Sage green — exact palette
    onSecondary = Color(0xFF00210F),
    secondaryContainer = Color(0xFF005232),
    onSecondaryContainer = Color(0xFFC0EDD6),
    tertiary = Color(0xFFC4A0A8),       // Muted mauve from burgundy
    onTertiary = Color(0xFF3D1520),
    tertiaryContainer = Color(0xFF562430),
    onTertiaryContainer = Color(0xFFF5D6DA),
    surface = Color(0xFF1A1A1A),        // Charcoal — close to #232323 exact palette
    onSurface = Color(0xFFEFEFED),      // Off-white — exact palette
    surfaceVariant = Color(0xFF3C2328), // Dark burgundy — exact palette
    onSurfaceVariant = Color(0xFFD4C0C2),
)

// ── Attack on Titan ───────────────────────────────────────────────────────────

val AttackOnTitanLightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF00210A),
    secondary = Color(0xFF5D4037),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEFEBE9),
    onSecondaryContainer = Color(0xFF1C0D08),
    tertiary = Color(0xFF455A64),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCFD8DC),
    onTertiaryContainer = Color(0xFF0D1B22),
)

val AttackOnTitanDarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF003A0F),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFFA1887F),
    onSecondary = Color(0xFF1C0D08),
    secondaryContainer = Color(0xFF3E2723),
    onSecondaryContainer = Color(0xFFEFEBE9),
    tertiary = Color(0xFF90A4AE),
    onTertiary = Color(0xFF0D1B22),
    tertiaryContainer = Color(0xFF263238),
    onTertiaryContainer = Color(0xFFCFD8DC),
    surface = Color(0xFF0D1512),
    onSurface = Color(0xFFDDE4DA),
    surfaceVariant = Color(0xFF2A362A),
    onSurfaceVariant = Color(0xFFBBC9B8),
)

// ── Naruto ────────────────────────────────────────────────────────────────────
// Brand palette: #F62D01 (red-orange), #F07404 (orange), #FFB902 (gold),
//               #0E3B8A (navy blue), #E883B2 (sakura pink)

val NarutoLightColors = lightColorScheme(
    primary = Color(0xFFF62D01),        // Naruto jumpsuit red-orange — exact palette
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD4),
    onPrimaryContainer = Color(0xFF3D0600),
    secondary = Color(0xFF0E3B8A),      // Sasuke navy blue — exact palette
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD6E4FF),
    onSecondaryContainer = Color(0xFF001944),
    tertiary = Color(0xFFF07404),       // Warm orange — exact palette
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFB902),   // Gold — exact palette
    onTertiaryContainer = Color(0xFF281900),
)

val NarutoDarkColors = darkColorScheme(
    primary = Color(0xFFFFB59A),        // Warm salmon — red-orange on dark bg
    onPrimary = Color(0xFF5C0F00),
    primaryContainer = Color(0xFF7F2000),
    onPrimaryContainer = Color(0xFFFFDAD4),
    secondary = Color(0xFF8AB4FF),      // Soft blue — navy on dark bg
    onSecondary = Color(0xFF002766),
    secondaryContainer = Color(0xFF003A99),
    onSecondaryContainer = Color(0xFFD6E4FF),
    tertiary = Color(0xFFFFB902),       // Gold — exact palette
    onTertiary = Color(0xFF3D2A00),
    tertiaryContainer = Color(0xFF5A3E00),
    onTertiaryContainer = Color(0xFFFFECC0),
    surface = Color(0xFF150900),        // Deep warm dark surface
    onSurface = Color(0xFFF0E0D6),
    surfaceVariant = Color(0xFF3B1428),  // Deep pink tint — derives from Sakura pink #E883B2
    onSurfaceVariant = Color(0xFFE0B8C8),
)

// ── Bleach ────────────────────────────────────────────────────────────────────

val BleachLightColors = lightColorScheme(
    primary = Color(0xFFB71C1C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFCDD2),
    onPrimaryContainer = Color(0xFF3D0000),
    secondary = Color(0xFFB8860B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFF9C4),
    onSecondaryContainer = Color(0xFF3A2600),
    tertiary = Color(0xFF1565C0),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBBDEFB),
    onTertiaryContainer = Color(0xFF001F45),
)

val BleachDarkColors = darkColorScheme(
    primary = Color(0xFFEF9A9A),
    onPrimary = Color(0xFF3D0000),
    primaryContainer = Color(0xFF7F0000),
    onPrimaryContainer = Color(0xFFFFCDD2),
    secondary = Color(0xFFFFD54F),
    onSecondary = Color(0xFF3A2600),
    secondaryContainer = Color(0xFF5A3800),
    onSecondaryContainer = Color(0xFFFFF9C4),
    tertiary = Color(0xFF64B5F6),
    onTertiary = Color(0xFF001F45),
    tertiaryContainer = Color(0xFF003380),
    onTertiaryContainer = Color(0xFFBBDEFB),
    surface = Color(0xFF0D0808),
    onSurface = Color(0xFFEADFDE),
    surfaceVariant = Color(0xFF2A1818),
    onSurfaceVariant = Color(0xFFCDBBBB),
)

// ── One Piece ─────────────────────────────────────────────────────────────────
// Brand palette: #D70000 (Luffy red), #2E63A4 (ocean blue), #AF6528 (ship wood),
//               #FFCE00 (straw hat yellow), #60BFF5 (sky blue), #000000 (black)

val OnePieceLightColors = lightColorScheme(
    primary = Color(0xFFD70000),        // Luffy's red vest — exact palette
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFCDD2),
    onPrimaryContainer = Color(0xFF410000),
    secondary = Color(0xFF2E63A4),      // Ocean / Going Merry blue — exact palette
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFBFDFFB),  // Derived from sky blue (#60BFF5)
    onSecondaryContainer = Color(0xFF001C40),
    tertiary = Color(0xFFAF6528),       // Ship wood brown — exact palette
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFCE00),   // Straw hat yellow — exact palette
    onTertiaryContainer = Color(0xFF2D1600),
)

val OnePieceDarkColors = darkColorScheme(
    primary = Color(0xFFFFB3B3),        // Luffy red softened for dark bg
    onPrimary = Color(0xFF680000),
    primaryContainer = Color(0xFF960000),    // Deep red — derives from #D70000
    onPrimaryContainer = Color(0xFFFFCDD2),
    secondary = Color(0xFF60BFF5),      // Sky blue — exact palette
    onSecondary = Color(0xFF003059),
    secondaryContainer = Color(0xFF004880),  // Ocean blue — derives from #2E63A4
    onSecondaryContainer = Color(0xFFD0E4FF),
    tertiary = Color(0xFFD4A060),       // Wood brown (#AF6528) lightened for dark bg
    onTertiary = Color(0xFF3D1F00),
    tertiaryContainer = Color(0xFF5A2E00),
    onTertiaryContainer = Color(0xFFFFDDB8),
    surface = Color(0xFF0A0A0A),        // Near-black — close to #000000 palette
    onSurface = Color(0xFFEAE0DE),
    surfaceVariant = Color(0xFF2A1818),
    onSurfaceVariant = Color(0xFFCDBFBB),
)

// ── Swatch preview colors for the palette picker UI ──────────────────────────

fun ColorPalette.swatchColors(): List<Color> = when (this) {
    ColorPalette.DYNAMIC -> listOf(
        Color(0xFF6750A4), Color(0xFF00BCD4), Color(0xFF4CAF50), Color(0xFFFF9800)
    )
    ColorPalette.ONE_PIECE -> listOf(
        Color(0xFFD70000), Color(0xFF2E63A4), Color(0xFFFFCE00), Color(0xFFAF6528)
    )
    ColorPalette.DEMON_SLAYER -> listOf(
        Color(0xFF8B3A42), Color(0xFF4CAF78), Color(0xFF3C2328), Color(0xFF232323)
    )
    ColorPalette.ATTACK_ON_TITAN -> listOf(
        Color(0xFF2E7D32), Color(0xFF5D4037), Color(0xFF455A64), Color(0xFFB71C1C)
    )
    ColorPalette.NARUTO -> listOf(
        Color(0xFFF62D01), Color(0xFF0E3B8A), Color(0xFFFFB902), Color(0xFFF07404)
    )
    ColorPalette.BLEACH -> listOf(
        Color(0xFFB71C1C), Color(0xFF212121), Color(0xFFB8860B), Color(0xFFE0E0E0)
    )
}
