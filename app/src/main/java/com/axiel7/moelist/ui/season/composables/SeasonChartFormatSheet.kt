package com.axiel7.moelist.ui.season.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaFormat
import com.axiel7.moelist.ui.season.SeasonChartEvent
import com.axiel7.moelist.ui.season.SeasonChartUiState

private val ItemPillShape = RoundedCornerShape(20.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonChartFormatSheet(
    uiState: SeasonChartUiState,
    event: SeasonChartEvent?,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    val formatOptions = remember {
        listOf(
            null, // All
            MediaFormat.TV,
            MediaFormat.OVA,
            MediaFormat.ONA,
            MediaFormat.MOVIE,
            MediaFormat.TV_SPECIAL,
            MediaFormat.SPECIAL
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
    ) {
        SeasonChartFormatContent(
            formatOptions = formatOptions,
            selectedFormat = uiState.selectedFormat,
            formatCounts = uiState.formatCounts,
            onFormatClick = {
                event?.onChangeFormat(it)
                onDismiss()
            }
        )
    }
}

@Composable
fun SeasonChartFormatContent(
    formatOptions: List<MediaFormat?>,
    selectedFormat: MediaFormat?,
    formatCounts: Map<MediaFormat?, Int>,
    onFormatClick: (MediaFormat?) -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.format),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)
        )

        formatOptions.forEach { format ->
            val isSelected = selectedFormat == format
            val count = formatCounts[format] ?: 0

            val checkScale by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.6f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "checkScale"
            )
            
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else
                    Color.Transparent,
                label = "backgroundColor"
            )

            val contentColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                label = "contentColor"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(ItemPillShape)
                    .background(backgroundColor)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFormatClick(format)
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = format?.localized() ?: stringResource(R.string.all),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else contentColor,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else contentColor,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .scale(checkScale)
                            .size(24.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}
