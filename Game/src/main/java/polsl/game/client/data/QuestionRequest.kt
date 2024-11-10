package polsl.game.client.data

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.data.Data
import polsl.game.proto.OpCodeProto
import polsl.game.proto.RequestProto
import polsl.game.server.repository.Question
import polsl.game.server.repository.toQuestion
import polsl.game.server.data.Players
import polsl.game.server.data.Results
import polsl.game.server.data.toPlayers
import polsl.game.server.data.toResults
import no.nordicsemi.android.ble.response.ReadResponse
import polsl.game.server.data.NameResult

/**
 * This class decodes the received packet using Protobuf.
 */
class Request : ReadResponse() {
    var userJoined: Players? = null
    var question: Question? = null
    var answerId: Int? = null
    var isGameOver: Boolean? = null
    var result: Results? = null
    var nameResult: NameResult? = null
    var haystack: Int? = null
    var resultStr: String? = null

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        val bytes = data.value!!
        val request = RequestProto.ADAPTER.decode(bytes)
        when (request.opCode) {
            OpCodeProto.PLAYERS -> { userJoined = request.players?.toPlayers() }
            OpCodeProto.NEW_QUESTION -> { question = request.question?.toQuestion() }
            OpCodeProto.RESPONSE -> { answerId = request.answerId }
            OpCodeProto.GAME_OVER -> { isGameOver = request.isGameOver }
            OpCodeProto.RESULT -> { result = request.results?.toResults() }
            OpCodeProto.HAYSTACK -> { haystack = request.haystackValue }
            OpCodeProto.ERROR -> { nameResult = NameResult(
                isEmptyName = request.isEmptyName,
                isDuplicateName = request.isDuplicateName,
            ) }
            OpCodeProto.RESULT_STR -> { resultStr = request.resultStr }
            else -> {}
        }
    }
}