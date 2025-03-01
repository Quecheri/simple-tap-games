package polsl.game.server


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import polsl.game.R
import polsl.game.server.data.Round
import polsl.game.server.data.WaitingForPlayers
import polsl.game.server.data.toViewState
import polsl.game.server.view.PlayersNameDialog
import polsl.game.server.view.ResultView
import polsl.game.server.view.StartGameView
import polsl.game.server.view.WaitingForClientsView
import polsl.game.server.viewmodel.ServerViewModel
import no.nordicsemi.android.common.permissions.ble.RequireBluetooth
import no.nordicsemi.android.common.ui.view.NordicAppBar
import polsl.game.server.repository.SHOULD_CLICK
import polsl.game.server.repository.SHOULD_NOT_CLICK
import polsl.game.server.repository.CONTROL_COMMUNICATION_FIRST
import polsl.game.server.view.BlinkContentView
import polsl.game.server.view.ImageContentView
import polsl.game.server.view.NimContentView
import polsl.game.server.view.StartRoundView
import polsl.game.server.viewmodel.GameType

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
            var blinkTimeout = 100L
            when (val currentState = serverViewState.state) {
                is WaitingForPlayers ->
                    if (currentState.connectedPlayers == 0) {
                        WaitingForClientsView()
                    } else {
                        if (openDialog) {
                            if(!serverViewModel.isNameLoaded())playersName=serverViewModel.getName()
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
                                            serverViewModel.setName(playersName)
                                            openDialog = false
                                        }
                                    } else isEmpty = true
                                },
                            )
                        } else {
                            StartGameView(
                                isAllNameCollected = serverViewState.isAllNameCollected,
                                joinedPlayer = serverViewState.userJoined,
                                onStartGame = { game,timeout,rounds,showMatches -> serverViewModel.startGame(game,timeout,rounds,showMatches) }
                            )
                        }
                    }
                is Round -> {
                    when (serverViewState.isGameOver) {
                        true -> ResultView(result = serverViewModel.getResultString())
                        else -> {
                                blinkTimeout = serverViewModel.getTimeout()*1L
                                val ticks by serverViewModel.ticks.collectAsState()
                                val isTimerRunning = ticks > 0
                                when(serverViewModel.getGameType())
                                {
                                    GameType.NIM ->
                                    {
                                        NimContentView(
                                            prompt = currentState.prompt.prompt,
                                            answers = serverViewState.toViewState(),
                                            ticks = ticks,
                                            randomSeed = serverViewModel.getSeed(),
                                            numOfMatches = serverViewModel.getGameScore(),
                                            showTextInfo = serverViewModel.getShowNumOfMatches(),
                                            modifier = Modifier.fillMaxWidth(),
                                            onAnswerSelected = { answerChosen ->
                                                serverViewModel.selectedAnswerServer(answerChosen)
                                                serverViewModel.stopCountDown()
                                            },
                                            onTimeOut = {if(serverViewModel.timerRunning) serverViewModel.selectedAnswerServer(1)},
                                        )
                                    }
                                    GameType.FAST_REACTION ->
                                    {
                                        ImageContentView(
                                            shouldReact = currentState.prompt.prompt==SHOULD_CLICK,
                                            ticks = ticks,
                                            modifier = Modifier.fillMaxWidth(),
                                            onAnswerSelected = { answerChosen ->
                                                serverViewModel.selectedAnswerServer(answerChosen)
                                                serverViewModel.stopCountDown()
                                            },
                                            onTimeOut = {if(serverViewModel.timerRunning) serverViewModel.selectedAnswerServer(-1)}
                                        )
                                    }
                                    GameType.COMBINATION ->
                                        if (serverViewModel.getPromptString() == CONTROL_COMMUNICATION_FIRST)
                                        {
                                            StartRoundView(
                                                title = stringResource(R.string.start_round_title),
                                                modifier = Modifier.fillMaxWidth(),
                                                onScreenClicked = {
                                                    serverViewModel.selectedAnswerServer(0)
                                                    serverViewModel.stopCountDown()
                                                },
                                            )
                                        }
                                        else if (serverViewModel.getPromptString()== SHOULD_CLICK) {
                                            BlinkContentView(
                                                title = stringResource(R.string.combination_active_title),
                                                modifier = Modifier.fillMaxWidth(),
                                                flashColor = Color.Green,
                                                onScreenClicked = {
                                                    serverViewModel.selectedAnswerServer(1)
                                                    serverViewModel.stopCountDown()
                                                },
                                                onTimeout = {
                                                    if (serverViewModel.timerRunning) serverViewModel.selectedAnswerServer(
                                                        -1
                                                    )
                                                },
                                            )
                                        }
                                        else if (serverViewModel.getPromptString() == SHOULD_NOT_CLICK){
                                                BlinkContentView(
                                                    title = stringResource(R.string.combination_preview_title),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    clicable = false,
                                                    startWithFlash = true,
                                                    flashColor = Color.Yellow,
                                                    flashTimeout = blinkTimeout,
                                                    onScreenClicked = {},
                                                    onTimeout = { serverViewModel.selectedAnswerServer(1) },
                                                )

                                        }

                                }
                        }
                    }
                }
                else -> {
                    when (serverViewState.isGameOver) {
                        true -> ResultView(result = serverViewModel.getResultString())
                        else -> {
                            when(serverViewModel.getGameType())
                            {
                                GameType.COMBINATION ->
                                    if(serverViewModel.isCombinationPreview())
                                    {
                                        BlinkContentView(
                                            title = stringResource(R.string.combination_preview_title),
                                            modifier = Modifier.fillMaxWidth(),
                                            clicable = false,
                                            flashColor = Color.Yellow,
                                            flashTimeout = blinkTimeout,
                                            onScreenClicked = {},
                                            onTimeout = {},
                                        )
                                    }
                                    else if (serverViewModel.getPromptString() == SHOULD_CLICK)
                                    {
                                        BlinkContentView(
                                            title = stringResource(R.string.combination_active_title),
                                            modifier = Modifier.fillMaxWidth(),
                                            flashColor = Color.Red,
                                            onScreenClicked = {
                                                serverViewModel.selectedAnswerServer(-2)
                                                serverViewModel.stopCountDown()
                                            },
                                            onTimeout = {},
                                        )
                                    }
                                    else if (serverViewModel.getPromptString() == SHOULD_NOT_CLICK){

                                            BlinkContentView(
                                                title = stringResource(R.string.combination_preview_title),
                                                modifier = Modifier.fillMaxWidth(),
                                                clicable = false,
                                                flashColor = Color.Yellow,
                                                flashTimeout = blinkTimeout,
                                                onScreenClicked = {},
                                                onTimeout = {},
                                            )
                                    }

                                GameType.NIM ->
                                {
                                    NimContentView(
                                        prompt = null,
                                        answers = emptyList(),
                                        ticks = 0,
                                        randomSeed = serverViewModel.getSeed(),
                                        numOfMatches = serverViewModel.getGameScore(),
                                        showTextInfo = serverViewModel.getShowNumOfMatches(),
                                        modifier = Modifier.fillMaxWidth(),
                                        onAnswerSelected = {},
                                        onTimeOut = {},
                                    )
                                }
                                GameType.FAST_REACTION ->
                                {

                                }
                            }

                        }
                    }

                }
            }
        }
    }
}