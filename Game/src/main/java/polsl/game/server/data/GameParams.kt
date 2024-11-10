package polsl.game.server.data

import polsl.game.proto.GameParamsProto
import polsl.game.server.viewmodel.GameType

data class GameParams(
    val gameType: GameType,
    var timeout: Int,
    var numParam1 : Int?
)

fun GameParams.toProto() = GameParamsProto(gameType.value, timeout,numParam1 ?:0 )
fun GameParamsProto.toGameParams() = GameParams(GameType.fromInt(gameType)!!,timeout,numParam1)
