package com.axiel7.moelist.ui.userlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusValues
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.TabRowItem
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.TabRowWithPager
import com.axiel7.moelist.ui.editmedia.EditMediaSheet
import com.axiel7.moelist.ui.userlist.composables.MediaListSortDialog
import com.axiel7.moelist.ui.userlist.composables.MediaListItemShimmer
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
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val tabRowItems = remember {
        listStatusValues(mediaType)
            .map {
                TabRowItem(value = it, title = it.stringRes)
            }.toTypedArray()
    }
    
    val pagerState = rememberPagerState { tabRowItems.size }
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showEditSheet by remember { mutableStateOf(false) }
    
    fun hideEditSheet() {
        scope.launch { editSheetState.hide() }.invokeOnCompletion { showEditSheet = false }
    }

    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    // Shared ViewModel key to sync FAB with the active Pager page
    val currentListStatus = tabRowItems[pagerState.currentPage].value
    val currentViewModel: UserMediaListViewModel = koinViewModel(
        key = "${mediaType.name}_${currentListStatus.name}",
        parameters = { parametersOf(mediaType, currentListStatus) }
    )
    val currentUiState by currentViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        floatingActionButton = {
            AnimatedVisibility(
                visible = !currentUiState.isLoadingRandom,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = { 
                        if (currentUiState.listSort != null) {
                            currentViewModel.toggleSortDialog(true) 
                        }
                    },
                    modifier = Modifier.padding(bottom = padding.calculateBottomPadding() + 16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_round_sort_24),
                        contentDescription = null
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                TabRowWithPager(
                    tabs = tabRowItems,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    isTabScrollable = true,
                    isPrimaryTab = true,
                    containerColor = Color.Transparent,
                    pagerState = pagerState,
                    showPager = false,
                    pageContent = { _, _ -> }
                )
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
                key = { tabRowItems[it].value }
            ) { page ->
                val pageOffsetFraction = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val fraction = 1f - kotlin.math.abs(pageOffsetFraction).coerceIn(0f, 1f)

                val pageAlpha = lerp(start = 0.85f, stop = 1f, fraction = fraction)
                val pageScale = lerp(start = 0.97f, stop = 1f, fraction = fraction)

                val currentPage = pagerState.currentPage
                val listStatus = tabRowItems[page].value
                val viewModel: UserMediaListViewModel = koinViewModel(
                    key = "${mediaType.name}_${listStatus.name}",
                    parameters = { parametersOf(mediaType, listStatus) }
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(currentPage) {
                    showEditSheet = false
                }

                if (uiState.openSortDialog && uiState.listSort != null) {
                    MediaListSortDialog(
                        uiState = uiState,
                        event = viewModel
                    )
                }

                if (showEditSheet && uiState.mediaInfo != null && page == currentPage) {
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

                when {
                    uiState.isLoadingRandom -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                            userScrollEnabled = false
                        ) {
                            items(3) {
                                MediaListItemShimmer()
                            }
                        }
                    }
                    uiState.listSort != null -> {
                        UserMediaListView(
                            uiState = uiState,
                            event = viewModel,
                            navActionManager = navActionManager,
                            isCompactScreen = isCompactScreen,
                            modifier = Modifier.graphicsLayer {
                                alpha = pageAlpha
                                scaleX = pageScale
                                scaleY = pageScale
                            },
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = padding.calculateBottomPadding() + innerPadding.calculateBottomPadding() + 80.dp
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
}
