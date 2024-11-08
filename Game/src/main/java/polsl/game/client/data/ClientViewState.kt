package polsl.game.client.data

import polsl.game.server.model.toViewState
import polsl.game.server.repository.Question
import polsl.game.server.data.DisplayAnswer
import polsl.game.server.data.Players
import polsl.game.server.data.Results
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import polsl.game.server.data.NameResult

/**
 * It holds the data to be used in the client screen.
 * @property state              connection state.
 * @property correctAnswerId    correct answer id.
 * @property selectedAnswerId   selected answer id.
 * @property ticks              timer duration.
 * @property question           question sent from the server.
 * @property userJoined         a list of all joined players.
 * @property isGameOver         returns true when game is over.
 * @property result             a list of players and their scores.
 * @property isTimerRunning     returns true when timer is running.
 */

data class ClientViewState(
    val state: ConnectionState = ConnectionState.Initializing,
    val correctAnswerId: Int? = null,
    val selectedAnswerId: Int? = null,
    val ticks: Long? = null,
    val question: Question? = null,
    val userJoined: Players? = null,
    val isGameOver: Boolean? = null,
    val isYourTurn: Boolean = false,
    val result: Results? = null,
    val nameResult: NameResult? = null,
    val error: String? = null,
    val isUserTyping: Boolean = false,
    val userRequestedPlayersNameDialog: Boolean = true,
    val haystack: Int = 50,
) {
    val isTimerRunning: Boolean = ticks?.let { it > 0 } == true

    private val isDuplicate: Boolean = nameResult?.isDuplicateName ?: false
    private val isEmptyName: Boolean = nameResult?.isEmptyName ?: false

    // Open the name dialog if name wasn't set, or an invalid name was reported.
    val openDialog: Boolean = nameResult?.isInvalid() ?: true
    val playersNameIsDuplicate: Boolean = isDuplicate && !isUserTyping && userRequestedPlayersNameDialog
    val playersNameIsError: Boolean = (isDuplicate || isEmptyName) && !isUserTyping
}

fun ClientViewState.toViewState(): List<DisplayAnswer> {
    return question?.let { question ->
        question.answers.map { it.toViewState(selectedAnswerId, correctAnswerId, isTimerRunning) }
    } ?: emptyList()
}