package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaFormat
import com.axiel7.moelist.ui.userlist.UserMediaListEvent
import com.axiel7.moelist.ui.userlist.UserMediaListUiState

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun UserMediaListControlBar(
    uiState: UserMediaListUiState,
    event: UserMediaListEvent?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Format Selector
        IconButton(
            onClick = { event?.toggleFormatSheet(true) },
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Icon(
                imageVector = Icons.Rounded.FilterList,
                contentDescription = "Format",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Center: Active State Pill
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .height(48.dp)
                .clip(CircleShape)
                .clickable { /* Could trigger something or just be informational */ },
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = CircleShape
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                AnimatedContent(
                    targetState = uiState.selectedFormat to (uiState.formatCounts[uiState.selectedFormat] ?: 0),
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> -height } + fadeOut())
                    },
                    label = "ActiveStateAnimation"
                ) { (format, count) ->
                    val formatText = format?.localized() ?: stringResource(R.string.all)
                    Text(
                        text = "$formatText ($count)",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Right: Sort Button
        Surface(
            onClick = { event?.toggleSortDialog(true) },
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
                    imageVector = Icons.AutoMirrored.Rounded.Sort,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = uiState.listSort?.localized() ?: "",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
