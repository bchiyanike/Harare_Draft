
// File: app/src/main/java/com/lionico/draft/data/engine/Board.kt
package com.lionico.draft.data.engine

import com.lionico.draft.data.model.Piece
import com.lionico.draft.data.model.PieceType
import com.lionico.draft.data.model.Player
import com.lionico.draft.data.model.Position

/**
 * Core board representation using an 8×8 matrix.
 * Only dark squares (where row + col is odd) contain pieces.
 * Light squares are always null.
 * 
 * Row 0 = top of board (Player 2's home)
 * Row 7 = bottom of board (Player 1's home)
 */
class Board {
    
    // 8×8 matrix - null means empty or light square
    private val squares = Array(8) { row ->
        Array<Piece?>(8) { col ->
            INITIAL_SETUP[row][col]
        }
    }
    
    /**
     * Gets the piece at the given position.
     * Returns null if empty or light square.
     */
    fun getPieceAt(position: Position): Piece? {
        return squares[position.row][position.col]
    }
    
    /**
     * Places a piece at the given position.
     * Throws if attempting to place on a light square.
     */
    fun setPieceAt(position: Position, piece: Piece?) {
        require(position.isPlayable) { "Cannot place piece on light square: $position" }
        squares[position.row][position.col] = piece
    }
    
    /**
     * Checks if the given position is empty.
     */
    fun isEmpty(position: Position): Boolean {
        return squares[position.row][position.col] == null
    }
    
    /**
     * Checks if the piece at the given position belongs to the specified player.
     */
    fun isPlayerPiece(position: Position, player: Player): Boolean {
        val piece = squares[position.row][position.col] ?: return false
        return piece.player == player
    }
    
    /**
     * Counts the number of pieces a player has on the board.
     */
    fun countPieces(player: Player): Int {
        var count = 0
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = squares[row][col]
                if (piece?.player == player) count++
            }
        }
        return count
    }
    
    /**
     * Returns a list of all positions containing pieces belonging to the specified player.
     */
    fun getPlayerPositions(player: Player): List<Position> {
        val positions = mutableListOf<Position>()
        for (row in 0..7) {
            for (col in 0..7) {
                if ((row + col) % 2 != 0) {
                    val piece = squares[row][col]
                    if (piece?.player == player) {
                        positions.add(Position(row, col))
                    }
                }
            }
        }
        return positions
    }
    
    /**
     * Creates a deep copy of the board.
     */
    fun copy(): Board {
        val newBoard = Board()
        for (row in 0..7) {
            for (col in 0..7) {
                newBoard.squares[row][col] = this.squares[row][col]
            }
        }
        return newBoard
    }
    
    /**
     * Resets the board to the starting position.
     */
    fun reset() {
        for (row in 0..7) {
            for (col in 0..7) {
                squares[row][col] = INITIAL_SETUP[row][col]
            }
        }
    }
    
    companion object {
        /**
         * Starting positions for African Draughts.
         * PLAYER_1 (bottom) on rows 5-7, PLAYER_2 (top) on rows 0-2.
         * Light squares (row + col even) are always null.
         */
        private val INITIAL_SETUP: Array<Array<Piece?>> = Array(8) { row ->
            Array(8) { col ->
                when {
                    (row + col) % 2 == 0 -> null  // Light square
                    row in 0..2 -> Piece(Player.PLAYER_2, PieceType.MAN)
                    row in 5..7 -> Piece(Player.PLAYER_1, PieceType.MAN)
                    else -> null
                }
            }
        }
    }
}