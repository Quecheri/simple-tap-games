package polsl.game.client.repository

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.ble.BleManager
import polsl.game.client.data.Request
import polsl.game.proto.OpCodeProto
import polsl.game.proto.RequestProto
import polsl.game.server.data.NameResult
import polsl.game.server.data.Players
import polsl.game.server.data.Results
import polsl.game.server.repository.Prompt
import polsl.game.spec.DeviceSpecifications
import polsl.game.spec.PacketMerger
import polsl.game.spec.PacketSplitter
import no.nordicsemi.android.ble.ktx.asResponseFlow
import no.nordicsemi.android.ble.ktx.suspend
import polsl.game.server.data.GameParams

class ClientConnection(
    context: Context,
    private val scope: CoroutineScope,
    private val device: BluetoothDevice,
) : BleManager(context) {
    private val TAG = ClientConnection::class.java.simpleName
    private var characteristic: BluetoothGattCharacteristic? = null

    private val _userJoined = MutableSharedFlow<Players>()
    val userJoined = _userJoined.asSharedFlow()
    private val _prompt = MutableSharedFlow<Prompt>()
    val prompt = _prompt.asSharedFlow()
    private val _answer = MutableSharedFlow<Int>()
    val answer = _answer.asSharedFlow()
    private val _score = MutableSharedFlow<Int>()
    val score = _score.asSharedFlow()
    private val _isGameOver = MutableSharedFlow<Boolean>()
    val isGameOver = _isGameOver.asSharedFlow()
    private val _result = MutableSharedFlow<Results>()
    val result = _result.asSharedFlow()
    private val _nameResult = MutableSharedFlow<NameResult>()
    val error = _nameResult.asSharedFlow()
    private val _resultStr = MutableSharedFlow<String>()
    val resultStr = _resultStr.asSharedFlow()
    private val _gameParams = MutableSharedFlow<GameParams>()
    val gameParams = _gameParams.asSharedFlow()

    override fun log(priority: Int, message: String) {
        Log.println(priority, TAG, message)
    }

    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
    }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            // Return false if a required service has not been discovered.
            gatt.getService(DeviceSpecifications.UUID_SERVICE_DEVICE)?.let { service ->
                characteristic = service.getCharacteristic(DeviceSpecifications.UUID_MSG_CHARACTERISTIC)
            }
            return characteristic != null
        }

        /**
         * Initialize the device by enabling notifications.
         */
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun initialize() {
            requestMtu(512).enqueue() // request Mtu-512

            setNotificationCallback(characteristic)
                // Merges packets until the entire text is present in the stream [PacketMerger.merge].
                .merge(PacketMerger())
                .asResponseFlow<Request>()
                .onEach {
                    it.nameResult?.let { error -> _nameResult.emit(error) }
                    it.userJoined?.let { userJoined -> _userJoined.emit(userJoined) }
                    it.prompt?.let { prompt -> _prompt.emit(prompt) }
                    it.answerId?.let { answer -> _answer.emit(answer) }
                    it.isGameOver?.let { isGameOver -> _isGameOver.emit(isGameOver) }
                    it.result?.let { results -> _result.emit(results) }
                    it.score?.let { haystack -> _score.emit(haystack) }
                    it.resultStr?.let { resultStr -> _resultStr.emit(resultStr) }
                    it.gameParams?.let { gameParams -> _gameParams.emit(gameParams) }
                }
                .launchIn(scope)
            enableNotifications(characteristic).enqueue()
        }

        /**
         * This method is called when the services get invalidated, i.e. when the device is disconnected.
         * When device is disconnected, the reference to the characteristics becomes null.
         */
        override fun onServicesInvalidated() {
            characteristic = null
        }

    /**
     * Connects to the server.
     */
    suspend fun connect() {
        connect(device)
            .retry(4, 300)
            .useAutoConnect(false)
            .timeout(100_000)
            .suspend()
    }

    /**
     * Send selected answer id. The data is split into MTU size packets using
     * packet splitter [PacketSplitter.chunk] before sending it to the server.
     */
    suspend fun sendSelectedAnswer(answer: Int) {
        val result = RequestProto(OpCodeProto.RESULT, answerId = answer)
        val resultByteArray = result.encode()
        writeCharacteristic(
            characteristic,
            resultByteArray,
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        )
        .split(PacketSplitter())
        .suspend()
    }

    /**
     * Send device name. The data is split into MTU size packets using
     * packet splitter [PacketSplitter.chunk] before sending it to the server.
     */
    suspend fun sendPlayersName(name: String) {
        val playersName = RequestProto(OpCodeProto.NAME, name = name)
        val deviceNameByteArray = playersName.encode()
        writeCharacteristic(
            characteristic,
            deviceNameByteArray,
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        )
        .split(PacketSplitter())
        .suspend()
    }

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }

}