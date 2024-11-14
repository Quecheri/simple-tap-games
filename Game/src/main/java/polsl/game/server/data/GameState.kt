package polsl.game.server.data

import polsl.game.server.repository.Prompt

sealed interface GameState

data class WaitingForPlayers(val connectedPlayers: Int): GameState

data object DownloadingQuestions: GameState
data object WaitingForRound: GameState

data class Round(
    val prompt: Prompt
): GameState
