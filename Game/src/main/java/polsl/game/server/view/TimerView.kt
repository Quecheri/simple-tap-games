package polsl.game.server.view

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import polsl.game.server.viewmodel.Timer
import no.nordicsemi.android.common.theme.NordicTheme

@Composable
fun TimerView(
    key: Any,
    duration: Long,
    modifier: Modifier = Modifier,
    onTimeOut: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        var progress by remember { mutableFloatStateOf(1f) }
        val progressAnimation by animateFloatAsState(
            targetValue = progress,
            animationSpec = if (progress == 1f) snap(0) else tween(
                durationMillis = duration.toInt(),
                easing = LinearEasing
            ),
            label = "Timer",
            finishedListener = { if (progress == 1f) {progress = 0f}
                onTimeOut()}
        )
        LinearProgressIndicator(
            progress = {progressAnimation},
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        )
        LaunchedEffect(key1 = key) {
            progress = if (progress == 1f) 0f else 1f
        }
        //Text(
        //    text = "Timer:  ${duration / 1000} s",
        //    fontSize = 36.sp,
        //   color = checkColor(duration)
        //)
    }
}

@Composable
private fun checkColor(ticks: Long) = when {
    ticks >= Timer.TOTAL_TIME / 4 -> Color.Green
    ticks >= Timer.TOTAL_TIME / 8 -> Color.Yellow
    else -> Color.Red
}

@Preview
@Composable
fun ShowTimer_Preview() {
    NordicTheme {
        TimerView(
            key = "",
            duration = 5_000L,
            modifier = Modifier.fillMaxWidth(),
            onTimeOut = {},
        )
    }
}