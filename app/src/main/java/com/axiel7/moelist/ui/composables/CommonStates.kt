package com.axiel7.moelist.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.filled.Info
import com.axiel7.moelist.ui.icons.filled.Warning
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.Motion

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // M3 Expressive loading indicator: a morphing shape inside a tonal container
            ContainedLoadingIndicator(
                modifier = Modifier.size(96.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.loading),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Info,
    title: String = stringResource(R.string.no_results),
    description: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    compact: Boolean = false,
) {
    val isPreview = LocalInspectionMode.current
    var visible by remember { mutableStateOf(isPreview) }
    LaunchedEffect(Unit) {
        if (!isPreview) visible = true
    }

    // Expressive shape for the icon container: a soft "cookie" for empty states
    val iconShape = MaterialShapes.Cookie9Sided.toShape()

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = Motion.effects()) + scaleIn(
            initialScale = 0.92f,
            animationSpec = Motion.spatial()
        )
    ) {
        if (compact) {
            CompactStateLayout(
                modifier = modifier,
                icon = icon,
                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                iconContainerShape = iconShape,
                title = title,
                description = description,
                actionLabel = actionLabel,
                onAction = onAction,
            )
        } else {
            FullStateLayout(
                modifier = modifier,
                icon = icon,
                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                iconContainerShape = iconShape,
                title = title,
                description = description,
                actionLabel = actionLabel,
                onAction = onAction,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ErrorState(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Warning,
    title: String = stringResource(R.string.something_went_wrong),
    message: String? = null,
    actionLabel: String = stringResource(R.string.retry),
    onAction: () -> Unit,
    compact: Boolean = false,
) {
    val isPreview = LocalInspectionMode.current
    var visible by remember { mutableStateOf(isPreview) }
    LaunchedEffect(Unit) {
        if (!isPreview) visible = true
    }

    // Expressive shape for the icon container: a "soft burst" so errors read differently
    val iconShape = MaterialShapes.SoftBurst.toShape()

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = Motion.effects()) + scaleIn(
            initialScale = 0.92f,
            animationSpec = Motion.spatial()
        )
    ) {
        if (compact) {
            CompactStateLayout(
                modifier = modifier,
                icon = icon,
                iconContainerColor = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.onErrorContainer,
                iconContainerShape = iconShape,
                title = title,
                description = message,
                actionLabel = actionLabel,
                onAction = onAction,
            )
        } else {
            FullStateLayout(
                modifier = modifier,
                icon = icon,
                iconContainerColor = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.onErrorContainer,
                iconContainerShape = iconShape,
                title = title,
                description = message,
                actionLabel = actionLabel,
                onAction = onAction,
            )
        }
    }
}

@Composable
private fun FullStateLayout(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconContainerColor: androidx.compose.ui.graphics.Color,
    iconTint: androidx.compose.ui.graphics.Color,
    iconContainerShape: Shape,
    title: String,
    description: String?,
    actionLabel: String?,
    onAction: (() -> Unit)?,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Surface(
            modifier = Modifier.size(96.dp),
            shape = iconContainerShape,
            color = iconContainerColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = iconTint
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        if (description != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            FilledTonalButton(onClick = onAction) {
                Text(text = actionLabel)
            }
        }

        Spacer(modifier = Modifier.weight(2f))
    }
}

@Composable
private fun CompactStateLayout(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconContainerColor: androidx.compose.ui.graphics.Color,
    iconTint: androidx.compose.ui.graphics.Color,
    iconContainerShape: Shape,
    title: String,
    description: String?,
    actionLabel: String?,
    onAction: (() -> Unit)?,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = iconContainerShape,
            color = iconContainerColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = iconTint
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        if (description != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(6.dp))
            TextButton(onClick = onAction) {
                Text(text = actionLabel)
            }
        }
    }
}

/**
 * Returns `true` when the given (already-localized) message matches the
 * session-expired string. Call sites can swap the retry action for a
 * "Sign in again" CTA based on this.
 */
@Composable
fun isSessionExpiredMessage(message: String?): Boolean {
    if (message == null) return false
    return message == stringResource(R.string.session_expired)
}

@Preview(showBackground = true)
@Composable
fun LoadingStatePreview() {
    MoeListTheme {
        LoadingState()
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyStatePreview() {
    MoeListTheme {
        EmptyState(
            title = "No results",
            description = "Try adjusting your filters or searching for something else.",
            actionLabel = "Clear filters",
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorStatePreview() {
    MoeListTheme {
        ErrorState(
            message = "Network error. Please check your internet connection.",
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorStateCompactPreview() {
    MoeListTheme {
        Box(modifier = Modifier.height(180.dp)) {
            ErrorState(
                message = "Can't reach MyAnimeList",
                onAction = {},
                compact = true
            )
        }
    }
}
