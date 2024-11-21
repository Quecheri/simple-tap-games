package polsl.game.server.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import polsl.game.R
import no.nordicsemi.android.common.theme.NordicTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResultView(result: String) {
    var openDialog by remember { mutableStateOf(true) }
    if (openDialog) {
        ResultDialog(
            onDismiss = { openDialog = false },
            onClick = { openDialog = false }
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            stickyHeader {
                Surface(shadowElevation = 4.dp) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = result,
                            style = MaterialTheme.typography.titleLarge,
                            )
                    }
                }
            }
        }
    }
}

@Composable
fun ResultDialog(
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    AlertDialog(
        properties = DialogProperties(
            dismissOnClickOutside = false
        ),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.game_over),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(
                onClick = onClick
            ) {
                Text(text = stringResource(id = R.string.show_result))
            }
        },
    )
}

@Preview
@Composable
fun ResultView_Preview() {
    NordicTheme {
        ResultView(
            result = stringResource(R.string.Nim_preview_result)
        )
    }
}

