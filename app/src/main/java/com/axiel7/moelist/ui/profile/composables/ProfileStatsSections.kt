package com.axiel7.moelist.ui.profile.composables

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.composables.RollingNumberText
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.outlined.CloudOff
import com.axiel7.moelist.ui.theme.Motion

/*
 * Profile list statuses as cards (replaces the old stacked bar + legend and the Activity tiles):
 *
 *   ┌──────────────┐┌──────────────┐
 *   │ Watching     ││ Plan to watch│   two large cards: what you're on now and what's next
 *   └──────────────┘└──────────────┘
 *   ┌────────┐┌────────┐┌──────────┐
 *   │Completed││On hold││ Dropped  │   three smaller, neutral cards
 *   └────────┘└────────┘└──────────┘
 *
 * The card heights come from the screen (see ProfileView), so the page fills a tall phone and
 * still fits a small one without scrolling. Every card opens its list.
 */

/** Gap between cards. */
val ProfileCardGap = 8.dp

/** Card corners at rest, and tightened while pressed (M3 Expressive pressed shape). */
private val CardCorner = 28.dp
private val CardPressedCorner = 16.dp

/** The statuses in display order: the two main ones first, then the other three. */
fun profileStatuses(mediaType: MediaType): List<ListStatus> =
    if (mediaType == MediaType.ANIME) {
        listOf(
            ListStatus.WATCHING, ListStatus.PLAN_TO_WATCH,
            ListStatus.COMPLETED, ListStatus.ON_HOLD, ListStatus.DROPPED
        )
    } else {
        listOf(
            ListStatus.READING, ListStatus.PLAN_TO_READ,
            ListStatus.COMPLETED, ListStatus.ON_HOLD, ListStatus.DROPPED
        )
    }

/**
 * The five status cards for one media type.
 *
 * - Loading: the same cards, with placeholder bars where the counts go (nothing jumps on load).
 * - Error: one card of the same total height with a Retry button.
 */
@Composable
fun ProfileStatusCards(
    mediaType: MediaType,
    counts: Map<ListStatus, Int>,
    isLoading: Boolean,
    isError: Boolean,
    largeCardHeight: Dp,
    smallCardHeight: Dp,
    onRetry: () -> Unit,
    onStatusClick: (ListStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isError && !isLoading) {
        StatusCardsError(
            height = largeCardHeight + ProfileCardGap + smallCardHeight,
            onRetry = onRetry,
            modifier = modifier,
        )
        return
    }

    val statuses = profileStatuses(mediaType)
    val main = statuses.take(2)
    val others = statuses.drop(2)
    val mainColors = listOf(
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer,
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ProfileCardGap)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ProfileCardGap)
        ) {
            main.forEachIndexed { index, status ->
                val (container, content) = mainColors[index]
                LargeStatusCard(
                    status = status,
                    count = counts[status] ?: 0,
                    isLoading = isLoading,
                    containerColor = container,
                    contentColor = content,
                    onClick = { onStatusClick(status) },
                    modifier = Modifier
                        .weight(1f)
                        // Minimum, not fixed: a large font makes the card grow instead of clip
                        .heightIn(min = largeCardHeight),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ProfileCardGap)
        ) {
            others.forEach { status ->
                SmallStatusCard(
                    status = status,
                    count = counts[status] ?: 0,
                    isLoading = isLoading,
                    onClick = { onStatusClick(status) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = smallCardHeight),
                )
            }
        }
    }
}

/** Card surface whose corners tighten while pressed, like the Home shortcut tiles. */
@Composable
private fun PressableStatusCard(
    onClick: () -> Unit,
    enabled: Boolean,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val corner by animateDpAsState(
        targetValue = if (isPressed) CardPressedCorner else CardCorner,
        animationSpec = Motion.fastSpatial(),
        label = "statusCardCorner"
    )
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(corner),
        color = containerColor,
        contentColor = contentColor,
        interactionSource = interactionSource,
        modifier = modifier,
        content = content,
    )
}

/** Watching / Plan to watch: big count on a coloured container. */
@Composable
private fun LargeStatusCard(
    status: ListStatus,
    count: Int,
    isLoading: Boolean,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PressableStatusCard(
        onClick = onClick,
        enabled = !isLoading,
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = status.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            // Pushes the count and label to the bottom of the card, whatever its height
            Spacer(modifier = Modifier.weight(1f))
            StatusCount(count = count, isLoading = isLoading, large = true)
            Text(
                text = status.localized(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Completed / On hold / Dropped: smaller, neutral, icon in the status colour. */
@Composable
private fun SmallStatusCard(
    status: ListStatus,
    count: Int,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PressableStatusCard(
        onClick = onClick,
        enabled = !isLoading,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = status.icon,
                contentDescription = null,
                tint = status.primaryColor(),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            StatusCount(count = count, isLoading = isLoading, large = false)
            Text(
                text = status.localized(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** The count (rolls when switching Anime / Manga), a placeholder while loading, dimmed at 0. */
@Composable
private fun StatusCount(
    count: Int,
    isLoading: Boolean,
    large: Boolean,
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .size(width = if (large) 56.dp else 32.dp, height = if (large) 36.dp else 22.dp)
                .defaultPlaceholder(visible = true)
        )
    } else {
        val contentColor = LocalContentColor.current
        RollingNumberText(
            targetValue = count,
            style = if (large) MaterialTheme.typography.displaySmall
            else MaterialTheme.typography.titleLarge,
            fontWeight = if (large) FontWeight.Black else FontWeight.Bold,
            color = if (count == 0) contentColor.copy(alpha = 0.5f) else contentColor,
        )
    }
}

/** Replaces all five cards when the stats can't be loaded. */
@Composable
private fun StatusCardsError(
    height: Dp,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(CardCorner),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = stringResource(R.string.something_went_wrong),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            TextButton(onClick = onRetry) {
                Text(text = stringResource(R.string.retry))
            }
        }
    }
}
