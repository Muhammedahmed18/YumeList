package com.axiel7.moelist.ui.login

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.composables.preferences.SettingsGroup
import com.axiel7.moelist.ui.composables.preferences.SettingsSwitchItem
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.OpenInBrowser
import com.axiel7.moelist.ui.main.MainViewModel
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.utils.ContextExtensions.openCustomTab
import com.axiel7.moelist.utils.ContextExtensions.showToast
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Shown on the Anime / Manga tabs and the profile while logged out.
 *
 * M3 Expressive: the logo in a scalloped shape, an emphasized title, a large login button with
 * the press morph (it shows a loading indicator while the login link is being prepared, so it
 * can't be tapped twice), and "use external browser" as a grouped switch.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = koinViewModel<MainViewModel>()
    val scope = rememberCoroutineScope()
    var useExternalBrowser by rememberSaveable { mutableStateOf(false) }
    var isOpening by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                // Scrolls in landscape / with large fonts instead of cutting off the button
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(MaterialShapes.Cookie9Sided.toShape())
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_yumelist_logo),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = stringResource(R.string.login_welcome),
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.please_login_to_use_this_feature),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                LoginButton(
                    isLoading = isOpening,
                    onClick = {
                        isOpening = true
                        scope.launch {
                            try {
                                val loginUrl = viewModel.generateLoginUrl()
                                context.openLoginUrl(
                                    loginUrl = loginUrl,
                                    useExternalBrowser = useExternalBrowser
                                )
                            } finally {
                                isOpening = false
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingsGroup {
                    SettingsSwitchItem(
                        title = stringResource(R.string.use_external_browser),
                        icon = Icons.Rounded.OpenInBrowser,
                        checked = useExternalBrowser,
                        onCheckedChange = { useExternalBrowser = it },
                        index = 0,
                        count = 1,
                    )
                }
            }
        }
    }//:Scaffold
}

/**
 * Full-width "Log in with MyAnimeList" button: 56dp, press morph, and a loading indicator in
 * place of the icon while [isLoading]. Shared with the onboarding login step.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        shapes = ButtonDefaults.shapes(),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        contentPadding = ButtonDefaults.contentPaddingFor(56.dp),
    ) {
        if (isLoading) {
            LoadingIndicator(
                modifier = Modifier.size(24.dp),
                color = LocalContentColor.current,
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_open_in_browser),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.iconSizeFor(56.dp))
            )
        }
        Spacer(modifier = Modifier.width(ButtonDefaults.iconSpacingFor(56.dp)))
        Text(
            text = stringResource(R.string.login_with_mal),
            style = ButtonDefaults.textStyleFor(56.dp)
        )
    }
}

fun Context.openLoginUrl(
    loginUrl: String,
    useExternalBrowser: Boolean
) {
    if (useExternalBrowser) {
        Intent(Intent.ACTION_VIEW, Uri.parse(loginUrl)).apply {
            try {
                showToast(getString(R.string.login_browser_warning))
                startActivity(this)
            } catch (e: ActivityNotFoundException) {
                showToast("No app found for this action")
            }
        }
    } else openCustomTab(loginUrl)
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    MoeListTheme {
        LoginView()
    }
}
