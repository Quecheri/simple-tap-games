package polsl.game.server.data

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.data.Data
import polsl.game.proto.OpCodeProto
import polsl.game.proto.RequestProto
import no.nordicsemi.android.ble.response.ReadResponse

/**
 * This class decodes the received packet using Protobuf.
 */
class PromptResponse : ReadResponse() {
    var name: String? = null
    var selectedAnswerId: Int? = null

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        val bytes = data.value!!
        val request = RequestProto.ADAPTER.decode(bytes)
        when (request.opCode) {
            OpCodeProto.NAME -> { name = request.name }
            OpCodeProto.RESULT -> { selectedAnswerId = request.answerId }
            else -> {}
        }
    }
}