package com.axiel7.moelist.ui.more.settings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.repeatOnLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.preferences.SettingsNavItem
import com.axiel7.moelist.ui.composables.preferences.SettingsScaffold
import com.axiel7.moelist.ui.composables.preferences.SettingsSection
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.Explore
import com.axiel7.moelist.ui.icons.rounded.OpenInBrowser
import com.axiel7.moelist.ui.icons.rounded.Palette
import com.axiel7.moelist.ui.icons.rounded.Tune
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.isMalLinkHandlingEnabled
import com.axiel7.moelist.utils.ContextExtensions.openByDefaultSettings

@Composable
fun SettingsView(
    navActionManager: NavActionManager
) {
    SettingsViewContent(navActionManager = navActionManager)
}

@Composable
private fun SettingsViewContent(
    navActionManager: NavActionManager
) {
    SettingsScaffold(
        title = stringResource(R.string.settings),
        onBack = navActionManager::goBack,
    ) {
        SettingsSection {
            item {
                SettingsNavItem(
                    title = stringResource(R.string.appearance),
                    subtitle = stringResource(R.string.settings_appearance_hint),
                    icon = Icons.Rounded.Palette,
                    onClick = dropUnlessResumed { navActionManager.toAppearance() },
                )
            }
            item {
                SettingsNavItem(
                    title = stringResource(R.string.lists_and_navigation),
                    subtitle = stringResource(R.string.settings_lists_navigation_hint),
                    icon = Icons.Rounded.Explore,
                    onClick = dropUnlessResumed { navActionManager.toNavigationSettings() },
                )
            }
            item {
                SettingsNavItem(
                    title = stringResource(R.string.content),
                    subtitle = stringResource(R.string.settings_content_hint),
                    icon = Icons.Rounded.Tune,
                    onClick = dropUnlessResumed { navActionManager.toContentSettings() },
                )
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AppIntegrationSection()
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ── App Integration ───────────────────────────────────────────────────────────

/**
 * "Open MAL links in the app" with a status badge. Tapping it opens the system "Open by
 * default" page. The status is re-checked every time the screen resumes, so it updates after
 * coming back from the system settings. When it isn't set up, the subtitle says what tapping
 * does and the badge uses the error colours so it reads as something to fix.
 */
@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun AppIntegrationSection() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isActive by remember { mutableStateOf(context.isMalLinkHandlingEnabled()) }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            isActive = context.isMalLinkHandlingEnabled()
        }
    }

    SettingsSection(title = stringResource(R.string.app_integration)) {
        item {
            SettingsNavItem(
                title = stringResource(R.string.open_mal_links_in_the_app),
                subtitle = stringResource(
                    if (isActive) R.string.app_integration_active_summary
                    else R.string.app_integration_not_set_up_summary
                ),
                icon = Icons.Rounded.OpenInBrowser,
                onClick = { context.openByDefaultSettings() },
                trailingContent = {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (isActive) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer,
                        contentColor = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onErrorContainer,
                    ) {
                        Text(
                            text = stringResource(
                                if (isActive) R.string.app_integration_active
                                else R.string.app_integration_not_set_up
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmallEmphasized,
                        )
                    }
                },
            )
        }
    }
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
