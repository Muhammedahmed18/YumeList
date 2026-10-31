package com.axiel7.moelist.ui.details.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.Genre

/**
 * Genres as non-interactive, fully rounded pills in a horizontally scrolling row, inside a
 * rounded section card (the pills scroll under the card's rounded edge).
 */
@Composable
fun DetailsGenres(
    genres: List<Genre>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        DetailsSectionTitle(text = stringResource(R.string.genres))
        DetailsSectionCard(contentPadding = PaddingValues(vertical = 12.dp)) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(genres) { genre ->
                    DetailsPill(
                        text = genre.localized(),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}
