package com.axiel7.moelist.ui.more.settings.appearance

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.ColorPalette
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.preferences.ColorPalettePicker
import com.axiel7.moelist.ui.composables.preferences.ListStylePicker
import com.axiel7.moelist.ui.composables.preferences.PureBlackOption
import com.axiel7.moelist.ui.composables.preferences.SettingsScaffold
import com.axiel7.moelist.ui.composables.preferences.SettingsSectionHeader
import com.axiel7.moelist.ui.composables.preferences.StartTabPicker
import com.axiel7.moelist.ui.composables.preferences.ThemeModePicker
import com.axiel7.moelist.ui.composables.preferences.TitleLanguagePicker
import com.axiel7.moelist.ui.theme.MoeListTheme
import org.koin.androidx.compose.koinViewModel

/*
 * Appearance: everything about how YumeList looks, in grouped M3 sections.
 *
 * - Theme: Follow system / Light / Dark, and a pure black chip while the app can be dark.
 * - Colors: Dynamic (wallpaper) or one of the built-in palettes.
 * - Start on, title language, and whether the lists switch status with a button or tabs.
 *
 * The same pickers are used by the onboarding "Make it yours" step (AppearancePickers.kt).
 */

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
    SettingsScaffold(
        title = stringResource(R.string.appearance),
        onBack = navActionManager::goBack,
    ) {
        // ── Theme ────────────────────────────────────────────────────────────
        SettingsSectionHeader(stringResource(R.string.theme_section))
        ThemeModePicker(
            selected = uiState.theme,
            onSelect = { event?.setTheme(it) },
        )
        PureBlackOption(
            theme = uiState.theme,
            pureBlack = uiState.pureBlack,
            onPureBlackChange = { event?.setPureBlack(it) },
        )

        // ── Colors: Dynamic (wallpaper) first, then the built-in palettes ───
        SettingsSectionHeader(stringResource(R.string.colors_section))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            ColorPalettePicker(
                selected = uiState.colorPalette,
                onSelect = { event?.setColorPalette(it) },
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            )
        }

        // ── Start on ─────────────────────────────────────────────────────────
        SettingsSectionHeader(stringResource(R.string.start_on))
        StartTabPicker(
            selected = uiState.startTab,
            onSelect = { event?.setStartTab(it) },
        )

        // ── Titles ───────────────────────────────────────────────────────────
        SettingsSectionHeader(stringResource(R.string.title_language))
        TitleLanguagePicker(
            selected = uiState.titleLanguage,
            onSelect = { event?.setTitleLanguage(it) },
        )

        // ── Lists ────────────────────────────────────────────────────────────
        SettingsSectionHeader(stringResource(R.string.list_switch_title))
        ListStylePicker(
            useListTabs = uiState.useListTabs,
            onUseListTabsChange = { event?.setUseListTabs(it) },
        )

        Spacer(Modifier.height(8.dp))
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showSystemUi = true)
@Composable
private fun AppearancePreview() {
    MoeListTheme {
        AppearanceViewContent(
            uiState = AppearanceUiState(theme = ThemeStyle.DARK, colorPalette = ColorPalette.SAKURA),
            event = null,
            navActionManager = NavActionManager.rememberNavActionManager(),
        )
    }
}
