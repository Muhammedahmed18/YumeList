package com.axiel7.moelist.ui.more.settings.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.StartTab
import com.axiel7.moelist.ui.base.TabletMode
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.preferences.SettingsRadioItem
import com.axiel7.moelist.ui.composables.preferences.SettingsScaffold
import com.axiel7.moelist.ui.composables.preferences.SettingsSection
import com.axiel7.moelist.ui.composables.preferences.SettingsSwitchItem
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.Book
import com.axiel7.moelist.ui.icons.rounded.Casino
import com.axiel7.moelist.ui.icons.rounded.Devices
import com.axiel7.moelist.ui.icons.rounded.History
import com.axiel7.moelist.ui.icons.rounded.Home
import com.axiel7.moelist.ui.icons.rounded.LocalMovies
import com.axiel7.moelist.ui.icons.rounded.MoreHoriz
import com.axiel7.moelist.ui.icons.rounded.PushPin
import com.axiel7.moelist.ui.icons.rounded.ScreenRotation
import com.axiel7.moelist.ui.icons.rounded.Smartphone
import com.axiel7.moelist.ui.icons.rounded.Tab
import com.axiel7.moelist.ui.icons.rounded.Tablet
import com.axiel7.moelist.ui.theme.MoeListTheme
import org.koin.androidx.compose.koinViewModel

/*
 * "Lists & navigation": where the app opens, the navigation bar, how the anime/manga lists
 * behave and when the tablet layout is used. The list switches used to live on their own
 * "Media lists" screen; their preference keys are unchanged.
 */

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
        title = stringResource(R.string.lists_and_navigation),
        onBack = navActionManager::goBack,
    ) {
        // ── Default tab ──────────────────────────────────────────────────────
        SettingsSection(
            title = stringResource(R.string.default_tab),
            selectableGroup = true,
        ) {
            StartTab.entries
                .filter { it != StartTab.MORE }
                .forEach { tab ->
                    item {
                        SettingsRadioItem(
                            title = stringResource(tab.stringRes),
                            subtitle = if (tab == StartTab.LAST_USED) {
                                stringResource(R.string.default_tab_last_used_summary)
                            } else null,
                            icon = tab.icon,
                            selected = uiState.startTab == tab,
                            onClick = { event?.setStartTab(tab) },
                        )
                    }
                }
        }

        // ── Navigation bar ───────────────────────────────────────────────────
        SettingsSection(title = stringResource(R.string.navigation_bar)) {
            item {
                SettingsSwitchItem(
                    title = stringResource(R.string.pinned_navigation_bar),
                    subtitle = stringResource(R.string.pinned_navigation_bar_summary),
                    icon = Icons.Rounded.PushPin,
                    checked = uiState.pinnedNavBar,
                    onCheckedChange = { event?.setPinnedNavBar(it) },
                )
            }
        }

        // ── Lists ────────────────────────────────────────────────────────────
        SettingsSection(title = stringResource(R.string.anime_manga_lists)) {
            item {
                SettingsSwitchItem(
                    title = stringResource(R.string.enable_list_tabs),
                    subtitle = stringResource(R.string.enable_list_tabs_subtitle),
                    icon = Icons.Rounded.Tab,
                    checked = uiState.useListTabs,
                    onCheckedChange = { event?.setUseListTabs(it) },
                )
            }
            item {
                SettingsSwitchItem(
                    title = stringResource(R.string.random_button_on_list),
                    subtitle = stringResource(R.string.random_button_on_list_summary),
                    icon = Icons.Rounded.Casino,
                    checked = uiState.randomListEntryEnabled,
                    onCheckedChange = { event?.setRandomListEntryEnabled(it) },
                )
            }
        }

        // ── Tablet layout ────────────────────────────────────────────────────
        // Last, since most phone users never need it. Each option says what it does.
        SettingsSection(
            title = stringResource(R.string.tablet_layout),
            selectableGroup = true,
        ) {
            TabletMode.entries.forEach { mode ->
                item {
                    SettingsRadioItem(
                        title = stringResource(mode.stringRes),
                        subtitle = stringResource(mode.summaryRes),
                        icon = mode.icon,
                        selected = uiState.tabletMode == mode,
                        onClick = { event?.setTabletMode(mode) },
                    )
                }
            }
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

private val TabletMode.summaryRes: Int
    get() = when (this) {
        TabletMode.AUTO -> R.string.tablet_mode_auto_summary
        TabletMode.ALWAYS -> R.string.tablet_mode_always_summary
        TabletMode.LANDSCAPE -> R.string.tablet_mode_landscape_summary
        TabletMode.NEVER -> R.string.tablet_mode_never_summary
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
