package com.axiel7.moelist.ui.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.more.composables.MoreItem
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openCustomTab
import com.axiel7.moelist.utils.MAL_ANNOUNCEMENTS_URL
import com.axiel7.moelist.utils.MAL_NEWS_URL
import org.koin.androidx.compose.koinViewModel

@Composable
fun MoreView(
    isLoggedIn: Boolean,
    navActionManager: NavActionManager,
    padding: PaddingValues,
) {
    val viewModel: MoreViewModel = koinViewModel()

    MoreViewContent(
        event = viewModel,
        navActionManager = navActionManager,
        padding = padding,
        isLoggedIn = isLoggedIn
    )
}

@Composable
private fun MoreViewContent(
    isLoggedIn: Boolean,
    event: MoreEvent?,
    navActionManager: NavActionManager,
    padding: PaddingValues = PaddingValues(),
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(padding)
    ) {
        Column(
            modifier = Modifier.padding(
                start = 24.dp,
                end = 24.dp,
                top = 24.dp,
                bottom = 8.dp
            )
        ) {
            Text(
                text = stringResource(R.string.more),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.more_info),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Discover: external news links
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            MoreItem(
                title = stringResource(R.string.anime_manga_news),
                subtitle = stringResource(R.string.news_summary),
                icon = R.drawable.ic_new_releases,
                onClick = { context.openCustomTab(MAL_NEWS_URL) }
            )
            MoreItem(
                title = stringResource(R.string.mal_announcements),
                subtitle = stringResource(R.string.mal_announcements_summary),
                icon = R.drawable.ic_campaign,
                onClick = { context.openCustomTab(MAL_ANNOUNCEMENTS_URL) }
            )
        }

        Spacer(Modifier.height(12.dp))

        // App navigation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            MoreItem(
                title = stringResource(R.string.notifications),
                icon = R.drawable.round_notifications_24,
                onClick = dropUnlessResumed { navActionManager.toNotifications() }
            )
            MoreItem(
                title = stringResource(R.string.settings),
                icon = R.drawable.ic_round_settings_24,
                onClick = dropUnlessResumed { navActionManager.toSettings() }
            )
            MoreItem(
                title = stringResource(R.string.about),
                icon = R.drawable.ic_info,
                onClick = dropUnlessResumed { navActionManager.toAbout() }
            )
        }

        if (isLoggedIn) {
            Spacer(Modifier.height(12.dp))

            // Logout — standalone destructive card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                MoreItem(
                    title = stringResource(R.string.logout),
                    subtitle = stringResource(R.string.logout_summary),
                    icon = R.drawable.ic_round_power_settings_new_24,
                    titleColor = MaterialTheme.colorScheme.onErrorContainer,
                    iconContainerColor = MaterialTheme.colorScheme.error,
                    iconTint = MaterialTheme.colorScheme.onError,
                    showTrailingArrow = false,
                    onClick = { event?.logOut() }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Preview
@Composable
fun MorePreview() {
    MoeListTheme {
        Surface {
            MoreViewContent(
                event = null,
                navActionManager = NavActionManager.rememberNavActionManager(),
                isLoggedIn = true
            )
        }
    }
}
