// File: app/src/main/java/com/lionico/draft/data/engine/Board.kt
package com.lionico.draft.data.engine

import com.lionico.draft.data.model.Piece
import com.lionico.draft.data.model.PieceType
import com.lionico.draft.data.model.Player
import com.lionico.draft.data.model.Position

/**
 * Core board representation using a 32-element IntArray for efficiency.
 * 
 * Square values:
 * 0 = empty
 * 1 = PLAYER_1 man
 * 2 = PLAYER_1 king
 * 3 = PLAYER_2 man
 * 4 = PLAYER_2 king
 */
class Board {
    
    // 32 playable dark squares
    val squares = IntArray(32) { INITIAL_SETUP[it] }
    
    /**
     * Gets the piece at the given position.
     * Returns null if the square is empty.
     */
    fun getPieceAt(position: Position): Piece? {
        return when (val value = squares[position.index]) {
            1 -> Piece(Player.PLAYER_1, PieceType.MAN)
            2 -> Piece(Player.PLAYER_1, PieceType.KING)
            3 -> Piece(Player.PLAYER_2, PieceType.MAN)
            4 -> Piece(Player.PLAYER_2, PieceType.KING)
            else -> null
        }
    }
    
    /**
     * Places a piece at the given position.
     * Pass null to clear the square.
     */
    fun setPieceAt(position: Position, piece: Piece?) {
        squares[position.index] = when (piece?.player) {
            Player.PLAYER_1 -> if (piece.type == PieceType.MAN) 1 else 2
            Player.PLAYER_2 -> if (piece.type == PieceType.MAN) 3 else 4
            null -> 0
        }
    }
    
    /**
     * Checks if the given position is empty.
     */
    fun isEmpty(position: Position): Boolean = squares[position.index] == 0
    
    /**
     * Checks if the piece at the given position belongs to the specified player.
     */
    fun isPlayerPiece(position: Position, player: Player): Boolean {
        val value = squares[position.index]
        return when (player) {
            Player.PLAYER_1 -> value == 1 || value == 2
            Player.PLAYER_2 -> value == 3 || value == 4
        }
    }
    
    /**
     * Counts the number of pieces a player has on the board.
     */
    fun countPieces(player: Player): Int {
        return squares.count { value ->
            when (player) {
                Player.PLAYER_1 -> value == 1 || value == 2
                Player.PLAYER_2 -> value == 3 || value == 4
            }
        }
    }
    
    /**
     * Counts the number of kings a player has on the board.
     */
    fun countKings(player: Player): Int {
        return squares.count { value ->
            when (player) {
                Player.PLAYER_1 -> value == 2
                Player.PLAYER_2 -> value == 4
            }
        }
    }
    
    /**
     * Returns a list of all positions containing pieces belonging to the specified player.
     */
    fun getPlayerPositions(player: Player): List<Position> {
        val positions = mutableListOf<Position>()
        for (index in 0..31) {
            val value = squares[index]
            val isPlayerPiece = when (player) {
                Player.PLAYER_1 -> value == 1 || value == 2
                Player.PLAYER_2 -> value == 3 || value == 4
            }
            if (isPlayerPiece) {
                positions.add(Position(index))
            }
        }
        return positions
    }
    
    /**
     * Creates a deep copy of the board.
     */
    fun copy(): Board {
        val newBoard = Board()
        squares.copyInto(newBoard.squares)
        return newBoard
    }
    
    /**
     * Resets the board to the starting position.
     */
    fun reset() {
        INITIAL_SETUP.copyInto(squares)
    }
    
    companion object {
        /**
         * Starting positions for African Draughts.
         * PLAYER_1 (bottom) on rows 5-7, PLAYER_2 (top) on rows 0-2.
         * 
         * Row 0 (top):    PLAYER_2 men (value 3)
         * Row 1:          PLAYER_2 men (value 3)
         * Row 2:          PLAYER_2 men (value 3)
         * Row 3:          Empty (value 0)
         * Row 4:          Empty (value 0)
         * Row 5:          PLAYER_1 men (value 1)
         * Row 6:          PLAYER_1 men (value 1)
         * Row 7 (bottom): PLAYER_1 men (value 1)
         */
        val INITIAL_SETUP = intArrayOf(
            3, 3, 3, 3,    // Row 0
            3, 3, 3, 3,    // Row 1
            3, 3, 3, 3,    // Row 2
            0, 0, 0, 0,    // Row 3
            0, 0, 0, 0,    // Row 4
            1, 1, 1, 1,    // Row 5
            1, 1, 1, 1,    // Row 6
            1, 1, 1, 1     // Row 7
        )
    }
}