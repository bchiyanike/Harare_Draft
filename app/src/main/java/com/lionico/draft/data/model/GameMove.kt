// File: app/src/main/java/com/lionico/draft/data/model/GameMove.kt
package com.lionico.draft.data.model

/**
 * A lightweight move record for serialization into movesJson.
 * Captures the complete move with the player who made it.
 */
data class GameMove(
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
    val capturedPositions: List<Pair<Int, Int>>,
    val player: Player,
    val promoted: Boolean
) {
    companion object {
        private const val CAPTURE_SEPARATOR = ";"
        private const val FIELD_SEPARATOR = ","
        private const val MOVE_SEPARATOR = "|"

        /**
         * Serializes a list of GameMove to the movesJson string.
         * Format per move: fromRow,fromCol,toRow,toCol,capturedR1:capturedC1;capturedR2:capturedC2,player,promoted
         * Moves joined by '|'.
         */
        fun serialize(moves: List<GameMove>): String {
            return moves.joinToString(MOVE_SEPARATOR) { move ->
                val captured = move.capturedPositions.joinToString(CAPTURE_SEPARATOR) { (r, c) -> "$r:$c" }
                val playerStr = if (move.player == Player.PLAYER_1) "1" else "2"
                val promotedStr = if (move.promoted) "true" else "false"
                "${move.fromRow}$FIELD_SEPARATOR${move.fromCol}$FIELD_SEPARATOR${move.toRow}$FIELD_SEPARATOR${move.toCol}$FIELD_SEPARATOR$captured$FIELD_SEPARATOR$playerStr$FIELD_SEPARATOR$promotedStr"
            }
        }

        /**
         * Deserializes a movesJson string back to a list of GameMove.
         */
        fun deserialize(json: String): List<GameMove> {
            if (json.isBlank()) return emptyList()
            return json.split(MOVE_SEPARATOR).map { moveStr ->
                val parts = moveStr.split(FIELD_SEPARATOR)
                val fromRow = parts[0].toInt()
                val fromCol = parts[1].toInt()
                val toRow = parts[2].toInt()
                val toCol = parts[3].toInt()
                val captured = if (parts[4].isNotEmpty()) {
                    parts[4].split(CAPTURE_SEPARATOR).map { pairStr ->
                        val (r, c) = pairStr.split(":").map { it.toInt() }
                        Pair(r, c)
                    }
                } else {
                    emptyList()
                }
                val player = if (parts[5] == "1") Player.PLAYER_1 else Player.PLAYER_2
                val promoted = parts[6].toBoolean()
                GameMove(fromRow, fromCol, toRow, toCol, captured, player, promoted)
            }
        }

        /**
         * Converts a domain Move and player to a serializable GameMove.
         */
        fun fromDomainMove(move: Move, player: Player): GameMove {
            return GameMove(
                fromRow = move.from.row,
                fromCol = move.from.col,
                toRow = move.to.row,
                toCol = move.to.col,
                capturedPositions = move.capturedPositions.map { Pair(it.row, it.col) },
                player = player,
                promoted = move.promotedToKing
            )
        }

        /**
         * Converts back to a domain Move (without player).
         */
        fun toDomainMove(gameMove: GameMove): Move {
            return Move(
                from = Position(gameMove.fromRow, gameMove.fromCol),
                to = Position(gameMove.toRow, gameMove.toCol),
                capturedPositions = gameMove.capturedPositions.map { (r, c) -> Position(r, c) },
                promotedToKing = gameMove.promoted
            )
        }
    }
}