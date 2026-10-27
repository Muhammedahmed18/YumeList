package com.axiel7.moelist.ui.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.preferences.SettingsGroup
import com.axiel7.moelist.ui.composables.preferences.SettingsNavItem
import com.axiel7.moelist.ui.composables.preferences.SettingsSectionHeader
import com.axiel7.moelist.ui.composables.preferences.settingsItemColors
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openCustomTab
import com.axiel7.moelist.utils.MAL_ANNOUNCEMENTS_URL
import com.axiel7.moelist.utils.MAL_NEWS_URL
import org.koin.androidx.compose.koinViewModel

@Composable
fun MoreView(
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
    padding: PaddingValues,
) {
    val viewModel: MoreViewModel = koinViewModel()

    MoreViewContent(
        event = viewModel,
        navActionManager = navActionManager,
        padding = padding,
        isLoggedIn = isLoggedIn
    )
}

@Composable
private fun MoreViewContent(
    isLoggedIn: Boolean,
    event: MoreEvent?,
    navActionManager: NavActionManager,
    padding: PaddingValues = PaddingValues(),
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    if (showLogoutDialog) {
        LogoutDialog(
            onConfirm = {
                showLogoutDialog = false
                event?.logOut()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(padding)
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 24.dp, bottom = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.more),
                style = MaterialTheme.typography.displaySmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.more_info),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Discover: external news links
        SettingsSectionHeader(text = stringResource(R.string.discover))
        SettingsGroup {
            SettingsNavItem(
                title = stringResource(R.string.anime_manga_news),
                subtitle = stringResource(R.string.news_summary),
                icon = painterResource(R.drawable.ic_new_releases),
                iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                iconContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                index = 0,
                count = 2,
                onClick = { context.openCustomTab(MAL_NEWS_URL) }
            )
            SettingsNavItem(
                title = stringResource(R.string.mal_announcements),
                subtitle = stringResource(R.string.mal_announcements_summary),
                icon = painterResource(R.drawable.ic_campaign),
                iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                iconContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                index = 1,
                count = 2,
                onClick = { context.openCustomTab(MAL_ANNOUNCEMENTS_URL) }
            )
        }

        // App
        SettingsSectionHeader(text = stringResource(R.string.app_name))
        SettingsGroup {
            SettingsNavItem(
                title = stringResource(R.string.notifications),
                icon = painterResource(R.drawable.round_notifications_24),
                index = 0,
                count = 3,
                onClick = dropUnlessResumed { navActionManager.toNotifications() }
            )
            SettingsNavItem(
                title = stringResource(R.string.settings),
                icon = painterResource(R.drawable.ic_round_settings_24),
                index = 1,
                count = 3,
                onClick = dropUnlessResumed { navActionManager.toSettings() }
            )
            SettingsNavItem(
                title = stringResource(R.string.about),
                icon = painterResource(R.drawable.ic_info),
                index = 2,
                count = 3,
                onClick = dropUnlessResumed { navActionManager.toAbout() }
            )
        }

        if (isLoggedIn) {
            Spacer(Modifier.height(24.dp))

            // Logout: standalone destructive item, behind a confirmation
            SettingsGroup {
                SettingsNavItem(
                    title = stringResource(R.string.logout),
                    subtitle = stringResource(R.string.logout_summary),
                    icon = painterResource(R.drawable.ic_round_power_settings_new_24),
                    iconContainerColor = MaterialTheme.colorScheme.error,
                    iconContentColor = MaterialTheme.colorScheme.onError,
                    titleColor = MaterialTheme.colorScheme.onErrorContainer,
                    colors = settingsItemColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    showChevron = false,
                    index = 0,
                    count = 1,
                    onClick = { showLogoutDialog = true }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

/** Same confirmation as the profile's logout button. */
@Composable
private fun LogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_round_power_settings_new_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text(text = stringResource(R.string.logout)) },
        text = { Text(text = stringResource(R.string.logout_summary)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(text = stringResource(R.string.logout), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shapes = ButtonDefaults.shapes()) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

@Preview
@Composable
fun MorePreview() {
    MoeListTheme {
        Surface {
            MoreViewContent(
                event = null,
                navActionManager = NavActionManager.rememberNavActionManager(),
                isLoggedIn = true
            )
        }
    }
}
