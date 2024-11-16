package polsl.game.server.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import polsl.game.server.data.ColorState
import polsl.game.server.data.DisplayAnswer
import no.nordicsemi.android.common.theme.NordicTheme
import polsl.game.R
import kotlin.random.Random

/**
 * Shows matches and list of answers.
 */
@Composable
fun NimContentView(
    question: String?,
    answers: List<DisplayAnswer>,
    ticks: Long,
    randomSeed: Int,
    numOfMatches: Int,
    modifier: Modifier = Modifier,
    onAnswerSelected: (Int) -> Unit,
    onTimeOut: (Int) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TimerView(
            key = question ?: 0,
            duration = if(question!=null)ticks else 0,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            onTimeOut = { if(question!=null)onTimeOut(1)},
        )
        val random = Random(randomSeed)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            repeat(numOfMatches) {
                val scaleFactor = 1.5f
                val xOffset = (random.nextInt(-50, 50) * scaleFactor).dp
                val yOffset = (random.nextInt(-50, 50) * scaleFactor).dp
                val rotation = random.nextInt(-180, 180).toFloat()

                Image(
                    painter = painterResource(id = R.drawable.match),
                    contentDescription = "Match",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(70.dp)
                        .offset(x = xOffset, y = yOffset)
                        .graphicsLayer(rotationZ = rotation)
                )
            }
        }
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            items(answers) { answer ->
                Text(
                    modifier = modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = answer.isSelected,
                            onClick = { onAnswerSelected(answer.id) },
                            enabled = answer.enableSelection
                        )
                        .background(
                            color = answer.color.color(),
                        )
                        .padding(30.dp),
                    text = answer.text
                )
            }
        }
    }
}



@Preview
@Composable
private fun NimContentView_Preview() {
    NordicTheme {
        NimContentView(
            question =
            "",
            ticks = 4000,
            numOfMatches = 50,
            randomSeed = 50,
            modifier = Modifier.fillMaxWidth(),
            onAnswerSelected = {},
            onTimeOut = {},
            answers = listOf(
                DisplayAnswer(1, "1",
                    isSelected = false,
                    enableSelection = true,
                    color = ColorState.NOT_SELECTED_AND_TIMER_RUNNING
                ),
                DisplayAnswer(1, "2",
                    isSelected = true,
                    enableSelection = true,
                    color = ColorState.SELECTED_AND_TIMER_RUNNING
                ),
                DisplayAnswer(1, "3",
                    isSelected = false,
                    enableSelection = true,
                    color = ColorState.NOT_SELECTED_AND_TIMER_RUNNING
                ),
                )
        )
    }
}