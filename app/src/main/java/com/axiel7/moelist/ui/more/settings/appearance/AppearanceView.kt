package com.axiel7.moelist.ui.more.settings.appearance

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.ColorPalette
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.preferences.SettingsGroup
import com.axiel7.moelist.ui.composables.preferences.SettingsIcon
import com.axiel7.moelist.ui.composables.preferences.SettingsScaffold
import com.axiel7.moelist.ui.composables.preferences.SettingsSwitchItem
import com.axiel7.moelist.ui.composables.preferences.icon
import com.axiel7.moelist.ui.composables.preferences.settingsItemColors
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.AutoAwesome
import com.axiel7.moelist.ui.icons.rounded.Contrast
import com.axiel7.moelist.ui.icons.rounded.Movie
import com.axiel7.moelist.ui.icons.rounded.Schedule
import com.axiel7.moelist.ui.icons.rounded.Star
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.Motion
import org.koin.androidx.compose.koinViewModel

/** The modes offered (the old AMOLED mode is now the Pure black switch). */
private val ThemeModes = listOf(ThemeStyle.FOLLOW_SYSTEM, ThemeStyle.LIGHT, ThemeStyle.DARK)

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

@Composable
private fun AppearanceViewContent(
    uiState: AppearanceUiState,
    event: AppearanceEvent?,
    navActionManager: NavActionManager,
) {
    val isDynamicAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val isLight = uiState.theme == ThemeStyle.LIGHT

    SettingsScaffold(
        title = stringResource(R.string.appearance),
        onBack = navActionManager::goBack,
    ) {
        // The whole app re-themes as soon as a choice is saved, so the preview always shows the
        // current mode, pure black and colors (and animates between them)
        AnimePreviewCard(modifier = Modifier.padding(top = 4.dp))

        Text(
            text = stringResource(R.string.appearance_flavor),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 20.dp)
        )

        SettingsGroup {
            ThemeModeItem(
                selected = uiState.theme,
                onSelect = { event?.setTheme(it) },
            )
            SettingsSwitchItem(
                title = stringResource(R.string.pure_black_mode),
                subtitle = stringResource(
                    if (isLight) R.string.pure_black_dark_only else R.string.pure_black_summary
                ),
                icon = Icons.Rounded.Contrast,
                checked = uiState.pureBlack,
                onCheckedChange = { event?.setPureBlack(it) },
                // Only meaningful when the app can be dark (Dark, or System at night)
                enabled = !isLight,
                index = 1,
                count = 3,
            )
            SettingsSwitchItem(
                title = stringResource(R.string.dynamic_colors),
                subtitle = stringResource(
                    if (isDynamicAvailable) R.string.dynamic_colors_summary
                    else R.string.dynamic_colors_unavailable
                ),
                icon = Icons.Rounded.AutoAwesome,
                checked = isDynamicAvailable && uiState.colorPalette == ColorPalette.DYNAMIC,
                onCheckedChange = { on ->
                    event?.setColorPalette(if (on) ColorPalette.DYNAMIC else ColorPalette.YUME)
                },
                enabled = isDynamicAvailable,
                index = 2,
                count = 3,
            )
        }
    }
}

// ── Theme mode ────────────────────────────────────────────────────────────────

/**
 * First row of the group: icon (follows the chosen mode) + "Theme Mode", with System / Light /
 * Dark as an M3 Expressive connected button group underneath.
 */
@Composable
private fun ThemeModeItem(
    selected: ThemeStyle,
    onSelect: (ThemeStyle) -> Unit,
) {
    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = 0, count = 3),
        colors = settingsItemColors(),
        leadingContent = {
            SettingsIcon(painter = rememberVectorPainter(selected.icon))
        },
        supportingContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
            ) {
                ThemeModes.forEachIndexed { index, mode ->
                    ToggleButton(
                        checked = selected == mode,
                        onCheckedChange = { onSelect(mode) },
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            ThemeModes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = stringResource(mode.stringRes),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
    ) {
        Text(text = stringResource(R.string.theme_mode))
    }
}

// ── Anime preview ─────────────────────────────────────────────────────────────

/**
 * A tiny YumeList screen in the current theme: a portrait poster, a score chip, a "Watching"
 * pill and an airing card, plus Expressive shape accents (flower, cookie, sunny). Every block
 * uses a real colour role and animates when the theme changes. Decorative, so TalkBack skips it.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AnimePreviewCard(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val background = animatedColor(scheme.surface)
    val outline = animatedColor(scheme.outlineVariant)
    val bar = animatedColor(scheme.surfaceContainerHighest)
    val primary = animatedColor(scheme.primary)
    val secondary = animatedColor(scheme.secondary)
    val tertiary = animatedColor(scheme.tertiary)
    val primaryContainer = animatedColor(scheme.primaryContainer)
    val onPrimaryContainer = animatedColor(scheme.onPrimaryContainer)
    val secondaryContainer = animatedColor(scheme.secondaryContainer)
    val onSecondaryContainer = animatedColor(scheme.onSecondaryContainer)
    val tertiaryContainer = animatedColor(scheme.tertiaryContainer)
    val onTertiaryContainer = animatedColor(scheme.onTertiaryContainer)
    val onPrimary = animatedColor(scheme.onPrimary)

    // toShape() is @Composable and already remembers the shape
    val cookie = MaterialShapes.Cookie4Sided.toShape()
    val sunny = MaterialShapes.Sunny.toShape()
    val flower = MaterialShapes.Flower.toShape()
    val cardShape = RoundedCornerShape(32.dp)
    val blockShape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(background)
            .border(1.5.dp, outline, cardShape)
            .padding(16.dp)
            .clearAndSetSemantics {},
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top bar: a search-bar pill and three Expressive shape accents
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp)
                    .clip(CircleShape)
                    .background(bar)
            )
            ShapeDot(shape = cookie, color = tertiary, size = 18)
            ShapeDot(shape = sunny, color = secondary, size = 22)
            ShapeDot(shape = flower, color = primary, size = 30)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Poster
            Box(
                modifier = Modifier
                    .weight(1.05f)
                    .fillMaxHeight()
                    .clip(blockShape)
                    .background(primaryContainer)
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Movie,
                    contentDescription = null,
                    tint = onPrimaryContainer,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(30.dp)
                )
                Column(
                    modifier = Modifier.align(Alignment.BottomStart),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        Modifier
                            .width(56.dp)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(onPrimaryContainer.copy(alpha = 0.55f))
                    )
                    Box(
                        Modifier
                            .width(36.dp)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(onPrimaryContainer.copy(alpha = 0.35f))
                    )
                }
            }

            // Score chip + status pill
            Column(
                modifier = Modifier
                    .weight(0.95f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(blockShape)
                        .background(tertiaryContainer),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = onTertiaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "9.1",
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        color = onTertiaryContainer
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(blockShape)
                        .background(secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.watching),
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        color = onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
            }

            // Airing card
            Box(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxHeight()
                    .clip(blockShape)
                    .background(primary)
                    .padding(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .clip(CircleShape)
                        .background(onPrimary.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Column(
                    modifier = Modifier.align(Alignment.BottomStart),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        Modifier
                            .width(40.dp)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(onPrimary.copy(alpha = 0.6f))
                    )
                    Box(
                        Modifier
                            .width(26.dp)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(onPrimary.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

@Composable
private fun ShapeDot(shape: Shape, color: Color, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(shape)
            .background(color)
    )
}

/** Colour that animates (effects spring) when the theme changes. */
@Composable
private fun animatedColor(target: Color): Color {
    val value by animateColorAsState(
        targetValue = target,
        animationSpec = Motion.effects(),
        label = "previewColor"
    )
    return value
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showSystemUi = true)
@Composable
private fun AppearancePreview() {
    MoeListTheme {
        AppearanceViewContent(
            uiState = AppearanceUiState(theme = ThemeStyle.DARK, colorPalette = ColorPalette.DYNAMIC),
            event = null,
            navActionManager = NavActionManager.rememberNavActionManager(),
        )
    }
}
