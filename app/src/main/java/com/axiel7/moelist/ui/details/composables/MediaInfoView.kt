package com.axiel7.moelist.ui.details.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.theme.MoeListTheme

@Composable
fun MediaInfoView(
    title: String,
    info: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .then(modifier)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        SelectionContainer {
            Text(
                text = info ?: stringResource(R.string.unknown),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MediaInfoPreview() {
    MoeListTheme {
        MediaInfoView(
            title = "Studio",
            info = "Wit Studio"
        )
    }
}