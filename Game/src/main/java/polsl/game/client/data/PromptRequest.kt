package polsl.game.client.data

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.data.Data
import polsl.game.proto.OpCodeProto
import polsl.game.proto.RequestProto
import polsl.game.server.repository.Prompt
import polsl.game.server.repository.toPrompt
import polsl.game.server.data.Players
import polsl.game.server.data.Results
import polsl.game.server.data.toPlayers
import polsl.game.server.data.toResults
import no.nordicsemi.android.ble.response.ReadResponse
import polsl.game.server.data.GameParams
import polsl.game.server.data.NameResult
import polsl.game.server.data.toGameParams

/**
 * This class decodes the received packet using Protobuf.
 */
class Request : ReadResponse() {
    var userJoined: Players? = null
    var prompt: Prompt? = null
    var answerId: Int? = null
    var isGameOver: Boolean? = null
    var result: Results? = null
    var nameResult: NameResult? = null
    var score: Int? = null
    var resultStr: String? = null
    var gameParams: GameParams? = null

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        val bytes = data.value!!
        val request = RequestProto.ADAPTER.decode(bytes)
        when (request.opCode) {
            OpCodeProto.PLAYERS -> { userJoined = request.players?.toPlayers() }
            OpCodeProto.NEW_PROMPT -> { prompt = request.prompt?.toPrompt() }
            OpCodeProto.RESPONSE -> { answerId = request.answerId }
            OpCodeProto.GAME_OVER -> { isGameOver = request.isGameOver }
            OpCodeProto.RESULT -> { result = request.results?.toResults() }
            OpCodeProto.SCORE -> { score = request.scoreValue }
            OpCodeProto.ERROR -> { nameResult = NameResult(
                isEmptyName = request.isEmptyName,
                isDuplicateName = request.isDuplicateName,
            ) }
            OpCodeProto.RESULT_STR -> { resultStr = request.resultStr }
            OpCodeProto.GAME_PARAMS -> { gameParams = request.gameParams?.toGameParams() }
            else -> {}
        }
    }
}