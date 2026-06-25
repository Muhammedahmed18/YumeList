package com.axiel7.moelist.ui.season.composables

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.anime.SeasonType
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.ui.season.SeasonChartEvent
import com.axiel7.moelist.ui.season.SeasonChartUiState
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.SeasonCalendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonChartFilterSheet(
    uiState: SeasonChartUiState,
    event: SeasonChartEvent?,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    bottomPadding: Dp = 0.dp
) {
    // Draft state — changes only commit to the ViewModel when Apply is pressed
    var draftSeason by remember { mutableStateOf(uiState.season) }
    var draftSort by remember { mutableStateOf(uiState.sort) }
    var draftIsNew by remember { mutableStateOf(uiState.isNew) }

    // The relative type (previous/current/next) the drafted season currently matches, if any.
    val isCurrentSeason = SeasonType.of(draftSeason) == SeasonType.CURRENT

    val scrollState = rememberLazyListState()

    LaunchedEffect(draftSeason.year) {
        val index = SeasonChartUiState.years.indexOf(draftSeason.year)
        if (index != -1) scrollState.scrollToItem(index)
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp + bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: title + reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.filters),
                    style = MaterialTheme.typography.titleLarge
                )
                TextButton(
                    onClick = {
                        draftSeason = SeasonCalendar.currentStartSeason
                        draftSort = MediaSort.ANIME_NUM_USERS
                        draftIsNew = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(text = stringResource(R.string.reset))
                }
            }

            Spacer(Modifier.height(8.dp))

            // Hero season stepper
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MorphingIconButton(
                        icon = Icons.Rounded.ChevronLeft,
                        contentDescription = stringResource(R.string.previous_season),
                        onClick = { draftSeason = draftSeason.previous() }
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            painter = painterResource(draftSeason.season.icon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = draftSeason.seasonYearText(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        // Fixed-height slot so swapping the tag/jump control never shifts the layout
                        Box(
                            modifier = Modifier.height(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCurrentSeason) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = stringResource(R.string.current_season),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    modifier = Modifier.clickable {
                                        draftSeason = SeasonCalendar.currentStartSeason
                                    }
                                ) {
                                    Text(
                                        text = stringResource(R.string.current_season),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    MorphingIconButton(
                        icon = Icons.Rounded.ChevronRight,
                        contentDescription = stringResource(R.string.next_season),
                        onClick = { draftSeason = draftSeason.next() }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Year chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp),
                state = scrollState,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SeasonChartUiState.years) {
                    FilterChip(
                        selected = draftSeason.year == it,
                        onClick = { draftSeason = draftSeason.copy(year = it) },
                        label = { Text(text = it.toString()) },
                        shape = MaterialTheme.shapes.large
                    )
                }
            }

            FilterSectionLabel(stringResource(R.string.show))

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = draftIsNew,
                    onClick = { draftIsNew = true },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text(text = stringResource(R.string.new_anime))
                }
                SegmentedButton(
                    selected = !draftIsNew,
                    onClick = { draftIsNew = false },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text(text = stringResource(R.string.continuing_anime))
                }
            }

            FilterSectionLabel(stringResource(R.string.sort_by))

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = draftSort == MediaSort.ANIME_NUM_USERS,
                    onClick = { draftSort = MediaSort.ANIME_NUM_USERS },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.People,
                            contentDescription = null,
                            modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                        )
                    }
                ) {
                    Text(text = stringResource(R.string.members))
                }
                SegmentedButton(
                    selected = draftSort == MediaSort.ANIME_SCORE,
                    onClick = { draftSort = MediaSort.ANIME_SCORE },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                        )
                    }
                ) {
                    Text(text = stringResource(R.string.score))
                }
            }

            Spacer(Modifier.height(20.dp))

            // Apply
            Button(
                onClick = {
                    event?.setSeason(season = draftSeason.season, year = draftSeason.year)
                    event?.onChangeSort(draftSort)
                    event?.onChangeIsNew(draftIsNew)
                    onApply()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.large,
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Text(
                    text = stringResource(R.string.apply),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

/**
 * Round icon button that morphs from a circle to a rounded square while pressed —
 * approximates the M3 expressive icon-button shape-morph without the alpha dependency.
 */
@Composable
private fun MorphingIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val cornerRadius by animateDpAsState(
        targetValue = if (pressed) 14.dp else 22.dp,
        label = "arrowCorner"
    )

    FilledTonalIconButton(
        onClick = onClick,
        shape = RoundedCornerShape(cornerRadius),
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}

@Composable
private fun FilterSectionLabel(text: String) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SeasonChartFilterSheetPreview() {
    MoeListTheme {
        Surface {
            SeasonChartFilterSheet(
                uiState = SeasonChartUiState(),
                event = null,
                onApply = {},
                onDismiss = {},
                sheetState = rememberModalBottomSheetState(),
            )
        }
    }
}
