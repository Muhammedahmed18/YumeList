package com.axiel7.moelist.ui.composables.media

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.media.ListStatus

/**
 * Inline list-status indicator (icon + localized label) used in the metadata
 * row of detailed media cards. Rendered on the card surface so it stays legible,
 * unlike [PosterStatusBadge] which overlays the poster art.
 */
@Composable
fun MediaStatusIndicator(
    status: ListStatus,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = status.icon,
            contentDescription = status.localized(),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = status.localized(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
