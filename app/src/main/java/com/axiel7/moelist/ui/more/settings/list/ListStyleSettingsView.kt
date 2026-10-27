package com.axiel7.moelist.ui.more.settings.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.preferences.SettingsGroup
import com.axiel7.moelist.ui.composables.preferences.SettingsScaffold
import com.axiel7.moelist.ui.composables.preferences.SettingsSectionHeader
import com.axiel7.moelist.ui.composables.preferences.SettingsSwitchItem
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.Tab
import com.axiel7.moelist.ui.theme.MoeListTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun ListStyleSettingsView(navActionManager: NavActionManager) {
    val viewModel: ListStyleSettingsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ListStyleSettingsViewContent(
        uiState = uiState,
        event = viewModel,
        navActionManager = navActionManager,
    )
}

@Composable
private fun ListStyleSettingsViewContent(
    uiState: ListStyleSettingsUiState,
    event: ListStyleSettingsEvent?,
    navActionManager: NavActionManager,
) {
    SettingsScaffold(
        title = stringResource(R.string.media_lists),
        onBack = navActionManager::goBack,
    ) {
        SettingsSectionHeader(stringResource(R.string.enable_list_tabs))

        SettingsGroup {
            SettingsSwitchItem(
                title = stringResource(R.string.enable_list_tabs),
                subtitle = stringResource(R.string.enable_list_tabs_subtitle),
                icon = Icons.Rounded.Tab,
                checked = uiState.useListTabs,
                onCheckedChange = { event?.setUseListTabs(it) },
                index = 0,
                count = 1,
            )
        }
    }
}

@Preview
@Composable
private fun ListStyleSettingsPreview() {
    MoeListTheme {
        ListStyleSettingsViewContent(
            uiState = ListStyleSettingsUiState(),
            event = null,
            navActionManager = NavActionManager.rememberNavActionManager(),
        )
    }
}
