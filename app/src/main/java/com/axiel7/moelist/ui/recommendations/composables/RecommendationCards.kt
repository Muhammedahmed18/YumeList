package com.axiel7.moelist.ui.recommendations.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.recommendation.RecommendedMedia
import com.axiel7.moelist.ui.composables.LocalSnackbarHostState
import com.axiel7.moelist.ui.composables.SnackbarType
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_HEIGHT
import com.axiel7.moelist.ui.composables.media.MEDIA_POSTER_SMALL_WIDTH
import com.axiel7.moelist.ui.composables.media.MediaItemVertical
import com.axiel7.moelist.ui.composables.media.MediaPoster
import com.axiel7.moelist.ui.composables.media.MetaTag
import com.axiel7.moelist.ui.composables.media.PosterStatusBadge
import com.axiel7.moelist.ui.composables.showAppSnackbar
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.rounded.PlaylistAdd
import com.axiel7.moelist.ui.icons.rounded.AutoAwesome
import com.axiel7.moelist.ui.icons.rounded.Block
import com.axiel7.moelist.ui.icons.rounded.Close
import com.axiel7.moelist.ui.theme.ShapeCard
import kotlinx.coroutines.launch

/** Height of a Home / group row of [RecommendationPosterCard]s (poster, 2-line title, reason). */
const val RECOMMENDATION_ROW_HEIGHT = 256

/**
 * One-time result of a quick action on a recommendation, turned into a snackbar by
 * [RecommendationActionSnackbar].
 */
@Immutable
data class RecommendationActionResult(
    val kind: Kind,
    val item: RecommendedMedia,
    val errorMessage: String? = null,
) {
    enum class Kind {
        /** Added to Plan to Watch / Plan to Read */
        ADDED_TO_PLAN,

        /** Marked "Not interested" (the snackbar offers Undo) */
        HIDDEN,

        /** The action failed: [errorMessage] says why */
        FAILED,
    }
}

/** Shows [result] as a snackbar once, then calls [onShown]. Undo of a hide calls [onUndoHide]. */
@Composable
fun RecommendationActionSnackbar(
    result: RecommendationActionResult?,
    onShown: () -> Unit,
    onUndoHide: (RecommendedMedia) -> Unit,
) {
    val snackbarHostState = LocalSnackbarHostState.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val addedToPtw = stringResource(R.string.added_to_plan_to_watch)
    val addedToPtr = stringResource(R.string.added_to_plan_to_read)
    val hiddenText = stringResource(R.string.recommendation_hidden)
    val undoText = stringResource(R.string.undo)
    val currentOnUndoHide by rememberUpdatedState(onUndoHide)

    LaunchedEffect(result) {
        val current = result ?: return@LaunchedEffect
        onShown()
        haptic.performHapticFeedback(
            if (current.kind == RecommendationActionResult.Kind.FAILED) HapticFeedbackType.Reject
            else HapticFeedbackType.Confirm
        )
        // Own scope: clearing the result restarts this effect, which must not cancel the snackbar
        scope.launch {
            val title = current.item.userPreferredTitle()
            when (current.kind) {
                RecommendationActionResult.Kind.ADDED_TO_PLAN -> snackbarHostState.showAppSnackbar(
                    message = if (current.item.mediaType == MediaType.ANIME) addedToPtw else addedToPtr,
                    type = SnackbarType.SUCCESS,
                    highlight = title,
                )

                RecommendationActionResult.Kind.HIDDEN -> {
                    val action = snackbarHostState.showAppSnackbar(
                        message = hiddenText,
                        type = SnackbarType.INFO,
                        highlight = title,
                        actionLabel = undoText,
                    )
                    if (action == SnackbarResult.ActionPerformed) currentOnUndoHide(current.item)
                }

                RecommendationActionResult.Kind.FAILED -> snackbarHostState.showAppSnackbar(
                    message = current.errorMessage.orEmpty(),
                    type = SnackbarType.ERROR,
                    highlight = title,
                )
            }
        }
    }
}

/** "Because you liked …" line under a recommendation. */
@Composable
fun RecommendationReason(
    reason: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(12.dp),
        )
        Text(
            text = stringResource(R.string.because_you_liked, reason),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Poster card for horizontal rows (Home's For You, the "Because you liked" rows).
 * Long press opens Add to Plan / Not interested.
 *
 * @param showReason show the "Because you liked …" line (off inside a group, whose header
 * already says it)
 */
@Composable
fun RecommendationPosterCard(
    item: RecommendedMedia,
    posterKey: String,
    status: ListStatus?,
    hideScore: Boolean,
    showReason: Boolean,
    onClick: () -> Unit,
    onAddToPlan: () -> Unit,
    onNotInterested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val reason = if (showReason) item.reasonPreferredTitle() else null

    Box(modifier = modifier) {
        MediaItemVertical(
            title = item.userPreferredTitle(),
            imageUrl = item.picture,
            posterKey = posterKey,
            status = status,
            score = item.mean.takeUnless { hideScore },
            subtitle = if (reason != null) {
                { RecommendationReason(reason = reason) }
            } else null,
            minLines = 2,
            onClick = onClick,
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                menuOpen = true
            },
        )
        RecommendationMenu(
            expanded = menuOpen,
            onDismiss = { menuOpen = false },
            mediaType = item.mediaType,
            canAddToPlan = status == null,
            onAddToPlan = onAddToPlan,
            onNotInterested = onNotInterested,
        )
    }
}

/** Long-press menu of a recommendation. */
@Composable
private fun RecommendationMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    mediaType: MediaType,
    canAddToPlan: Boolean,
    onAddToPlan: () -> Unit,
    onNotInterested: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        if (canAddToPlan) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(
                            if (mediaType == MediaType.ANIME) R.string.add_to_plan_to_watch
                            else R.string.add_to_plan_to_read
                        )
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd, contentDescription = null)
                },
                onClick = {
                    onDismiss()
                    onAddToPlan()
                },
            )
        }
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.not_interested)) },
            leadingIcon = { Icon(imageVector = Icons.Rounded.Block, contentDescription = null) },
            onClick = {
                onDismiss()
                onNotInterested()
            },
        )
    }
}

/**
 * Full-width card for the Recommendations screen: poster, title, reason, format and how many
 * MAL users recommended it, with Add to Plan and Not interested buttons.
 */
@Composable
fun RecommendationListItem(
    item: RecommendedMedia,
    posterKey: String,
    status: ListStatus?,
    hideScore: Boolean,
    onClick: () -> Unit,
    onAddToPlan: () -> Unit,
    onNotInterested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        shape = ShapeCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(modifier = Modifier.height(MEDIA_POSTER_SMALL_HEIGHT.dp)) {
            Box(
                modifier = Modifier.size(
                    width = MEDIA_POSTER_SMALL_WIDTH.dp,
                    height = MEDIA_POSTER_SMALL_HEIGHT.dp
                )
            ) {
                MediaPoster(
                    url = item.picture,
                    showShadow = false,
                    sharedKey = posterKey,
                    modifier = Modifier.fillMaxSize()
                )
                if (status != null) {
                    PosterStatusBadge(
                        status = status,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
            ) {
                Text(
                    text = item.userPreferredTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
                item.reasonPreferredTitle()?.let { RecommendationReason(reason = it) }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.mediaFormat?.let { MetaTag(text = it.localized()) }
                    val mean = item.mean
                    if (!hideScore && mean != null && mean > 0f) {
                        MetaTag(text = "★ $mean")
                    }
                    if (item.numRecommendations > 0) {
                        MetaTag(
                            text = pluralStringResource(
                                R.plurals.num_recommended_by,
                                item.numRecommendations,
                                item.numRecommendations
                            )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onNotInterested) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.not_interested),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (status == null) {
                    IconButton(onClick = onAddToPlan) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd,
                            contentDescription = stringResource(
                                if (item.mediaType == MediaType.ANIME) R.string.add_to_plan_to_watch
                                else R.string.add_to_plan_to_read
                            ),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
