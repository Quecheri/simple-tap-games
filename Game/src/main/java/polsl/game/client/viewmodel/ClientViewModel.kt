package polsl.game.client.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.stateAsFlow
import polsl.game.client.data.ClientViewState
import polsl.game.client.repository.ClientConnection
import polsl.game.client.repository.ScannerRepository
import polsl.game.server.repository.CONTROL_COMMUNICATION_FIRST
import polsl.game.server.repository.CONTROL_COMMUNICATION_SECOND
import polsl.game.server.repository.CONTROL_COMMUNICATION_THIRD
import polsl.game.server.viewmodel.GameType
import polsl.game.server.viewmodel.Timer
import polsl.game.server.viewmodel.TimerViewModel
import polsl.game.spec.StorageManager
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val scannerRepository: ScannerRepository,
) : TimerViewModel(context) {
    private var clientManager: ClientConnection? = null
    private val _clientState: MutableStateFlow<ClientViewState> =
        MutableStateFlow(ClientViewState())
    val clientState = _clientState.asStateFlow()
    private val storageManager = StorageManager(context)
    private var name: String = ""
    private var nameLoaded: Boolean = false
    private var preview: Boolean = false

    init {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e("ClientViewModel", "Error", throwable)
            _clientState.value = _clientState.value.copy(error = throwable.message)
        }
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val device = scannerRepository.searchForServer()

            ClientConnection(context, viewModelScope, device)
                .apply {
                    stateAsFlow()
                        .onEach { _clientState.value = _clientState.value.copy(state = it) }
                        .launchIn(viewModelScope)
                    error
                        .onEach { _clientState.value = _clientState.value.copy(nameResult = it) }
                        .launchIn(viewModelScope)
                    userJoined
                        .onEach { _clientState.value = _clientState.value.copy(userJoined = it) }
                        .launchIn(viewModelScope)
                    prompt
                        .onEach {
                            if(it.prompt== CONTROL_COMMUNICATION_SECOND) preview=true
                            else if(it.prompt==CONTROL_COMMUNICATION_THIRD) preview=false
                            _clientState.value = _clientState.value.copy(
                                selectedAnswerId = it.correctAnswerId,
                                correctAnswerId = null,
                                isPreview = preview,
                                ticks = Timer.TOTAL_TIME,
                                prompt = it,
                                isYourTurn = isYourTurn(1),
                            )
                            startCountDown()
                        }
                        .launchIn(viewModelScope)
                    answer
                        .onEach {
                            _clientState.value = _clientState.value.copy(
                                correctAnswerId = it,
                                isYourTurn = isYourTurn(0),
                                ticks = ticks.value
                            )
                        }
                        .launchIn(viewModelScope)
                    isGameOver
                        .onEach { _clientState.value = _clientState.value.copy(isGameOver = it) }
                        .launchIn(viewModelScope)
                    result
                        .onEach { _clientState.value = _clientState.value.copy(result = it) }
                        .launchIn(viewModelScope)
                    score
                        .onEach { _clientState.value = _clientState.value.copy(score = it) }
                        .launchIn(viewModelScope)
                    resultStr
                        .onEach { _clientState.value = _clientState.value.copy(resultStr = it) }
                        .launchIn(viewModelScope)
                    gameParams
                        .onEach {
                            _clientState.value = _clientState.value.copy(gameParams = it)
                            Timer.TOTAL_TIME = clientState.value.gameParams?.timeout?.toLong() ?: Timer.TOTAL_TIME
                        }
                        .launchIn(viewModelScope)
                }
                .apply { connect() }
                .apply { clientManager = this }
        }
    }


    fun isCombinationPreview():Boolean
    {
        return _clientState.value.gameParams!=null && _clientState.value.gameParams!!.gameType==GameType.COMBINATION && preview
    }
    private fun isYourTurn(source:Int): Boolean {
        return when (source){
            0 ->_clientState.value.prompt?.prompt== CONTROL_COMMUNICATION_FIRST//Answer
            1 ->true //Prompt
            else -> false

        }
    }

    override fun onCleared() {
        super.onCleared()
        clientManager?.release()
        clientManager = null
    }

    fun sendAnswer(answerId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if(_clientState.value.gameParams!=null && _clientState.value.gameParams!!.gameType==GameType.COMBINATION)
            {
                //Needed to properly render animations in combination game mode
                delay(100)
            }
            clientManager?.sendSelectedAnswer(answerId)
            _clientState.value = _clientState.value.copy(
                selectedAnswerId = answerId,
                isYourTurn = isYourTurn(0),
                ticks = ticks.value
            )
        }

    }
    fun getName():String
    {
        nameLoaded=true
        if(name.isNotEmpty())
            return name;
        else
        {
            val loaded= storageManager.loadData();
            if(loaded!=null)
                return loaded
        }
        return ""
    }
    fun setName(name:String)
    {
        storageManager.saveData(value = name)

        nameLoaded=false
        this.name=name
    }
    fun isNameLoaded():Boolean
    {
        return nameLoaded;
    }

    fun onUserTyping() {
        _clientState.value = _clientState.value.copy(isUserTyping = true)
    }

    fun dismissPlayersNameDialog() {
        _clientState.value = _clientState.value.copy(userRequestedPlayersNameDialog = false)
    }

    fun sendName(playersName: String) {
        _clientState.value = _clientState.value.copy(isUserTyping = false, nameResult = null)
        viewModelScope.launch(Dispatchers.IO) {
            clientManager?.sendPlayersName(playersName)
        }
    }
}