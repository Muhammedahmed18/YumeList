package com.axiel7.moelist.ui.more.about

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.BuildConfig
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.LocalSnackbarHostState
import com.axiel7.moelist.ui.composables.SnackbarType
import com.axiel7.moelist.ui.composables.preferences.SettingsGroup
import com.axiel7.moelist.ui.composables.preferences.SettingsNavItem
import com.axiel7.moelist.ui.composables.preferences.SettingsScaffold
import com.axiel7.moelist.ui.composables.preferences.SettingsSectionHeader
import com.axiel7.moelist.ui.composables.showAppSnackbar
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.PlaylistAdd
import com.axiel7.moelist.ui.icons.rounded.Api
import com.axiel7.moelist.ui.icons.rounded.BugReport
import com.axiel7.moelist.ui.icons.rounded.CalendarMonth
import com.axiel7.moelist.ui.icons.rounded.EmojiEvents
import com.axiel7.moelist.ui.icons.rounded.Favorite
import com.axiel7.moelist.ui.icons.rounded.Gavel
import com.axiel7.moelist.ui.icons.rounded.NewReleases
import com.axiel7.moelist.ui.icons.rounded.Notifications
import com.axiel7.moelist.ui.icons.rounded.Palette
import com.axiel7.moelist.ui.icons.rounded.Schedule
import com.axiel7.moelist.ui.icons.rounded.Verified
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openAction
import com.axiel7.moelist.utils.GITHUB_ISSUES_URL
import com.axiel7.moelist.utils.GITHUB_RELEASES_URL
import com.axiel7.moelist.utils.GITHUB_REPO_URL
import com.axiel7.moelist.utils.MAL_API_DOCS_URL
import com.axiel7.moelist.utils.MOELIST_ORIGINAL_URL
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutView(navActionManager: NavActionManager) {
    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var versionClicks by remember { mutableIntStateOf(0) }

    SettingsScaffold(
        title = stringResource(R.string.about),
        onBack = navActionManager::goBack,
    ) {
        // ── Hero ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Logo in an M3 Expressive scalloped shape (same family as the profile avatar)
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(MaterialShapes.Cookie9Sided.toShape())
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_yumelist_logo),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMediumEmphasized,
            )
            Spacer(modifier = Modifier.height(8.dp))
            AssistChip(
                onClick = {
                    if (versionClicks >= 7) {
                        scope.launch { snackbarHostState.showAppSnackbar("✧◝(⁰▿⁰)◜✧", type = SnackbarType.INFO) }
                        versionClicks = 0
                    } else versionClicks++
                },
                label = {
                    Text("${BuildConfig.VERSION_NAME} · ${stringResource(R.string.stable_release)}")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Verified,
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize),
                    )
                },
                shape = CircleShape,
            )
        }

        // ── Description ───────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Text(
                text = stringResource(R.string.app_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )
        }

        // ── Core Highlights ───────────────────────────────────
        SettingsSectionHeader(stringResource(R.string.core_highlights))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AboutHighlightCard(
                    title = stringResource(R.string.highlight_track),
                    subtitle = stringResource(R.string.highlight_track_desc),
                    icon = Icons.AutoMirrored.Rounded.PlaylistAdd,
                    shape = MaterialShapes.Cookie4Sided.toShape(),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                AboutHighlightCard(
                    title = stringResource(R.string.highlight_seasonal),
                    subtitle = stringResource(R.string.highlight_seasonal_desc),
                    icon = Icons.Rounded.CalendarMonth,
                    shape = MaterialShapes.Flower.toShape(),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
            Row(
                modifier = Modifier.height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AboutHighlightCard(
                    title = stringResource(R.string.highlight_discover),
                    subtitle = stringResource(R.string.highlight_discover_desc),
                    icon = Icons.Rounded.EmojiEvents,
                    shape = MaterialShapes.Sunny.toShape(),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                AboutHighlightCard(
                    title = stringResource(R.string.highlight_themes),
                    subtitle = stringResource(R.string.highlight_themes_desc),
                    icon = Icons.Rounded.Palette,
                    shape = MaterialShapes.Clover4Leaf.toShape(),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
            Row(
                modifier = Modifier.height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AboutHighlightCard(
                    title = stringResource(R.string.highlight_airing),
                    subtitle = stringResource(R.string.highlight_airing_desc),
                    icon = Icons.Rounded.Schedule,
                    shape = MaterialShapes.Cookie6Sided.toShape(),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                AboutHighlightCard(
                    title = stringResource(R.string.highlight_notifications),
                    subtitle = stringResource(R.string.highlight_notifications_desc),
                    icon = Icons.Rounded.Notifications,
                    shape = MaterialShapes.SoftBurst.toShape(),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }

        // ── Connect & Share ───────────────────────────────────
        SettingsSectionHeader(stringResource(R.string.connect_and_share))
        SettingsGroup {
            SettingsNavItem(
                title = stringResource(R.string.github),
                subtitle = stringResource(R.string.github_summary),
                icon = painterResource(R.drawable.ic_github),
                index = 0,
                count = 3,
                onClick = { context.openAction(GITHUB_REPO_URL) },
            )
            SettingsNavItem(
                title = stringResource(R.string.changelog),
                subtitle = stringResource(R.string.changelog_summary),
                icon = Icons.Rounded.NewReleases,
                index = 1,
                count = 3,
                onClick = { context.openAction(GITHUB_RELEASES_URL) },
            )
            SettingsNavItem(
                title = stringResource(R.string.report_bug),
                subtitle = stringResource(R.string.report_bug_summary),
                icon = Icons.Rounded.BugReport,
                index = 2,
                count = 3,
                onClick = { context.openAction(GITHUB_ISSUES_URL) },
            )
        }

        // ── Credits ───────────────────────────────────────────
        SettingsSectionHeader(stringResource(R.string.credits))
        SettingsGroup {
            SettingsNavItem(
                title = stringResource(R.string.built_on_moelist),
                subtitle = stringResource(R.string.built_on_moelist_summary),
                icon = rememberVectorPainter(Icons.Rounded.Favorite),
                iconContainerColor = MaterialTheme.colorScheme.errorContainer,
                iconContentColor = MaterialTheme.colorScheme.onErrorContainer,
                index = 0,
                count = 3,
                onClick = { context.openAction(MOELIST_ORIGINAL_URL) },
            )
            SettingsNavItem(
                title = stringResource(R.string.powered_by_mal),
                subtitle = stringResource(R.string.powered_by_mal_summary),
                icon = Icons.Rounded.Api,
                index = 1,
                count = 3,
                onClick = { context.openAction(MAL_API_DOCS_URL) },
            )
            SettingsNavItem(
                title = stringResource(R.string.open_source_licenses),
                subtitle = stringResource(R.string.open_source_licenses_summary),
                icon = Icons.Rounded.Gavel,
                index = 2,
                count = 3,
                onClick = { context.startActivity(Intent(context, OssLicensesMenuActivity::class.java)) },
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Footer ────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.about_made_with),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = stringResource(R.string.about_by_author),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.copyright),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Preview
@Composable
fun AboutPreview() {
    MoeListTheme {
        Surface {
            AboutView(navActionManager = NavActionManager.rememberNavActionManager())
        }
    }
}
