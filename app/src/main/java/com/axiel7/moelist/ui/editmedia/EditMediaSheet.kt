package com.axiel7.moelist.ui.editmedia

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
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
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusValues
import com.axiel7.moelist.data.model.media.MediaFormat
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.scoreText
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.composables.score.ScoreSlider
import com.axiel7.moelist.ui.editmedia.composables.DeleteMediaEntryDialog
import com.axiel7.moelist.ui.editmedia.composables.EditMediaDatePicker
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.DateUtils
import com.axiel7.moelist.utils.DateUtils.toEpochMillis
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

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
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                shape = CircleShape
            ) {
                Spacer(modifier = Modifier.size(width = 32.dp, height = 4.dp))
            }
        }
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
                .imePadding()
                .animateContentSize(),
        ) {
            // Immersive Header
            ListItem(
                headlineContent = {
                    Text(
                        text = uiState.mediaInfo?.userPreferredTitle() ?: stringResource(R.string.edit_entry),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        uiState.mediaInfo?.mediaFormat?.let { format ->
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = format.localized(),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = uiState.status.localized(),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                },
                leadingContent = {
                    MediaPoster(
                        url = uiState.mediaInfo?.mainPicture?.medium,
                        modifier = Modifier
                            .size(width = 56.dp, height = 80.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                },
                trailingContent = {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Button(
                            onClick = { event?.updateListItem() },
                            shape = CircleShape,
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(stringResource(if (uiState.isNewEntry) R.string.add else R.string.apply))
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            // Modern Status Selection (Custom Tonal Capsules)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                statusValues.forEach { status ->
                    val isSelected = uiState.status == status
                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                      else MaterialTheme.colorScheme.surfaceContainerHighest,
                        label = "statusContainer"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer 
                                      else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "statusContent"
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.2f else 1f,
                        label = "statusScale"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(CircleShape)
                            .background(containerColor)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                event?.onChangeStatus(status)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = status.icon),
                            contentDescription = status.localized(),
                            tint = contentColor,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer(scaleX = scale, scaleY = scale)
                        )
                    }
                }
            }

            // Grouped Section: Progress (Modern Hero Card)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(28.dp)
            ) {
                Column {
                    val progressLabel = if (uiState.mediaType == MediaType.ANIME) stringResource(R.string.episodes)
                    else stringResource(R.string.chapters)
                    val total = uiState.mediaInfo?.totalDuration()
                    val current = uiState.progress ?: 0
                    
                    ListItem(
                        headlineContent = {
                            Text(
                                text = progressLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            if (total != null && total > 0) {
                                val percentage = (current.toFloat() / total * 100).roundToInt()
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "$percentage%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                event?.onChangeProgress(current - 1)
                            },
                            enabled = current > 0,
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape
                        ) {
                            Icon(painterResource(R.drawable.round_remove_24), contentDescription = null)
                        }

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = current.toString(),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = " / ${total.toStringPositiveValueOrUnknown()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )
                        }

                        FilledTonalIconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                event?.onChangeProgress(current + 1)
                            },
                            enabled = total == null || current < total,
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Volume Progress (Manga only)
                    if (uiState.mediaType == MediaType.MANGA) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        
                        val currentVolumes = uiState.volumeProgress ?: 0
                        val totalVolumes = uiState.mediaInfo?.totalVolumes()

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.volumes),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalIconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    event?.onChangeVolumeProgress(currentVolumes - 1)
                                },
                                enabled = currentVolumes > 0,
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape
                            ) {
                                Icon(painterResource(R.drawable.round_remove_24), contentDescription = null)
                            }

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = currentVolumes.toString(),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = " / ${totalVolumes.toStringPositiveValueOrUnknown()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                                )
                            }

                            FilledTonalIconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    event?.onChangeVolumeProgress(currentVolumes + 1)
                                },
                                enabled = totalVolumes == null || currentVolumes < totalVolumes,
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
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // Grouped Section: Rating (Modern Hero Card)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.edit_rating),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ScoreSlider(
                        score = uiState.score,
                        onValueChange = { event?.onChangeScore(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Grouped Section: Dates (Separate Pill Style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start Date Pill
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .clickable {
                                datePickerState.selectedDateMillis = uiState.startDate?.toEpochMillis()
                                event?.openStartDatePicker()
                            }
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.start_date),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = uiState.startDate?.toString() ?: stringResource(R.string.unknown),
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (uiState.startDate != null) {
                                IconButton(
                                    onClick = { event?.onChangeStartDate(null) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Rounded.Clear, null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // End Date Pill
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .clickable {
                                datePickerState.selectedDateMillis = uiState.finishDate?.toEpochMillis()
                                event?.openFinishDatePicker()
                            }
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.end_date),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = uiState.finishDate?.toString() ?: stringResource(R.string.unknown),
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (uiState.finishDate != null) {
                                IconButton(
                                    onClick = { event?.onChangeFinishDate(null) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Rounded.Clear, null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Delete Action (Modern Tonal M3 Treatment)
            if (!uiState.isNewEntry) {
                FilledTonalButton(
                    onClick = { event?.toggleDeleteDialog(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = stringResource(R.string.delete),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun EditMediaSheetPreview() {
    val mockMedia = AnimeNode(
        id = 1,
        title = "Frieren: Beyond Journey's End",
        mediaFormat = MediaFormat.TV,
        numEpisodes = 28
    )

    MoeListTheme {
        Surface {
            EditMediaSheetContent(
                uiState = EditMediaUiState(
                    mediaType = MediaType.ANIME,
                    status = ListStatus.WATCHING,
                    progress = 12,
                    mediaInfo = mockMedia
                ),
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
