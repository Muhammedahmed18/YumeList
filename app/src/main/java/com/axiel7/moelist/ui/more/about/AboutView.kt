package com.axiel7.moelist.ui.more.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.axiel7.moelist.BuildConfig
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.base.navigation.NavActionManager
import com.axiel7.moelist.ui.composables.DefaultScaffoldWithTopAppBar
import com.axiel7.moelist.ui.composables.preferences.PlainPreferenceView
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openAction
import com.axiel7.moelist.utils.ContextExtensions.showToast
import com.axiel7.moelist.utils.GITHUB_REPO_URL

@Composable
fun AboutView(
    navActionManager: NavActionManager
) {
    val context = LocalContext.current
    var versionClicks by remember { mutableIntStateOf(0) }

    DefaultScaffoldWithTopAppBar(
        title = stringResource(R.string.about),
        navigateBack = navActionManager::goBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            PlainPreferenceView(
                title = stringResource(R.string.version),
                subtitle = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                icon = R.drawable.ic_yumelist_logo,
                onClick = {
                    if (versionClicks >= 7) {
                        context.showToast("✧◝(⁰▿⁰)◜✧")
                        versionClicks = 0
                    } else versionClicks++
                }
            )

            PlainPreferenceView(
                title = stringResource(R.string.github),
                subtitle = stringResource(R.string.github_summary),
                icon = R.drawable.ic_github,
                onClick = {
                    context.openAction(GITHUB_REPO_URL)
                }
            )
        }
    }
}

@Preview
@Composable
fun AboutPreview() {
    MoeListTheme {
        Surface {
            AboutView(
                navActionManager = NavActionManager.rememberNavActionManager()
            )
        }
    }
}
