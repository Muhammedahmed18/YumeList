package com.axiel7.moelist.ui.more.settings.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.preferences.SettingsScaffold
import com.axiel7.moelist.ui.composables.preferences.SettingsSection
import com.axiel7.moelist.ui.composables.preferences.SettingsSwitchItem
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.AutoAwesome
import com.axiel7.moelist.ui.icons.rounded.Group
import com.axiel7.moelist.ui.icons.rounded.PriorityHigh
import com.axiel7.moelist.ui.icons.rounded.Star
import com.axiel7.moelist.ui.theme.MoeListTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun ContentSettingsView(navActionManager: NavActionManager) {
    val viewModel: ContentSettingsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ContentSettingsViewContent(
        uiState = uiState,
        event = viewModel,
        navActionManager = navActionManager,
    )
}

@Composable
private fun ContentSettingsViewContent(
    uiState: ContentUiState,
    event: ContentEvent?,
    navActionManager: NavActionManager,
) {
    // Title language moved to Appearance (with the other "how it looks" choices)
    SettingsScaffold(
        title = stringResource(R.string.content),
        onBack = navActionManager::goBack,
    ) {
        // ── Filters ──────────────────────────────────────────────────────────
        SettingsSection(title = stringResource(R.string.content_filters)) {
            item {
                SettingsSwitchItem(
                    title = stringResource(R.string.show_nsfw),
                    subtitle = stringResource(R.string.show_nsfw_summary),
                    icon = Icons.Rounded.PriorityHigh,
                    checked = uiState.showNsfw,
                    onCheckedChange = { event?.setShowNsfw(it) },
                )
            }
            item {
                SettingsSwitchItem(
                    title = stringResource(R.string.hide_scores),
                    subtitle = stringResource(R.string.hide_scores_summary),
                    icon = Icons.Rounded.Star,
                    checked = uiState.hideScores,
                    onCheckedChange = { event?.setHideScores(it) },
                )
            }
        }

        // ── Home ─────────────────────────────────────────────────────────────
        SettingsSection(title = stringResource(R.string.title_home)) {
            item {
                SettingsSwitchItem(
                    title = stringResource(R.string.show_for_you),
                    subtitle = stringResource(R.string.show_for_you_summary),
                    icon = Icons.Rounded.AutoAwesome,
                    checked = uiState.showForYou,
                    onCheckedChange = { event?.setShowForYou(it) },
                )
            }
        }

        // ── Details page ─────────────────────────────────────────────────────
        SettingsSection(title = stringResource(R.string.details_page)) {
            item {
                SettingsSwitchItem(
                    title = stringResource(R.string.always_load_characters),
                    subtitle = stringResource(R.string.always_load_characters_summary),
                    icon = Icons.Rounded.Group,
                    checked = uiState.loadCharacters,
                    onCheckedChange = { event?.setLoadCharacters(it) },
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun ContentSettingsPreview() {
    MoeListTheme {
        ContentSettingsViewContent(
            uiState = ContentUiState(),
            event = null,
            navActionManager = NavActionManager.rememberNavActionManager(),
        )
    }
}
