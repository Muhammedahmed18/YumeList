package com.axiel7.moelist.ui.more.about

import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.BuildConfig
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.LocalSnackbarHostState
import com.axiel7.moelist.ui.composables.SnackbarType
import com.axiel7.moelist.ui.composables.preferences.SettingsNavItem
import com.axiel7.moelist.ui.composables.preferences.SettingsScaffold
import com.axiel7.moelist.ui.composables.preferences.SettingsSection
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
import com.axiel7.moelist.ui.icons.rounded.Group
import com.axiel7.moelist.ui.icons.rounded.NewReleases
import com.axiel7.moelist.ui.icons.rounded.Notifications
import com.axiel7.moelist.ui.icons.rounded.Palette
import com.axiel7.moelist.ui.icons.rounded.Schedule
import com.axiel7.moelist.ui.icons.rounded.Share
import com.axiel7.moelist.ui.icons.rounded.Verified
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.copyToClipBoard
import com.axiel7.moelist.utils.ContextExtensions.openAction
import com.axiel7.moelist.utils.ContextExtensions.openShareSheet
import com.axiel7.moelist.utils.GITHUB_RELEASES_URL
import com.axiel7.moelist.utils.GITHUB_REPO_URL
import com.axiel7.moelist.utils.MAL_API_DOCS_URL
import com.axiel7.moelist.utils.MOELIST_ORIGINAL_URL
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.coroutines.launch

/** Taps on the version chip that trigger the easter egg. */
private const val EasterEggTaps = 7

/** App version, Android version and device, for bug reports. */
private fun diagnosticsText(): String =
    "YumeList ${BuildConfig.VERSION_NAME} · Android ${Build.VERSION.RELEASE} " +
        "(API ${Build.VERSION.SDK_INT}) · ${Build.MANUFACTURER} ${Build.MODEL}"

/** A new GitHub issue with the diagnostics already filled in under the description. */
private fun newIssueUrl(): String =
    "$GITHUB_REPO_URL/issues/new?body=" + Uri.encode("\n\n---\n${diagnosticsText()}")

/** One feature on the About screen and where tapping it goes. */
private data class Highlight(
    val title: Int,
    val subtitle: Int,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

/**
 * About:
 *
 * - Hero: logo, name, and the version chip (tap = copy version + device info for bug reports;
 *   tapping it 7 times still finds the easter egg)
 * - Support & feedback: report a bug (pre-filled with that info), changelog, source, share
 * - Explore YumeList: the app's features as tiles that open each one. Onboarding doesn't
 *   introduce these, so this is where people discover them.
 * - Credits & legal: MoeList, the MAL API, the people behind the app, open-source licenses
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutView(
    navActionManager: NavActionManager,
    isLoggedIn: Boolean = true,
) {
    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    var versionClicks by remember { mutableIntStateOf(0) }

    // One shape for every feature icon: the same scalloped cookie as the logo
    val highlightShape = MaterialShapes.Cookie9Sided.toShape()
    val highlights = listOf(
        Highlight(R.string.highlight_track, R.string.highlight_track_desc, Icons.AutoMirrored.Rounded.PlaylistAdd) {
            // Guests have no list yet: send them to sign in
            if (isLoggedIn) navActionManager.toUserList(MediaType.ANIME) else navActionManager.toLogin()
        },
        Highlight(R.string.highlight_seasonal, R.string.highlight_seasonal_desc, Icons.Rounded.CalendarMonth) {
            navActionManager.toSeasonChart()
        },
        Highlight(R.string.highlight_discover, R.string.highlight_discover_desc, Icons.Rounded.EmojiEvents) {
            navActionManager.toMediaRanking(MediaType.ANIME)
        },
        Highlight(R.string.highlight_themes, R.string.highlight_themes_desc, Icons.Rounded.Palette) {
            navActionManager.toAppearance()
        },
        Highlight(R.string.highlight_airing, R.string.highlight_airing_desc, Icons.Rounded.Schedule) {
            navActionManager.toCalendar()
        },
        Highlight(R.string.highlight_notifications, R.string.highlight_notifications_desc, Icons.Rounded.Notifications) {
            navActionManager.toNotifications()
        },
    )

    SettingsScaffold(
        title = stringResource(R.string.about),
        onBack = navActionManager::goBack,
        // The hero below shows the name large, so the bar stays small
        largeTopBar = false,
    ) {
        // ── Hero ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Logo in an M3 Expressive scalloped shape, in the app icon's own colours:
            // white 夢 on its indigo
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(MaterialShapes.Cookie9Sided.toShape())
                    .background(colorResource(R.color.colorYumeListIcon)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_yumelist_mark),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White,
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
                    // First tap of a streak copies the info (one "Copied!" toast, not seven)
                    if (versionClicks == 0) context.copyToClipBoard(diagnosticsText())
                    versionClicks++
                    if (versionClicks >= EasterEggTaps) {
                        scope.launch { snackbarHostState.showAppSnackbar("✧◝(⁰▿⁰)◜✧", type = SnackbarType.INFO) }
                        versionClicks = 0
                    }
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
            Text(
                text = stringResource(R.string.about_tap_to_copy),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Plain text, no card: it's an introduction, not a section
            Text(
                text = stringResource(R.string.app_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }

        // ── Support & feedback ────────────────────────────────
        SettingsSection(title = stringResource(R.string.about_support)) {
            item {
                SettingsNavItem(
                    title = stringResource(R.string.report_bug),
                    subtitle = stringResource(R.string.report_bug_summary),
                    icon = Icons.Rounded.BugReport,
                    opensExternally = true,
                    onClick = { context.openAction(newIssueUrl()) },
                )
            }
            item {
                SettingsNavItem(
                    title = stringResource(R.string.changelog),
                    subtitle = stringResource(R.string.changelog_summary),
                    icon = Icons.Rounded.NewReleases,
                    opensExternally = true,
                    onClick = { context.openAction(GITHUB_RELEASES_URL) },
                )
            }
            item {
                SettingsNavItem(
                    title = stringResource(R.string.github),
                    subtitle = stringResource(R.string.github_summary),
                    icon = painterResource(R.drawable.ic_github),
                    opensExternally = true,
                    onClick = { context.openAction(GITHUB_REPO_URL) },
                )
            }
            item {
                SettingsNavItem(
                    title = stringResource(R.string.about_share_app),
                    subtitle = stringResource(R.string.about_share_app_summary),
                    icon = Icons.Rounded.Share,
                    showChevron = false,
                    onClick = { context.openShareSheet(GITHUB_RELEASES_URL) },
                )
            }
        }

        // ── Explore YumeList ──────────────────────────────────
        SettingsSectionHeader(stringResource(R.string.about_explore))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            highlights.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    pair.forEach { highlight ->
                        AboutHighlightCard(
                            title = stringResource(highlight.title),
                            subtitle = stringResource(highlight.subtitle),
                            icon = highlight.icon,
                            shape = highlightShape,
                            onClick = highlight.onClick,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }

        // ── Credits & legal ───────────────────────────────────
        SettingsSection(title = stringResource(R.string.about_credits_legal)) {
            item {
                SettingsNavItem(
                    title = stringResource(R.string.built_on_moelist),
                    subtitle = stringResource(R.string.built_on_moelist_summary),
                    icon = rememberVectorPainter(Icons.Rounded.Favorite),
                    iconContainerColor = MaterialTheme.colorScheme.errorContainer,
                    iconContentColor = MaterialTheme.colorScheme.onErrorContainer,
                    opensExternally = true,
                    onClick = { context.openAction(MOELIST_ORIGINAL_URL) },
                )
            }
            item {
                SettingsNavItem(
                    title = stringResource(R.string.powered_by_mal),
                    subtitle = stringResource(R.string.powered_by_mal_summary),
                    icon = Icons.Rounded.Api,
                    opensExternally = true,
                    onClick = { context.openAction(MAL_API_DOCS_URL) },
                )
            }
            item {
                SettingsNavItem(
                    title = stringResource(R.string.about_people),
                    subtitle = stringResource(R.string.about_people_summary),
                    icon = Icons.Rounded.Group,
                    onClick = { navActionManager.toCredits() },
                )
            }
            item {
                SettingsNavItem(
                    title = stringResource(R.string.open_source_licenses),
                    subtitle = stringResource(R.string.open_source_licenses_summary),
                    icon = Icons.Rounded.Gavel,
                    onClick = { context.startActivity(Intent(context, OssLicensesMenuActivity::class.java)) },
                )
            }
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
