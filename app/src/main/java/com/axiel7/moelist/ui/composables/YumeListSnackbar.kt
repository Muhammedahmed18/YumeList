package com.axiel7.moelist.ui.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.Check
import com.axiel7.moelist.ui.icons.rounded.Info
import com.axiel7.moelist.ui.icons.rounded.PriorityHigh
import com.axiel7.moelist.ui.theme.Motion
import kotlinx.coroutines.delay

/*
 * The app's one snackbar.
 *
 * - Three types ([SnackbarType]) with their own icon badge, so an error never shows a check.
 * - Optional action (Undo, Retry...), swipe to dismiss, and an optional highlighted part
 *   ("Updated · *Frieren*") passed as its own field.
 * - Real durations (short for info / success, long for errors and anything with an action),
 *   stretched by Android's accessibility timeout setting.
 * - Shown by [AppSnackbarHost]; screens can lift it above their own bottom controls with
 *   [ProvideSnackbarOffset].
 */

enum class SnackbarType { SUCCESS, ERROR, INFO }

class AppSnackbarVisuals(
    override val message: String,
    val type: SnackbarType,
    /** Shown after the message as " · highlight", in bold accent colour (e.g. a title). */
    val highlight: String? = null,
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration =
        if (actionLabel != null || type == SnackbarType.ERROR) SnackbarDuration.Long
        else SnackbarDuration.Short,
) : SnackbarVisuals {
    override val withDismissAction: Boolean = false
}

/**
 * Shows a snackbar, replacing the one on screen (no queue of stale messages).
 * Suspends until it's dismissed; returns [SnackbarResult.ActionPerformed] if the action was tapped.
 */
suspend fun SnackbarHostState.showAppSnackbar(
    message: String,
    type: SnackbarType = SnackbarType.INFO,
    highlight: String? = null,
    actionLabel: String? = null,
): SnackbarResult {
    currentSnackbarData?.dismiss()
    return showSnackbar(
        AppSnackbarVisuals(
            message = message,
            type = type,
            highlight = highlight,
            actionLabel = actionLabel,
        )
    )
}
// ---------------------------------------------------------------------------------------------
// Offset: lets a screen lift the snackbar above its own bottom controls
// ---------------------------------------------------------------------------------------------

/** Extra bottom space requested by the screens on screen; the newest request wins. */
@Stable
class SnackbarOffsetState {
    private val requests = mutableStateListOf<Pair<Any, Dp>>()

    val bottom: Dp get() = requests.lastOrNull()?.second ?: 0.dp

    internal fun add(owner: Any, offset: Dp) {
        requests.removeAll { it.first === owner }
        requests.add(owner to offset)
    }

    internal fun remove(owner: Any) {
        requests.removeAll { it.first === owner }
    }
}

val LocalSnackbarOffset = staticCompositionLocalOf { SnackbarOffsetState() }

/**
 * While this is in the composition, snackbars sit [bottom] higher (e.g. above a floating
 * toolbar). Removed automatically when the screen leaves; safe during screen transitions.
 */
@Composable
fun ProvideSnackbarOffset(bottom: Dp) {
    val state = LocalSnackbarOffset.current
    DisposableEffect(state, bottom) {
        val owner = Any()
        state.add(owner, bottom)
        onDispose { state.remove(owner) }
    }
}

// ---------------------------------------------------------------------------------------------
// Host
// ---------------------------------------------------------------------------------------------

/**
 * Replaces Material's SnackbarHost: handles the timeout (with the accessibility multiplier),
 * and slides the snackbar up on the expressive spring, fading out on dismiss.
 */
@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val current = hostState.currentSnackbarData
    val accessibilityManager = LocalAccessibilityManager.current

    LaunchedEffect(current) {
        if (current != null) {
            val millis = when (current.visuals.duration) {
                SnackbarDuration.Short -> 4_000L
                SnackbarDuration.Long -> 10_000L
                SnackbarDuration.Indefinite -> null
            }
            if (millis != null) {
                val timeout = accessibilityManager?.calculateRecommendedTimeoutMillis(
                    originalTimeoutMillis = millis,
                    containsIcons = true,
                    containsText = true,
                    containsControls = current.visuals.actionLabel != null,
                ) ?: millis
                delay(timeout)
                current.dismiss()
            }
        }
    }

    val slideIn = Motion.spatial<IntOffset>()
    val fade = Motion.fastEffects<Float>()

    AnimatedContent(
        targetState = current,
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
        transitionSpec = {
            (slideInVertically(slideIn) { it } + fadeIn(fade)) togetherWith
                (slideOutVertically { it / 2 } + fadeOut(fade)) using
                SizeTransform(clip = false)
        },
        label = "appSnackbar"
    ) { data ->
        if (data != null) {
            SwipeableSnackbar(data)
        } else {
            Box(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SwipeableSnackbar(data: SnackbarData) {
    val swipeState = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = swipeState,
        backgroundContent = {},
        onDismiss = { data.dismiss() },
    ) {
        AppSnackbar(data = data)
    }
}

// ---------------------------------------------------------------------------------------------
// Snackbar
// ---------------------------------------------------------------------------------------------

/**
 * M3 Expressive snackbar: inverse surface (dark tonal on AMOLED, so it isn't a bright slab),
 * type badge, message with optional highlight, optional action. Rounded 20dp corners still look
 * right when the text wraps.
 */
@Composable
fun AppSnackbar(
    data: SnackbarData,
    modifier: Modifier = Modifier,
) {
    val visuals = data.visuals as? AppSnackbarVisuals
    val type = visuals?.type ?: SnackbarType.INFO
    val colors = MaterialTheme.colorScheme
    val isAmoled = colors.surface == Color.Black

    val container = if (isAmoled) colors.surfaceContainerHighest else colors.inverseSurface
    val content = if (isAmoled) colors.onSurface else colors.inverseOnSurface
    val accent = if (isAmoled) colors.primary else colors.inversePrimary

    val (badgeContainer, badgeContent, badgeIcon) = when (type) {
        SnackbarType.SUCCESS -> Triple(colors.primaryContainer, colors.onPrimaryContainer, Icons.Rounded.Check)
        SnackbarType.ERROR -> Triple(colors.errorContainer, colors.onErrorContainer, Icons.Rounded.PriorityHigh)
        SnackbarType.INFO -> Triple(colors.secondaryContainer, colors.onSecondaryContainer, Icons.Rounded.Info)
    }

    val text = buildAnnotatedString {
        append(data.visuals.message)
        val highlight = visuals?.highlight
        if (!highlight.isNullOrBlank()) {
            append(" · ")
            withStyle(SpanStyle(color = accent, fontWeight = FontWeight.Bold)) {
                append(highlight)
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .widthIn(max = 600.dp)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                dismiss {
                    data.dismiss()
                    true
                }
            },
        shape = RoundedCornerShape(20.dp),
        color = container,
        contentColor = content,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.padding(
                start = 12.dp,
                end = if (data.visuals.actionLabel != null) 8.dp else 16.dp,
                top = 8.dp,
                bottom = 8.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TypeBadge(icon = badgeIcon, container = badgeContainer, content = badgeContent)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp)
            )
            data.visuals.actionLabel?.let { label ->
                TextButton(
                    onClick = { data.performAction() },
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.textButtonColors(contentColor = accent),
                ) {
                    Text(text = label, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TypeBadge(icon: ImageVector, container: Color, content: Color) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(18.dp)
        )
    }
}
