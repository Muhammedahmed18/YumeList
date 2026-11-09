package com.axiel7.moelist.ui.composables.preferences

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.ui.base.ColorPalette
import com.axiel7.moelist.ui.base.StartTab
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.MenuBook
import com.axiel7.moelist.ui.icons.filled.Tv
import com.axiel7.moelist.ui.icons.rounded.ArrowDropDown
import com.axiel7.moelist.ui.icons.rounded.AutoAwesome
import com.axiel7.moelist.ui.icons.rounded.Check
import com.axiel7.moelist.ui.icons.rounded.Contrast
import com.axiel7.moelist.ui.icons.rounded.History
import com.axiel7.moelist.ui.icons.rounded.Home
import com.axiel7.moelist.ui.icons.rounded.Tab
import com.axiel7.moelist.ui.icons.rounded.KeyboardArrowDown
import com.axiel7.moelist.ui.theme.Motion
import com.axiel7.moelist.ui.theme.colorSchemeFor

/*
 * Pickers shared by the onboarding "Make it yours" step and the Appearance settings, so the
 * two always look and behave the same:
 *
 * - [ThemeModePicker] + [PureBlackOption]: Follow system / Light / Dark as an M3 connected
 *   button group, with a "Pure black" chip under it while the app can be dark.
 * - [ColorPalettePicker]: M3-style palette swatches, Dynamic (wallpaper) first.
 * - [StartTabPicker]: where the app opens, as a connected button group.
 * - [TitleLanguagePicker]: Romaji / English / Japanese as a connected button group, with a
 *   "Shown as" line for the selected one.
 * - [ListStylePicker]: floating status button or tabs on the anime / manga lists, as a
 *   connected button group.
 */

// ── Connected button group ────────────────────────────────────────────────────

/**
 * An M3 Expressive connected button group: equal-width toggle buttons that touch, with the
 * outer corners fully rounded. The checked one fills with the accent colour and its corners
 * morph. With [icon], each button shows the icon above its label.
 */
@Composable
private fun <T> ConnectedChoiceGroup(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    labelRes: (T) -> Int,
    modifier: Modifier = Modifier,
    icon: ((T) -> ImageVector)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, option ->
            ToggleButton(
                checked = selected == option,
                onCheckedChange = { onSelect(option) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = if (icon != null) 56.dp else 48.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon(option),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.height(2.dp))
                    }
                    Text(
                        text = stringResource(labelRes(option)),
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// ── Theme mode ────────────────────────────────────────────────────────────────

private val ThemeModes = listOf(ThemeStyle.FOLLOW_SYSTEM, ThemeStyle.LIGHT, ThemeStyle.DARK)

/** "Follow system" spelled out here (the setting value itself is still called System). */
private val ThemeStyle.pickerLabel: Int
    get() = when (this) {
        ThemeStyle.FOLLOW_SYSTEM -> R.string.follow_system
        ThemeStyle.LIGHT -> R.string.theme_light
        ThemeStyle.DARK -> R.string.theme_dark
    }

/** Follow system / Light / Dark: icons above labels, as a connected button group. */
@Composable
fun ThemeModePicker(
    selected: ThemeStyle,
    onSelect: (ThemeStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    ConnectedChoiceGroup(
        options = ThemeModes,
        selected = selected,
        onSelect = onSelect,
        labelRes = { it.pickerLabel },
        icon = { it.icon },
        modifier = modifier,
    )
}

/**
 * "Pure black" as a filter chip under the theme buttons. It only exists while the app can be
 * dark (Dark or Follow system), so it slides away in Light mode instead of sitting greyed out.
 * With Follow system, a short hint says when it applies.
 */
@Composable
fun PureBlackOption(
    theme: ThemeStyle,
    pureBlack: Boolean,
    onPureBlackChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = theme != ThemeStyle.LIGHT,
        modifier = modifier,
        enter = fadeIn(Motion.effects()) + expandVertically(Motion.spatial()),
        exit = fadeOut(Motion.effects()) + shrinkVertically(Motion.spatial()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = pureBlack,
                onClick = { onPureBlackChange(!pureBlack) },
                label = { Text(text = stringResource(R.string.pure_black_chip)) },
                leadingIcon = {
                    Icon(
                        imageVector = if (pureBlack) Icons.Rounded.Check else Icons.Rounded.Contrast,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                },
                shape = CircleShape,
            )
            if (theme == ThemeStyle.FOLLOW_SYSTEM) {
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.pure_black_system_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Just the "Pure black" chip, for placing on a section title line (the onboarding step, where
 * height is tight). Same rule as [PureBlackOption]: only shown while the app can be dark.
 */
@Composable
fun PureBlackChip(
    theme: ThemeStyle,
    pureBlack: Boolean,
    onPureBlackChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = theme != ThemeStyle.LIGHT,
        modifier = modifier,
        enter = fadeIn(Motion.effects()) + scaleIn(Motion.fastSpatial(), initialScale = 0.8f),
        exit = fadeOut(Motion.effects()) + scaleOut(Motion.fastSpatial(), targetScale = 0.8f),
    ) {
        FilterChip(
            selected = pureBlack,
            onClick = { onPureBlackChange(!pureBlack) },
            label = { Text(text = stringResource(R.string.pure_black_chip)) },
            leadingIcon = {
                Icon(
                    imageVector = if (pureBlack) Icons.Rounded.Check else Icons.Rounded.Contrast,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            },
            shape = CircleShape,
        )
    }
}

// ── Colour palette ────────────────────────────────────────────────────────────

/**
 * One swatch per palette, like Android's own "Wallpaper & style" picker: Dynamic (wallpaper
 * colours) first, then Yume Blue and the built-in palettes. Each swatch shows the palette's
 * real primary / secondary / tertiary colours in the current light or dark mode.
 * Below Android 12 there are no wallpaper colours, so Dynamic isn't offered there.
 */
@Composable
fun ColorPalettePicker(
    selected: ColorPalette,
    onSelect: (ColorPalette) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val dynamicAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val palettes = remember(dynamicAvailable) {
        if (dynamicAvailable) ColorPalette.entries.toList()
        else ColorPalette.entries.filter { it != ColorPalette.DYNAMIC }
    }
    // Below Android 12 a saved "Dynamic" shows as Yume Blue, which is what the app uses then
    val current = if (!dynamicAvailable && selected == ColorPalette.DYNAMIC) ColorPalette.YUME
    else selected

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        palettes.forEach { palette ->
            val scheme = remember(palette, isDark) { colorSchemeFor(palette, isDark, context) }
            PaletteSwatch(
                name = stringResource(palette.stringRes),
                primary = scheme.primary,
                secondary = scheme.secondary,
                tertiary = scheme.tertiary,
                isDynamic = palette == ColorPalette.DYNAMIC,
                selected = current == palette,
                onClick = { onSelect(palette) },
            )
        }
    }
}

/**
 * A round swatch split M3-style: primary on the top half, secondary bottom-left, tertiary
 * bottom-right. Selected: a ring springs out around it and a check pops in the middle.
 */
@Composable
private fun PaletteSwatch(
    name: String,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    isDynamic: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val ringWidth by animateDpAsState(
        targetValue = if (selected) 3.dp else 0.dp,
        animationSpec = Motion.fastSpatial(),
        label = "swatchRing",
    )
    val ringColor by animateColorAsState(
        targetValue = if (selected) colors.primary else Color.Transparent,
        animationSpec = Motion.fastEffects(),
        label = "swatchRingColor",
    )
    val inset by animateDpAsState(
        targetValue = if (selected) 5.dp else 2.dp,
        animationSpec = Motion.fastSpatial(),
        label = "swatchInset",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .semantics { contentDescription = name }
            .padding(horizontal = 5.dp, vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .border(ringWidth, ringColor, CircleShape)
                .padding(inset),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val arcSize = Size(size.width, size.height)
                // Top half: primary
                drawArc(primary, startAngle = 180f, sweepAngle = 180f, useCenter = true, size = arcSize)
                // Bottom right: tertiary · bottom left: secondary
                drawArc(tertiary, startAngle = 0f, sweepAngle = 90f, useCenter = true, size = arcSize)
                drawArc(secondary, startAngle = 90f, sweepAngle = 90f, useCenter = true, size = arcSize)
            }
            when {
                selected -> SwatchBadge {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = colors.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                }
                isDynamic -> SwatchBadge {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = colors.onSurface,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
        Text(
            text = name,
            style = if (selected) MaterialTheme.typography.labelSmallEmphasized
            else MaterialTheme.typography.labelSmall,
            color = if (selected) colors.onSurface else colors.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

/** The small round chip in the middle of a swatch (check when selected, sparkle for Dynamic). */
@Composable
private fun SwatchBadge(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

// ── Start tab ─────────────────────────────────────────────────────────────────

/** The tabs the app can open on (More isn't offered). */
val StartTabChoices = listOf(StartTab.LAST_USED, StartTab.HOME, StartTab.ANIME, StartTab.MANGA)

val StartTab.pickerIcon: ImageVector
    get() = when (this) {
        StartTab.LAST_USED -> Icons.Rounded.History
        StartTab.HOME -> Icons.Rounded.Home
        StartTab.ANIME -> Icons.Filled.Tv
        StartTab.MANGA -> Icons.AutoMirrored.Rounded.MenuBook
    }

/** Last used / Home / Anime / Manga: icons above labels, as a connected button group. */
@Composable
fun StartTabPicker(
    selected: StartTab,
    onSelect: (StartTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    ConnectedChoiceGroup(
        options = StartTabChoices,
        selected = selected,
        onSelect = onSelect,
        labelRes = { it.stringRes },
        icon = { it.pickerIcon },
        modifier = modifier,
    )
}

// ── Title language ────────────────────────────────────────────────────────────

/**
 * Romaji / English / Japanese as a connected button group, with one line under it showing a
 * title the way the selected language writes it. Pass a title from the user's own list when
 * there is one; otherwise a well-known example is used.
 */
@Composable
fun TitleLanguagePicker(
    selected: TitleLanguage,
    onSelect: (TitleLanguage) -> Unit,
    modifier: Modifier = Modifier,
    romajiSample: String = "Shingeki no Kyojin",
    englishSample: String = "Attack on Titan",
    japaneseSample: String = "進撃の巨人",
    showSample: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ConnectedChoiceGroup(
            options = TitleLanguage.entries.toList(),
            selected = selected,
            onSelect = onSelect,
            labelRes = { it.stringRes },
        )
        if (showSample) {
            val sample = when (selected) {
                TitleLanguage.ROMAJI -> romajiSample
                TitleLanguage.ENGLISH -> englishSample
                TitleLanguage.JAPANESE -> japaneseSample
            }
            // Read here: AnimatedContent's transitionSpec isn't @Composable
            val fade = Motion.fastEffects<Float>()
            AnimatedContent(
                targetState = sample,
                transitionSpec = { fadeIn(fade) togetherWith fadeOut(fade) },
                label = "titleSample",
            ) { text ->
                Text(
                    text = stringResource(R.string.title_shown_as, text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 6.dp),
                )
            }
        }
    }
}

// ── List style ────────────────────────────────────────────────────────────────

/**
 * How the anime / manga lists switch status: a floating button that opens a menu
 * ([useListTabs] off) or tabs along the top (on). A connected button group like the others,
 * with one line under it explaining the selected option ([showDescription]).
 */
@Composable
fun ListStylePicker(
    useListTabs: Boolean,
    onUseListTabsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showDescription: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ConnectedChoiceGroup(
            options = listOf(false, true),
            selected = useListTabs,
            onSelect = onUseListTabsChange,
            labelRes = { tabs -> if (tabs) R.string.list_style_tabs else R.string.list_style_fab },
            icon = { tabs -> if (tabs) Icons.Rounded.Tab else Icons.Rounded.ArrowDropDown },
        )
        if (showDescription) {
            // Read here: AnimatedContent's transitionSpec isn't @Composable
            val fade = Motion.fastEffects<Float>()
            AnimatedContent(
                targetState = useListTabs,
                transitionSpec = { fadeIn(fade) togetherWith fadeOut(fade) },
                label = "listStyleHint",
            ) { tabs ->
                Text(
                    text = stringResource(
                        if (tabs) R.string.list_style_tabs_desc else R.string.list_style_fab_desc
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 6.dp),
                )
            }
        }
    }
}
