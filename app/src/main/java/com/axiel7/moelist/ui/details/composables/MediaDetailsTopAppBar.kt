package com.axiel7.moelist.ui.details.composables

import android.Manifest
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.ui.composables.LocalSnackbarHostState
import com.axiel7.moelist.ui.composables.showSnackbarShort
import com.axiel7.moelist.ui.details.MediaDetailsEvent
import com.axiel7.moelist.ui.details.MediaDetailsUiState
import com.axiel7.moelist.utils.DateUtils.parseDate
import kotlinx.coroutines.launch
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MediaDetailsTopAppBar(
    uiState: MediaDetailsUiState,
    event: MediaDetailsEvent?,
    showTitle: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateBack: () -> Unit,
    onOpenClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    val isPreview = LocalInspectionMode.current
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val savedForNotification = when (uiState.mediaDetails?.status) {
        MediaStatus.AIRING -> uiState.notification
        MediaStatus.NOT_AIRED -> uiState.startNotification
        else -> null
    }

    val notificationDisabledMessage = stringResource(R.string.notification_disabled)
    val enableNotificationLabel = stringResource(R.string.enable_notification)
    val disableNotificationLabel = stringResource(R.string.disable_notification)
    val airingNotificationEnabledMessage = stringResource(R.string.airing_notification_enabled)
    val startAiringNotificationEnabledMessage = stringResource(R.string.start_airing_notification_enabled)
    val invalidStartDateMessage = stringResource(R.string.invalid_start_date)
    val invalidBroadcastMessage = stringResource(R.string.invalid_broadcast)

    fun onClickNotification(permissionGranted: Boolean) {
        val enable = savedForNotification == null
        (uiState.mediaDetails as? AnimeDetails)?.let { details ->
            if (enable && permissionGranted) {
                if (details.status != MediaStatus.NOT_AIRED
                    && details.broadcast?.dayOfTheWeek != null
                    && details.broadcast.startTime != null
                ) {
                    event?.scheduleAiringAnimeNotification(
                        title = details.title.orEmpty(),
                        animeId = details.id,
                        weekDay = details.broadcast.dayOfTheWeek,
                        jpHour = LocalTime.parse(details.broadcast.startTime)
                    )
                    scope.launch { snackbarHostState.showSnackbarShort(airingNotificationEnabledMessage) }
                } else if (details.status == MediaStatus.NOT_AIRED && details.startDate != null) {
                    val startDate = details.startDate.parseDate()
                    if (startDate != null) {
                        event?.scheduleAnimeStartNotification(
                            title = details.title.orEmpty(),
                            animeId = details.id,
                            startDate = startDate
                        )
                        scope.launch { snackbarHostState.showSnackbarShort(startAiringNotificationEnabledMessage) }
                    } else {
                        scope.launch { snackbarHostState.showSnackbarShort(invalidStartDateMessage) }
                    }
                } else {
                    if (details.broadcast?.dayOfTheWeek == null
                        || details.broadcast.startTime == null
                    ) {
                        scope.launch { snackbarHostState.showSnackbarShort(invalidBroadcastMessage) }
                    } else if (details.startDate == null) {
                        scope.launch { snackbarHostState.showSnackbarShort(invalidStartDateMessage) }
                    }
                }
            } else {
                event?.removeAiringAnimeNotification(animeId = details.id)
                scope.launch { snackbarHostState.showSnackbarShort(notificationDisabledMessage) }
            }
        }
    }

    val notificationPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isPreview) {
            rememberPermissionState(
                permission = Manifest.permission.POST_NOTIFICATIONS,
                onPermissionResult = { onClickNotification(it) }
            )
        } else null

    TopAppBar(
        title = {
            Text(
                text = uiState.mediaDetails?.userPreferredTitle().orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = androidx.compose.ui.Modifier.alpha(if (showTitle) 1f else 0f)
            )
        },
        navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
        },
        actions = {
            IconButton(onClick = onOpenClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = stringResource(R.string.view_on_mal)
                )
            }
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = stringResource(R.string.share)
                )
            }
            if (uiState.mediaDetails?.status == MediaStatus.AIRING
                || uiState.mediaDetails?.status == MediaStatus.NOT_AIRED
            ) {
                IconButton(
                    onClick = {
                        if (notificationPermission == null || notificationPermission.status.isGranted) {
                            onClickNotification(true)
                        } else {
                            notificationPermission.launchPermissionRequest()
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (savedForNotification != null) Icons.Rounded.Notifications
                        else Icons.Rounded.NotificationsOff,
                        contentDescription = if (savedForNotification != null) disableNotificationLabel
                        else enableNotificationLabel
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        scrollBehavior = scrollBehavior
    )
}
