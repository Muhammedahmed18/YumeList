package com.axiel7.moelist.ui.more.credits

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.more.composables.MoreItem
import com.axiel7.moelist.ui.more.settings.SettingsTitle
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.GENERAL_HELP_CREDIT_URL
import com.axiel7.moelist.utils.LOGO_CREDIT_URL

val contributorsCredits = mapOf(
    "@uragiristereo" to "https://github.com/uragiristereo",
    "@krishnapandey24" to "https://github.com/krishnapandey24",
)

@Composable
fun CreditsView(
    navActionManager: NavActionManager
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.credits),
        navigateBack = navActionManager::goBack
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(it)
        ) {
            HorizontalDivider()
            SettingsTitle(text = stringResource(R.string.support))
            MoreItem(
                title = stringResource(R.string.logo_design),
                subtitle = "@danielvd_art",
                onClick = {
                    context.openLink(LOGO_CREDIT_URL)
                }
            )
            MoreItem(
                title = stringResource(R.string.new_logo_design),
                subtitle = "@WSTxda",
                onClick = {
                    context.openLink("https://www.instagram.com/wstxda/")
                }
            )
            MoreItem(
                title = stringResource(R.string.website),
                subtitle = "@MaximilianGT500",
                onClick = {
                    context.openLink("https://github.com/MaximilianGT500")
                }
            )
            MoreItem(
                title = stringResource(R.string.general_help),
                subtitle = "@Jeluchu",
                onClick = {
                    context.openLink(GENERAL_HELP_CREDIT_URL)
                }
            )
            MoreItem(
                title = stringResource(R.string.api_help),
                subtitle = "@Glodanif",
                onClick = {}
            )
            HorizontalDivider()
            SettingsTitle(text = stringResource(R.string.contributors))
            contributorsCredits.forEach { (username, link) ->
                MoreItem(
                    title = username,
                    onClick = {
                        context.openLink(link)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun CreditsPreview() {
    MoeListTheme {
        Surface {
            CreditsView(
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}