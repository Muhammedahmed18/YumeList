package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.MediaSort.Companion.animeListSortItems
import com.axiel7.moelist.data.model.media.MediaSort.Companion.mangaListSortItems
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.Motion
import com.axiel7.moelist.ui.userlist.UserMediaListEvent
import com.axiel7.moelist.ui.userlist.UserMediaListUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Grouped-list corner tokens
private val RowMinHeight = 64.dp
private val OuterCorner = 24.dp       // top of first row / bottom of last row
private val InnerCorner = 6.dp        // corners between connected rows
private val PressedCorner = 14.dp     // squish while pressed
private val SelectedCorner = RowMinHeight / 2 // full pill

private val IconContainerSize = 40.dp

private const val DismissDelayMillis = 220L
private const val StaggerDelayMillis = 25L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaListSortDialog(
    uiState: UserMediaListUiState,
    event: UserMediaListEvent?,
) {
    val sortOptions = remember(uiState.mediaType) {
        if (uiState.mediaType == MediaType.ANIME) animeListSortItems.toList()
        else mangaListSortItems.toList()
    }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var isClosing by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { event?.toggleSortDialog(false) },
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    ) {
        MediaListSortContent(
            sortOptions = sortOptions,
            selectedSort = uiState.listSort,
            enabled = !isClosing,
            onSortClick = { sort ->
                if (!isClosing) {
                    isClosing = true
                    event?.onChangeSort(sort)
                    // Let the shape morph + check draw play before the sheet closes
                    scope.launch {
                        delay(DismissDelayMillis)
                        sheetState.hide()
                    }.invokeOnCompletion {
                        event?.toggleSortDialog(false)
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaListSortContent(
    sortOptions: List<MediaSort>,
    selectedSort: MediaSort?,
    onSortClick: (MediaSort) -> Unit,
    enabled: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.sort_by),
            style = MaterialTheme.typography.titleLargeEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp).padding(top = 4.dp)
        )
        if (selectedSort != null) {
            Text(
                text = stringResource(R.string.sort_current, selectedSort.localized()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp).padding(top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            sortOptions.forEachIndexed { index, sort ->
                SortOptionRow(
                    sort = sort,
                    isSelected = selectedSort == sort,
                    isFirst = index == 0,
                    isLast = index == sortOptions.lastIndex,
                    index = index,
                    enabled = enabled,
                    onClick = { onSortClick(sort) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SortOptionRow(
    sort: MediaSort,
    isSelected: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    index: Int,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Staggered entry: each row springs up slightly after the previous one.
    // The spec is read here, in composition, because Motion reads the theme.
    val entrySpec: AnimationSpec<Float> = Motion.fastSpatial()
    val entry = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * StaggerDelayMillis)
        entry.animateTo(1f, animationSpec = entrySpec)
    }

    // Position-aware corners that morph to a full pill when selected
    fun cornerTarget(isOuterEdge: Boolean) = when {
        isSelected -> SelectedCorner
        isPressed -> PressedCorner
        isOuterEdge -> OuterCorner
        else -> InnerCorner
    }
    val topCorner by animateDpAsState(
        targetValue = cornerTarget(isFirst),
        animationSpec = Motion.fastSpatial(),
        label = "topCorner"
    )
    val bottomCorner by animateDpAsState(
        targetValue = cornerTarget(isLast),
        animationSpec = Motion.fastSpatial(),
        label = "bottomCorner"
    )
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = Motion.fastSpatial(),
        label = "pressScale"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = Motion.fastEffects(),
        label = "containerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = Motion.fastEffects(),
        label = "contentColor"
    )

    val shape = RoundedCornerShape(
        topStart = topCorner,
        topEnd = topCorner,
        bottomStart = bottomCorner,
        bottomEnd = bottomCorner
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                val progress = entry.value
                alpha = progress.coerceIn(0f, 1f)
                translationY = (1f - progress) * 24.dp.toPx()
                scaleX = pressScale
                scaleY = pressScale
            }
            .heightIn(min = RowMinHeight)
            .clip(shape)
            .background(containerColor)
            .selectable(
                selected = isSelected,
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                role = Role.RadioButton,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    onClick()
                }
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SortIcon(
            painter = sort.getIconPainter(),
            isSelected = isSelected
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = sort.localized(),
            style = if (isSelected) MaterialTheme.typography.bodyLargeEmphasized
            else MaterialTheme.typography.bodyLarge,
            color = contentColor,
            modifier = Modifier.weight(1f)
        )

        AnimatedCheck(
            visible = isSelected,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Leading icon in a container that morphs from a circle into the Material
 * 4-sided cookie shape (with a quarter spin) when selected. The icon stays upright.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SortIcon(
    painter: Painter,
    isSelected: Boolean,
) {
    val morph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Cookie4Sided) }

    val morphProgress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = Motion.fastSpatial(),
        label = "iconMorph"
    )
    val rotation by animateFloatAsState(
        targetValue = if (isSelected) 90f else 0f,
        animationSpec = Motion.fastSpatial(),
        label = "iconRotation"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = Motion.fastEffects(),
        label = "iconContainerColor"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = Motion.fastEffects(),
        label = "iconColor"
    )

    Box(
        modifier = Modifier.size(IconContainerSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(IconContainerSize)
                .graphicsLayer { rotationZ = rotation }
                // Springs overshoot; keep the morph inside its valid 0..1 range
                .clip(MorphShape(morph, morphProgress.coerceIn(0f, 1f)))
                .background(containerColor)
        )
        Icon(
            painter = painter,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

/** Shape that draws a [Morph] at a given progress, scaled to the layout size. */
private class MorphShape(
    private val morph: Morph,
    private val progress: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        // MaterialShapes are normalized to a 0..1 box, so scale to the layout size
        val path = morph.toPath(progress).asComposePath()
        path.transform(Matrix().apply { scale(size.width, size.height) })
        return Outline.Generic(path)
    }
}

/** A checkmark whose stroke draws itself in when [visible] becomes true. */
@Composable
private fun AnimatedCheck(
    visible: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (visible) 280 else 120,
            easing = FastOutSlowInEasing
        ),
        label = "checkProgress"
    )
    val path = remember { Path() }
    val segment = remember { Path() }
    val measure = remember { PathMeasure() }

    Canvas(modifier = modifier) {
        if (progress <= 0f) return@Canvas
        val w = size.width
        val h = size.height
        path.reset()
        path.moveTo(w * 0.20f, h * 0.52f)
        path.lineTo(w * 0.42f, h * 0.72f)
        path.lineTo(w * 0.80f, h * 0.30f)

        measure.setPath(path, false)
        segment.reset()
        measure.getSegment(0f, measure.length * progress, segment, true)

        drawPath(
            path = segment,
            color = color,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
private fun MediaSort.getIconPainter(): Painter {
    return when (this) {
        MediaSort.ANIME_TITLE, MediaSort.MANGA_TITLE -> painterResource(R.drawable.round_title_24)
        MediaSort.SCORE, MediaSort.ANIME_SCORE -> painterResource(R.drawable.ic_round_details_star_24)
        MediaSort.UPDATED -> painterResource(R.drawable.ic_history_24)
        MediaSort.ANIME_START_DATE, MediaSort.MANGA_START_DATE -> painterResource(R.drawable.round_calendar_today_24)
        MediaSort.ANIME_NUM_USERS -> painterResource(R.drawable.ic_round_group_24)
    }
}

@Preview(showBackground = true)
@Composable
fun MediaListSortDialogPreview() {
    MoeListTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            MediaListSortContent(
                sortOptions = animeListSortItems.toList(),
                selectedSort = MediaSort.SCORE,
                onSortClick = {}
            )
        }
    }
}
