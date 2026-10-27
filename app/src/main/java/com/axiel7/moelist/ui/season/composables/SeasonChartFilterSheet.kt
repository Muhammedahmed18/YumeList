package com.axiel7.moelist.ui.season.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.Autorenew
import com.axiel7.moelist.ui.icons.rounded.NewReleases
import com.axiel7.moelist.ui.icons.rounded.People
import com.axiel7.moelist.ui.icons.rounded.RestartAlt
import com.axiel7.moelist.ui.icons.rounded.Star
import com.axiel7.moelist.ui.season.SeasonChartEvent
import com.axiel7.moelist.ui.season.SeasonChartUiState
import com.axiel7.moelist.ui.theme.MoeListTheme

private val DefaultSort = MediaSort.ANIME_NUM_USERS
private const val DEFAULT_IS_NEW = true

/**
 * Season chart filters: what to show (new / continuing) and how to sort.
 *
 * Season and year moved to their own sheet (the season name on the floating toolbar), so this
 * one is short. Choices are drafts until Apply, which is only enabled when something actually
 * changed (no pointless reload). Reset goes back to the defaults (New, Members).
 */
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
    // Draft state: changes only commit to the ViewModel when Apply is pressed
    var draftSort by remember { mutableStateOf(uiState.sort) }
    var draftIsNew by remember { mutableStateOf(uiState.isNew) }
    val hasChanges = draftSort != uiState.sort || draftIsNew != uiState.isNew
    val isDefault = draftSort == DefaultSort && draftIsNew == DEFAULT_IS_NEW

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp + bottomPadding)
        ) {
            SheetHeader(
                title = stringResource(R.string.filters),
                action = {
                    TextButton(
                        onClick = {
                            draftSort = DefaultSort
                            draftIsNew = DEFAULT_IS_NEW
                        },
                        enabled = !isDefault,
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text(text = stringResource(R.string.reset))
                    }
                }
            )

            // ── Show ─────────────────────────────────────────────────────────
            SheetSectionLabel(stringResource(R.string.show))
            ConnectedChoice(
                options = listOf(
                    ChoiceOption(stringResource(R.string.new_anime), Icons.Rounded.NewReleases),
                    ChoiceOption(stringResource(R.string.continuing_anime), Icons.Rounded.Autorenew),
                ),
                selectedIndex = if (draftIsNew) 0 else 1,
                onSelect = { draftIsNew = it == 0 },
            )

            // ── Sort by ──────────────────────────────────────────────────────
            SheetSectionLabel(stringResource(R.string.sort_by))
            ConnectedChoice(
                options = listOf(
                    ChoiceOption(stringResource(R.string.members), Icons.Rounded.People),
                    ChoiceOption(stringResource(R.string.score), Icons.Rounded.Star),
                ),
                selectedIndex = if (draftSort == MediaSort.ANIME_SCORE) 1 else 0,
                onSelect = {
                    draftSort = if (it == 1) MediaSort.ANIME_SCORE else MediaSort.ANIME_NUM_USERS
                },
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    event?.onChangeSort(draftSort)
                    event?.onChangeIsNew(draftIsNew)
                    onApply()
                },
                enabled = hasChanges,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                contentPadding = ButtonDefaults.contentPaddingFor(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.apply),
                    style = ButtonDefaults.textStyleFor(56.dp)
                )
            }
        }
    }
}

private class ChoiceOption(val label: String, val icon: ImageVector)

/**
 * M3 Expressive connected button group (single choice): the selected button fills and morphs
 * into a pill, every button squishes on press.
 */
@Composable
private fun ConnectedChoice(
    options: List<ChoiceOption>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        options.forEachIndexed { index, option ->
            ToggleButton(
                checked = selectedIndex == index,
                onCheckedChange = { onSelect(index) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                icon = {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Text(text = option.label, maxLines = 1)
            }
        }
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
