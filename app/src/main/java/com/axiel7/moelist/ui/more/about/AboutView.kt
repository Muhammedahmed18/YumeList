package com.axiel7.moelist.ui.more.about

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Api
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.BuildConfig
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.composables.LocalSnackbarHostState
import com.axiel7.moelist.ui.composables.showSnackbarShort
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openAction
import com.axiel7.moelist.utils.GITHUB_ISSUES_URL
import com.axiel7.moelist.utils.GITHUB_RELEASES_URL
import com.axiel7.moelist.utils.GITHUB_REPO_URL
import com.axiel7.moelist.utils.MAL_API_DOCS_URL
import com.axiel7.moelist.utils.MOELIST_ORIGINAL_URL
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.coroutines.launch

@Composable
fun AboutView(navActionManager: NavActionManager) {
    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var versionClicks by remember { mutableIntStateOf(0) }

    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.about),
        navigateBack = navActionManager::goBack,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {

            // ── Hero ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                        )
                        .border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_yumelist_logo),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = {
                        if (versionClicks >= 7) {
                            scope.launch { snackbarHostState.showSnackbarShort("✧◝(⁰▿⁰)◜✧") }
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
                )
            }

            // ── Description card ──────────────────────────────────
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.app_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Core Highlights ───────────────────────────────────
            Text(
                text = stringResource(R.string.core_highlights),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AboutHighlightCard(
                    title = stringResource(R.string.highlight_track),
                    subtitle = stringResource(R.string.highlight_track_desc),
                    icon = Icons.Rounded.PlaylistAdd,
                    modifier = Modifier.weight(1f),
                )
                AboutHighlightCard(
                    title = stringResource(R.string.highlight_seasonal),
                    subtitle = stringResource(R.string.highlight_seasonal_desc),
                    icon = Icons.Rounded.CalendarMonth,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AboutHighlightCard(
                    title = stringResource(R.string.highlight_discover),
                    subtitle = stringResource(R.string.highlight_discover_desc),
                    icon = Icons.Rounded.Explore,
                    modifier = Modifier.weight(1f),
                )
                AboutHighlightCard(
                    title = stringResource(R.string.highlight_themes),
                    subtitle = stringResource(R.string.highlight_themes_desc),
                    icon = Icons.Rounded.Palette,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Connect & Share ───────────────────────────────────
            Text(
                text = stringResource(R.string.connect_and_share),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AboutActionRow(
                    title = stringResource(R.string.github),
                    subtitle = stringResource(R.string.github_summary),
                    icon = R.drawable.ic_github,
                    onClick = { context.openAction(GITHUB_REPO_URL) },
                )
                AboutActionRow(
                    title = stringResource(R.string.changelog),
                    subtitle = stringResource(R.string.changelog_summary),
                    icon = Icons.Rounded.NewReleases,
                    onClick = { context.openAction(GITHUB_RELEASES_URL) },
                )
                AboutActionRow(
                    title = stringResource(R.string.report_bug),
                    subtitle = stringResource(R.string.report_bug_summary),
                    icon = Icons.Rounded.BugReport,
                    onClick = { context.openAction(GITHUB_ISSUES_URL) },
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Credits ───────────────────────────────────────────
            Text(
                text = stringResource(R.string.credits),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AboutActionRow(
                    title = stringResource(R.string.built_on_moelist),
                    subtitle = stringResource(R.string.built_on_moelist_summary),
                    icon = Icons.Rounded.Favorite,
                    iconTint = MaterialTheme.colorScheme.error,
                    onClick = { context.openAction(MOELIST_ORIGINAL_URL) },
                )
                AboutActionRow(
                    title = stringResource(R.string.powered_by_mal),
                    subtitle = stringResource(R.string.powered_by_mal_summary),
                    icon = Icons.Rounded.Api,
                    onClick = { context.openAction(MAL_API_DOCS_URL) },
                )
                AboutActionRow(
                    title = stringResource(R.string.open_source_licenses),
                    subtitle = stringResource(R.string.open_source_licenses_summary),
                    icon = Icons.Rounded.Gavel,
                    onClick = { context.startActivity(Intent(context, OssLicensesMenuActivity::class.java)) },
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Footer ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Made with",
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
                        text = "by Muhammed",
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
