package com.axiel7.moelist.ui.userlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusValues
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.TabRowItem
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.LoadingDialog
import com.axiel7.moelist.ui.composables.TabRowWithPager
import com.axiel7.moelist.ui.editmedia.EditMediaSheet
import com.axiel7.moelist.ui.userlist.composables.MediaListSortDialog
import com.axiel7.moelist.utils.ContextExtensions.showToast
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMediaListWithTabsView(
    mediaType: MediaType,
    isCompactScreen: Boolean,
    navActionManager: NavActionManager,
    padding: PaddingValues,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val tabRowItems = remember {
        listStatusValues(mediaType)
            .map {
                TabRowItem(value = it, title = it.stringRes)
            }.toTypedArray()
    }
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showEditSheet by remember { mutableStateOf(false) }
    fun hideEditSheet() {
        scope.launch { editSheetState.hide() }.invokeOnCompletion { showEditSheet = false }
    }

    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
        ) {
            TabRowWithPager(
                tabs = tabRowItems,
                modifier = Modifier.fillMaxSize(),
                beyondBoundsPageCount = 1,
                isTabScrollable = true,
                isPrimaryTab = false // Using Secondary style for a cleaner, modern look
            ) { page ->
                val listStatus = tabRowItems[page].value
                val viewModel: UserMediaListViewModel = koinViewModel(
                    key = "${mediaType.name}_${listStatus.name}",
                    parameters = { parametersOf(mediaType, listStatus) }
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                if (uiState.openSortDialog && uiState.listSort != null) {
                    MediaListSortDialog(
                        uiState = uiState,
                        event = viewModel
                    )
                }

                if (uiState.isLoadingRandom) {
                    LoadingDialog()
                }

                if (showEditSheet && uiState.mediaInfo != null) {
                    EditMediaSheet(
                        sheetState = editSheetState,
                        mediaInfo = uiState.mediaInfo!!,
                        myListStatus = uiState.myListStatus,
                        bottomPadding = systemBarsPadding.calculateBottomPadding(),
                        onEdited = { status, removed ->
                            hideEditSheet()
                            viewModel.onChangeItemMyListStatus(status, removed)
                        },
                        onDismissed = { hideEditSheet() }
                    )
                }

                LaunchedEffect(uiState.randomId) {
                    uiState.randomId?.let { id ->
                        navActionManager.toMediaDetails(uiState.mediaType, id)
                        viewModel.onRandomIdOpen()
                    }
                }

                LaunchedEffect(uiState.message) {
                    if (uiState.message != null) {
                        if (uiState.mediaList.isNotEmpty()) {
                            context.showToast(uiState.message.orEmpty())
                            viewModel.onMessageDisplayed()
                        }
                    }
                }

                if (uiState.listSort != null) {
                    UserMediaListView(
                        uiState = uiState,
                        event = viewModel,
                        navActionManager = navActionManager,
                        isCompactScreen = isCompactScreen,
                        contentPadding = PaddingValues(
                            top = 8.dp, // Added small top padding for breathability
                            bottom = padding.calculateBottomPadding() + 8.dp
                        ),
                        onShowEditSheet = { item ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onItemSelected(item)
                            showEditSheet = true
                        },
                    )
                }
            }
        }
    }
}
