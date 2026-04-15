package com.axiel7.moelist.ui.editmedia

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusValues
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.scoreText
import com.axiel7.moelist.ui.composables.SelectableIconToggleButton
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.composables.score.ScoreSlider
import com.axiel7.moelist.ui.editmedia.composables.DeleteMediaEntryDialog
import com.axiel7.moelist.ui.editmedia.composables.EditMediaDateField
import com.axiel7.moelist.ui.editmedia.composables.EditMediaDatePicker
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.DateUtils
import com.axiel7.moelist.utils.DateUtils.toEpochMillis
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMediaSheet(
    sheetState: SheetState,
    mediaInfo: BaseMediaNode,
    myListStatus: BaseMyListStatus?,
    bottomPadding: Dp = 0.dp,
    onEdited: (BaseMyListStatus?, removed: Boolean) -> Unit,
    onDismissed: () -> Unit
) {
    val viewModel: EditMediaViewModel = koinViewModel { parametersOf(mediaInfo.mediaType) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(mediaInfo) {
        viewModel.setMediaInfo(mediaInfo)
    }
    LaunchedEffect(myListStatus) {
        if (myListStatus != null)
            viewModel.setEditVariables(myListStatus)
    }

    EditMediaSheetContent(
        uiState = uiState,
        event = viewModel,
        sheetState = sheetState,
        bottomPadding = bottomPadding,
        onEdited = onEdited,
        onDismissed = onDismissed,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EditMediaSheetContent(
    uiState: EditMediaUiState,
    event: EditMediaEvent?,
    sheetState: SheetState,
    bottomPadding: Dp = 0.dp,
    onEdited: (BaseMyListStatus?, removed: Boolean) -> Unit,
    onDismissed: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val statusValues = remember(uiState.mediaType) {
        listStatusValues(uiState.mediaType)
    }
    val datePickerState = rememberDatePickerState()
    val isKeyboardVisible = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current

    if (uiState.openStartDatePicker || uiState.openFinishDatePicker) {
        EditMediaDatePicker(
            datePickerState = datePickerState,
            onDateSelected = {
                if (uiState.openStartDatePicker) {
                    event?.onChangeStartDate(DateUtils.getLocalDateFromMillis(it))
                } else {
                    event?.onChangeFinishDate(DateUtils.getLocalDateFromMillis(it))
                }
            },
            onDismiss = {
                event?.closeDatePickers()
            }
        )
    }

    if (uiState.openDeleteDialog) {
        DeleteMediaEntryDialog(
            onConfirm = { event?.deleteEntry() },
            onDismiss = { event?.toggleDeleteDialog(false) }
        )
    }

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess == true) {
            event?.onDismiss()
            onEdited(uiState.myListStatus, uiState.removed)
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissed,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false),
    ) {
        BackHandler(enabled = true) {
            if (isKeyboardVisible) keyboardController?.hide()
            else onDismissed()
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp + bottomPadding)
                .imePadding(),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MediaPoster(
                    url = uiState.mediaInfo?.mainPicture?.medium,
                    modifier = Modifier
                        .size(width = 56.dp, height = 80.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = uiState.mediaInfo?.userPreferredTitle() ?: stringResource(R.string.edit_entry),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${uiState.mediaInfo?.mediaFormat?.localized().orEmpty()} • ${uiState.status.localized()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }

                    FilledIconButton(
                        onClick = { event?.updateListItem() },
                        modifier = Modifier.padding(start = 16.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = stringResource(if (uiState.isNewEntry) R.string.add else R.string.apply)
                        )
                    }
                }
            }

            // Status Selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                statusValues.forEach { status ->
                    val isSelected = uiState.status == status
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                event?.onChangeStatus(status)
                            }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = status.icon),
                            contentDescription = status.localized(),
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Progress Widget
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            event?.onChangeProgress((uiState.progress ?: 0) - 1)
                        },
                        enabled = (uiState.progress ?: 0) > 0,
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape
                    ) {
                        Icon(painterResource(R.drawable.round_remove_24), contentDescription = null)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (uiState.mediaType == MediaType.ANIME) stringResource(R.string.episodes)
                            else stringResource(R.string.chapters),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = (uiState.progress ?: 0).toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = " / ${uiState.mediaInfo?.totalDuration().toStringPositiveValueOrUnknown()}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FilledTonalIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            event?.onChangeProgress((uiState.progress ?: 0) + 1)
                        },
                        enabled = uiState.mediaInfo?.totalDuration() == null || (uiState.progress ?: 0) < (uiState.mediaInfo?.totalDuration() ?: Int.MAX_VALUE),
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(painterResource(R.drawable.ic_round_add_24), contentDescription = null)
                    }
                }
            }

            // Volume Progress (Manga only)
            if (uiState.mediaType == MediaType.MANGA) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                event?.onChangeVolumeProgress((uiState.volumeProgress ?: 0) - 1)
                            },
                            enabled = (uiState.volumeProgress ?: 0) > 0,
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape
                        ) {
                            Icon(painterResource(R.drawable.round_remove_24), contentDescription = null)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.volumes),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = (uiState.volumeProgress ?: 0).toString(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = " / ${uiState.mediaInfo?.totalVolumes().toStringPositiveValueOrUnknown()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        FilledTonalIconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                event?.onChangeVolumeProgress((uiState.volumeProgress ?: 0) + 1)
                            },
                            enabled = uiState.mediaInfo?.totalVolumes() == null || (uiState.volumeProgress ?: 0) < (uiState.mediaInfo?.totalVolumes() ?: Int.MAX_VALUE),
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(painterResource(R.drawable.ic_round_add_24), contentDescription = null)
                        }
                    }
                }
            }

            // Rating Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.edit_rating),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val scoreText = uiState.score.scoreText()
                    Text(
                        text = if (scoreText.isEmpty()) "" else "$scoreText (${uiState.score})",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                ScoreSlider(
                    score = uiState.score,
                    onValueChange = { event?.onChangeScore(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 24.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Dates in a Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EditMediaDateField(
                    date = uiState.startDate,
                    label = stringResource(R.string.start_date),
                    icon = R.drawable.round_calendar_today_24,
                    modifier = Modifier.weight(1f),
                    removeDate = { event?.onChangeStartDate(null) },
                    onClick = {
                        datePickerState.selectedDateMillis = uiState.startDate?.toEpochMillis()
                        event?.openStartDatePicker()
                    }
                )
                EditMediaDateField(
                    date = uiState.finishDate,
                    label = stringResource(R.string.end_date),
                    icon = R.drawable.round_event_available_24,
                    modifier = Modifier.weight(1f),
                    removeDate = { event?.onChangeFinishDate(null) },
                    onClick = {
                        datePickerState.selectedDateMillis = uiState.finishDate?.toEpochMillis()
                        event?.openFinishDatePicker()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delete Action
            FilledTonalButton(
                onClick = { event?.toggleDeleteDialog(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                enabled = !uiState.isNewEntry
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = stringResource(R.string.delete))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun EditMediaSheetPreview() {
    MoeListTheme {
        Surface {
            EditMediaSheetContent(
                uiState = EditMediaUiState(mediaType = MediaType.ANIME),
                event = null,
                sheetState = SheetState(
                    skipPartiallyExpanded = true,
                    density = LocalDensity.current,
                    initialValue = SheetValue.Expanded
                ),
                onEdited = { _, _ -> },
                onDismissed = {},
            )
        }
    }
}