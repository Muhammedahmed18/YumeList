package com.axiel7.moelist.ui.more.settings.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Tab
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.ViewStream
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusAnimeValues
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusMangaValues
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.ItemsPerRow
import com.axiel7.moelist.ui.base.ListStyle
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.BackIconButton
import com.axiel7.moelist.ui.more.settings.SettingsTitle
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListStyleSettingsViewContent(
    uiState: ListStyleSettingsUiState,
    event: ListStyleSettingsEvent?,
    navActionManager: NavActionManager,
) {
    val showGridSection = uiState.useGeneralListStyle && uiState.generalListStyle == ListStyle.GRID
            || !uiState.useGeneralListStyle

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.media_lists)) },
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
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // ── List Style ───────────────────────────────────────────────────
            SectionLabel(stringResource(R.string.list_style))

            // Separated styles toggle
            ListToggleTile(
                title = stringResource(R.string.use_separated_list_styles),
                icon = Icons.Rounded.ViewAgenda,
                checked = !uiState.useGeneralListStyle,
                onClick = { event?.setUseGeneralListStyle(!uiState.useGeneralListStyle) },
                modifier = Modifier.fillMaxWidth(),
            )

            // Unified: 4 visual style preview cards in 2 rows
            AnimatedVisibility(
                visible = uiState.useGeneralListStyle,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ListStylePreviewCard(
                            style = ListStyle.STANDARD,
                            selected = uiState.generalListStyle == ListStyle.STANDARD,
                            onClick = { event?.setGeneralListStyle(ListStyle.STANDARD) },
                            modifier = Modifier.weight(1f),
                        )
                        ListStylePreviewCard(
                            style = ListStyle.COMPACT,
                            selected = uiState.generalListStyle == ListStyle.COMPACT,
                            onClick = { event?.setGeneralListStyle(ListStyle.COMPACT) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ListStylePreviewCard(
                            style = ListStyle.MINIMAL,
                            selected = uiState.generalListStyle == ListStyle.MINIMAL,
                            onClick = { event?.setGeneralListStyle(ListStyle.MINIMAL) },
                            modifier = Modifier.weight(1f),
                        )
                        ListStylePreviewCard(
                            style = ListStyle.GRID,
                            selected = uiState.generalListStyle == ListStyle.GRID,
                            onClick = { event?.setGeneralListStyle(ListStyle.GRID) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Separated: per-status filter chips for Anime and Manga
            AnimatedVisibility(
                visible = !uiState.useGeneralListStyle,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SettingsTitle(text = stringResource(R.string.title_anime_list))
                    listStatusAnimeValues.forEach { status ->
                        val style = event?.getListStyle(MediaType.ANIME, status)
                            ?.collectAsStateWithLifecycle()
                        StatusStyleRow(
                            status = status,
                            currentStyle = style?.value ?: ListStyle.STANDARD,
                            onStyleChange = { event?.setListStyle(MediaType.ANIME, status, it) },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    SettingsTitle(text = stringResource(R.string.title_manga_list))
                    listStatusMangaValues.forEach { status ->
                        val style = event?.getListStyle(MediaType.MANGA, status)
                            ?.collectAsStateWithLifecycle()
                        StatusStyleRow(
                            status = status,
                            currentStyle = style?.value ?: ListStyle.STANDARD,
                            onStyleChange = { event?.setListStyle(MediaType.MANGA, status, it) },
                        )
                    }
                }
            }

            // ── Grid ─────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = showGridSection,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionLabel(stringResource(R.string.items_per_row))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(ItemsPerRow.DEFAULT, ItemsPerRow.TWO, ItemsPerRow.THREE, ItemsPerRow.FOUR)
                            .forEach { option ->
                                ItemsPerRowCard(
                                    option = option,
                                    selected = uiState.itemsPerRow == option,
                                    onClick = { event?.setItemsPerRow(option) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                    }
                }
            }

            // ── Tabs ─────────────────────────────────────────────────────────
            SectionLabel(stringResource(R.string.enable_list_tabs))

            ListToggleTile(
                title = stringResource(R.string.enable_list_tabs),
                subtitle = stringResource(R.string.enable_list_tabs_subtitle),
                icon = Icons.Rounded.Tab,
                checked = uiState.useListTabs,
                onClick = { event?.setUseListTabs(!uiState.useListTabs) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── List Style preview card ───────────────────────────────────────────────────

private val ListStyle.icon: ImageVector
    get() = when (this) {
        ListStyle.STANDARD -> Icons.Rounded.ViewAgenda
        ListStyle.COMPACT  -> Icons.Rounded.ViewStream
        ListStyle.MINIMAL  -> Icons.AutoMirrored.Rounded.ViewList
        ListStyle.GRID     -> Icons.Rounded.GridView
    }

@Composable
private fun ListStylePreviewCard(
    style: ListStyle,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "style_border",
    )

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
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = stringResource(style.stringRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
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

// ── Items per row card ────────────────────────────────────────────────────────

@Composable
private fun ItemsPerRowCard(
    option: ItemsPerRow,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "row_border",
    )

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
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GridDotsPreview(
                    columns = if (option == ItemsPerRow.DEFAULT) 2 else option.value,
                    tint = if (selected) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(option.stringRes),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
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
                        .padding(6.dp)
                        .size(12.dp),
                )
            }
        }
    }
}

@Composable
private fun GridDotsPreview(columns: Int, tint: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(columns.coerceIn(1, 4)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(tint.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
            )
        }
    }
}

// ── Per-status style row ──────────────────────────────────────────────────────

@Composable
private fun StatusStyleRow(
    status: ListStatus,
    currentStyle: ListStyle,
    onStyleChange: (ListStyle) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = status.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = status.localized(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
        ) {
            items(ListStyle.entries) { style ->
                FilterChip(
                    selected = currentStyle == style,
                    onClick = { onStyleChange(style) },
                    label = {
                        Text(
                            text = stringResource(style.stringRes),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                )
            }
        }
    }
}

// ── Full-width toggle tile ────────────────────────────────────────────────────

@Composable
private fun ListToggleTile(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val borderColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "toggle_border",
    )

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        border = BorderStroke(2.dp, borderColor),
    ) {
        Box {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (checked) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
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
private fun ListStyleSettingsPreview() {
    MoeListTheme {
        ListStyleSettingsViewContent(
            uiState = ListStyleSettingsUiState(),
            event = null,
            navActionManager = NavActionManager.rememberNavActionManager(),
        )
    }
}
