package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaSort.Companion.animeListSortItems
import com.axiel7.moelist.data.model.media.MediaSort.Companion.mangaListSortItems
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.userlist.UserMediaListEvent
import com.axiel7.moelist.ui.userlist.UserMediaListUiState

private val PillShape = RoundedCornerShape(percent = 50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaListSortDialog(
    uiState: UserMediaListUiState,
    event: UserMediaListEvent?,
) {
    val sortOptions = remember(uiState.mediaType) {
        if (uiState.mediaType == MediaType.ANIME) animeListSortItems.toList()
        else mangaListSortItems.toList()
    }
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { event?.toggleSortDialog(false) },
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        MediaListSortContent(
            sortOptions = sortOptions,
            selectedSort = uiState.listSort,
            onSortClick = {
                event?.onChangeSort(it)
                event?.toggleSortDialog(false)
            }
        )
    }
}

@Composable
fun MediaListSortContent(
    sortOptions: List<MediaSort>,
    selectedSort: MediaSort?,
    onSortClick: (MediaSort) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 28.dp)
    ) {
        Text(
            text = stringResource(R.string.sort_by),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(bottom = 10.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        sortOptions.forEach { sort ->
            val isSelected = selectedSort == sort

            val checkScale by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.7f,
                animationSpec = tween(durationMillis = 180),
                label = "checkScale"
            )
            val checkAlpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0f,
                animationSpec = tween(durationMillis = 180),
                label = "checkAlpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 1.dp)
                    .clip(PillShape)
                    .background(
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                        else
                            Color.Transparent
                    )
                    .clickable { onSortClick(sort) }
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Text(
                    text = sort.localized(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .scale(checkScale)
                        .size(22.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = checkAlpha),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = checkAlpha),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MediaListSortDialogPreview() {
    MoeListTheme {
        Surface {
            MediaListSortContent(
                sortOptions = animeListSortItems.toList(),
                selectedSort = MediaSort.SCORE,
                onSortClick = {}
            )
        }
    }
}