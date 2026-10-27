package com.axiel7.moelist.ui.more.settings.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.StartTab
import com.axiel7.moelist.ui.base.TabletMode
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.preferences.SettingsChoiceCard
import com.axiel7.moelist.ui.composables.preferences.SettingsGroup
import com.axiel7.moelist.ui.composables.preferences.SettingsScaffold
import com.axiel7.moelist.ui.composables.preferences.SettingsSectionHeader
import com.axiel7.moelist.ui.composables.preferences.SettingsSwitchItem
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.Book
import com.axiel7.moelist.ui.icons.rounded.Devices
import com.axiel7.moelist.ui.icons.rounded.History
import com.axiel7.moelist.ui.icons.rounded.Home
import com.axiel7.moelist.ui.icons.rounded.LocalMovies
import com.axiel7.moelist.ui.icons.rounded.MoreHoriz
import com.axiel7.moelist.ui.icons.rounded.PushPin
import com.axiel7.moelist.ui.icons.rounded.ScreenRotation
import com.axiel7.moelist.ui.icons.rounded.Smartphone
import com.axiel7.moelist.ui.icons.rounded.Tablet
import com.axiel7.moelist.ui.theme.MoeListTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun NavigationSettingsView(navActionManager: NavActionManager) {
    val viewModel: NavigationSettingsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NavigationSettingsViewContent(
        uiState = uiState,
        event = viewModel,
        navActionManager = navActionManager,
    )
}

@Composable
private fun NavigationSettingsViewContent(
    uiState: NavigationSettingsUiState,
    event: NavigationSettingsEvent?,
    navActionManager: NavActionManager,
) {
    SettingsScaffold(
        title = stringResource(R.string.navigation),
        onBack = navActionManager::goBack,
    ) {
        // ── Default Tab ──────────────────────────────────────────────────────
        SettingsSectionHeader(stringResource(R.string.default_section))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val startTabs = StartTab.entries.filter { it != StartTab.MORE }
            startTabs.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    pair.forEach { tab ->
                        ChoiceTile(
                            icon = tab.icon,
                            label = stringResource(tab.stringRes),
                            selected = uiState.startTab == tab,
                            onClick = { event?.setStartTab(tab) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }

        // ── Tablet Mode ──────────────────────────────────────────────────────
        SettingsSectionHeader(stringResource(R.string.tablet_mode))

        Row(
            modifier = Modifier.height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TabletMode.entries.forEach { mode ->
                ChoiceTile(
                    icon = mode.icon,
                    label = stringResource(mode.stringRes),
                    selected = uiState.tabletMode == mode,
                    onClick = { event?.setTabletMode(mode) },
                    compact = true,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }

        // ── Navigation Bar ───────────────────────────────────────────────────
        SettingsSectionHeader(stringResource(R.string.navigation_bar))

        SettingsGroup {
            SettingsSwitchItem(
                title = stringResource(R.string.pinned_navigation_bar),
                subtitle = stringResource(R.string.pinned_navigation_bar_summary),
                icon = Icons.Rounded.PushPin,
                checked = uiState.pinnedNavBar,
                onCheckedChange = { event?.setPinnedNavBar(it) },
                index = 0,
                count = 1,
            )
        }
    }
}

private val StartTab.icon: ImageVector
    get() = when (this) {
        StartTab.LAST_USED -> Icons.Rounded.History
        StartTab.HOME -> Icons.Rounded.Home
        StartTab.ANIME -> Icons.Rounded.LocalMovies
        StartTab.MANGA -> Icons.Rounded.Book
        StartTab.MORE -> Icons.Rounded.MoreHoriz
    }

private val TabletMode.icon: ImageVector
    get() = when (this) {
        TabletMode.AUTO -> Icons.Rounded.Devices
        TabletMode.ALWAYS -> Icons.Rounded.Tablet
        TabletMode.LANDSCAPE -> Icons.Rounded.ScreenRotation
        TabletMode.NEVER -> Icons.Rounded.Smartphone
    }

/** Icon + label picker tile (start tab, tablet mode). */
@Composable
private fun ChoiceTile(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant

    SettingsChoiceCard(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compact) 8.dp else 16.dp,
                    vertical = if (compact) 14.dp else 18.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else tint,
                modifier = Modifier.size(if (compact) 24.dp else 28.dp),
            )
            Text(
                text = label,
                style = when {
                    compact && selected -> MaterialTheme.typography.labelMediumEmphasized
                    compact -> MaterialTheme.typography.labelMedium
                    selected -> MaterialTheme.typography.labelLargeEmphasized
                    else -> MaterialTheme.typography.labelLarge
                },
                color = if (selected) tint else Color.Unspecified,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun NavigationSettingsPreview() {
    MoeListTheme {
        NavigationSettingsViewContent(
            uiState = NavigationSettingsUiState(),
            event = null,
            navActionManager = NavActionManager.rememberNavActionManager(),
        )
    }
}
