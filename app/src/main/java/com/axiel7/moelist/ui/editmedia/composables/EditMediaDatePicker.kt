package com.axiel7.moelist.ui.editmedia.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.R

/**
 * Date picker dialog for the edit sheet.
 *
 * @param onClear when set (the date already has a value), a "Clear" button is shown next to
 * Cancel. It replaces the small ✕ that used to sit inside the date chip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMediaDatePicker(
    datePickerState: DatePickerState,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    onClear: (() -> Unit)? = null,
) {
    val dateConfirmEnabled by remember {
        derivedStateOf { datePickerState.selectedDateMillis != null }
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let(onDateSelected)
                },
                enabled = dateConfirmEnabled,
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(text = stringResource(R.string.ok))
            }
        },
        dismissButton = {
            Row {
                if (onClear != null) {
                    TextButton(
                        onClick = onClear,
                        shapes = ButtonDefaults.shapes(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                    ) {
                        Text(text = stringResource(R.string.clear))
                    }
                }
                TextButton(onClick = onDismiss, shapes = ButtonDefaults.shapes()) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
