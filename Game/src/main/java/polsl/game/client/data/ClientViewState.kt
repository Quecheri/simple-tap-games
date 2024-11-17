package polsl.game.client.data

import polsl.game.server.model.toViewState
import polsl.game.server.repository.Prompt
import polsl.game.server.data.DisplayAnswer
import polsl.game.server.data.Players
import polsl.game.server.data.Results
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import polsl.game.server.data.GameParams
import polsl.game.server.data.NameResult

/**
 * It holds the data to be used in the client screen.
 * @property state              connection state.
 * @property correctAnswerId    correct answer id.
 * @property selectedAnswerId   selected answer id.
 * @property ticks              timer duration.
 * @property prompt           question sent from the server.
 * @property userJoined         a list of all joined players.
 * @property isGameOver         returns true when game is over.
 * @property result             a list of players and their scores.
 * @property isTimerRunning     returns true when timer is running.
 * TODO add new params
 */

data class ClientViewState(
    val state: ConnectionState = ConnectionState.Initializing,
    val correctAnswerId: Int? = null,
    val selectedAnswerId: Int? = null,
    val ticks: Long? = null,
    val prompt: Prompt? = null,
    val userJoined: Players? = null,
    val isGameOver: Boolean? = null,
    val isYourTurn: Boolean = false,
    val result: Results? = null,
    val nameResult: NameResult? = null,
    val error: String? = null,
    val isUserTyping: Boolean = false,
    val userRequestedPlayersNameDialog: Boolean = true,
    val haystack: Int? = null,
    val resultStr: String? = null,
    val gameParams: GameParams? = null,
    val blinkQueue: Int? = null,
) {
    val isTimerRunning: Boolean = ticks?.let { it > 0 } == true

    private val isDuplicate: Boolean = nameResult?.isDuplicateName ?: false
    private val isEmptyName: Boolean = nameResult?.isEmptyName ?: false

    // Open the name dialog if name wasn't set, or an invalid name was reported.
    val openDialog: Boolean = nameResult?.isInvalid() ?: true
    val playersNameIsDuplicate: Boolean = isDuplicate && !isUserTyping && userRequestedPlayersNameDialog
    val playersNameIsError: Boolean = (isDuplicate || isEmptyName) && !isUserTyping

    internal fun getProgress() :Float
    {
        if(gameParams?.numParam1 != null)
        {
            return 1-haystack!!.toFloat()/ gameParams.numParam1!!
        }
        assert(false)
        return 0F
    }
}

fun ClientViewState.toViewState(): List<DisplayAnswer> {
    return prompt?.let { question ->
        question.answers.map { it.toViewState(selectedAnswerId, correctAnswerId, isTimerRunning) }
    } ?: emptyList()
}