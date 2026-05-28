package com.axiel7.moelist.ui.details.composables

import android.Manifest
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.ui.details.MediaDetailsEvent
import com.axiel7.moelist.ui.details.MediaDetailsUiState
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.DateUtils.parseDate
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MediaDetailsTopAppBar(
    uiState: MediaDetailsUiState,
    event: MediaDetailsEvent?,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateBack: () -> Unit,
    onOpenClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val savedForNotification = when (uiState.mediaDetails?.status) {
        MediaStatus.AIRING -> uiState.notification
        MediaStatus.NOT_AIRED -> uiState.startNotification
        else -> null
    }

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
                    context.showToast(R.string.airing_notification_enabled)
                } else if (details.status == MediaStatus.NOT_AIRED && details.startDate != null) {
                    val startDate = details.startDate.parseDate()
                    if (startDate != null) {
                        event?.scheduleAnimeStartNotification(
                            title = details.title.orEmpty(),
                            animeId = details.id,
                            startDate = startDate
                        )
                        context.showToast(R.string.start_airing_notification_enabled)
                    } else {
                        context.showToast(R.string.invalid_start_date)
                    }
                } else {
                    if (details.broadcast?.dayOfTheWeek == null
                        || details.broadcast.startTime == null
                    ) {
                        context.showToast(R.string.invalid_broadcast)
                    } else if (details.startDate == null) {
                        context.showToast(R.string.invalid_start_date)
                    }
                }
            } else {
                event?.removeAiringAnimeNotification(animeId = details.id)
                context.showToast("Notification disabled")
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

    // With a regular (pinned/enterAlways) TopAppBar the bar height is fixed, so we
    // drive the title + color reveal off how much content has scrolled under the bar.
    val isScrolled by remember {
        derivedStateOf { scrollBehavior.state.overlappedFraction > 0.01f }
    }

    val titleAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 1f else 0f,
        label = "titleAlpha"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isScrolled) MaterialTheme.colorScheme.onSurface else Color.White,
        label = "appBarContentColor"
    )

    TopAppBar(
        title = {
            Text(
                text = uiState.mediaDetails?.userPreferredTitle().orEmpty(),
                modifier = Modifier.graphicsLayer { alpha = titleAlpha },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = contentColor
                )
            }
        },
        actions = {
            IconButton(onClick = onOpenClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = stringResource(R.string.view_on_mal),
                    tint = contentColor
                )
            }
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = stringResource(R.string.share),
                    tint = contentColor
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
                        contentDescription = "notification",
                        tint = contentColor
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor
        ),
        scrollBehavior = scrollBehavior
    )
}