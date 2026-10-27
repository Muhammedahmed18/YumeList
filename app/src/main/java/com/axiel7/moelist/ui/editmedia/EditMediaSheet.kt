package com.axiel7.moelist.ui.editmedia

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusValues
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.composables.LocalSnackbarHostState
import com.axiel7.moelist.ui.composables.RollingNumberText
import com.axiel7.moelist.ui.composables.SnackbarType
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.composables.score.ScoreSlider
import com.axiel7.moelist.ui.composables.showAppSnackbar
import com.axiel7.moelist.ui.editmedia.composables.DeleteMediaEntryDialog
import com.axiel7.moelist.ui.editmedia.composables.EditMediaDatePicker
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.Add
import com.axiel7.moelist.ui.icons.rounded.CalendarToday
import com.axiel7.moelist.ui.icons.rounded.Check
import com.axiel7.moelist.ui.icons.rounded.Delete
import com.axiel7.moelist.ui.icons.rounded.EventAvailable
import com.axiel7.moelist.ui.icons.rounded.Remove
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.Motion
import com.axiel7.moelist.ui.theme.ShapeExtraLarge
import com.axiel7.moelist.utils.DateUtils
import com.axiel7.moelist.utils.DateUtils.toEpochMillis
import com.axiel7.moelist.utils.DateUtils.toLocalized
import com.axiel7.moelist.utils.NumExtensions.toStringPositiveValueOrUnknown
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalDate

/** Horizontal margin of the sheet's cards. */
private val SheetPadding: Dp = 20.dp

/** Height of the status buttons and the bottom action buttons. */
private val ActionHeight: Dp = 56.dp

/**
 * Edit / add a list entry.
 *
 * @param bottomPadding kept for API compatibility; the sheet now handles the navigation bar
 * and keyboard insets itself (ModalBottomSheet's window insets), so callers don't need it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMediaSheet(
    sheetState: SheetState,
    mediaInfo: BaseMediaNode,
    myListStatus: BaseMyListStatus?,
    @Suppress("UNUSED_PARAMETER") bottomPadding: Dp = 0.dp,
    onEdited: (BaseMyListStatus?, removed: Boolean) -> Unit,
    onDismissed: () -> Unit
) {
    val viewModel: EditMediaViewModel = koinViewModel { parametersOf(mediaInfo.mediaType) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // The ViewModel is shared by every entry opened on this screen, so start from a clean state
    // each time: an entry that isn't in the list yet (myListStatus == null) used to inherit the
    // previous entry's status, score and dates.
    LaunchedEffect(mediaInfo.id, myListStatus) {
        viewModel.startEditing(mediaInfo, myListStatus)
    }

    EditMediaSheetContent(
        uiState = uiState,
        event = viewModel,
        sheetState = sheetState,
        onEdited = onEdited,
        onDismissed = onDismissed,
    )
}

/**
 * M3 Expressive edit sheet. The sheet now sizes to its content (no inner Scaffold forcing it to
 * full height); the cards scroll if the screen is short, and the Delete / Save row stays pinned
 * at the bottom.
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)
@Composable
private fun EditMediaSheetContent(
    uiState: EditMediaUiState,
    event: EditMediaEvent?,
    sheetState: SheetState,
    onEdited: (BaseMyListStatus?, removed: Boolean) -> Unit,
    onDismissed: () -> Unit
) {
    val snackbarHostState = LocalSnackbarHostState.current
    val datePickerState = rememberDatePickerState()
    val isKeyboardVisible = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current

    if (uiState.openStartDatePicker || uiState.openFinishDatePicker) {
        val clearDate: (() -> Unit)? = when {
            uiState.openStartDatePicker && uiState.startDate != null ->
                { { event?.onChangeStartDate(null) } }
            uiState.openFinishDatePicker && uiState.finishDate != null ->
                { { event?.onChangeFinishDate(null) } }
            else -> null
        }
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
            },
            // Clearing now happens here (the date tiles have no ✕); only offered if a date is set
            onClear = clearDate,
        )
    }

    if (uiState.openDeleteDialog) {
        DeleteMediaEntryDialog(
            mediaTitle = uiState.mediaInfo?.userPreferredTitle(),
            onConfirm = { event?.deleteEntry() },
            onDismiss = { event?.toggleDeleteDialog(false) }
        )
    }

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            snackbarHostState.showAppSnackbar(uiState.message, type = SnackbarType.ERROR)
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
        // Keep the sheet below the status bar with a *fixed* padding, and give the content only
        // the bottom insets (navigation bar / keyboard).
        // The default top inset is "status bar minus the sheet's current offset", so it grows
        // as the sheet rises. When the content was within a status bar's height of the screen
        // height, the sheet got taller as it rose, its expanded position moved up with it and
        // the opening animation never settled. While a sheet animates, a touch grabs the sheet
        // instead of reaching the content, so every button (status, +/-, slider, Save) stopped
        // responding until the sheet was closed.
        modifier = Modifier.statusBarsPadding(),
        contentWindowInsets = { WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom) },
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false),
        shape = BottomSheetDefaults.ExpandedShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        BackHandler(enabled = true) {
            if (isKeyboardVisible) keyboardController?.hide()
            else onDismissed()
        }

        // Scrollable cards: take the space they need, scroll only if the screen is too short
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SheetPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SheetHeader(uiState = uiState)
            StatusSection(uiState = uiState, event = event)
            ProgressSection(uiState = uiState, event = event)
            ScoreSection(uiState = uiState, event = event)
            DatesSection(
                uiState = uiState,
                onStartDateClick = {
                    val millis = (uiState.startDate ?: LocalDate.now()).toEpochMillis()
                    datePickerState.selectedDateMillis = millis
                    datePickerState.displayedMonthMillis = millis
                    event?.openStartDatePicker()
                },
                onFinishDateClick = {
                    val millis = (uiState.finishDate ?: LocalDate.now()).toEpochMillis()
                    datePickerState.selectedDateMillis = millis
                    datePickerState.displayedMonthMillis = millis
                    event?.openFinishDatePicker()
                },
            )
        }

        // Pinned action row
        ActionRow(uiState = uiState, event = event)
    }
}

// ---------------------------------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------------------------------

@Composable
private fun SheetHeader(uiState: EditMediaUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // MediaPoster clips itself (ShapePoster)
        MediaPoster(
            url = uiState.mediaInfo?.mainPicture?.medium,
            modifier = Modifier.size(width = 64.dp, height = 90.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = uiState.mediaInfo?.userPreferredTitle()
                    ?: stringResource(R.string.edit_entry),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            uiState.mediaInfo?.mediaFormat?.let { format ->
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = format.localized(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Section card
// ---------------------------------------------------------------------------------------------

/** A titled card; [trailing] sits at the end of the title row (e.g. the selected status). */
@Composable
private fun SectionCard(
    title: String,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = ShapeExtraLarge
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                trailing?.invoke()
            }
            content()
        }
    }
}

/** Shapes for button [index] of [count] in a connected button group. */
@Composable
private fun connectedShapes(index: Int, count: Int): ToggleButtonShapes = when {
    count == 1 -> ToggleButtonShapes(
        shape = CircleShape,
        pressedShape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
        checkedShape = CircleShape,
    )
    index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
    index == count - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
}

// ---------------------------------------------------------------------------------------------
// Status
// ---------------------------------------------------------------------------------------------

/**
 * Status as an M3 Expressive **connected button group**: one icon toggle per status, 2dp apart.
 * The selected one turns primary and morphs to a full pill; every button squishes on press.
 * The selected status name is shown in the card title, since the buttons are icon-only.
 */
@Composable
private fun StatusSection(
    uiState: EditMediaUiState,
    event: EditMediaEvent?,
) {
    val haptic = LocalHapticFeedback.current
    val statusValues = remember(uiState.mediaType) { listStatusValues(uiState.mediaType) }
    val labelFade = Motion.fastEffects<Float>()

    SectionCard(
        title = stringResource(R.string.status),
        trailing = {
            AnimatedContent(
                targetState = uiState.status,
                transitionSpec = { fadeIn(labelFade) togetherWith fadeOut(labelFade) },
                label = "statusLabel"
            ) { status ->
                Text(
                    text = status.localized(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
        ) {
            statusValues.forEachIndexed { index, status ->
                ToggleButton(
                    checked = uiState.status == status,
                    onCheckedChange = {
                        if (uiState.status != status) {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            event?.onChangeStatus(status)
                        }
                    },
                    shapes = connectedShapes(index, statusValues.size),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = ActionHeight)
                ) {
                    Icon(
                        imageVector = status.icon,
                        contentDescription = status.localized(),
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Progress
// ---------------------------------------------------------------------------------------------

/**
 * Progress stepper: − / rolling number / +. Both buttons morph on press (round -> rounded
 * square). Manga gets a connected Chapters / Volumes switch. The wavy bar shows how far along
 * the entry is (static wave, no endless animation).
 */
@Composable
private fun ProgressSection(
    uiState: EditMediaUiState,
    event: EditMediaEvent?,
) {
    val haptic = LocalHapticFeedback.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val isManga = uiState.mediaType == MediaType.MANGA
    val isVolumeTab = isManga && selectedTabIndex == 1
    val current = if (isVolumeTab) uiState.volumeProgress ?: 0 else uiState.progress ?: 0
    val total = if (isVolumeTab) uiState.mediaInfo?.totalVolumes() else uiState.mediaInfo?.totalDuration()

    fun change(value: Int) {
        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
        if (isVolumeTab) event?.onChangeVolumeProgress(value)
        else event?.onChangeProgress(value)
    }

    SectionCard(
        title = stringResource(
            when {
                isVolumeTab -> R.string.volumes
                isManga -> R.string.chapters
                else -> R.string.episodes
            }
        ),
    ) {
        if (isManga) {
            val tabs = listOf(stringResource(R.string.chapters), stringResource(R.string.volumes))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
            ) {
                tabs.forEachIndexed { index, label ->
                    ToggleButton(
                        checked = selectedTabIndex == index,
                        onCheckedChange = { selectedTabIndex = index },
                        shapes = connectedShapes(index, tabs.size),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = { change(current - 1) },
                enabled = current > 0,
                shapes = IconButtonShapes(
                    shape = IconButtonDefaults.mediumRoundShape,
                    pressedShape = IconButtonDefaults.mediumPressedShape,
                ),
                modifier = Modifier.size(ActionHeight)
            ) {
                Icon(Icons.Rounded.Remove, contentDescription = "-1")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RollingNumberText(
                    targetValue = current,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "/ ${total.toStringPositiveValueOrUnknown()}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            FilledIconButton(
                onClick = { change(current + 1) },
                enabled = total == null || total <= 0 || current < total,
                shapes = IconButtonShapes(
                    shape = IconButtonDefaults.mediumRoundShape,
                    pressedShape = IconButtonDefaults.mediumPressedShape,
                ),
                modifier = Modifier.size(ActionHeight)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.plus_one))
            }
        }

        if (total != null && total > 0) {
            val animatedProgress by animateFloatAsState(
                targetValue = (current.toFloat() / total).coerceIn(0f, 1f),
                animationSpec = Motion.spatial(),
                label = "editProgress"
            )
            // Flat M3 Expressive bar: thick rounded track, small gap, end-stop dot
            LinearProgressIndicator(
                progress = { animatedProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                gapSize = 4.dp,
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Score
// ---------------------------------------------------------------------------------------------

@Composable
private fun ScoreSection(
    uiState: EditMediaUiState,
    event: EditMediaEvent?,
) {
    SectionCard(title = stringResource(R.string.edit_rating)) {
        ScoreSlider(
            score = uiState.score,
            onValueChange = { event?.onChangeScore(it) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Dates
// ---------------------------------------------------------------------------------------------

@Composable
private fun DatesSection(
    uiState: EditMediaUiState,
    onStartDateClick: () -> Unit,
    onFinishDateClick: () -> Unit,
) {
    SectionCard(title = stringResource(R.string.dates)) {
        // Two tiles side by side (same height as before, no extra scrolling): the label on a
        // small top line and the full date below it, so the date has the whole tile width.
        // Clearing a date is done from the date picker ("Clear").
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DateTile(
                label = stringResource(R.string.start_date),
                date = uiState.startDate,
                icon = Icons.Rounded.CalendarToday,
                onClick = onStartDateClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            DateTile(
                label = stringResource(R.string.end_date),
                date = uiState.finishDate,
                icon = Icons.Rounded.EventAvailable,
                onClick = onFinishDateClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

/**
 * A date tile: icon + label on top, the date underneath in the phone's own format
 * ("Apr 23, 2025" in US English, "23 Apr 2025" in UK English). The text shrinks a little if a
 * language's format is long, so it's never cut off. Filled when a date is set.
 */
@Composable
private fun DateTile(
    label: String,
    date: LocalDate?,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val dateText = date?.toLocalized()?.ifEmpty { null }
    val isSet = dateText != null

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = if (isSet) colors.primaryContainer else colors.surfaceContainerHigh,
        contentColor = if (isSet) colors.onPrimaryContainer else colors.onSurfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = dateText ?: "—",
                style = MaterialTheme.typography.titleSmallEmphasized,
                color = if (isSet) colors.onPrimaryContainer else colors.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 11.sp,
                    maxFontSize = MaterialTheme.typography.titleSmallEmphasized.fontSize
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Actions
// ---------------------------------------------------------------------------------------------

/**
 * Pinned Delete / Save row. Delete opens the confirmation dialog; Save shows the Expressive
 * loading indicator while the update runs. Both buttons morph on press.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ActionRow(
    uiState: EditMediaUiState,
    event: EditMediaEvent?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SheetPadding, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!uiState.isNewEntry) {
            FilledTonalIconButton(
                onClick = { event?.toggleDeleteDialog(true) },
                shapes = IconButtonShapes(
                    shape = IconButtonDefaults.mediumSquareShape,
                    pressedShape = IconButtonDefaults.mediumPressedShape,
                ),
                modifier = Modifier.size(ActionHeight),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete))
            }
        }

        Button(
            onClick = { if (!uiState.isLoading) event?.updateListItem() },
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier
                .weight(1f)
                .height(ActionHeight),
        ) {
            if (uiState.isLoading) {
                LoadingIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    text = stringResource(
                        if (uiState.isNewEntry) R.string.add else R.string.apply
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
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
                uiState = EditMediaUiState(
                    mediaType = MediaType.ANIME,
                    status = ListStatus.WATCHING,
                    progress = 12
                ),
                event = null,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
                onEdited = { _, _ -> },
                onDismissed = {},
            )
        }
    }
}
