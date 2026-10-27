package com.axiel7.moelist.ui.details.composables

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.media.MediaStatus
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.ui.composables.LocalSnackbarHostState
import com.axiel7.moelist.ui.composables.SnackbarType
import com.axiel7.moelist.ui.composables.showAppSnackbar
import com.axiel7.moelist.ui.details.MediaDetailsEvent
import com.axiel7.moelist.ui.details.MediaDetailsUiState
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.ArrowBack
import com.axiel7.moelist.ui.icons.automirrored.rounded.OpenInNew
import com.axiel7.moelist.ui.icons.rounded.Add
import com.axiel7.moelist.ui.icons.rounded.Edit
import com.axiel7.moelist.ui.icons.rounded.Notifications
import com.axiel7.moelist.ui.icons.rounded.NotificationsOff
import com.axiel7.moelist.ui.icons.rounded.Share
import com.axiel7.moelist.ui.theme.Motion
import com.axiel7.moelist.utils.ContextExtensions.copyToClipBoard
import com.axiel7.moelist.utils.DateUtils.parseDate
import kotlinx.coroutines.launch
import java.time.LocalTime

/** Font size above which the title lambda is rendering the expanded (large) row. */
private val ExpandedTitleMinFontSize = 28.sp

/**
 * Sized for a two-line headlineSmall title (2 x 32dp line height) under the 64dp icon row,
 * plus the one-line alternative title (~24dp) and bottom padding.
 */
private val ExpandedHeightWithSubtitle = 168.dp
private val ExpandedHeightWithoutSubtitle = 144.dp

/**
 * M3 Expressive large flexible top app bar for the details screen. Expanded, it shows the title
 * (up to two lines, auto-shrinking) and the alternative title over the blurred backdrop;
 * scrolling collapses it into a solid one-line bar, and an Edit shortcut appears.
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun MediaDetailsTopAppBar(
    uiState: MediaDetailsUiState,
    event: MediaDetailsEvent?,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateBack: () -> Unit,
    onOpenClick: () -> Unit,
    onShareClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    val context = LocalContext.current
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
                    scope.launch { snackbarHostState.showAppSnackbar(airingNotificationEnabledMessage, type = SnackbarType.SUCCESS) }
                } else if (details.status == MediaStatus.NOT_AIRED && details.startDate != null) {
                    val startDate = details.startDate.parseDate()
                    if (startDate != null) {
                        event?.scheduleAnimeStartNotification(
                            title = details.title.orEmpty(),
                            animeId = details.id,
                            startDate = startDate
                        )
                        scope.launch { snackbarHostState.showAppSnackbar(startAiringNotificationEnabledMessage, type = SnackbarType.SUCCESS) }
                    } else {
                        scope.launch { snackbarHostState.showAppSnackbar(invalidStartDateMessage, type = SnackbarType.ERROR) }
                    }
                } else {
                    if (details.broadcast?.dayOfTheWeek == null
                        || details.broadcast.startTime == null
                    ) {
                        scope.launch { snackbarHostState.showAppSnackbar(invalidBroadcastMessage, type = SnackbarType.ERROR) }
                    } else if (details.startDate == null) {
                        scope.launch { snackbarHostState.showAppSnackbar(invalidStartDateMessage, type = SnackbarType.ERROR) }
                    }
                }
            } else {
                event?.removeAiringAnimeNotification(animeId = details.id)
                scope.launch { snackbarHostState.showAppSnackbar(notificationDisabledMessage, type = SnackbarType.INFO) }
            }
        }
    }

    // Android 13+ asks for the notification permission (standard AndroidX launcher, replaces
    // Accompanist). Older versions, and previews, don't need it.
    val notificationPermissionLauncher =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isPreview) {
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                onClickNotification(granted)
            }
        } else null

    val details = uiState.mediaDetails
    val title = details?.userPreferredTitle().orEmpty()
    val altTitle = if (App.titleLanguage == TitleLanguage.ENGLISH) details?.title
    else details?.alternativeTitles?.en
    // Only true once the bar has (almost) collapsed; derivedStateOf so scrolling doesn't
    // recompose the bar on every frame
    val isCollapsed by remember(scrollBehavior) {
        derivedStateOf { scrollBehavior.state.collapsedFraction > 0.85f }
    }

    LargeFlexibleTopAppBar(
        title = {
            // The same lambda is used for the large (expanded) and small (collapsed) rows.
            // The expanded row uses a large display style: allow 2 lines and shrink long titles
            // to fit instead of cutting them off. The collapsed row stays on one line.
            val isExpandedRow = LocalTextStyle.current.fontSize >= ExpandedTitleMinFontSize
            Text(
                text = title,
                maxLines = if (isExpandedRow) 2 else 1,
                overflow = TextOverflow.Ellipsis,
                // Fixed size for every title so short and long titles look the same
                style = if (isExpandedRow) MaterialTheme.typography.headlineSmall
                else LocalTextStyle.current,
                modifier = Modifier.combinedClickable(
                    // Long-press copies the title (was on the hero title before)
                    onLongClick = { if (title.isNotEmpty()) context.copyToClipBoard(title) },
                    onClick = {}
                )
            )
        },
        subtitle = if (!altTitle.isNullOrBlank()) {
            {
                Text(
                    text = altTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else null,
        expandedHeight = if (!altTitle.isNullOrBlank()) ExpandedHeightWithSubtitle
        else ExpandedHeightWithoutSubtitle,
        navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
        },
        actions = {
            // Edit shortcut once the hero (and its status button) has scrolled away
            AnimatedVisibility(
                visible = details != null && isCollapsed,
                enter = fadeIn(Motion.fastEffects()) + scaleIn(Motion.fastSpatial()),
                exit = fadeOut(Motion.fastEffects()) + scaleOut(Motion.fastSpatial()),
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = if (uiState.isNewEntry) Icons.Rounded.Add else Icons.Rounded.Edit,
                        contentDescription = if (uiState.isNewEntry) stringResource(R.string.add)
                        else stringResource(R.string.edit)
                    )
                }
            }
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
            if (details?.status == MediaStatus.AIRING
                || details?.status == MediaStatus.NOT_AIRED
            ) {
                IconButton(
                    onClick = {
                        val launcher = notificationPermissionLauncher
                        if (launcher == null ||
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            onClickNotification(true)
                        } else {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
        // Transparent while expanded so the blurred backdrop shows through; solid once collapsed
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            subtitleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        scrollBehavior = scrollBehavior
    )
}
