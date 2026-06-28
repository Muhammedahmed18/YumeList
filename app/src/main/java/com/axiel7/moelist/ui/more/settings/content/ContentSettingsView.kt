package com.axiel7.moelist.ui.more.settings.content

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.BackIconButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentSettingsViewContent(
    uiState: ContentUiState,
    event: ContentEvent?,
    navActionManager: NavActionManager,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.content)) },
                navigationIcon = { BackIconButton(onClick = navActionManager::goBack) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // ── Title Language ───────────────────────────────────────────────
            SectionLabel(stringResource(R.string.title_language))

            Row(
                modifier = Modifier.height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TitleLanguage.entries.forEach { language ->
                    TitleLanguageCard(
                        language = language,
                        selected = uiState.titleLanguage == language,
                        onClick = { event?.setTitleLanguage(language) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }

            // ── Content Filters ──────────────────────────────────────────────
            SectionLabel(stringResource(R.string.content_filters))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ContentToggleTile(
                        title = stringResource(R.string.show_nsfw),
                        icon = Icons.Rounded.Block,
                        checked = uiState.showNsfw,
                        onClick = { event?.setShowNsfw(!uiState.showNsfw) },
                        modifier = Modifier.weight(1f),
                    )
                    ContentToggleTile(
                        title = stringResource(R.string.hide_scores),
                        icon = Icons.Rounded.Star,
                        checked = uiState.hideScores,
                        onClick = { event?.setHideScores(!uiState.hideScores) },
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ContentToggleTile(
                        title = stringResource(R.string.always_load_characters),
                        icon = Icons.Rounded.Group,
                        checked = uiState.loadCharacters,
                        onClick = { event?.setLoadCharacters(!uiState.loadCharacters) },
                        modifier = Modifier.weight(1f),
                    )
                    ContentToggleTile(
                        title = stringResource(R.string.random_button_on_list),
                        icon = Icons.Rounded.Casino,
                        checked = uiState.randomListEntryEnabled,
                        onClick = { event?.setRandomListEntryEnabled(!uiState.randomListEntryEnabled) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
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
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "lang_border",
    )
    val sampleTitle = when (language) {
        TitleLanguage.ROMAJI -> "Shingeki no Kyojin"
        TitleLanguage.ENGLISH -> "Attack on Titan"
        TitleLanguage.JAPANESE -> "進撃の巨人"
    }

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        border = BorderStroke(2.dp, borderColor),
    ) {
        Box {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(language.stringRes),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = sampleTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(16.dp),
                )
            }
        }
    }
}

// ── Content toggle tile ───────────────────────────────────────────────────────

@Composable
private fun ContentToggleTile(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "tile_border",
    )

    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 96.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        border = BorderStroke(2.dp, borderColor),
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (checked) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                )
            }
            if (checked) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(16.dp),
                )
            }
        }
    }
}

// ── Section label ─────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 2.dp),
    )
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
