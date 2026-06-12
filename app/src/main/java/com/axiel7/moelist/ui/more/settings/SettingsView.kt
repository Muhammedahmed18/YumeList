package com.axiel7.moelist.ui.more.settings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.BackIconButton
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.isMalLinkHandlingEnabled
import com.axiel7.moelist.utils.ContextExtensions.openByDefaultSettings

@Composable
fun SettingsView(
    navActionManager: NavActionManager
) {
    SettingsViewContent(navActionManager = navActionManager)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsViewContent(
    navActionManager: NavActionManager
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingsNavCard(
                title = stringResource(R.string.appearance),
                subtitle = stringResource(R.string.settings_appearance_hint),
                icon = Icons.Rounded.Palette,
                onClick = dropUnlessResumed { navActionManager.toAppearance() },
            )
            SettingsNavCard(
                title = stringResource(R.string.navigation),
                subtitle = stringResource(R.string.settings_navigation_hint),
                icon = Icons.Rounded.Explore,
                onClick = dropUnlessResumed { navActionManager.toNavigationSettings() },
            )
            SettingsNavCard(
                title = stringResource(R.string.media_lists),
                subtitle = stringResource(R.string.settings_media_lists_hint),
                icon = Icons.Rounded.GridView,
                onClick = dropUnlessResumed { navActionManager.toListStyleSettings() },
            )
            SettingsNavCard(
                title = stringResource(R.string.content),
                subtitle = stringResource(R.string.settings_content_hint),
                icon = Icons.Rounded.Tune,
                onClick = dropUnlessResumed { navActionManager.toContentSettings() },
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AppIntegrationCard()
            }
        }
    }
}

// ── App Integration card ──────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun AppIntegrationCard(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isActive = remember { context.isMalLinkHandlingEnabled() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.OpenInBrowser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.app_integration),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.open_mal_links_in_the_app),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (isActive) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.tertiaryContainer,
                ) {
                    Text(
                        text = if (isActive) stringResource(R.string.app_integration_active)
                               else stringResource(R.string.app_integration_not_set_up),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                FilledTonalButton(onClick = { context.openByDefaultSettings() }) {
                    Text(
                        text = if (isActive) stringResource(R.string.app_integration_manage)
                               else stringResource(R.string.app_integration_enable),
                    )
                }
            }
        }
    }
}

// ── Settings nav card ─────────────────────────────────────────────────────────

@Composable
private fun SettingsNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// Used by CreditsView and ListStyleSettingsView
@Composable
fun SettingsTitle(text: String) {
    Text(
        text = text,
        modifier = androidx.compose.ui.Modifier
            .padding(start = 72.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.secondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Preview
@Composable
fun SettingsPreview() {
    MoeListTheme {
        SettingsViewContent(
            navActionManager = NavActionManager.rememberNavActionManager()
        )
    }
}
