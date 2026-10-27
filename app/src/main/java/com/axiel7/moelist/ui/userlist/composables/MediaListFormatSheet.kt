package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.composables.media.AnimeFormatOptions
import com.axiel7.moelist.ui.composables.media.MangaFormatOptions
import com.axiel7.moelist.ui.composables.media.MediaFormatSheetContent
import com.axiel7.moelist.ui.userlist.UserMediaListEvent
import com.axiel7.moelist.ui.userlist.UserMediaListUiState
import kotlinx.coroutines.launch

/**
 * Format filter for the anime / manga lists, using the shared [MediaFormatSheetContent]
 * (same look as the season chart's sheet).
 *
 * The counts are for the status tab you're on, so the header shows that status ("Format ·
 * Watching"). Picking a row applies straight away, then the sheet slides away before closing
 * (it used to vanish without the slide).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaListFormatSheet(
    uiState: UserMediaListUiState,
    event: UserMediaListEvent?,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    fun close() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { event?.toggleFormatSheet(false) }
    }

    ModalBottomSheet(
        onDismissRequest = { event?.toggleFormatSheet(false) },
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        MediaFormatSheetContent(
            options = if (uiState.mediaType == MediaType.ANIME) AnimeFormatOptions else MangaFormatOptions,
            selected = uiState.selectedFormat,
            counts = uiState.formatCounts,
            context = uiState.listStatus?.localized(),
            bottomPadding = bottomPadding,
            onSelect = { format ->
                if (format != uiState.selectedFormat) event?.onChangeFormat(format)
                close()
            },
        )
    }
}
