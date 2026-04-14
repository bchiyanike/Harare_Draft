// File: app/src/main/java/com/lionico/draft/data/engine/Rules.kt
package com.lionico.draft.data.engine

import com.lionico.draft.data.model.Player
import com.lionico.draft.data.model.Position

/**
 * Game rules and constants for African Draughts.
 * Contains direction vectors and helper functions for move validation.
 */
object Rules {
    
    /**
     * Movement directions for men (forward only).
     * Values represent index offsets in the 32-element board array.
     * 
     * For PLAYER_1 (bottom, moving up):
     * -4 = up-left diagonal
     * -5 = up-right diagonal
     * 
     * For PLAYER_2 (top, moving down):
     * +4 = down-left diagonal
     * +5 = down-right diagonal
     */
    fun getManDirections(player: Player): List<Int> {
        return when (player) {
            Player.PLAYER_1 -> listOf(-4, -5)  // Moving up the board
            Player.PLAYER_2 -> listOf(4, 5)    // Moving down the board
        }
    }
    
    /**
     * Movement directions for kings (all diagonals).
     * Kings can move both forward and backward.
     */
    val kingDirections = listOf(-5, -4, 4, 5)
    
    /**
     * Checks if a position is on the king row (promotion row).
     * 
     * For PLAYER_1 (bottom player): King row is row 0 (top)
     * For PLAYER_2 (top player): King row is row 7 (bottom)
     */
    fun isKingRow(position: Position, player: Player): Boolean {
        val row = position.row()
        return when (player) {
            Player.PLAYER_1 -> row == 0
            Player.PLAYER_2 -> row == 7
        }
    }
    
    /**
     * Checks if moving from a position in a given direction is valid.
     * Prevents wrapping around board edges.
     * 
     * @param from Starting position
     * @param direction Index offset to move
     * @return true if the move stays on the board and doesn't wrap
     */
    fun isValidDirection(from: Position, direction: Int): Boolean {
        val fromRow = from.row()
        val fromCol = from.col()
        val toIndex = from.index + direction
        
        // Check if destination is within board bounds
        if (toIndex !in 0..31) return false
        
        val toPosition = Position(toIndex)
        val toRow = toPosition.row()
        val toCol = toPosition.col()
        
        // Valid diagonal move must change both row and column by exactly 1
        return kotlin.math.abs(fromCol - toCol) == 1 && 
               kotlin.math.abs(fromRow - toRow) == 1
    }
    
    /**
     * Total number of pieces each player starts with.
     */
    const val STARTING_PIECE_COUNT = 12
    
    /**
     * Board size (8x8).
     */
    const val BOARD_SIZE = 8
    
    /**
     * Number of playable dark squares.
     */
    const val PLAYABLE_SQUARES = 32
}