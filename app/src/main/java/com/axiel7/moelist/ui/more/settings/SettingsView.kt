package com.axiel7.moelist.ui.more.settings

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tab
import androidx.compose.material.icons.rounded.Tablet
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.ui.base.ItemsPerRow
import com.axiel7.moelist.ui.base.ListStyle
import com.axiel7.moelist.ui.base.StartTab
import com.axiel7.moelist.ui.base.TabletMode
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.BackIconButton
import com.axiel7.moelist.ui.composables.preferences.ListPreferenceView
import com.axiel7.moelist.ui.composables.preferences.PlainPreferenceView
import com.axiel7.moelist.ui.composables.preferences.SwitchPreferenceView
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openByDefaultSettings
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsView(
    navActionManager: NavActionManager
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsViewContent(
        uiState = uiState,
        event = viewModel,
        navActionManager = navActionManager,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsViewContent(
    uiState: SettingsUiState,
    event: SettingsEvent?,
    navActionManager: NavActionManager
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = { BackIconButton(onClick = navActionManager::goBack) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(bottom = 16.dp)
        ) {

            // ── Appearance ──────────────────────────────────────────────────
            SettingsCardSection(title = stringResource(R.string.display)) {
                ListPreferenceView(
                    title = stringResource(R.string.theme),
                    entriesValues = ThemeStyle.entriesLocalized,
                    value = uiState.theme,
                    icon = Icons.Rounded.Palette,
                    useTonalContainer = true,
                    onValueChange = { event?.setTheme(it) }
                )

                SettingsDivider()

                SwitchPreferenceView(
                    title = stringResource(R.string.black_theme_variant),
                    value = uiState.useBlackColors,
                    icon = Icons.Rounded.DarkMode,
                    useTonalContainer = true,
                    onValueChange = { event?.setUseBlackColors(it) }
                )

            }

            // ── Navigation ──────────────────────────────────────────────────
            SettingsCardSection(title = stringResource(R.string.navigation)) {
                ListPreferenceView(
                    title = stringResource(R.string.default_section),
                    entriesValues = StartTab.entriesLocalized,
                    value = uiState.startTab,
                    icon = Icons.Rounded.Home,
                    useTonalContainer = true,
                    onValueChange = { event?.setStartTab(it) }
                )

                SettingsDivider()

                SwitchPreferenceView(
                    title = stringResource(R.string.pinned_navigation_bar),
                    value = uiState.pinnedNavBar,
                    icon = Icons.Rounded.PushPin,
                    useTonalContainer = true,
                    onValueChange = { event?.setPinnedNavBar(it) }
                )

                SettingsDivider()

                ListPreferenceView(
                    title = stringResource(R.string.tablet_mode),
                    entriesValues = TabletMode.entriesLocalized,
                    value = uiState.tabletMode,
                    icon = Icons.Rounded.Tablet,
                    useTonalContainer = true,
                    onValueChange = { event?.setTabletMode(it) }
                )
            }

            // ── Media Lists ─────────────────────────────────────────────────
            SettingsCardSection(title = stringResource(R.string.media_lists)) {
                SwitchPreferenceView(
                    title = stringResource(R.string.use_separated_list_styles),
                    value = !uiState.useGeneralListStyle,
                    icon = Icons.Rounded.ViewAgenda,
                    useTonalContainer = true,
                    onValueChange = { event?.setUseGeneralListStyle(!it) }
                )

                SettingsDivider()

                if (uiState.useGeneralListStyle) {
                    ListPreferenceView(
                        title = stringResource(R.string.list_style),
                        entriesValues = ListStyle.entriesLocalized,
                        value = uiState.generalListStyle,
                        icon = Icons.AutoMirrored.Rounded.FormatListBulleted,
                        useTonalContainer = true,
                        onValueChange = { event?.setGeneralListStyle(it) }
                    )
                } else {
                    PlainPreferenceView(
                        title = stringResource(R.string.list_style),
                        icon = Icons.AutoMirrored.Rounded.FormatListBulleted,
                        useTonalContainer = true,
                        onClick = dropUnlessResumed { navActionManager.toListStyleSettings() }
                    )
                }

                if (uiState.generalListStyle == ListStyle.GRID || !uiState.useGeneralListStyle) {
                    SettingsDivider()
                    ListPreferenceView(
                        title = stringResource(R.string.items_per_row),
                        entriesValues = ItemsPerRow.entriesLocalized,
                        value = uiState.itemsPerRow,
                        icon = Icons.Rounded.GridView,
                        useTonalContainer = true,
                        onValueChange = { event?.setItemsPerRow(it) }
                    )
                }

                SettingsDivider()

                SwitchPreferenceView(
                    title = stringResource(R.string.enable_list_tabs),
                    subtitle = stringResource(R.string.enable_list_tabs_subtitle),
                    value = uiState.useListTabs,
                    icon = Icons.Rounded.Tab,
                    useTonalContainer = true,
                    onValueChange = { event?.setUseListTabs(it) }
                )
            }

            // ── Content ─────────────────────────────────────────────────────
            SettingsCardSection(title = stringResource(R.string.content)) {
                ListPreferenceView(
                    title = stringResource(R.string.title_language),
                    entriesValues = TitleLanguage.entriesLocalized,
                    value = uiState.titleLanguage,
                    icon = Icons.Rounded.Title,
                    useTonalContainer = true,
                    onValueChange = { event?.setTitleLanguage(it) }
                )

                SettingsDivider()

                SwitchPreferenceView(
                    title = stringResource(R.string.show_nsfw),
                    subtitle = stringResource(R.string.nsfw_summary),
                    value = uiState.showNsfw,
                    icon = Icons.Rounded.Block,
                    useTonalContainer = true,
                    onValueChange = { event?.setShowNsfw(it) }
                )

                SettingsDivider()

                SwitchPreferenceView(
                    title = stringResource(R.string.hide_scores),
                    value = uiState.hideScores,
                    icon = Icons.Rounded.Star,
                    useTonalContainer = true,
                    onValueChange = { event?.setHideScores(it) }
                )

                SettingsDivider()

                SwitchPreferenceView(
                    title = stringResource(R.string.always_load_characters),
                    value = uiState.loadCharacters,
                    icon = Icons.Rounded.Group,
                    useTonalContainer = true,
                    onValueChange = { event?.setLoadCharacters(it) }
                )

                SettingsDivider()

                SwitchPreferenceView(
                    title = stringResource(R.string.random_button_on_list),
                    value = uiState.randomListEntryEnabled,
                    icon = Icons.Rounded.Casino,
                    useTonalContainer = true,
                    onValueChange = { event?.setRandomListEntryEnabled(it) }
                )
            }

            // ── App Integration ─────────────────────────────────────────────
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SettingsCardSection(title = stringResource(R.string.app_integration)) {
                    PlainPreferenceView(
                        title = stringResource(R.string.open_mal_links_in_the_app),
                        icon = Icons.Rounded.OpenInBrowser,
                        useTonalContainer = true,
                        onClick = { context.openByDefaultSettings() }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCardSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge
        )
        content()
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
}

@Composable
fun SettingsTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .padding(start = 72.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.secondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Preview
@Composable
fun SettingsPreview() {
    MoeListTheme {
        Surface {
            SettingsViewContent(
                uiState = SettingsUiState(),
                event = null,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}
