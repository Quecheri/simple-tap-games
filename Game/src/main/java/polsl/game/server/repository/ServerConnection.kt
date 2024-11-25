package polsl.game.server.repository

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.ble.BleManager
import polsl.game.proto.OpCodeProto
import polsl.game.proto.RequestProto
import polsl.game.server.data.Players
import polsl.game.spec.DeviceSpecifications
import polsl.game.spec.PacketMerger
import polsl.game.spec.PacketSplitter
import polsl.game.server.data.PromptResponse
import polsl.game.server.data.toProto
import no.nordicsemi.android.ble.ktx.asResponseFlow
import no.nordicsemi.android.ble.ktx.suspend
import polsl.game.server.data.GameParams

class ServerConnection(
    context: Context,
    private val scope: CoroutineScope,
    private val device: BluetoothDevice,
): BleManager(context) {
    private var serverCharacteristic: BluetoothGattCharacteristic? = null
    private val TAG = ServerConnection::class.java.simpleName

    private val _playersName = MutableSharedFlow<String>()
    val playersName = _playersName.asSharedFlow()
    private val _score = MutableSharedFlow<String>()
    val score = _score.asSharedFlow()
    private val _clientAnswer = MutableSharedFlow<Int>()
    val clientAnswer = _clientAnswer.asSharedFlow()

    override fun log(priority: Int, message: String) {
        Log.println(priority, TAG, message)
    }

    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
    }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            return true
        }

        override fun onServerReady(server: BluetoothGattServer) {
            server.getService(DeviceSpecifications.UUID_SERVICE_DEVICE)?.let { service ->
                serverCharacteristic = service.getCharacteristic(DeviceSpecifications.UUID_MSG_CHARACTERISTIC)
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun initialize() {
            setWriteCallback(serverCharacteristic)
                // Merges packets until the entire text is present in the stream [PacketMerger.merge].
                .merge(PacketMerger())
                .asResponseFlow<PromptResponse>()
                .onEach {
                    it.name?.let { name ->  _playersName.emit(name)}
                    it.selectedAnswerId?.let { result ->  _clientAnswer.emit(result) }
                }
                .launchIn(scope)

            waitUntilNotificationsEnabled(serverCharacteristic)
                .enqueue()
        }

        override fun onServicesInvalidated() {
            serverCharacteristic = null
        }

    /**
     * Connects to the server.
     */
    suspend fun connect() {
        connect(device)
            .retry(4, 300)
            .useAutoConnect(false)
            .timeout(10_000)
            .suspend()
    }

    /**
     * Send result after game over. The data is split into MTU size packets using
     * packet splitter [PacketSplitter.chunk] before sending it to the client.
     */
    suspend fun gameOver(isGameOver: Boolean) {
        val request = RequestProto(OpCodeProto.GAME_OVER, isGameOver = isGameOver)
        val requestByteArray = request.encode()
        sendNotification(serverCharacteristic, requestByteArray)
            .split(PacketSplitter())
            .suspend()
    }



    /**
     * Send correct answer id. The data is split into MTU size packets using
     * packet splitter [PacketSplitter.chunk] before sending it to the client.
     */
    suspend fun sendAnswerId(AnswerId: Int) {
        val request = RequestProto(OpCodeProto.RESPONSE, answerId = AnswerId)
        val requestByteArray = request.encode()
        sendNotification(serverCharacteristic, requestByteArray)
            .split(PacketSplitter())
            .suspend()
    }

    suspend fun sendScore(score: Int) {
        val request = RequestProto(OpCodeProto.SCORE, scoreValue = score)
        val requestByteArray = request.encode()
        sendNotification(serverCharacteristic, requestByteArray)
            .split(PacketSplitter())
            .suspend()
    }

    /**
     * Send question. The data is split into MTU size
     * packets using packet splitter [PacketSplitter.chunk] before sending it to the client.
     */
    suspend fun sendPrompt(prompt: Prompt) {
        val request = RequestProto(OpCodeProto.NEW_PROMPT, prompt = prompt.toProto())
        val requestByteArray = request.encode()
        sendNotification(serverCharacteristic, requestByteArray)
            .split(PacketSplitter())
            .suspend()
    }

    /**
     * Send players name to all clients to eliminate duplicates. The data is split into MTU size
     * packets using packet splitter [PacketSplitter.chunk] before sending it to the client.
     */
    suspend fun sendNameToAllPlayers(players: Players) {
        val request = RequestProto(OpCodeProto.PLAYERS, players = players.toProto())
        val requestByteArray = request.encode()
        sendNotification(serverCharacteristic, requestByteArray)
            .split(PacketSplitter())
            .suspend()
    }

    /**
     * Send final result and scores to all clients. The data is split into MTU size
     * packets using packet splitter [PacketSplitter.chunk] before sending it to the client.
     */
    suspend fun sendResultStr(results: String) {
        val request = RequestProto(OpCodeProto.RESULT_STR, resultStr=results)
        val requestByteArray = request.encode()
        sendNotification(serverCharacteristic, requestByteArray)
            .split(PacketSplitter())
            .suspend()
    }

    /**
     * Send final result and scores to all clients. The data is split into MTU size
     * packets using packet splitter [PacketSplitter.chunk] before sending it to the client.
     */
    suspend fun sendGameParams(params: GameParams) {
        val request = RequestProto(OpCodeProto.GAME_PARAMS, gameParams = params.toProto())
        val requestByteArray = request.encode()
        sendNotification(serverCharacteristic, requestByteArray)
            .split(PacketSplitter())
            .suspend()
    }

    /**
     * Sends whether or not name provided by the client is empty. The data is split into MTU size
     * packets using packet splitter [PacketSplitter.chunk] before sending it to the client.
     */
    suspend fun sendEmptyNameError(isEmptyName: Boolean){
        val request = RequestProto(OpCodeProto.ERROR, isEmptyName = isEmptyName)
        val requestByteArray = request.encode()
        sendNotification(serverCharacteristic, requestByteArray)
            .split(PacketSplitter())
            .suspend()
    }

    /**
     * Sends whether or not name provided by the client is duplicate. The data is split into MTU size
     * packets using packet splitter [PacketSplitter.chunk] before sending it to the client.
     */
    suspend fun sendDuplicateNameError(isDuplicateName: Boolean){
        val request = RequestProto(OpCodeProto.ERROR, isDuplicateName = isDuplicateName)
        val requestByteArray = request.encode()
        sendNotification(serverCharacteristic, requestByteArray)
            .split(PacketSplitter())
            .suspend()
    }

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }

}