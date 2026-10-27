package com.axiel7.moelist.ui.season.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.data.model.anime.StartSeason
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.ArrowForward
import com.axiel7.moelist.ui.icons.rounded.Today
import com.axiel7.moelist.ui.season.SeasonChartUiState
import com.axiel7.moelist.utils.SeasonCalendar

/**
 * "Jump to season": opened from the season name on the floating toolbar.
 *
 * - The four seasons as a connected button group, a scrolling row of years, and a "Current"
 *   shortcut in the header (loads the current season straight away).
 * - The button at the bottom names the target ("Go to Fall 2015") and loads it. It's only
 *   enabled when the picked season differs from the one on screen.
 *
 * @param current the season currently shown
 * @param onSelect called with the season to load; the caller closes the sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonChartSeasonSheet(
    current: StartSeason,
    onSelect: (StartSeason) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    bottomPadding: Dp = 0.dp,
) {
    var draftSeason by rememberSaveable { mutableStateOf(current.season) }
    var draftYear by rememberSaveable { mutableStateOf(current.year) }
    val draft = StartSeason(year = draftYear, season = draftSeason)
    val isShowingCurrent = current == SeasonCalendar.currentStartSeason

    val years = SeasonChartUiState.years
    val yearListState = rememberLazyListState()
    // Open with the picked year roughly in the middle of the row
    LaunchedEffect(Unit) {
        val index = years.indexOf(current.year)
        if (index != -1) yearListState.scrollToItem((index - 2).coerceAtLeast(0))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp + bottomPadding)
        ) {
            SheetHeader(
                title = stringResource(R.string.season),
                action = if (!isShowingCurrent) {
                    {
                        TextButton(
                            onClick = { onSelect(SeasonCalendar.currentStartSeason) },
                            shapes = ButtonDefaults.shapes(),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Today,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                            Text(text = stringResource(R.string.current_season))
                        }
                    }
                } else null,
            )

            // ── Season ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
            ) {
                val seasons = Season.entries
                seasons.forEachIndexed { index, season ->
                    ToggleButton(
                        checked = draftSeason == season,
                        onCheckedChange = { draftSeason = season },
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            seasons.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 64.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(season.icon),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = season.localized(),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // ── Year ─────────────────────────────────────────────────────────
            SheetSectionLabel(stringResource(R.string.year))

            LazyRow(
                state = yearListState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(years, key = { it }) { year ->
                    ToggleButton(
                        checked = draftYear == year,
                        onCheckedChange = { draftYear = year },
                        // Default shapes: a round pill that squishes on press
                    ) {
                        Text(text = year.toString())
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onSelect(draft) },
                enabled = draft != current,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                contentPadding = ButtonDefaults.contentPaddingFor(56.dp, hasEndIcon = true)
            ) {
                Text(
                    text = draft.seasonYearText(),
                    style = ButtonDefaults.textStyleFor(56.dp)
                )
                Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(56.dp)))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.iconSizeFor(56.dp))
                )
            }
        }
    }
}
