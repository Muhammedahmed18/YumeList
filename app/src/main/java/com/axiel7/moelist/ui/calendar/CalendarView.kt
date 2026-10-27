package com.axiel7.moelist.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.WeekDay
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.composables.LocalSnackbarHostState
import com.axiel7.moelist.ui.composables.PosterSection
import com.axiel7.moelist.ui.composables.SnackbarType
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.MediaItemVerticalPlaceholder
import com.axiel7.moelist.ui.composables.posterSharedKey
import com.axiel7.moelist.ui.composables.showAppSnackbar
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.SeasonCalendar
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun CalendarView(
    navActionManager: NavActionManager
) {
    val viewModel: CalendarViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CalendarContent(
        uiState = uiState,
        event = viewModel,
        navActionManager = navActionManager,
    )
}

@Composable
private fun CalendarContent(
    uiState: CalendarUiState,
    event: CalendarEvent?,
    navActionManager: NavActionManager,
) {
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            snackbarHostState.showAppSnackbar(uiState.message, type = SnackbarType.ERROR)
            event?.onMessageDisplayed()
        }
    }

    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.calendar),
        navigateBack = dropUnlessResumed { navActionManager.goBack() },
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { padding ->
        val today = SeasonCalendar.currentWeekday
        val pagerState = rememberPagerState(initialPage = today.ordinal) { WeekDay.entries.size }
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CalendarDaySelector(
                selectedIndex = pagerState.currentPage,
                today = today,
                onSelect = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
            )
            // Swiping between days still works; the selector follows the pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = { it }
            ) { page ->
                val weekday = page + 1
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = MEDIA_POSTER_SMALL_WIDTH.dp),
                    modifier = Modifier.fillMaxHeight(),
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        top = 8.dp,
                        end = 8.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    items(
                        items = uiState.weekAnime(weekday),
                        contentType = { "media" }
                    ) { item ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            val posterKey = posterSharedKey(PosterSection.CALENDAR, MediaType.ANIME, item.node.id)
                            MediaItemVertical(
                                imageUrl = item.node.mainPicture?.large,
                                posterKey = posterKey,
                                title = item.node.userPreferredTitle(),
                                status = item.node.myListStatus?.status,
                                subtitle = {
                                    Text(
                                        text = item.node.broadcast?.localStartTime() ?: "??",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                minLines = 2,
                                onClick = dropUnlessResumed {
                                    navActionManager.toMediaDetails(
                                        MediaType.ANIME,
                                        item.node.id,
                                        posterUrl = item.node.mainPicture?.large,
                                        posterKey = posterKey,
                                    )
                                }
                            )
                        }
                    }
                    if (uiState.isLoading) {
                        items(10) {
                            MediaItemVerticalPlaceholder()
                        }
                    }
                }
            }//:HorizontalPager
        }//:Column
    }//:Scaffold
}

/**
 * M3 Expressive day selector: the seven days as a connected button group (one row, no
 * scrolling). The selected day is filled and morphs into a pill; today keeps a tonal
 * container, an emphasized label and a dot, so it stands out even when another day is selected.
 */
@Composable
private fun CalendarDaySelector(
    selectedIndex: Int,
    today: WeekDay,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val days = WeekDay.entries
    val todayLabel = stringResource(R.string.today)
    val locale = Locale.getDefault()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        days.forEachIndexed { index, day ->
            val isToday = day == today
            val fullName = stringResource(day.stringRes)
            // Short localized name ("Mon", "lun."), WeekDay follows DayOfWeek order (Monday first)
            val shortName = remember(day, locale) {
                DayOfWeek.of(day.ordinal + 1).getDisplayName(TextStyle.SHORT, locale)
            }
            ToggleButton(
                checked = selectedIndex == index,
                onCheckedChange = { onSelect(index) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    days.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                colors = if (isToday) {
                    ToggleButtonDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                } else ToggleButtonDefaults.colors(),
                contentPadding = PaddingValues(horizontal = 2.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(DaySelectorHeight)
                    .semantics {
                        contentDescription = if (isToday) "$fullName, $todayLabel" else fullName
                    }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = shortName,
                        style = if (isToday) MaterialTheme.typography.labelLargeEmphasized
                        else MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip
                    )
                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(4.dp)
                                .background(LocalContentColor.current, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

private val DaySelectorHeight = 48.dp

@Preview
@Composable
fun CalendarPreview() {
    MoeListTheme {
        Surface {
            CalendarContent(
                uiState = CalendarUiState(),
                event = null,
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}
