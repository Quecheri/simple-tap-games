package polsl.game.server.data

import polsl.game.proto.PlayerProto
import polsl.game.proto.PlayersProto

/**
 * A list of player's name.
 */
data class Players(
    val player: List<Player>
)

data class Player(
    val name: String
)

fun Players.toProto() = PlayersProto(player.map { it.toProto() })

fun Player.toProto() = PlayerProto(name)

fun PlayersProto.toPlayers() = Players(player.map { it.player() })

fun PlayerProto.player( )= Player(name)