package com.axiel7.moelist.ui.season

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.BackIconButton
import com.axiel7.moelist.ui.composables.EmptyState
import com.axiel7.moelist.ui.composables.ErrorState
import com.axiel7.moelist.ui.composables.LoadingState
import com.axiel7.moelist.ui.composables.TextIconHorizontal
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaItemDetailedPlaceholder
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.score.SmallScoreIndicator
import com.axiel7.moelist.ui.season.composables.SeasonChartFilterSheet
import com.axiel7.moelist.ui.season.composables.SeasonChartFormatSheet
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.NumExtensions.format
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SeasonChartView(
    navActionManager: NavActionManager
) {
    val viewModel: SeasonChartViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SeasonChartViewContent(
        uiState = uiState,
        event = viewModel,
        navActionManager = navActionManager
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeasonChartViewContent(
    uiState: SeasonChartUiState,
    event: SeasonChartEvent?,
    navActionManager: NavActionManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val filterSheetState = rememberModalBottomSheetState()
    var showFilterSheet by remember { mutableStateOf(false) }
    fun hideFilterSheet() {
        scope.launch { filterSheetState.hide() }.invokeOnCompletion { showFilterSheet = false }
    }

    val formatSheetState = rememberModalBottomSheetState()
    var showFormatSheet by remember { mutableStateOf(false) }
    fun hideFormatSheet() {
        scope.launch { formatSheetState.hide() }.invokeOnCompletion { showFormatSheet = false }
    }

    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    if (showFilterSheet) {
        SeasonChartFilterSheet(
            uiState = uiState,
            event = event,
            onApply = {
                hideFilterSheet()
                event?.onApplyFilters()
            },
            onDismiss = { hideFilterSheet() },
            sheetState = filterSheetState,
            bottomPadding = bottomBarPadding
        )
    }

    if (showFormatSheet) {
        SeasonChartFormatSheet(
            uiState = uiState,
            event = event,
            onDismiss = { hideFormatSheet() },
            sheetState = formatSheetState
        )
    }

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            context.showToast(uiState.message)
            event?.onMessageDisplayed()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(text = uiState.season.seasonYearText()) },
                    navigationIcon = {
                        BackIconButton(onClick = navActionManager::goBack)
                    },
                    scrollBehavior = scrollBehavior
                )
                
                // Control Bar with Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Pill: Format
                    val selectedFormatText = uiState.selectedFormat?.localized() ?: stringResource(R.string.all)
                    val count = uiState.formatCounts[uiState.selectedFormat] ?: 0
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { showFormatSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$selectedFormatText ($count)",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    // Right Pill: Filter
                    Surface(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier
                            .height(48.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        shape = CircleShape
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_round_filter_list_24),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.filters),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading && uiState.animes.isEmpty() -> {
                    LoadingState(modifier = Modifier.fillMaxSize())
                }
                uiState.message != null && uiState.animes.isEmpty() -> {
                    ErrorState(
                        modifier = Modifier.fillMaxSize(),
                        message = uiState.message,
                        onAction = { event?.onApplyFilters() }
                    )
                }
                !uiState.isLoading && uiState.animes.isEmpty() -> {
                    EmptyState(modifier = Modifier.fillMaxSize())
                }
                else -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(minSize = MEDIA_POSTER_SMALL_WIDTH.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            top = 8.dp,
                            end = 12.dp,
                            bottom = bottomBarPadding + 16.dp
                        ),
                        verticalItemSpacing = 16.dp,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.filteredAnimes,
                            key = { it.node.id }
                        ) { item ->
                            MediaItemVertical(
                                imageUrl = item.node.mainPicture?.large,
                                title = item.node.userPreferredTitle(),
                                posterOverlay = if (!uiState.hideScore) {
                                    {
                                        Surface(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(6.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f)
                                        ) {
                                            SmallScoreIndicator(
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                score = item.node.mean,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                } else null,
                                badgeContent = item.node.myListStatus?.status?.let { status ->
                                    {
                                        Icon(
                                            painter = painterResource(status.icon),
                                            contentDescription = status.localized(),
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                subtitle = {
                                    item.node.numListUsers?.format()?.let { users ->
                                        TextIconHorizontal(
                                            text = users,
                                            icon = R.drawable.ic_round_group_24,
                                            color = MaterialTheme.colorScheme.outline,
                                            fontSize = 12.sp,
                                            iconSize = 14.dp
                                        )
                                    }
                                },
                                minLines = 2,
                                onClick = dropUnlessResumed {
                                    navActionManager.toMediaDetails(MediaType.ANIME, item.node.id)
                                }
                            )
                        }
                        
                        if (uiState.isLoading) {
                            items(10) {
                                MediaItemDetailedPlaceholder()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SeasonChartPreview() {
    MoeListTheme {
        Surface {
            SeasonChartViewContent(
                uiState = SeasonChartUiState(),
                event = null,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}
