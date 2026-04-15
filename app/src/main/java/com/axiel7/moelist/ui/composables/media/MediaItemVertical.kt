package com.axiel7.moelist.ui.composables.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.composables.defaultPlaceholder
import com.axiel7.moelist.ui.theme.MoeListTheme

const val MEDIA_ITEM_VERTICAL_HEIGHT = 200

@Composable
fun MediaItemVertical(
    title: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    badgeContent: @Composable (RowScope.() -> Unit)? = null,
    subtitle: @Composable (() -> Unit)? = null,
    subtitle2: @Composable (() -> Unit)? = null,
    minLines: Int = 1,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .width(MEDIA_POSTER_SMALL_WIDTH.dp)
            .sizeIn(
                minHeight = MEDIA_ITEM_VERTICAL_HEIGHT.dp
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier.padding(bottom = 8.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            MediaPoster(
                url = imageUrl,
                showShadow = false,
                modifier = Modifier
                    .size(
                        width = MEDIA_POSTER_SMALL_WIDTH.dp,
                        height = MEDIA_POSTER_SMALL_HEIGHT.dp
                    )
            )

            if (badgeContent != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topEnd = 8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    content = badgeContent
                )
            }
        }

        Text(
            text = title,
            modifier = Modifier
                .width(MEDIA_POSTER_SMALL_WIDTH.dp),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            minLines = minLines
        )

        Column(
            modifier = Modifier.padding(top = 2.dp),
        ) {
            subtitle?.let { it() }
            subtitle2?.let { it() }
        }
    }
}

@Composable
fun MediaItemVerticalPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .size(
                width = MEDIA_POSTER_SMALL_WIDTH.dp,
                height = MEDIA_ITEM_VERTICAL_HEIGHT.dp
            ),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .size(
                    width = MEDIA_POSTER_SMALL_WIDTH.dp,
                    height = MEDIA_POSTER_SMALL_HEIGHT.dp
                )
                .clip(RoundedCornerShape(8.dp))
                .defaultPlaceholder(visible = true)
        )

        Text(
            text = "Loading Title",
            modifier = Modifier
                .padding(top = 8.dp)
                .defaultPlaceholder(visible = true),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 2
        )
    }
}

@Preview
@Composable
fun MediaItemVerticalPreview() {
    MoeListTheme {
        Surface {
            Row(modifier = Modifier.padding(16.dp)) {
                MediaItemVertical(
                    imageUrl = null,
                    title = "This is a very large anime title that should serve as a preview example",
                    badgeContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_star_16),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "8.34",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    },
                    subtitle = {
                        Text(
                            text = "TV • 12 Eps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = {}
                )
            }
        }
    }
}