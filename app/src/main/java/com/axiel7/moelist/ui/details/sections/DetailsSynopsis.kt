package com.axiel7.moelist.ui.details.sections

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.details.MediaDetailsUiState
import com.axiel7.moelist.ui.theme.Motion
import com.axiel7.moelist.utils.ContextExtensions.copyToClipBoard

private const val COLLAPSED_SYNOPSIS_LINES = 6

/**
 * Synopsis + background in a rounded section card. Tap the text or the Show more / Show less
 * pill to expand / collapse (size change uses the spatial spring), long-press to copy. The expanded state survives scrolling the section off screen.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailsSynopsis(
    uiState: MediaDetailsUiState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val synopsisAndBackground = uiState.mediaDetails?.synopsisAndBackground()
    // rememberSaveable: LazyColumn disposes off-screen sections, this keeps the expanded state
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLow

    Column(modifier = modifier) {
        DetailsSectionTitle(text = stringResource(R.string.synopsis))
        DetailsSectionCard {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = synopsisAndBackground
                        ?: AnnotatedString(stringResource(R.string.lorem_ipsun)),
                    modifier = Modifier
                        // Synopsis expand/collapse: size change = spatial spring
                        .animateContentSize(animationSpec = Motion.spatial())
                        .defaultPlaceholder(visible = uiState.isLoading)
                        .combinedClickable(
                            onLongClick = {
                                uiState.mediaDetails?.synopsis?.let {
                                    context.copyToClipBoard(it)
                                }
                            },
                            onClick = { isExpanded = !isExpanded }
                        ),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 26.sp,
                        letterSpacing = 0.2.sp
                    ),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = if (isExpanded) Int.MAX_VALUE else COLLAPSED_SYNOPSIS_LINES
                )
                val gradientAlpha by animateFloatAsState(
                    targetValue = if (isExpanded) 0f else 1f,
                    animationSpec = Motion.effects(),
                    label = "synopsisGradient"
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer { alpha = gradientAlpha }
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.5f to Color.Transparent,
                                1.0f to containerColor
                            )
                        )
                )
            }
            DetailsExpandButton(
                expanded = isExpanded,
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            )
        }
    }
}
