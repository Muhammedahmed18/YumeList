package com.axiel7.moelist.ui.details.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.Genre
import com.axiel7.moelist.ui.composables.InfoTitle

/**
 * Genres as non-interactive pills. They used to be AssistChips with an empty onClick, which
 * showed a ripple and read as buttons that did nothing.
 */
@Composable
fun DetailsGenres(
    genres: List<Genre>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        InfoTitle(text = stringResource(R.string.genres))
        LazyRow(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = DetailsHorizontalPadding),
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
