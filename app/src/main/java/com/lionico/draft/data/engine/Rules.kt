// File: app/src/main/java/com/lionico/draft/data/engine/Rules.kt
package com.lionico.draft.data.engine

import com.lionico.draft.data.model.PieceType
import com.lionico.draft.data.model.Player
import com.lionico.draft.data.model.Position

/**
 * Game rules and constants for African Draughts.
 * Uses row/col vectors instead of index offsets.
 */
object Rules {

    /**
     * Movement direction vectors as (dr, dc) pairs.
     * dr = change in row, dc = change in column.
     */
    data class Direction(val dr: Int, val dc: Int)
    
    /**
     * Movement directions for men (forward only).
     * 
     * For PLAYER_1 (bottom, row 7, moving up): dr = -1
     * For PLAYER_2 (top, row 0, moving down): dr = +1
     */
    fun getManDirections(player: Player): List<Direction> {
        val dr = when (player) {
            Player.PLAYER_1 -> -1  // Moving up (decreasing row)
            Player.PLAYER_2 -> 1   // Moving down (increasing row)
        }
        return listOf(
            Direction(dr, -1),  // Left diagonal
            Direction(dr, 1)    // Right diagonal
        )
    }
    
    /**
     * Movement directions for kings (all diagonals).
     */
    val kingDirections = listOf(
        Direction(-1, -1),  // Up-left
        Direction(-1, 1),   // Up-right
        Direction(1, -1),   // Down-left
        Direction(1, 1)     // Down-right
    )
    
    /**
     * Returns the valid movement directions for a given piece type and player.
     */
    fun getDirections(pieceType: PieceType, player: Player): List<Direction> {
        return when (pieceType) {
            PieceType.MAN -> getManDirections(player)
            PieceType.KING -> kingDirections
        }
    }
    
    /**
     * Checks if a position is on the king row (promotion row).
     * 
     * For PLAYER_1 (bottom player): King row is row 0 (top)
     * For PLAYER_2 (top player): King row is row 7 (bottom)
     */
    fun isKingRow(position: Position, player: Player): Boolean {
        return when (player) {
            Player.PLAYER_1 -> position.row == 0
            Player.PLAYER_2 -> position.row == 7
        }
    }
    
    /**
     * Checks if a move from start to end is a valid capture.
     * A capture must:
     * - Move exactly 2 rows and 2 columns (diagonal jump)
     * - Land on a playable dark square
     */
    fun isValidCaptureMove(start: Position, end: Position): Boolean {
        val dr = end.row - start.row
        val dc = end.col - start.col
        return kotlin.math.abs(dr) == 2 && 
               kotlin.math.abs(dc) == 2 && 
               end.isPlayable
    }
    
    /**
     * Checks if a move from start to end is a valid regular move.
     * A regular move must:
     * - Move exactly 1 row and 1 column (diagonal step)
     * - Land on a playable dark square
     */
    fun isValidRegularMove(start: Position, end: Position): Boolean {
        val dr = end.row - start.row
        val dc = end.col - start.col
        return kotlin.math.abs(dr) == 1 && 
               kotlin.math.abs(dc) == 1 && 
               end.isPlayable
    }
    
    /**
     * Total number of pieces each player starts with.
     */
    const val STARTING_PIECE_COUNT = 12
    
    /**
     * Board size (8×8).
     */
    const val BOARD_SIZE = 8
}