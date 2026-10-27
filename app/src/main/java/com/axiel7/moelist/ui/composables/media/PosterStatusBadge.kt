package com.axiel7.moelist.ui.composables.media

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.ui.theme.ShapeChip

/**
 * List status over a poster: icon-only chip filled with the status colour.
 * Always placed at the top-end corner of the poster.
 */
@Composable
fun PosterStatusBadge(
    status: ListStatus,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = ShapeChip,
        color = status.primaryColor(),
        contentColor = status.onPrimaryColor(),
    ) {
        Icon(
            imageVector = status.icon,
            contentDescription = status.localized(),
            modifier = Modifier
                .padding(5.dp)
                .size(14.dp),
        )
    }
}
