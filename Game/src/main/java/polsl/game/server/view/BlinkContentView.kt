package polsl.game.server.view
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.theme.NordicTheme
import polsl.game.R

/**
 * A Composable that reacts to screen clicks, handles timeout, flashes a specified color briefly, and shows a progress bar.
 */
@Composable
fun BlinkContentView(
    title: String = "",
    modifier: Modifier = Modifier,
    clicable: Boolean = true,
    //Message is delayed by 100s in combination gamemode
    flashTimeout:Long = 100,
    flashColor: Color = Color.Red,
    startWithFlash: Boolean = false,
    onTimeout: () -> Unit,
    onScreenClicked: () -> Unit
) {
    var shouldFlash by remember { mutableStateOf(false) }
    var shouldStart by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(startWithFlash) {
        if (startWithFlash) {
            coroutineScope.launch {
                shouldStart = false
                shouldFlash = true
                delay(flashTimeout*3)
                shouldFlash = false
            }
            coroutineScope.launch {
                delay(flashTimeout*3)
                onTimeout();
            }
        }
    }


    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                if (shouldStart && clicable) {
                    shouldStart = false
                    coroutineScope.launch {
                        shouldFlash = true
                        delay(flashTimeout)
                        shouldFlash = false
                    }
                    coroutineScope.launch {
                        onScreenClicked()
                    }
                }
            }
            .background(if (shouldFlash) flashColor else Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )

        }
    }
}

@Preview
@Composable
private fun BlinkContentView_Preview() {
    NordicTheme {
        BlinkContentView(
            title = stringResource(R.string.combination_preview_title),
            flashColor = Color.Yellow,
            startWithFlash = true,
            onTimeout = {},
            onScreenClicked = {}
        )
    }
}
