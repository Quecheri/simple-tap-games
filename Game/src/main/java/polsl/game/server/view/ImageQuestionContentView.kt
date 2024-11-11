package polsl.game.server.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.theme.NordicTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import polsl.game.R

/**
 * Shows questions and list of answers.
 */
@Composable
fun ImageQuestionContentView(
    shouldReact: Boolean,
    ticks: Long,
    modifier: Modifier = Modifier,
    onAnswerSelected: (Int) -> Unit,
    onTimeOut: (Int) -> Unit,
) {
    val startTime = remember { System.currentTimeMillis() }
    TimerView(
        key = shouldReact,
        duration = ticks,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        onTimeOut = { onTimeOut(-1) },
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                val endTime = System.currentTimeMillis()
                val reactionTime = endTime - startTime
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
                modifier = modifier
                    .padding(16.dp)
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