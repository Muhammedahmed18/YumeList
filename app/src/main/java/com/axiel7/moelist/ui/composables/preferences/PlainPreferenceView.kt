package com.axiel7.moelist.ui.composables.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlainPreferenceView(
    title: String,
    titleTint: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: Any? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    iconPadding: PaddingValues = PaddingValues(16.dp),
    useTonalContainer: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                if (useTonalContainer) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when (icon) {
                            is ImageVector -> Icon(
                                imageVector = icon,
                                contentDescription = title,
                                modifier = Modifier.size(20.dp),
                                tint = if (enabled) iconTint else iconTint.copy(alpha = 0.38f)
                            )
                            is Int -> Icon(
                                painter = painterResource(icon),
                                contentDescription = title,
                                modifier = Modifier.size(20.dp),
                                tint = if (enabled) iconTint else iconTint.copy(alpha = 0.38f)
                            )
                        }
                    }
                } else {
                    when (icon) {
                        is ImageVector -> Icon(
                            imageVector = icon,
                            contentDescription = title,
                            modifier = Modifier.padding(iconPadding),
                            tint = if (enabled) iconTint else iconTint.copy(alpha = 0.38f)
                        )
                        is Int -> Icon(
                            painter = painterResource(icon),
                            contentDescription = title,
                            modifier = Modifier.padding(iconPadding),
                            tint = if (enabled) iconTint else iconTint.copy(alpha = 0.38f)
                        )
                    }
                }
            } else {
                Spacer(
                    modifier = Modifier
                        .padding(iconPadding)
                        .size(24.dp)
                )
            }

            Column(
                modifier = if (subtitle != null)
                    Modifier.padding(16.dp)
                else Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    color = if (enabled) titleTint else titleTint.copy(alpha = 0.38f)
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
