package polsl.game.server.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.theme.NordicTheme
import polsl.game.R

@Composable
fun ImageQuestionContentView(
    shouldReact: Boolean,
    ticks: Long,
    modifier: Modifier = Modifier,
    onAnswerSelected: (Int) -> Unit,
    onTimeOut: (Int) -> Unit,
) {
    var shouldStart by remember { mutableStateOf(true) }

    TimerEffect(
        duration = ticks,
        shouldStart = shouldStart,
        onTimeOut = { onTimeOut(-1) }
    )
    val startTime = remember { System.currentTimeMillis() }
    TimerView(
        key = shouldReact,
        duration = ticks,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        onTimeOut = {  },
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                val endTime = System.currentTimeMillis()
                val reactionTime = endTime - startTime
                shouldStart = false
                onAnswerSelected(reactionTime.toInt())
            }
    ) {
        Column(
            modifier = modifier
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = if (shouldReact) R.drawable.beaver else R.drawable.capybara),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}

@Preview
@Composable
private fun ImageQuestionContentView_Preview() {
    NordicTheme {
        ImageQuestionContentView(
            shouldReact =false,
            ticks = 4000,
            modifier = Modifier.fillMaxWidth(),
            onAnswerSelected = {},
            onTimeOut = {},
        )
    }
}