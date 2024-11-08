package polsl.game.server

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import polsl.game.R
import polsl.game.server.data.DownloadingQuestions
import polsl.game.server.data.Round
import polsl.game.server.data.WaitingForPlayers
import polsl.game.server.data.WaitingForRound
import polsl.game.server.data.toViewState
import polsl.game.server.view.PlayersNameDialog
import polsl.game.server.view.QuestionContentView
import polsl.game.server.view.ResultView
import polsl.game.server.view.StartGameView
import polsl.game.server.view.WaitingForClientsView
import polsl.game.server.viewmodel.ServerViewModel
import no.nordicsemi.android.common.permissions.ble.RequireBluetooth
import no.nordicsemi.android.common.ui.view.NordicAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerScreen(
    onNavigationUp: () -> Unit,
) {
    Column {
        var playersName by rememberSaveable { mutableStateOf("") }
        NordicAppBar(
            title = {
                Text(
                    text = when (playersName.isNotEmpty()) {
                        true -> stringResource(id = R.string.good_luck_player, playersName)
                        else -> stringResource(id = R.string.good_luck_player, "")
                    }
                )
            },

            onNavigationButtonClick = onNavigationUp
        )

        RequireBluetooth {
            val serverViewModel: ServerViewModel = hiltViewModel()
            val serverViewState by serverViewModel.serverViewState.collectAsState()
            var openDialog by rememberSaveable { mutableStateOf(true) }
            var isDuplicate by rememberSaveable { mutableStateOf(false) }
            var isEmpty by rememberSaveable { mutableStateOf(false) }
            val isError = isEmpty || isDuplicate

            when (val currentState = serverViewState.state) {
                is WaitingForPlayers ->
                    if (currentState.connectedPlayers == 0) {
                        WaitingForClientsView()
                    } else {
                        if (openDialog) {
                            PlayersNameDialog(
                                playersName = playersName,
                                isDuplicate = isDuplicate,
                                isError = isError,
                                onDismiss = { openDialog = false },
                                onNameSet = {
                                    playersName = it
                                    isDuplicate = false
                                    isEmpty = false
                                },
                                onSendClick = {
                                    playersName = playersName.trim()
                                    if (playersName.isNotEmpty()) {
                                        isEmpty = false
                                        if (serverViewState.isDuplicate(playersName)) isDuplicate = true
                                        else {
                                            serverViewModel.saveServerPlayer(playersName)
                                            openDialog = false
                                        }
                                    } else isEmpty = true
                                },
                            )
                        } else {
                            StartGameView(
                                isAllNameCollected = serverViewState.isAllNameCollected,
                                joinedPlayer = serverViewState.userJoined,
                                onStartGame = { serverViewModel.startGame() }
                            )
                        }
                    }
                is Round -> {
                    Text(
                        text = "Haystack: " + serverViewState.haystack,
                        modifier = Modifier.padding(16.dp)
                    )
                    when (serverViewState.isGameOver) {
                        true -> ResultView(result = serverViewState.result)
                        else -> {

                                val ticks by serverViewModel.ticks.collectAsState()
                                val isTimerRunning = ticks > 0

                                QuestionContentView(
                                    question = currentState.question.question,
                                    answers = serverViewState.toViewState(),
                                    ticks = ticks,
                                    modifier = Modifier.fillMaxWidth(),
                                    onAnswerSelected = { answerChosen ->
                                        serverViewModel.selectedAnswerServer(answerChosen)
                                        serverViewModel.stopCountDown()
                                    },
                                    onTimeOut = {if(serverViewModel.timerRunning) serverViewModel.selectedAnswerServer(1)}
                                )
                        }
                    }
                }
                else -> {
                    when (serverViewState.isGameOver) {
                        true -> ResultView(result = serverViewState.result)
                        else -> {
                            Text(
                                text = "Haystack: " + serverViewState.haystack,
                                modifier = Modifier.padding(16.dp)
                            )
                            Text(
                                text = "Wait for your turn:",
                                modifier = Modifier.padding(16.dp)
                            )

                        }
                    }

                }
            }
        }
    }
}