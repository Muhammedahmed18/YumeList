package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.Add
import com.axiel7.moelist.ui.icons.rounded.Star
import com.axiel7.moelist.ui.theme.Motion
import com.axiel7.moelist.ui.theme.ShapeSmall

/*
 * Building blocks shared by the four user list styles (standard, compact, minimal, grid).
 */

// ---------------------------------------------------------------------------------------------
// Segmented list container
// ---------------------------------------------------------------------------------------------

/** Outer corners of a segmented group: M3 Expressive list container shape (Large, 16dp). */
private val SegmentOuterCorner: Dp = 16.dp

/** Inner corners between segments: M3 Expressive list item shape (ExtraSmall, 4dp). */
private val SegmentInnerCorner: Dp = 4.dp

/** Horizontal margin of the list items from the screen edge. */
val ListSegmentHorizontalPadding: Dp = 12.dp

/**
 * One row of an M3 Expressive *segmented* list: rows sit [ListItemDefaults.SegmentedGap] (2dp)
 * apart, only the first row has large top corners and only the last row has large bottom
 * corners, the rest use small 4dp corners, so the whole list reads as one grouped surface.
 *
 * - While pressed, all four corners animate to the large radius (the Expressive pressed shape).
 * - When rows are added, removed or reordered, the corners animate to their new position.
 * - The click ripple is clipped to the row's shape (no square ripple over the rounded card).
 *
 * Use `index = 0, count = 1` for a standalone, fully rounded card (e.g. the tablet grid).
 *
 * @param onClick null makes the row non-interactive (used by the loading placeholders)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListSegment(
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFirst = index <= 0
    val isLast = index >= count - 1

    val cornerSpec = Motion.fastSpatial<Dp>()
    val topCorner by animateDpAsState(
        targetValue = if (isFirst || isPressed) SegmentOuterCorner else SegmentInnerCorner,
        animationSpec = cornerSpec,
        label = "segmentTopCorner"
    )
    val bottomCorner by animateDpAsState(
        targetValue = if (isLast || isPressed) SegmentOuterCorner else SegmentInnerCorner,
        animationSpec = cornerSpec,
        label = "segmentBottomCorner"
    )
    val shape = RoundedCornerShape(
        topStart = topCorner,
        topEnd = topCorner,
        bottomStart = bottomCorner,
        bottomEnd = bottomCorner,
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ListSegmentHorizontalPadding)
            .padding(top = if (isFirst) 0.dp else ListItemDefaults.SegmentedGap)
            // clip -> background -> clickable: the ripple is drawn inside the rounded shape
            .clip(shape)
            .background(containerColor)
            .then(
                if (onClick != null) {
                    Modifier.combinedClickable(
                        interactionSource = interactionSource,
                        indication = ripple(),
                        onClick = onClick,
                        onLongClick = onLongClick,
                    )
                } else Modifier
            )
    ) {
        content()
    }
}

// ---------------------------------------------------------------------------------------------
// Progress
// ---------------------------------------------------------------------------------------------

/**
 * Episode / chapter progress as a flat M3 Expressive bar: a 6dp rounded track, a small gap
 * between the filled and empty parts, and a stop dot at the end. Replaces the wavy bar.
 * The fill still grows smoothly when progress changes (e.g. after +1).
 *
 * @param progress 0..1
 */
@Composable
fun ListProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = Motion.spatial(),
        label = "listProgress"
    )
    LinearProgressIndicator(
        // The spring can overshoot slightly, keep it inside the track
        progress = { animatedProgress.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp),
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        gapSize = 3.dp,
    )
}

// ---------------------------------------------------------------------------------------------
// +1 buttons
// ---------------------------------------------------------------------------------------------

/**
 * Round +1 icon button for the compact, minimal and grid styles. On press it morphs from a
 * circle into a rounded square and springs back (M3 Expressive pressed shape).
 */
@Composable
fun PlusOneIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledIconButton(
        onClick = onClick,
        shapes = IconButtonShapes(
            shape = IconButtonDefaults.smallRoundShape,
            pressedShape = IconButtonDefaults.smallPressedShape,
        ),
        modifier = modifier.size(36.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = stringResource(R.string.plus_one),
            modifier = Modifier.size(20.dp)
        )
    }
}

/** "+1" pill button for the standard style, with the same pressed shape morph. */
@Composable
fun PlusOneButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        shapes = ButtonDefaults.shapes(),
        modifier = modifier
            .height(36.dp)
            .widthIn(min = 72.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.plus_one),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}
