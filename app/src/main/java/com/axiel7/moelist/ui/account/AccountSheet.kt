package com.axiel7.moelist.ui.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.preferences.SettingsGroup
import com.axiel7.moelist.ui.composables.preferences.SettingsNavItem
import com.axiel7.moelist.ui.composables.preferences.SettingsSectionHeader
import com.axiel7.moelist.ui.composables.preferences.settingsItemColors
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.KeyboardArrowRight
import com.axiel7.moelist.ui.login.LoginButton
import com.axiel7.moelist.ui.login.openLoginUrl
import com.axiel7.moelist.ui.main.MainViewModel
import com.axiel7.moelist.utils.ContextExtensions.openCustomTab
import com.axiel7.moelist.utils.MAL_ANNOUNCEMENTS_URL
import com.axiel7.moelist.utils.MAL_NEWS_URL
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.axiel7.moelist.ui.theme.ShapeExtraLarge

/**
 * The Account sheet, opened from the avatar on Home. It replaces the old "More" tab:
 *
 * - Header: "Account" title, and a logout button (asks first) on the right
 * - Profile card (avatar, name, "View profile") that opens the Profile screen. A guest gets a
 *   sign-in card here instead, and no logout button
 * - Discover: MAL news and announcements
 * - App: Notifications, Settings, About
 *
 * Tapping an item closes the sheet with its own animation first, then navigates, so the new
 * screen doesn't slide in under a sheet that is still on screen.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AccountSheet(
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel = koinViewModel<MainViewModel>()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val profilePicture by viewModel.profilePicture.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var isOpeningLogin by remember { mutableStateOf(false) }

    // Slide the sheet away, then do the action
    val closeThen: (() -> Unit) -> Unit = { action ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onDismiss()
            action()
        }
    }

    if (showLogoutDialog) {
        LogoutDialog(
            onConfirm = {
                showLogoutDialog = false
                viewModel.logOut()
                closeThen {}
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    val itemColors = settingsItemColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            // Header: title on the left, logout on the right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(start = 8.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.account),
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (isLoggedIn) {
                    FilledTonalIconButton(
                        onClick = { showLogoutDialog = true },
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_power_settings_new_24),
                            contentDescription = stringResource(R.string.logout),
                        )
                    }
                }
            }

            if (isLoggedIn) {
                ProfileCard(
                    username = username,
                    pictureUrl = profilePicture,
                    onClick = { closeThen { navActionManager.toProfile() } }
                )
            } else {
                GuestCard(
                    isLoading = isOpeningLogin,
                    onSignIn = {
                        isOpeningLogin = true
                        scope.launch {
                            try {
                                context.openLoginUrl(
                                    loginUrl = viewModel.generateLoginUrl(),
                                    useExternalBrowser = false
                                )
                            } finally {
                                isOpeningLogin = false
                                onDismiss()
                            }
                        }
                    }
                )
            }

            // Discover: external MAL pages
            SettingsSectionHeader(text = stringResource(R.string.discover))
            SettingsGroup {
                SettingsNavItem(
                    title = stringResource(R.string.anime_manga_news),
                    subtitle = stringResource(R.string.news_summary),
                    icon = painterResource(R.drawable.ic_new_releases),
                    iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    colors = itemColors,
                    index = 0,
                    count = 2,
                    onClick = { closeThen { context.openCustomTab(MAL_NEWS_URL) } }
                )
                SettingsNavItem(
                    title = stringResource(R.string.mal_announcements),
                    subtitle = stringResource(R.string.mal_announcements_summary),
                    icon = painterResource(R.drawable.ic_campaign),
                    iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    colors = itemColors,
                    index = 1,
                    count = 2,
                    onClick = { closeThen { context.openCustomTab(MAL_ANNOUNCEMENTS_URL) } }
                )
            }

            // More: the items from the old More tab
            SettingsSectionHeader(text = stringResource(R.string.more))
            SettingsGroup {
                SettingsNavItem(
                    title = stringResource(R.string.notifications),
                    icon = painterResource(R.drawable.round_notifications_24),
                    colors = itemColors,
                    index = 0,
                    count = 3,
                    onClick = { closeThen { navActionManager.toNotifications() } }
                )
                SettingsNavItem(
                    title = stringResource(R.string.settings),
                    icon = painterResource(R.drawable.ic_round_settings_24),
                    colors = itemColors,
                    index = 1,
                    count = 3,
                    onClick = { closeThen { navActionManager.toSettings() } }
                )
                SettingsNavItem(
                    title = stringResource(R.string.about),
                    icon = painterResource(R.drawable.ic_info),
                    colors = itemColors,
                    index = 2,
                    count = 3,
                    onClick = { closeThen { navActionManager.toAbout() } }
                )
            }
        }
    }
}

/** Avatar, name and "View profile": opens the Profile screen. */
@Composable
private fun ProfileCard(
    username: String?,
    pictureUrl: String?,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = ShapeExtraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AccountAvatar(url = pictureUrl, size = 56.dp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = username ?: stringResource(R.string.title_profile),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.view_profile),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
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

/** Guest: what an account gives, and the sign-in button. */
@Composable
private fun GuestCard(
    isLoading: Boolean,
    onSignIn: () -> Unit,
) {
    Surface(
        shape = ShapeExtraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AccountAvatar(url = null, size = 48.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.guest_profile_title),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                )
            }
            Text(
                text = stringResource(R.string.guest_profile_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LoginButton(
                isLoading = isLoading,
                onClick = onSignIn,
            )
        }
    }
}

/** Same scalloped shape as the Profile avatar. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AccountAvatar(url: String?, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(MaterialShapes.Cookie12Sided.toShape())
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center
    ) {
        if (url.isNullOrEmpty()) {
            Image(
                painter = painterResource(R.drawable.ic_round_account_circle_24),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            AsyncImage(
                model = url,
                contentDescription = null,
                placeholder = painterResource(R.drawable.ic_round_account_circle_24),
                error = painterResource(R.drawable.ic_round_account_circle_24),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

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
