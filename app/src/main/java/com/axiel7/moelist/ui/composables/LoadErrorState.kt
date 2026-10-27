package com.axiel7.moelist.ui.composables

import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R
import kotlinx.coroutines.launch

/**
 * Decides how a screen shows an error message from its ViewModel, and keeps the full-screen
 * error visible until the user retries.
 *
 * Why: screens used to show the full-screen error only while `uiState.message` was set, but
 * the same message was also shown as a snackbar and then cleared via `onMessageDisplayed()`.
 * After the snackbar, the screen saw "no error + no content" and wrongly switched to the
 * "No results" empty state.
 *
 * Now:
 * - nothing on screen yet ([hasContent] false) -> the message is kept here and shown as the
 *   full-screen error (no duplicate snackbar) until retry or until content arrives
 * - content already on screen (e.g. a refresh failed) -> shown as a snackbar, content stays
 *
 * Set the returned state's value to null when the user taps Retry.
 *
 * @param onRetry when set, the error snackbar gets a Retry button that calls it
 */
@Composable
fun rememberLoadErrorState(
    message: String?,
    hasContent: Boolean,
    onMessageConsumed: () -> Unit,
    onRetry: (() -> Unit)? = null,
): MutableState<String?> {
    val snackbarHostState = LocalSnackbarHostState.current
    val loadError = rememberSaveable { mutableStateOf<String?>(null) }
    val retryLabel = stringResource(R.string.retry)
    val currentOnRetry by rememberUpdatedState(onRetry)
    val scope = rememberCoroutineScope()

    LaunchedEffect(message) {
        if (message != null) {
            if (hasContent) {
                onMessageConsumed()
                // Own scope: consuming the message restarts this effect, which must not cancel
                // the snackbar (and its Retry) that's showing
                scope.launch {
                    val result = snackbarHostState.showAppSnackbar(
                        message = message,
                        type = SnackbarType.ERROR,
                        actionLabel = if (currentOnRetry != null) retryLabel else null,
                    )
                    if (result == SnackbarResult.ActionPerformed) currentOnRetry?.invoke()
                }
            } else {
                loadError.value = message
                onMessageConsumed()
            }
        }
    }

    // Content showed up (retry worked, refresh, etc.) -> drop the full-screen error
    LaunchedEffect(hasContent) {
        if (hasContent) loadError.value = null
    }

    return loadError
}
