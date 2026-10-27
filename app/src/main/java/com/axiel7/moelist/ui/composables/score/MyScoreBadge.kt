package com.axiel7.moelist.ui.composables.score

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.Person
import com.axiel7.moelist.ui.theme.ShapeChip

/**
 * The user's own score. Same pill as [PosterScoreChip] (so the two read as one family), but
 * filled with the theme's primary colour and a person icon instead of the star, which always
 * means the community score. Nothing is drawn when there's no score yet.
 */
@Composable
fun MyScoreBadge(
    score: Int?,
    modifier: Modifier = Modifier,
) {
    if (score == null || score <= 0) return
    val description = "${stringResource(R.string.score)} $score"
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        shape = ShapeChip,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
