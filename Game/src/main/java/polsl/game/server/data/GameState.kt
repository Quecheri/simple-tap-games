package polsl.game.server.data

import polsl.game.server.repository.Question

sealed interface GameState

data class WaitingForPlayers(val connectedPlayers: Int): GameState

data object DownloadingQuestions: GameState
data object WaitingForRound: GameState

data class Round(
    val question: Question
): GameState
