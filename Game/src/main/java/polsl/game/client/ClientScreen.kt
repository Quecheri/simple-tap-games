package polsl.game.client

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import polsl.game.R
import polsl.game.client.viewmodel.ClientViewModel
import polsl.game.server.view.PlayersNameDialog
import polsl.game.server.view.StringQuestionContentView
import polsl.game.client.data.toViewState
import polsl.game.client.view.*
import polsl.game.server.view.ResultView
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.common.permissions.ble.RequireBluetooth
import no.nordicsemi.android.common.permissions.ble.RequireLocation
import no.nordicsemi.android.common.ui.view.NordicAppBar
import polsl.game.server.repository.SHOULD_CLICK
import polsl.game.server.view.BlinkContentView
import polsl.game.server.view.ImageQuestionContentView
import polsl.game.server.viewmodel.GameType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientScreen(
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
            RequireLocation {
                val clientViewModel: ClientViewModel = hiltViewModel()
                val clientViewState by clientViewModel.clientState.collectAsState()

                clientViewState.error?.let { message ->
                    ErrorView(message)
                } ?: run {
                    val ticks by clientViewModel.ticks.collectAsState()

                    when (val state = clientViewState.state) {
                        ConnectionState.Initializing -> InitializingView()
                        ConnectionState.Connecting -> ConnectingView()
                        is ConnectionState.Disconnected -> DisconnectedView(state.reason)
                        ConnectionState.Ready -> {
                            when (clientViewState.isGameOver) {
                                true -> ResultView(
                                    result = (clientViewState.resultStr ?: "Error while receving result")
                                )
                                else -> {

                                        clientViewState.prompt?.let {
                                            if(clientViewState.isYourTurn)
                                            {
                                                when(clientViewState.gameParams!!.gameType)
                                                {
                                                    GameType.NIM ->
                                                    {
                                                        Text(
                                                            text = "Haystack: " + clientViewState.haystack,
                                                            modifier = Modifier.padding(16.dp)
                                                        )
                                                        StringQuestionContentView(
                                                            question = clientViewState.prompt?.prompt,
                                                            answers = clientViewState.toViewState(),
                                                            ticks = ticks,
                                                            modifier = Modifier.fillMaxWidth(),
                                                            onAnswerSelected = { answerChosen ->
                                                                clientViewModel.sendAnswer(answerChosen)
                                                                clientViewModel.stopCountDown()},
                                                            onTimeOut = { if(clientViewModel.timerRunning) clientViewModel.sendAnswer(1)
                                                            },
                                                        )
                                                    }
                                                    GameType.FAST_REACTION ->
                                                    {
                                                        ImageQuestionContentView(
                                                            shouldReact = clientViewState.prompt?.prompt==SHOULD_CLICK,
                                                            ticks = ticks,
                                                            progress = clientViewState.getProgress(),
                                                            modifier = Modifier.fillMaxWidth(),
                                                            onAnswerSelected = { answerChosen ->
                                                                clientViewModel.sendAnswer(answerChosen)
                                                                clientViewModel.stopCountDown()},
                                                            onTimeOut = { if(clientViewModel.timerRunning) clientViewModel.sendAnswer(-1)},
                                                        )
                                                    }
                                                    GameType.COMBINATION ->
                                                        if (clientViewState.prompt?.prompt==SHOULD_CLICK)
                                                        BlinkContentView(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            ticks = ticks,
                                                            flashColor = Color.Green,
                                                            onScreenClicked = {
                                                                clientViewModel.sendAnswer(1)
                                                                clientViewModel.stopCountDown()},
                                                            onTimeout = {if(clientViewModel.timerRunning) clientViewModel.sendAnswer(-1)},
                                                        )
                                                        else
                                                            {
                                                                BlinkContentView(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    ticks = ticks,
                                                                    flashColor = Color.Yellow,
                                                                    startWithFlash = true,
                                                                    onScreenClicked = {},
                                                                    onTimeout = {clientViewModel.sendAnswer(1)},
                                                                )
                                                            }
                                                }
                                            }
                                            else
                                            {
                                                if(clientViewState.gameParams != null){
                                                    when(clientViewState.gameParams!!.gameType)
                                                    {
                                                        GameType.COMBINATION->
                                                            BlinkContentView(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                ticks = 99999,
                                                                flashColor =  Color.Red,
                                                                onScreenClicked = {clientViewModel.sendAnswer(-1)
                                                                    clientViewModel.stopCountDown()},
                                                                onTimeout = {},
                                                            )
                                                        else ->
                                                            Text(
                                                                text = "Wait for your turn",
                                                                modifier = Modifier.padding(16.dp)
                                                            )
                                                    }
                                                }
                                                else
                                                {
                                                    Text(
                                                        text = "Wait for your turn",
                                                        modifier = Modifier.padding(16.dp)
                                                    )
                                                }

                                            }
                                        } ?: run {
                                            if (clientViewState.openDialog) {
                                                PlayersNameDialog(
                                                    playersName = playersName,
                                                    isDuplicate = clientViewState.playersNameIsDuplicate,
                                                    isError = clientViewState.playersNameIsError,
                                                    onDismiss = {
                                                        clientViewModel.dismissPlayersNameDialog()
                                                    },
                                                    onNameSet = {
                                                        playersName = it
                                                        clientViewModel.onUserTyping()
                                                    },
                                                    onSendClick = {
                                                        playersName = playersName.trim()
                                                        clientViewModel.sendName(playersName)
                                                    }
                                                )
                                            } else clientViewState.userJoined?.let { ConnectedView(it.player) }
                                        }
                                }
                            }
                        }
                        else -> LoadingView()
                    }
                }
            }
        }
    }
}


