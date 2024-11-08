package polsl.game.server.model

import polsl.game.server.repository.Answer
import polsl.game.server.data.ColorState
import polsl.game.server.data.DisplayAnswer

fun Answer.toViewState(
    selectedAnswerId: Int? = null,
    correctAnswerId: Int? = null,
    isTimerRunning: Boolean,
): DisplayAnswer =
    DisplayAnswer(
        id,
        text,
        isSelected = id == selectedAnswerId,
        enableSelection = isTimerRunning && selectedAnswerId == null,
        color = when {
            id == correctAnswerId -> ColorState.CORRECT
            id == selectedAnswerId && isTimerRunning -> ColorState.SELECTED_AND_TIMER_RUNNING
            isTimerRunning || id != selectedAnswerId -> ColorState.NOT_SELECTED_AND_TIMER_RUNNING
            else -> ColorState.NONE
        }
    )