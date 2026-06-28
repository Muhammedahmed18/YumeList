package com.axiel7.moelist.ui.more.settings.appearance

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Anchor
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Brightness4
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.ColorPalette
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.BackIconButton
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.swatchColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppearanceView(navActionManager: NavActionManager) {
    val viewModel: AppearanceViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AppearanceViewContent(
        uiState = uiState,
        event = viewModel,
        navActionManager = navActionManager,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceViewContent(
    uiState: AppearanceUiState,
    event: AppearanceEvent?,
    navActionManager: NavActionManager,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.appearance),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = { BackIconButton(onClick = navActionManager::goBack) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 20.dp),
        ) {
            // ── Theme Mode ─────────────────────────────────────────────────────
            Text(
                text = stringResource(R.string.theme_mode),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 2.dp, bottom = 10.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ThemeCard(
                        themeStyle = ThemeStyle.FOLLOW_SYSTEM,
                        selected = uiState.theme == ThemeStyle.FOLLOW_SYSTEM,
                        onClick = { event?.setTheme(ThemeStyle.FOLLOW_SYSTEM) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    ThemeCard(
                        themeStyle = ThemeStyle.LIGHT,
                        selected = uiState.theme == ThemeStyle.LIGHT,
                        onClick = { event?.setTheme(ThemeStyle.LIGHT) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ThemeCard(
                        themeStyle = ThemeStyle.DARK,
                        selected = uiState.theme == ThemeStyle.DARK,
                        onClick = { event?.setTheme(ThemeStyle.DARK) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    ThemeCard(
                        themeStyle = ThemeStyle.AMOLED,
                        selected = uiState.theme == ThemeStyle.AMOLED,
                        onClick = { event?.setTheme(ThemeStyle.AMOLED) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Anime Style Theme ──────────────────────────────────────────────
            Text(
                text = stringResource(R.string.anime_style_theme),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 2.dp, bottom = 10.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ColorPalette.entries.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        pair.forEach { palette ->
                            PaletteCard(
                                palette = palette,
                                selected = uiState.colorPalette == palette,
                                onClick = { event?.setColorPalette(palette) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Theme Card ────────────────────────────────────────────────────────────────

@Composable
private fun ThemeCard(
    themeStyle: ThemeStyle,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "theme_border",
    )
    val cardBg by animateColorAsState(
        targetValue = when {
            selected && themeStyle == ThemeStyle.AMOLED -> Color(0xFF080810)
            selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        label = "theme_card_bg",
    )
    val contentColor by animateColorAsState(
        targetValue = when {
            selected && themeStyle == ThemeStyle.AMOLED -> Color.White
            selected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "theme_content_color",
    )

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        border = BorderStroke(2.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = themeStyle.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(42.dp),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(themeStyle.stringRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor,
            )
        }
    }
}

// ── Palette Card ──────────────────────────────────────────────────────────────

@Composable
private fun PaletteCard(
    palette: ColorPalette,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val swatches = palette.swatchColors()
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "palette_border",
    )
    val cardBg by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "palette_card_bg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "palette_content_color",
    )
    val circleStroke = MaterialTheme.colorScheme.surface

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, borderColor),
        modifier = modifier,
        color = cardBg,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = palette.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(palette.stringRes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
            Spacer(modifier = Modifier.height(10.dp))
            OverlappingColorCircles(
                colors = swatches,
                strokeColor = circleStroke,
            )
        }
    }
}

// ── Overlapping color circles ─────────────────────────────────────────────────

@Composable
private fun OverlappingColorCircles(
    colors: List<Color>,
    strokeColor: Color,
) {
    val circleSize = 18.dp
    val step = 11.dp
    val totalWidth = circleSize + step * (colors.size - 1)

    Box(modifier = Modifier.size(width = totalWidth, height = circleSize)) {
        colors.forEachIndexed { index, color ->
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .offset(x = step * index)
                    .zIndex((colors.size - index).toFloat())
                    .clip(CircleShape)
                    .background(color)
                    .border(1.5.dp, strokeColor, CircleShape)
            )
        }
    }
}

// ── Icon mappings ─────────────────────────────────────────────────────────────

private val ThemeStyle.icon: ImageVector
    get() = when (this) {
        ThemeStyle.FOLLOW_SYSTEM -> Icons.Rounded.Brightness4
        ThemeStyle.LIGHT -> Icons.Rounded.LightMode
        ThemeStyle.DARK -> Icons.Rounded.DarkMode
        ThemeStyle.AMOLED -> Icons.Rounded.Contrast
    }

private val ColorPalette.icon: ImageVector
    get() = when (this) {
        ColorPalette.DYNAMIC -> Icons.Rounded.AutoAwesome
        ColorPalette.ONE_PIECE -> Icons.Rounded.Anchor
        ColorPalette.DEMON_SLAYER -> Icons.Rounded.LocalFireDepartment
        ColorPalette.ATTACK_ON_TITAN -> Icons.Rounded.Shield
        ColorPalette.NARUTO -> Icons.Rounded.Bolt
        ColorPalette.BLEACH -> Icons.Rounded.Flare
    }

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showSystemUi = true)
@Composable
private fun AppearancePreview() {
    MoeListTheme {
        AppearanceViewContent(
            uiState = AppearanceUiState(theme = ThemeStyle.DARK, colorPalette = ColorPalette.DEMON_SLAYER),
            event = null,
            navActionManager = NavActionManager.rememberNavActionManager(),
        )
    }
}
