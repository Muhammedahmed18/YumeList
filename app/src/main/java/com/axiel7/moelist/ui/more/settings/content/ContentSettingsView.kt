package com.axiel7.moelist.ui.more.settings.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.preferences.SettingsChoiceCard
import com.axiel7.moelist.ui.composables.preferences.SettingsGroup
import com.axiel7.moelist.ui.composables.preferences.SettingsScaffold
import com.axiel7.moelist.ui.composables.preferences.SettingsSectionHeader
import com.axiel7.moelist.ui.composables.preferences.SettingsSwitchItem
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.Block
import com.axiel7.moelist.ui.icons.rounded.Casino
import com.axiel7.moelist.ui.icons.rounded.Group
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
    SettingsScaffold(
        title = stringResource(R.string.content),
        onBack = navActionManager::goBack,
    ) {
        // ── Title Language ───────────────────────────────────────────────────
        SettingsSectionHeader(stringResource(R.string.title_language))

        Row(
            modifier = Modifier.height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TitleLanguage.entries.forEach { language ->
                TitleLanguageCard(
                    language = language,
                    selected = uiState.titleLanguage == language,
                    onClick = { event?.setTitleLanguage(language) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }

        // ── Content Filters ──────────────────────────────────────────────────
        SettingsSectionHeader(stringResource(R.string.content_filters))

        SettingsGroup {
            SettingsSwitchItem(
                title = stringResource(R.string.show_nsfw),
                icon = Icons.Rounded.Block,
                checked = uiState.showNsfw,
                onCheckedChange = { event?.setShowNsfw(it) },
                index = 0,
                count = 4,
            )
            SettingsSwitchItem(
                title = stringResource(R.string.hide_scores),
                icon = Icons.Rounded.Star,
                checked = uiState.hideScores,
                onCheckedChange = { event?.setHideScores(it) },
                index = 1,
                count = 4,
            )
            SettingsSwitchItem(
                title = stringResource(R.string.always_load_characters),
                icon = Icons.Rounded.Group,
                checked = uiState.loadCharacters,
                onCheckedChange = { event?.setLoadCharacters(it) },
                index = 2,
                count = 4,
            )
            SettingsSwitchItem(
                title = stringResource(R.string.random_button_on_list),
                icon = Icons.Rounded.Casino,
                checked = uiState.randomListEntryEnabled,
                onCheckedChange = { event?.setRandomListEntryEnabled(it) },
                index = 3,
                count = 4,
            )
        }
    }
}

// ── Title Language card ───────────────────────────────────────────────────────

@Composable
private fun TitleLanguageCard(
    language: TitleLanguage,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sampleTitle = when (language) {
        TitleLanguage.ROMAJI -> "Shingeki no Kyojin"
        TitleLanguage.ENGLISH -> "Attack on Titan"
        TitleLanguage.JAPANESE -> "進撃の巨人"
    }

    SettingsChoiceCard(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
    ) {
        Column(
            // End padding leaves room for the check
            modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 24.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(language.stringRes),
                style = if (selected) MaterialTheme.typography.labelLargeEmphasized
                else MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = sampleTitle,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
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
