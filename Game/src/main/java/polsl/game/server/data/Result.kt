package polsl.game.server.data

import polsl.game.proto.ResultProto
import polsl.game.proto.ResultsProto

/**
 * Final result to send to all players.
 * @property result contains a list of players with score.
 */
data class Results(
    val result: List<Result>
)

data class Result(
    val name: String,
    var score: Int,
)

fun Results.toProto() = ResultsProto(result.map { it.toProto() })

fun Result.toProto() = ResultProto(name, score)

fun ResultsProto.toResults() = Results(result.map { it.result() })

fun ResultProto.result() = Result(name, score)




