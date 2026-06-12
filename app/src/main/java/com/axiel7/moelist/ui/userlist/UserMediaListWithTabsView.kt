package com.axiel7.moelist.ui.userlist

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.ListStatus.Companion.listStatusValues
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.TabRowItem
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.TabRowWithPager
import com.axiel7.moelist.ui.editmedia.EditMediaSheet
import com.axiel7.moelist.ui.userlist.composables.MediaListFormatSheet
import com.axiel7.moelist.ui.userlist.composables.MediaListItemShimmer
import com.axiel7.moelist.ui.userlist.composables.MediaListSortDialog
import com.axiel7.moelist.ui.userlist.composables.UserMediaListControlBar
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
    useListTabs: Boolean = true,
    onSortClickTrigger: (() -> Unit) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val tabRowItems = remember {
        listStatusValues(mediaType)
            .map {
                TabRowItem(value = it, title = it.stringRes)
            }.toTypedArray()
    }

    val pagerState = rememberPagerState { tabRowItems.size }
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val statusSheetState = rememberModalBottomSheetState()
    var showEditSheet by remember { mutableStateOf(false) }
    var showStatusSheet by remember { mutableStateOf(false) }

    fun hideEditSheet(onComplete: () -> Unit = {}) {
        scope.launch { editSheetState.hide() }.invokeOnCompletion {
            showEditSheet = false
            onComplete()
        }
    }

    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    // Shared ViewModel key to sync triggers with the active Pager page
    val currentListStatus = tabRowItems[pagerState.currentPage].value
    val currentViewModel: UserMediaListViewModel = koinViewModel(
        key = "${mediaType.name}_${currentListStatus.name}",
        parameters = { parametersOf(mediaType, currentListStatus) }
    )
    val currentUiState by currentViewModel.uiState.collectAsStateWithLifecycle()

    // Register the sort click action to be triggered from outside (the top bar)
    LaunchedEffect(currentViewModel) {
        onSortClickTrigger {
            if (currentUiState.listSort != null) {
                currentViewModel.toggleSortDialog(true)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentWindowInsets = with(density) {
            WindowInsets(bottom = padding.calculateBottomPadding().roundToPx())
        },
        floatingActionButton = {
            if (!useListTabs) {
                ExtendedFloatingActionButton(
                    onClick = { showStatusSheet = true },
                    icon = {
                        Icon(
                            imageVector = currentListStatus.icon,
                            contentDescription = null,
                        )
                    },
                    text = { Text(currentListStatus.localized()) },
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                tonalElevation = 3.dp
            ) {
                Column {
                    if (useListTabs) {
                        TabRowWithPager(
                            tabs = tabRowItems,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            isTabScrollable = true,
                            isPrimaryTab = true,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            pagerState = pagerState,
                            showPager = false,
                            pageContent = { _, _ -> }
                        )
                    }

                    UserMediaListControlBar(
                        uiState = currentUiState,
                        event = currentViewModel
                    )
                }
            }

            if (currentUiState.openSortDialog && currentUiState.listSort != null) {
                MediaListSortDialog(
                    uiState = currentUiState,
                    event = currentViewModel
                )
            }

            if (currentUiState.openFormatSheet) {
                MediaListFormatSheet(
                    uiState = currentUiState,
                    event = currentViewModel
                )
            }

            if (!useListTabs && showStatusSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showStatusSheet = false },
                    sheetState = statusSheetState,
                    dragHandle = { BottomSheetDefaults.DragHandle() },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                ) {
                    Text(
                        text = stringResource(R.string.status),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                    )
                    tabRowItems.forEachIndexed { index, item ->
                        val isSelected = pagerState.currentPage == index
                        val rowBg by animateColorAsState(
                            targetValue = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else
                                Color.Transparent,
                            label = "status_bg_$index",
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(rowBg)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                    showStatusSheet = false
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Icon(
                                imageVector = item.value.icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = item.value.localized(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
                key = { tabRowItems[it].value }
            ) { page ->
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

                if (showEditSheet && uiState.mediaInfo != null && page == currentPage) {
                    EditMediaSheet(
                        sheetState = editSheetState,
                        mediaInfo = uiState.mediaInfo!!,
                        myListStatus = uiState.myListStatus,
                        onEdited = { status, removed ->
                            hideEditSheet {
                                viewModel.onChangeItemMyListStatus(status, removed)
                            }
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
                            contentPadding = PaddingValues(top = 8.dp),
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
