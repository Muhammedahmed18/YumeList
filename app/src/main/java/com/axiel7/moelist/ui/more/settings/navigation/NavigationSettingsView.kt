package com.axiel7.moelist.ui.more.settings.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalMovies
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Tablet
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.StartTab
import com.axiel7.moelist.ui.base.TabletMode
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.BackIconButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationSettingsViewContent(
    uiState: NavigationSettingsUiState,
    event: NavigationSettingsEvent?,
    navActionManager: NavActionManager,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.navigation)) },
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
                .padding(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // ── Default Tab ──────────────────────────────────────────────────
            SectionLabel(
                text = stringResource(R.string.default_section),
                modifier = Modifier.padding(horizontal = 18.dp),
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val startTabs = StartTab.entries.filter { it != StartTab.MORE }
                startTabs.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        pair.forEach { tab ->
                            StartTabCard(
                                tab = tab,
                                selected = uiState.startTab == tab,
                                onClick = { event?.setStartTab(tab) },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                            )
                        }
                    }
                }
            }

            // ── Tablet Mode ──────────────────────────────────────────────────
            SectionLabel(
                text = stringResource(R.string.tablet_mode),
                modifier = Modifier.padding(horizontal = 18.dp),
            )

            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TabletMode.entries.forEach { mode ->
                    TabletModeCard(
                        mode = mode,
                        selected = uiState.tabletMode == mode,
                        onClick = { event?.setTabletMode(mode) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── Navigation Bar ───────────────────────────────────────────────
            SectionLabel(
                text = stringResource(R.string.navigation_bar),
                modifier = Modifier.padding(horizontal = 18.dp),
            )

            PinnedNavBarCard(
                pinned = uiState.pinnedNavBar,
                onClick = { event?.setPinnedNavBar(!uiState.pinnedNavBar) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

// ── Start Tab card ────────────────────────────────────────────────────────────

private val StartTab.icon: ImageVector
    get() = when (this) {
        StartTab.LAST_USED -> Icons.Rounded.History
        StartTab.HOME      -> Icons.Rounded.Home
        StartTab.ANIME     -> Icons.Rounded.LocalMovies
        StartTab.MANGA     -> Icons.Rounded.Book
        StartTab.MORE      -> Icons.Rounded.MoreHoriz
    }

@Composable
private fun StartTabCard(
    tab: StartTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "tab_border",
    )

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        border = BorderStroke(2.dp, borderColor),
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = stringResource(tab.stringRes),
                    style = MaterialTheme.typography.labelMedium,
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
                        .padding(8.dp)
                        .size(16.dp),
                )
            }
        }
    }
}

// ── Tablet Mode card ──────────────────────────────────────────────────────────

private val TabletMode.icon: ImageVector
    get() = when (this) {
        TabletMode.AUTO      -> Icons.Rounded.Devices
        TabletMode.ALWAYS    -> Icons.Rounded.Tablet
        TabletMode.LANDSCAPE -> Icons.Rounded.ScreenRotation
        TabletMode.NEVER     -> Icons.Rounded.Smartphone
    }

@Composable
private fun TabletModeCard(
    mode: TabletMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "mode_border",
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
                val iconTint = if (selected) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                Icon(
                    imageVector = mode.icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = stringResource(mode.stringRes),
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
                        .size(14.dp),
                )
            }
        }
    }
}

// ── Pinned Nav Bar card ───────────────────────────────────────────────────────

@Composable
private fun PinnedNavBarCard(
    pinned: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (pinned) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "nav_bar_border",
    )

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
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
                    imageVector = Icons.Rounded.PushPin,
                    contentDescription = null,
                    tint = if (pinned) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.pinned_navigation_bar),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (pinned) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (pinned) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.pinned_navigation_bar_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                NavBarMockup(pinned = pinned)
            }
            if (pinned) {
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

@Composable
private fun NavBarMockup(pinned: Boolean) {
    val bgColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val barColor by animateColorAsState(
        targetValue = if (pinned) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        label = "mockup_bar",
    )
    val dotColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .width(52.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor),
    ) {
        // Fake scrollable content
        Column(
            modifier = Modifier
                .padding(top = 8.dp, start = 6.dp, end = 6.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(3) { alpha ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant
                                .copy(alpha = 0.15f - alpha * 0.04f)
                        )
                )
            }
        }

        // Nav bar strip at the bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .align(Alignment.BottomCenter)
                .background(barColor),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(dotColor, CircleShape)
                )
            }
        }
    }
}

// ── Section label ─────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 2.dp),
    )
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
