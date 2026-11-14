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
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.ui.composables.BackIconButton
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.KeyboardArrowRight
import com.axiel7.moelist.ui.icons.rounded.Check
import com.axiel7.moelist.ui.icons.rounded.CheckCircle
import com.axiel7.moelist.ui.icons.rounded.Close
import com.axiel7.moelist.ui.theme.Motion
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ListItem
import com.axiel7.moelist.ui.icons.automirrored.rounded.OpenInNew
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import com.axiel7.moelist.ui.theme.ReadingMaxWidth
import com.axiel7.moelist.ui.theme.ShapeMedium
import androidx.compose.material3.TopAppBar

/*
 * Shared M3 Expressive building blocks for the More, Settings and About screens.
 *
 * - [SettingsScaffold]: large flexible top bar that collapses on scroll + a scrolling column.
 * - [SettingsSection] + [SettingsNavItem] / [SettingsSwitchItem] / [SettingsRadioItem]: grouped
 *   (segmented) list items. Items in a section share one rounded block (16dp outer corners, 4dp
 *   inner, 2dp gaps) and their corners morph while pressed. The section works out each item's
 *   position itself, so items can be added, removed or hidden without renumbering anything.
 * - [SettingsGroup]: the plain column behind a section, for callers that still pass `index` and
 *   `count` by hand.
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
    actions: @Composable RowScope.() -> Unit = {},
    // false = a small top bar, for pages whose own header already shows a big title (About)
    largeTopBar: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollBehavior = if (largeTopBar) {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    } else {
        TopAppBarDefaults.pinnedScrollBehavior()
    }
    val barColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (largeTopBar) {
                LargeFlexibleTopAppBar(
                    title = { Text(text = title) },
                    subtitle = subtitle?.let { { Text(text = it) } },
                    navigationIcon = { BackIconButton(onClick = onBack) },
                    actions = actions,
                    scrollBehavior = scrollBehavior,
                    colors = barColors,
                )
            } else {
                TopAppBar(
                    title = { Text(text = title) },
                    navigationIcon = { BackIconButton(onClick = onBack) },
                    actions = actions,
                    scrollBehavior = scrollBehavior,
                    colors = barColors,
                )
            }
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
                // Large screens: centred, at most ReadingMaxWidth wide (the scroll area stays full width)
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = ReadingMaxWidth)
                .fillMaxWidth()
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

/** Where an item sits inside its section; decides which corners are rounded. */
@Immutable
data class SettingsItemPosition(val index: Int, val count: Int)

/**
 * Position of the current item, provided by [SettingsSection]. Outside a section an item is a
 * standalone block (index 0 of 1), so every corner is rounded.
 */
val LocalSettingsItemPosition = compositionLocalOf { SettingsItemPosition(index = 0, count = 1) }

/** Collects the items of a [SettingsSection]. Skip an item by not calling [item] for it. */
class SettingsSectionScope internal constructor() {
    internal val items = mutableListOf<@Composable () -> Unit>()

    fun item(content: @Composable () -> Unit) {
        items += content
    }
}

/**
 * An optional section header over a group of items. Each item gets its index and the item
 * count automatically, so the rounded corners stay right when items are conditional:
 *
 * ```
 * SettingsSection(title = "Filters") {
 *     item { SettingsSwitchItem(...) }
 *     if (supported) item { SettingsSwitchItem(...) }
 * }
 * ```
 *
 * Set [selectableGroup] when the items are radio choices, so TalkBack reads them as one group.
 * Nothing is drawn (not even the header) when the section has no items.
 */
@Composable
fun SettingsSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    selectableGroup: Boolean = false,
    content: SettingsSectionScope.() -> Unit,
) {
    val items = SettingsSectionScope().apply(content).items
    if (items.isEmpty()) return

    if (title != null) {
        SettingsSectionHeader(text = title)
    }
    SettingsGroup(
        modifier = if (selectableGroup) modifier.selectableGroup() else modifier,
    ) {
        items.forEachIndexed { index, item ->
            CompositionLocalProvider(
                LocalSettingsItemPosition provides SettingsItemPosition(index, items.size)
            ) {
                item()
            }
        }
    }
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

private val SettingsIconShape = ShapeMedium

/**
 * A clickable grouped item: optional tonal icon, title, optional subtitle and, by default, a
 * chevron. Pass [trailingContent] to show something else at the end (a status badge...).
 */
@Composable
fun SettingsNavItem(
    title: String,
    index: Int = LocalSettingsItemPosition.current.index,
    count: Int = LocalSettingsItemPosition.current.count,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: Painter? = null,
    iconContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    titleColor: Color = Color.Unspecified,
    showChevron: Boolean = true,
    // true when the row leaves the app (browser, store...): shows "open in new" instead of the chevron
    opensExternally: Boolean = false,
    enabled: Boolean = true,
    colors: ListItemColors = settingsItemColors(),
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val trailing: (@Composable () -> Unit)? = trailingContent
        ?: if (showChevron) {
            {
                Icon(
                    imageVector = if (opensExternally) Icons.AutoMirrored.Rounded.OpenInNew
                    else Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = if (opensExternally) Modifier.size(20.dp) else Modifier,
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
    index: Int = LocalSettingsItemPosition.current.index,
    count: Int = LocalSettingsItemPosition.current.count,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showChevron: Boolean = true,
    opensExternally: Boolean = false,
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
        opensExternally = opensExternally,
        trailingContent = trailingContent,
    )
}

/**
 * A grouped item that only shows information (a credit without a link...). Same colours and
 * grouped corners as [SettingsNavItem], but not clickable: no ripple, no chevron.
 */
@Composable
fun SettingsInfoItem(
    title: String,
    index: Int = LocalSettingsItemPosition.current.index,
    count: Int = LocalSettingsItemPosition.current.count,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val isFirst = index == 0
    val isLast = index == count - 1
    // Matches the segmented items: large outer corners, small inner ones
    val shape = RoundedCornerShape(
        topStart = if (isFirst) InfoItemOuterCorner else InfoItemInnerCorner,
        topEnd = if (isFirst) InfoItemOuterCorner else InfoItemInnerCorner,
        bottomStart = if (isLast) InfoItemOuterCorner else InfoItemInnerCorner,
        bottomEnd = if (isLast) InfoItemOuterCorner else InfoItemInnerCorner,
    )
    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = subtitle?.let { { Text(text = it) } },
        colors = settingsItemColors(),
        modifier = modifier.clip(shape),
    )
}

private val InfoItemOuterCorner = 16.dp
private val InfoItemInnerCorner = 4.dp

/**
 * A grouped item with a switch. The whole row toggles (with switch semantics), and the switch
 * thumb shows a check when on and a cross when off.
 */
@Composable
fun SettingsSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    index: Int = LocalSettingsItemPosition.current.index,
    count: Int = LocalSettingsItemPosition.current.count,
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

/**
 * A grouped item for picking one option out of several (use it inside a [SettingsSection] with
 * `selectableGroup = true`). The whole row selects, a radio button at the end shows the state
 * and TalkBack reads it as a radio button. The selected row is tinted, like the format picker.
 */
@Composable
fun SettingsRadioItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    index: Int = LocalSettingsItemPosition.current.index,
    count: Int = LocalSettingsItemPosition.current.count,
) {
    val colors = MaterialTheme.colorScheme

    SegmentedListItem(
        selected = selected,
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        modifier = modifier.semantics { role = Role.RadioButton },
        enabled = enabled,
        colors = ListItemDefaults.segmentedColors(
            containerColor = colors.surfaceContainer,
            supportingContentColor = colors.onSurfaceVariant,
            selectedContainerColor = colors.secondaryContainer,
            selectedContentColor = colors.onSecondaryContainer,
            selectedSupportingContentColor = colors.onSecondaryContainer,
            selectedTrailingContentColor = colors.primary,
        ),
        leadingContent = icon?.let {
            {
                val container by animateColorAsState(
                    targetValue = if (selected) colors.primaryContainer
                    else colors.surfaceContainerHighest,
                    animationSpec = Motion.effects(),
                    label = "settingsRadioIconContainer",
                )
                val content by animateColorAsState(
                    targetValue = if (selected) colors.onPrimaryContainer
                    else colors.onSurfaceVariant,
                    animationSpec = Motion.effects(),
                    label = "settingsRadioIconContent",
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
            // onClick = null: the row handles the selection, so TalkBack reads one control
            RadioButton(selected = selected, onClick = null, enabled = enabled)
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
 * spring open to 28dp and a check pops in at the top end. Radio-button semantics are set, so
 * TalkBack reads "radio button, selected". Put a row of cards in a `selectableGroup()`.
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
        // A one-of-several choice: TalkBack reads "radio button, selected"
        modifier = modifier.semantics { role = Role.RadioButton },
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
