package com.axiel7.moelist.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.theme.MoeListTheme

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
            Surface(
                modifier = Modifier.size(112.dp),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                tonalElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 5.dp,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

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

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(500)
        )
    ) {
        if (compact) {
            CompactStateLayout(
                modifier = modifier,
                icon = icon,
                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
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
                title = title,
                description = description,
                actionLabel = actionLabel,
                onAction = onAction,
            )
        }
    }
}

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

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(500)
        )
    ) {
        if (compact) {
            CompactStateLayout(
                modifier = modifier,
                icon = icon,
                iconContainerColor = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.onErrorContainer,
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
            shape = RoundedCornerShape(32.dp),
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
            shape = RoundedCornerShape(18.dp),
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
