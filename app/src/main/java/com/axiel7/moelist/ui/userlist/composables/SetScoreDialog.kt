package com.axiel7.moelist.ui.userlist.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.composables.score.ScoreSlider
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.outlined.CheckCircle
import com.axiel7.moelist.ui.theme.MoeListTheme
import com.axiel7.moelist.ui.theme.ShapeExtraLarge
import com.axiel7.moelist.utils.DateUtils.toLocalized
import java.time.LocalDate

/**
 * M3 dialog shown right after an anime/manga entry moves to Completed
 * (via the quick +1 episode/chapter action) while its score is still unset.
 * Lets the user rate it on the spot instead of forgetting to later.
 */
@Composable
fun SetScoreDialog(
    mediaTitle: String,
    finishDate: LocalDate?,
    onDismiss: () -> Unit,
    onFinish: (score: Int) -> Unit,
) {
    var score by remember { mutableIntStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = ShapeExtraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 8.dp)
                )

                Text(
                    text = stringResource(R.string.set_score_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = mediaTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                if (finishDate != null) {
                    Text(
                        text = stringResource(
                            R.string.set_score_dialog_finished_on,
                            finishDate.toLocalized()
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                ScoreSlider(
                    score = score,
                    onValueChange = { score = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.skip))
                    }
                    Button(
                        onClick = { onFinish(score) },
                        shape = CircleShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.finish))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun SetScoreDialogPreview() {
    MoeListTheme {
        SetScoreDialog(
            mediaTitle = "Frieren: Beyond Journey's End",
            finishDate = LocalDate.now(),
            onDismiss = {},
            onFinish = {},
        )
    }
}
