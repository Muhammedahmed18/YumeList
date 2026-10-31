package com.axiel7.moelist.ui.details.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.MusicNote
import com.axiel7.moelist.ui.icons.rounded.PlayArrow
import com.axiel7.moelist.ui.icons.rounded.PlayCircle
import com.axiel7.moelist.ui.icons.rounded.StopCircle

/** Height of the opening / ending buttons (same as the status split button). */
private val ThemeButtonHeight: Dp = 48.dp

/**
 * Opening / ending buttons (anime only), as an M3 Expressive **connected button pair**: two
 * matching tonal buttons, 2dp apart, round outside corners and small inner corners, each
 * squishing while pressed. Labels stay on one line (ellipsis if needed) and the song count is a
 * small badge, so both buttons always have the same height. With only openings or only endings,
 * that one button takes the full width.
 *
 * Tapping one opens the themes sheet, which lives at the screen level so it isn't dismissed
 * when this section scrolls out of the list.
 *
 * @param onOpenThemes called with the sheet title and its songs
 */
@Composable
fun DetailsThemes(
    openings: List<String>,
    endings: List<String>,
    onOpenThemes: (title: String, songs: List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val openingLabel = stringResource(R.string.opening)
    val endingLabel = stringResource(R.string.ending)
    val hasBoth = openings.isNotEmpty() && endings.isNotEmpty()

    Column(modifier = modifier) {
        DetailsSectionTitle(text = stringResource(R.string.music_themes))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DetailsHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
        ) {
            if (openings.isNotEmpty()) {
                ThemeButton(
                    label = openingLabel,
                    count = openings.size,
                    icon = Icons.Rounded.PlayCircle,
                    shapes = if (hasBoth) {
                        ButtonShapes(
                            shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                            pressedShape = ButtonGroupDefaults.connectedLeadingButtonPressShape,
                        )
                    } else ButtonDefaults.shapes(),
                    onClick = { onOpenThemes(openingLabel, openings) },
                    modifier = Modifier.weight(1f),
                )
            }
            if (endings.isNotEmpty()) {
                ThemeButton(
                    label = endingLabel,
                    count = endings.size,
                    icon = Icons.Rounded.StopCircle,
                    shapes = if (hasBoth) {
                        ButtonShapes(
                            shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                            pressedShape = ButtonGroupDefaults.connectedTrailingButtonPressShape,
                        )
                    } else ButtonDefaults.shapes(),
                    onClick = { onOpenThemes(endingLabel, endings) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ThemeButton(
    label: String,
    count: Int,
    icon: ImageVector,
    shapes: ButtonShapes,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        shapes = shapes,
        modifier = modifier.height(ThemeButtonHeight),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            // Shrinks before the badge does, so the count is always visible
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Badge(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        ) {
            Text(text = count.toString())
        }
    }
}

/** Bottom sheet listing the songs of one theme type; tapping a song opens the streaming sheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemesSheet(
    sheetState: SheetState,
    title: String,
    themes: List<String>,
    bottomPadding: Dp,
    onThemeClick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.padding(bottom = bottomPadding + 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            )
            LazyColumn {
                items(themes) { song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeClick(song) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = song,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        )
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
