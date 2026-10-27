package com.axiel7.moelist.ui.details.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.composables.InfoTitle
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.details.MediaDetailsUiState
import com.axiel7.moelist.ui.theme.ShapePoster
import com.axiel7.moelist.utils.CHARACTER_URL
import com.axiel7.moelist.utils.ContextExtensions.openLink

/**
 * Characters row (anime only). Characters load on demand unless the "load characters"
 * setting is on; tapping a character opens its MAL page.
 */
@Composable
fun DetailsCharacters(
    uiState: MediaDetailsUiState,
    onLoadCharacters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(modifier = modifier) {
        InfoTitle(text = stringResource(R.string.characters))
        if (uiState.characters.isNotEmpty() || uiState.isLoadingCharacters) {
            LazyRow(
                modifier = Modifier.padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = DetailsHorizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.characters,
                    contentType = { "character" }
                ) { item ->
                    MediaItemVertical(
                        imageUrl = item.node.mainPicture?.medium,
                        title = item.fullName(),
                        subtitle = {
                            CharacterRolePill(
                                role = item.role?.localized().orEmpty(),
                                isMain = item.role?.name.equals("MAIN", ignoreCase = true)
                            )
                        },
                        minLines = 2,
                        onClick = {
                            context.openLink(CHARACTER_URL + item.node.id)
                        }
                    )
                }
                if (uiState.isLoadingCharacters && uiState.characters.isEmpty()) {
                    items(4, contentType = { "placeholder" }) {
                        CharacterShimmerItem()
                    }
                }
            }
        } else {
            FilledTonalButton(
                onClick = onLoadCharacters,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .padding(horizontal = DetailsHorizontalPadding, vertical = 8.dp)
                    .fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.view_characters))
            }
        }
    }
}

@Composable
private fun CharacterRolePill(
    role: String,
    isMain: Boolean,
) {
    if (role.isEmpty()) return
    Surface(
        shape = CircleShape,
        color = if (isMain) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = if (isMain) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            text = role,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun CharacterShimmerItem(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(width = 100.dp, height = 140.dp)
    ) {
        // Poster placeholder, same shape as the real posters
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .clip(ShapePoster)
                .defaultPlaceholder(visible = true)
        )
        // Role pill placeholder, overlaid where the real CharacterRolePill sits
        Spacer(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
                .size(width = 52.dp, height = 18.dp)
                .clip(CircleShape)
                .defaultPlaceholder(visible = true)
        )
    }
}
