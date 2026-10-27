package com.axiel7.moelist.ui.editmedia.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.Delete

/**
 * Confirmation before removing an entry from the list. The destructive action is a filled
 * error-colored "Delete" button (not a neutral "OK"), and the entry's title is shown so it's
 * clear what will be removed.
 *
 * @param mediaTitle shown above the confirmation text when known
 */
@Composable
fun DeleteMediaEntryDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    mediaTitle: String? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = mediaTitle ?: stringResource(R.string.delete),
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.delete_confirmation),
                textAlign = TextAlign.Center,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(text = stringResource(R.string.delete), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}
