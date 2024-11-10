package polsl.game.server.viewmodel

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import polsl.game.server.repository.Question
import polsl.game.server.repository.QuestionRepository
import polsl.game.server.repository.AdvertisingManager
import polsl.game.server.data.*
import polsl.game.server.repository.ServerConnection
import polsl.game.server.repository.ServerManager
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ble.observer.ServerObserver
import polsl.game.server.model.FastReactionStrategy
import polsl.game.server.model.GameStrategy
import polsl.game.server.model.NimStrategy
import javax.inject.Inject

@HiltViewModel
class ServerViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val advertiser: AdvertisingManager,
    private val serverManager: ServerManager,
    private val questionRepository: QuestionRepository,
) : TimerViewModel(context) {
    private val TAG: String = ServerViewModel::class.java.simpleName

    private val _serverState: MutableStateFlow<ServerViewState> =
        MutableStateFlow(ServerViewState())
    val serverViewState = _serverState.asStateFlow()
    private var clients: MutableStateFlow<List<ServerConnection>> = MutableStateFlow(emptyList())
    private lateinit var question: Question
    private var rollingPointer = -1
    private val mapNameWithDevice: MutableStateFlow<List<Name>> = MutableStateFlow(emptyList())

    private var strategy: GameStrategy?=null
    private var gameType: GameType?=null

    internal fun getGameType():GameType
    {
        return if (gameType!=null) gameType!! else GameType.NSY_GAME
    }

    internal fun getResultString():String
    {
        var result = ""
        when (gameType!!) {
            GameType.NIM -> {
                val loser = if(rollingPointer==-1) _serverState.value.result.last() else _serverState.value.result[rollingPointer]
                val loserName = loser.name
                result = "Player $loserName lost NIM"
            }
            GameType.FAST_REACTION -> {
                val sumOfScores = _serverState.value.result.sumOf{it.score}
                val numOfQuestions = (strategy as FastReactionStrategy).getInitialNumberOfQuestions()
                val incorrect = (numOfQuestions-sumOfScores)/2
                val correct = numOfQuestions - incorrect

                result = "Incorrect reactions: $incorrect \nCorrect reactions: $correct"
            }
            GameType.NSY_GAME -> {
                assert(false)
            }
        }

        return result
    }
    fun getGameStateString():String
    {
        return if(strategy!=null) strategy!!.getGameStateString() else ""
    }

    init {
        startServer()
    }
    private fun rollPointer() // TODO move rollPointer to strategy - it should be randomized for second game
    {
        rollingPointer++
        if(rollingPointer==clients.value.size) {
            rollingPointer = -1
        }
    }

    fun startGame(gameType: GameType) {
        stopAdvertising()
        this.gameType = gameType
        when (gameType) {
            GameType.NIM -> {
                Log.d("StartGame", "Starting NIM game")
                strategy = NimStrategy(questionRepository)
            }
            GameType.FAST_REACTION -> {
                Log.d("StartGame", "Starting Fast Reaction game")
                strategy = FastReactionStrategy(questionRepository)
            }
            GameType.NSY_GAME -> {
                Log.d("StartGame", "NSY")
            }
        }
        if(strategy!=null)
        {
            viewModelScope.launch {
                _serverState.value = _serverState.value.copy(state = DownloadingQuestions)
                question = strategy!!.getQuestion()
                sendParams(gameType)
                /** Send first Question */
                showQuestion(question)
            }
        }
        else
        {
            assert(false, { "Not supported game" })
        }
    }

    private suspend fun sendParams(gameType: GameType) {
        /** Send game params */
        val timeout = 2000
        Timer.TOTAL_TIME = timeout.toLong()

        clients.value.forEach {
            it.sendGameParams(GameParams(gameType, timeout, 20));//TODO parametrize timeout
        }
    }

    fun showNextQuestion() {
        viewModelScope.launch {
           if (!strategy!!.isGameOver())
           {
               rollPointer()
               question = strategy!!.getQuestion()
               showQuestion(question)
           }else
           {
               _serverState.value = _serverState.value.copy(isGameOver = true)
               /** Send game over flag and results string to all players.*/
               clients.value.forEach {
                   it.gameOver(true)
                   it.sendResultStr(
                       getResultString()
                   )
               }
           }
        }
    }

    private suspend fun showQuestion(question: Question) {
        viewModelScope.launch {
            if (rollingPointer == -1) {
                _serverState.value = _serverState.value.copy(
                    state = Round(question),
                    ticks = Timer.TOTAL_TIME,
                    selectedAnswerId = null,
                    correctAnswerId = null,
                )
                startCountDown()
            }
            else{
                _serverState.value = _serverState.value.copy(state = WaitingForRound)
                clients.value[rollingPointer].sendQuestion(question)
            }
        }
    }

    private fun startServer() {
        viewModelScope.launch {
            try {
                advertiser.startAdvertising()
            } catch (exception: Exception) {
                throw Exception("Could not start server.", exception)
            }
        }
        serverManager.setServerObserver(object : ServerObserver {

            override fun onServerReady() {
                Log.w(TAG, "Server is Ready.")
            }

            override fun onDeviceConnectedToServer(device: BluetoothDevice) {
                ServerConnection(getApplication(), viewModelScope, device)
                    .apply {
                        stateAsFlow()
                            .onEach { connectionState ->
                                val currentState = _serverState.value.state
                                when (connectionState) {
                                    ConnectionState.Ready -> {
                                        clients.value += this
                                        _serverState.value = _serverState.value.copy(
                                            state = WaitingForPlayers(clients.value.size)
                                        )
                                    }
                                    is ConnectionState.Disconnected -> {
                                        clients.value -= this
                                        removePlayer(device)
                                        when (currentState) {
                                            is WaitingForPlayers -> {
                                                _serverState.value = _serverState.value.copy(
                                                    state = WaitingForPlayers(clients.value.size)
                                                )
                                            }
                                            else -> Log.d(TAG, "${device.address} disconnected from the server.")
                                        }
                                    }
                                    else -> {}
                                }
                            }
                            .launchIn(viewModelScope)
                    }
                    .apply {
                        playersName
                            .onEach {
                                // Validates name and if its valid it will save the name,
                                // otherwise sends an error message to the client.
                                validateName(it, device)
                            }
                            .launchIn(viewModelScope)
                    }
                    .apply {
                        clientAnswer
                            .onEach { saveScore(it, device.address)
                                showNextQuestion()}
                            .launchIn(viewModelScope)
                    }
                    .apply {
                        useServer(serverManager)
                        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                            Log.e("ServerViewModel", "Error", throwable)
                        }
                        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
                            connect()
                        }
                    }
            }

            override fun onDeviceDisconnectedFromServer(device: BluetoothDevice) {
                Log.w(TAG, "${device.address} disconnected from the server.")
                removePlayer(device)
            }
        })
        serverManager.open()
    }

    /**
     * A method to remove a disconnected player from the list of all connected users and the result list.
     */
    private fun removePlayer(device: BluetoothDevice) {
        if (_serverState.value.userJoined.isNotEmpty()) {
            mapName(device.address)?.let { disconnectedPlayer ->
                mapNameWithDevice.value -= Name(
                    name = disconnectedPlayer,
                    deviceAddress = device.address
                )
                val oldState = _serverState.value
                _serverState.value =
                    oldState.copy(userJoined = oldState.userJoined - Player(disconnectedPlayer))
                // Checks and removes the corresponding player's name if it is not removed from the list.
                val player = _serverState.value.result.find { it.name == disconnectedPlayer }
                if (player?.name?.isNotEmpty() == true) {
                    _serverState.value = _serverState.value.copy(
                        result = _serverState.value.result - Result(
                            player.name,
                            player.score
                        )
                    )
                }
            }
        }
    }

    /** Validate players name sent from a client device.
     * If it is valid, it notifies the client that the name is correct; otherwise,
     * it sends the corresponding error message, either name empty or duplicate name.
     */
    private fun validateName(playersName: String, device: BluetoothDevice) {
        val name = playersName.trim()
        if (name.isEmpty()) {
            viewModelScope.launch {
                clients.value.forEach {
                    if (it.bluetoothDevice == device) {
                        it.sendEmptyNameError(isEmptyName = true)
                    }
                }
            }
        } else if ((_serverState.value.userJoined.find { it.name == name }?.name == name)) {
            viewModelScope.launch {
                clients.value.forEach {
                    if (it.bluetoothDevice == device) {
                        it.sendDuplicateNameError(isDuplicateName = true)
                    }
                }
            }
        } else {
            mapNameAndDevice(
                playerName = playersName,
                deviceAddress = device.address
            )
            viewModelScope.launch {
                clients.value.forEach {
                    if (it.bluetoothDevice == device) {
                        it.sendDuplicateNameError(isDuplicateName = false)
                        it.sendEmptyNameError(isEmptyName = false)
                    }
                }
            }
        }
    }

    private fun stopServer() {
        serverManager.close()
    }

    private fun stopAdvertising() {
        advertiser.stopAdvertising()
    }

    override fun onCleared() {
        super.onCleared()
        stopAdvertising()

        clients.value.forEach {
            it.release()
        }
        stopServer()
    }


    /** Save the player's name and send it to all players to prevent duplicates. */
    private fun savePlayersName(playerName: String) {
        val oldState = _serverState.value
        _serverState.value = oldState.copy(
            userJoined = oldState.userJoined + Player(playerName),
            result = oldState.result + Result(
                name = playerName,
                score = 0
            )
        )
        viewModelScope.launch {
            clients.value.forEach {
                it.sendNameToAllPlayers(Players(_serverState.value.userJoined))
            }
        }
    }

    fun saveServerPlayer(playerName: String) {
        advertiser.address?.let { mapNameAndDevice(playerName, it) }
    }

    fun selectedAnswerServer(selectedAnswer: Int) {
        viewModelScope.launch {
            _serverState.value = _serverState.value.copy(selectedAnswerId = selectedAnswer)
            advertiser.address?.let { saveScore(selectedAnswer, it) }
            showNextQuestion()
        }

    }

    /** Save score. Before updating the score, it will check for the players' names that are
     * associated with the device address. The mapping is done to avoid the possibility of
     * the client providing a new name each time.
     */
    private fun saveScore(result: Int , deviceAddress: String) {
        viewModelScope.launch {
            strategy!!.updateScore(result)
            var resultBoost = result // NIM case
            if(question.correctAnswerId != null) //Fast reaction game case
                resultBoost = if(question.correctAnswerId == result) 1 else -1
        _serverState.value.result.find { it.name == mapName(deviceAddress) }
            ?.let { it.score +=  resultBoost }
            clients.value.onEach { client ->
                client.sendHaystack(strategy!!.getScore())
            }
        }
    }

    /** Map players name with device address. */
    private fun mapNameAndDevice(playerName: String, deviceAddress: String) {
        mapNameWithDevice.value += Name(
            name = playerName,
            deviceAddress = deviceAddress
        )
        savePlayersName(playerName = playerName)
    }

    private fun mapName(deviceAddress: String): String? {
        return mapNameWithDevice.value.find { it.deviceAddress == deviceAddress }?.name
    }
}
enum class GameType(val value: Int) {
    NIM(0),
    FAST_REACTION(1),
    NSY_GAME(2);

    companion object {
        fun fromInt(value: Int): GameType? {
            return entries.find { it.value == value }
        }
    }
    override fun toString(): String {
        return when (this) {
            NIM -> "NIM"
            FAST_REACTION -> "Fast reaction"
            NSY_GAME -> "NSY game"
        }
    }
}