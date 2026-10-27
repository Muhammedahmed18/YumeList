package com.axiel7.moelist.ui.season.composables

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.ui.composables.media.AnimeFormatOptions
import com.axiel7.moelist.ui.composables.media.MediaFormatSheetContent
import com.axiel7.moelist.ui.season.SeasonChartEvent
import com.axiel7.moelist.ui.season.SeasonChartUiState

/**
 * Format filter for the season chart. The list itself is the shared
 * [MediaFormatSheetContent] (same as the anime / manga lists): picking a row applies straight
 * away and closes the sheet (filtering is local, no reload).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonChartFormatSheet(
    uiState: SeasonChartUiState,
    event: SeasonChartEvent?,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    bottomPadding: Dp = 0.dp,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        MediaFormatSheetContent(
            options = AnimeFormatOptions,
            selected = uiState.selectedFormat,
            counts = uiState.formatCounts,
            bottomPadding = bottomPadding,
            onSelect = { format ->
                if (format != uiState.selectedFormat) event?.onChangeFormat(format)
                onDismiss()
            },
        )
    }
}
