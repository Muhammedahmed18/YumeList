package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.userlist.UserMediaListEvent
import com.axiel7.moelist.ui.userlist.UserMediaListUiState

@Composable
fun SortChip(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) 
        else 
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        label = "chipBackground"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isActive) 
            MaterialTheme.colorScheme.onPrimaryContainer 
        else 
            MaterialTheme.colorScheme.onSurfaceVariant,
        label = "chipContent"
    )

    val borderColor = if (isActive) 
        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) 
    else 
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_round_sort_24),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 0.2.sp
        )

        if (isActive) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

@Composable
fun SortChip(
    uiState: UserMediaListUiState,
    event: UserMediaListEvent?,
    modifier: Modifier = Modifier
) {
    // Assuming default sort is null or can be checked. 
    // If you have a specific default sort, update the logic here.
    val isActive = uiState.listSort != null 
    
    SortChip(
        text = uiState.listSort?.localized() ?: stringResource(R.string.sort_by),
        isActive = isActive,
        onClick = { event?.toggleSortDialog(true) },
        modifier = modifier,
    )
}
