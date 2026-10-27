package com.axiel7.moelist.ui.composables.preferences

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.ui.composables.BackIconButton
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.KeyboardArrowRight
import com.axiel7.moelist.ui.icons.rounded.Check
import com.axiel7.moelist.ui.icons.rounded.CheckCircle
import com.axiel7.moelist.ui.icons.rounded.Close
import com.axiel7.moelist.ui.theme.Motion

/*
 * Shared M3 Expressive building blocks for the More, Settings and About screens.
 *
 * - [SettingsScaffold]: large flexible top bar that collapses on scroll + a scrolling column.
 * - [SettingsGroup] + [SettingsNavItem] / [SettingsSwitchItem]: grouped (segmented) list items.
 *   Items in a group share one rounded block (16dp outer corners, 4dp inner, 2dp gaps) and
 *   their corners morph while pressed. Pass each item's `index` and the group's `count`.
 * - [SettingsChoiceCard]: a selectable visual card (theme, tab, style pickers); selection fills
 *   it and rounds its corners with an expressive spring.
 */

/** Container colour of grouped items: one step above the page so the groups read as blocks. */
@Composable
fun settingsItemColors(
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
): ListItemColors {
    val colors = MaterialTheme.colorScheme
    // A switch row reports "selected" while on; keep the same container so only the switch
    // shows the state (a whole page of tinted rows would be noisy)
    return ListItemDefaults.segmentedColors(
        containerColor = containerColor,
        supportingContentColor = colors.onSurfaceVariant,
        trailingContentColor = colors.onSurfaceVariant,
        selectedContainerColor = containerColor,
        selectedContentColor = colors.onSurface,
        selectedSupportingContentColor = colors.onSurfaceVariant,
        selectedTrailingContentColor = colors.onSurfaceVariant,
    )
}

/**
 * Scaffold for settings-style screens: an M3 Expressive large flexible top bar that collapses
 * into a small one as you scroll, over a scrolling column with 16dp side margins.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(text = title) },
                subtitle = subtitle?.let { { Text(text = it) } },
                navigationIcon = { BackIconButton(onClick = onBack) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        // The column handles the bottom inset itself so content can scroll under the nav bar
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 24.dp)
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)),
            content = content,
        )
    }
}

/** Section title above a group, in the emphasized label style. */
@Composable
fun SettingsSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLargeEmphasized,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
    )
}

/** A block of segmented items, with the M3 Expressive 2dp gap between them. */
@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        content = content,
    )
}

/** Leading icon of a settings item: the icon in a tonal rounded square. */
@Composable
fun SettingsIcon(
    painter: Painter,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    shape: Shape = SettingsIconShape,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(shape)
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(22.dp),
        )
    }
}

private val SettingsIconShape = RoundedCornerShape(12.dp)

/**
 * A clickable grouped item: optional tonal icon, title, optional subtitle and, by default, a
 * chevron. Pass [trailingContent] to show something else at the end (a status badge...).
 */
@Composable
fun SettingsNavItem(
    title: String,
    index: Int,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: Painter? = null,
    iconContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    titleColor: Color = Color.Unspecified,
    showChevron: Boolean = true,
    enabled: Boolean = true,
    colors: ListItemColors = settingsItemColors(),
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val trailing: (@Composable () -> Unit)? = trailingContent
        ?: if (showChevron) {
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                )
            }
        } else {
            null
        }

    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        leadingContent = icon?.let {
            {
                SettingsIcon(
                    painter = it,
                    containerColor = iconContainerColor,
                    contentColor = iconContentColor,
                )
            }
        },
        supportingContent = subtitle?.let { { Text(text = it) } },
        trailingContent = trailing,
    ) {
        Text(text = title, color = titleColor)
    }
}

/** [SettingsNavItem] with an [ImageVector] icon. */
@Composable
fun SettingsNavItem(
    title: String,
    index: Int,
    count: Int,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showChevron: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    SettingsNavItem(
        title = title,
        index = index,
        count = count,
        onClick = onClick,
        modifier = modifier,
        subtitle = subtitle,
        icon = rememberVectorPainter(icon),
        showChevron = showChevron,
        trailingContent = trailingContent,
    )
}

/**
 * A grouped item with a switch. The whole row toggles (with switch semantics), and the switch
 * thumb shows a check when on and a cross when off.
 */
@Composable
fun SettingsSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    SegmentedListItem(
        checked = checked,
        onCheckedChange = onCheckedChange,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        modifier = modifier,
        enabled = enabled,
        colors = settingsItemColors(),
        leadingContent = icon?.let {
            {
                // The icon brightens when the setting is on
                val container by animateColorAsState(
                    targetValue = if (checked) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerHighest,
                    animationSpec = Motion.effects(),
                    label = "settingsIconContainer",
                )
                val content by animateColorAsState(
                    targetValue = if (checked) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = Motion.effects(),
                    label = "settingsIconContent",
                )
                SettingsIcon(
                    painter = rememberVectorPainter(it),
                    containerColor = container,
                    contentColor = content,
                )
            }
        },
        supportingContent = subtitle?.let { { Text(text = it) } },
        trailingContent = {
            // onCheckedChange = null: the row handles the toggle, so TalkBack reads one switch
            SettingsSwitch(checked = checked, enabled = enabled)
        },
    ) {
        Text(text = title)
    }
}

/** M3 switch with a check / cross icon in the thumb. Display only (the row toggles it). */
@Composable
fun SettingsSwitch(
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onCheckedChange: ((Boolean) -> Unit)? = null,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        thumbContent = {
            Icon(
                imageVector = if (checked) Icons.Rounded.Check else Icons.Rounded.Close,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize),
            )
        },
    )
}

/**
 * A selectable visual card for pickers (start tab, title language, list style...).
 *
 * Unselected: `surfaceContainer` with 16dp corners. Selected: `secondaryContainer`, corners
 * spring open to 28dp and a check pops in at the top end. Selection semantics are set, so
 * TalkBack reads "selected".
 */
@Composable
fun SettingsChoiceCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val corner by animateDpAsState(
        targetValue = if (selected) 28.dp else 16.dp,
        animationSpec = Motion.fastSpatial(),
        label = "choiceCorner",
    )
    val container by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = Motion.effects(),
        label = "choiceContainer",
    )

    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(corner),
        color = container,
    ) {
        Box {
            content()
            AnimatedVisibility(
                visible = selected,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                enter = scaleIn(animationSpec = Motion.fastSpatial()) + fadeIn(Motion.fastEffects()),
                exit = scaleOut(animationSpec = Motion.fastSpatial()) + fadeOut(Motion.fastEffects()),
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
