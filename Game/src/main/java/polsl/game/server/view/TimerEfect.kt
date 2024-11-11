package polsl.game.server.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay


@Composable
fun TimerEffect(
    duration: Long,
    shouldStart: Boolean,
    onTimeOut: () -> Unit
) {
    val currentOnTimeOut by rememberUpdatedState(onTimeOut)
    var isCancelled by remember { mutableStateOf(false) }

    LaunchedEffect(shouldStart) {
        if (shouldStart) {
            isCancelled = false
            delay(duration)
            if (!isCancelled) {
                currentOnTimeOut()
            }
        }
    }

    DisposableEffect(shouldStart) {
        onDispose {
            isCancelled = true
        }
    }
}