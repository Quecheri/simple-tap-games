package polsl.game.server.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.theme.NordicTheme
import polsl.game.R
import kotlin.random.Random


/**
 * Shows questions and list of answers.
 */
@Composable
fun ImageQuestionContentView(
    shouldReact: Boolean,
    ticks: Long,
    progress: Float,
    modifier: Modifier = Modifier,
    onAnswerSelected: (Int) -> Unit,
    onTimeOut: (Int) -> Unit,
) {
    val startTime = remember { System.currentTimeMillis() }
    var shouldStart by remember { mutableStateOf(true) }
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    )
    TimerEffect(
        duration = ticks,
        shouldStart = shouldStart,
        onTimeOut = {onTimeOut(-1)}
    )

    val (height, width) = LocalConfiguration.current.run { screenHeightDp.dp to screenWidthDp.dp }
    val animalSize = 220.dp
    val paddingY = 100.dp //Status bar, navigation bar etc.

    val offsetX = remember {Random.nextInt((width-animalSize).value.toInt())}
    val offsetY = remember {Random.nextInt((height-animalSize-paddingY).value.toInt())}
    val rotation = remember {Random.nextInt(-40, 40).toFloat()}

    Image(
        painter = painterResource(id = if (shouldReact) R.drawable.beaver else R.drawable.capybara),
        contentDescription = null,
        modifier = Modifier
            .size(animalSize)
            .offset(x = offsetX.dp, y = offsetY.dp)
            .graphicsLayer(rotationZ = rotation)
            .fillMaxSize()
            .clickable {
                val endTime = System.currentTimeMillis()
                val reactionTime = endTime - startTime
                shouldStart = false
                onAnswerSelected(reactionTime.toInt())
            }
    )
}

@Preview
@Composable
private fun ImageQuestionContentView_Preview() {
    NordicTheme {
        ImageQuestionContentView(
            shouldReact =false,
            ticks = 4000,
            progress = 0.85F,
            modifier = Modifier.fillMaxWidth(),
            onAnswerSelected = {},
            onTimeOut = {},
        )
    }
}