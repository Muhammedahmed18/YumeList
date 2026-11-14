package com.axiel7.moelist.ui.more.notifications

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.EmptyState
import com.axiel7.moelist.ui.composables.preferences.SettingsNavItem
import com.axiel7.moelist.ui.composables.preferences.SettingsScaffold
import com.axiel7.moelist.ui.composables.preferences.SettingsSection
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.NotificationsOff
import com.axiel7.moelist.ui.theme.MoeListTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotificationsView(
    navActionManager: NavActionManager
) {
    val viewModel: NotificationsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NotificationsViewContent(
        uiState = uiState,
        event = viewModel,
        navActionManager = navActionManager,
    )
}

/**
 * Scheduled airing notifications, in the same grouped style as Settings.
 * "Delete all" is a top-bar action (only while there is something to delete) and asks first,
 * because it can't be undone.
 */
@Composable
private fun NotificationsViewContent(
    uiState: NotificationsUiState,
    event: NotificationsEvent?,
    navActionManager: NavActionManager,
) {
    val keys = uiState.notifications?.asMap()?.keys?.toList().orEmpty()
    var showDeleteAllDialog by rememberSaveable { mutableStateOf(false) }

    if (showDeleteAllDialog) {
        DeleteAllNotificationsDialog(
            onConfirm = {
                showDeleteAllDialog = false
                event?.removeAllNotifications()
            },
            onDismiss = { showDeleteAllDialog = false },
        )
    }

    SettingsScaffold(
        title = stringResource(R.string.notifications),
        onBack = navActionManager::goBack,
        actions = {
            if (keys.isNotEmpty()) {
                IconButton(onClick = { showDeleteAllDialog = true }) {
                    Icon(
                        painter = painterResource(R.drawable.round_delete_sweep_24),
                        contentDescription = stringResource(R.string.delete_all)
                    )
                }
            }
        },
    ) {
        if (keys.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.NotificationsOff,
                title = stringResource(R.string.empty_notifications_title),
                description = stringResource(R.string.empty_notifications_desc),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
            )
        } else {
            SettingsSection {
                keys.forEach { key ->
                    item {
                        SettingsNavItem(
                            title = (uiState.notifications?.get(key) as? String).orEmpty(),
                            onClick = dropUnlessResumed {
                                key.toString().toIntOrNull()?.let { id ->
                                    navActionManager.toMediaDetails(
                                        mediaType = MediaType.ANIME,
                                        id = id
                                    )
                                }
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = {
                                        event?.removeNotification(key.name.toIntOrNull() ?: 0)
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.delete_outline_24),
                                        contentDescription = stringResource(R.string.delete)
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

/** Same pattern as the "delete entry" dialog: error-coloured confirm, plain cancel. */
@Composable
private fun DeleteAllNotificationsDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(R.drawable.round_delete_sweep_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text(text = stringResource(R.string.delete_all_notifications_title)) },
        text = { Text(text = stringResource(R.string.delete_all_notifications_desc)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(text = stringResource(R.string.delete_all), fontWeight = FontWeight.Bold)
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
fun NotificationsPreview() {
    MoeListTheme {
        Surface {
            NotificationsViewContent(
                uiState = NotificationsUiState(),
                event = null,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}