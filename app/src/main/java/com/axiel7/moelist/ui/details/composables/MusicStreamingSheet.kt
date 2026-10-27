package com.axiel7.moelist.ui.details.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.Localizable
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openAction
import com.axiel7.moelist.utils.StringExtensions.buildQueryFromThemeText

private enum class MusicStreaming(
    val searchUrl: String,
) : Localizable {
    YouTube("https://www.youtube.com/results?search_query="),
    Spotify("https://open.spotify.com/search/"),
    AppleMusic("https://music.apple.com/search?term="),
    YouTubeMusic("https://music.youtube.com/search?q="),
    Deezer("https://www.deezer.com/search/"),
    ;

    @Composable
    override fun localized() = when (this) {
        YouTube -> "YouTube"
        Spotify -> "Spotify"
        AppleMusic -> "Apple Music"
        YouTubeMusic -> "YouTube Music"
        Deezer -> "Deezer"
    }

    val icon: Int
        @DrawableRes
        get() = when (this) {
            YouTube -> R.drawable.youtube
            Spotify -> R.drawable.spotify
            AppleMusic -> R.drawable.apple_music
            YouTubeMusic -> R.drawable.youtube_music
            Deezer -> R.drawable.deezer
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicStreamingSheet(
    songTitle: String,
    bottomPadding: Dp,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets.statusBars }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp + bottomPadding)
        ) {
            Text(
                text = stringResource(R.string.music_themes),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            MusicStreaming.entries.forEachIndexed { index, service ->
                ListItem(
                    headlineContent = {
                        Text(
                            text = service.localized(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(service.icon),
                            contentDescription = service.localized(),
                            modifier = Modifier.size(32.dp),
                            tint = Color.Unspecified
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        context.openAction(
                            service.searchUrl + songTitle.buildQueryFromThemeText()
                        )
                        onDismiss()
                    }
                )
                if (index < MusicStreaming.entries.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MusicStreamingSheetPreview() {
    MoeListTheme {
        MusicStreamingSheet(
            songTitle = "",
            bottomPadding = 0.dp,
            onDismiss = {}
        )
    }
}
