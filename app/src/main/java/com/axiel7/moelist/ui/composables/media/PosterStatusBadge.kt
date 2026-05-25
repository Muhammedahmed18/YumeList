package com.axiel7.moelist.ui.composables.media

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.media.ListStatus

@Composable
fun PosterStatusBadge(
    status: ListStatus,
    iconSize: Dp = 14.dp,
) {
    Icon(
        imageVector = status.icon,
        contentDescription = status.localized(),
        modifier = Modifier.size(iconSize),
        tint = MaterialTheme.colorScheme.primary,
    )
}
