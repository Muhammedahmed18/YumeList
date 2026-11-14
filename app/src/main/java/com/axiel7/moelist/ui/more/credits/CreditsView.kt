package com.axiel7.moelist.ui.more.credits

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.preferences.SettingsInfoItem
import com.axiel7.moelist.ui.composables.preferences.SettingsNavItem
import com.axiel7.moelist.ui.composables.preferences.SettingsScaffold
import com.axiel7.moelist.ui.composables.preferences.SettingsSection
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openLink
import com.axiel7.moelist.utils.GENERAL_HELP_CREDIT_URL
import com.axiel7.moelist.utils.LOGO_CREDIT_URL

val contributorsCredits = mapOf(
    "@uragiristereo" to "https://github.com/uragiristereo",
    "@krishnapandey24" to "https://github.com/krishnapandey24",
)

/** Credits, in the same grouped style as Settings and About. Every link opens outside the app. */
@Composable
fun CreditsView(
    navActionManager: NavActionManager
) {
    val context = LocalContext.current

    SettingsScaffold(
        title = stringResource(R.string.credits),
        onBack = navActionManager::goBack,
    ) {
        SettingsSection(title = stringResource(R.string.support)) {
            item {
                SettingsNavItem(
                    title = stringResource(R.string.logo_design),
                    subtitle = "@danielvd_art",
                    opensExternally = true,
                    onClick = { context.openLink(LOGO_CREDIT_URL) },
                )
            }
            item {
                SettingsNavItem(
                    title = stringResource(R.string.new_logo_design),
                    subtitle = "@WSTxda",
                    opensExternally = true,
                    onClick = { context.openLink("https://www.instagram.com/wstxda/") },
                )
            }
            item {
                SettingsNavItem(
                    title = stringResource(R.string.website),
                    subtitle = "@MaximilianGT500",
                    opensExternally = true,
                    onClick = { context.openLink("https://github.com/MaximilianGT500") },
                )
            }
            item {
                SettingsNavItem(
                    title = stringResource(R.string.general_help),
                    subtitle = "@Jeluchu",
                    opensExternally = true,
                    onClick = { context.openLink(GENERAL_HELP_CREDIT_URL) },
                )
            }
            item {
                // No link for this credit, so the row isn't tappable
                SettingsInfoItem(
                    title = stringResource(R.string.api_help),
                    subtitle = "@Glodanif",
                )
            }
        }

        SettingsSection(title = stringResource(R.string.contributors)) {
            contributorsCredits.forEach { (username, link) ->
                item {
                    SettingsNavItem(
                        title = username,
                        opensExternally = true,
                        onClick = { context.openLink(link) },
                    )
                }
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