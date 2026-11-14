package com.axiel7.moelist.ui.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll

/**
 * Scaffold with a small top app bar.
 *
 * Top app bar rule for the app:
 * - Small bar (this, or a plain `TopAppBar`): screens with tabs, a day/filter selector or other
 *   controls directly under the bar (Calendar, Ranking, Recommendations, Seasonal chart, Profile).
 * - Large flexible bar ([com.axiel7.moelist.ui.composables.preferences.SettingsScaffold]):
 *   settings-like and secondary pages that are one scrolling column (Settings, Credits,
 *   Notifications). About uses SettingsScaffold with a small bar, because its own header already
 *   shows the app name large. Details uses its own large flexible bar over the backdrop.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultScaffoldWithTopAppBar(
    title: String,
    navigateBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable (() -> Unit) = {},
    contentWindowInsets: WindowInsets = WindowInsets.systemBars,
    content: @Composable (PaddingValues) -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        rememberTopAppBarState()
    )
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            DefaultTopAppBar(
                title = title,
                scrollBehavior = topAppBarScrollBehavior,
                navigateBack = navigateBack,
                actions = actions
            )
        },
        floatingActionButton = floatingActionButton,
        contentWindowInsets = contentWindowInsets,
        content = content
    )
}