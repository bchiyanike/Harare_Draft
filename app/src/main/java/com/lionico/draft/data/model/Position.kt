// File: app/src/main/java/com/lionico/draft/data/model/Position.kt
package com.lionico.draft.data.model

/**
 * Represents a position on the 8×8 board using row and column coordinates.
 * Only dark squares (where row + col is odd) are playable.
 * 
 * Row 0 = top of board (Player 2's home)
 * Row 7 = bottom of board (Player 1's home)
 * Column 0 = left edge, Column 7 = right edge
 */
data class Position(
    val row: Int,
    val col: Int
) {
    init {
        require(row in 0..7) { "Row must be between 0 and 7" }
        require(col in 0..7) { "Column must be between 0 and 7" }
    }
    
    /**
     * Whether this position is a playable dark square.
     */
    val isPlayable: Boolean
        get() = (row + col) % 2 != 0
    
    /**
     * Returns the position one step in the given direction, or null if off-board.
     */
    fun step(dr: Int, dc: Int): Position? {
        val newRow = row + dr
        val newCol = col + dc
        return if (newRow in 0..7 && newCol in 0..7) {
            Position(newRow, newCol)
        } else {
            null
        }
    }
    
    /**
     * Returns the position two steps in the given direction, or null if off-board.
     * Used for capture landing squares.
     */
    fun jump(dr: Int, dc: Int): Position? {
        val newRow = row + dr * 2
        val newCol = col + dc * 2
        return if (newRow in 0..7 && newCol in 0..7) {
            Position(newRow, newCol)
        } else {
            null
        }
    }
    
    /**
     * Returns the position halfway between this and target.
     * Used to find the captured piece.
     */
    fun midpoint(target: Position): Position {
        return Position(
            row = (row + target.row) / 2,
            col = (col + target.col) / 2
        )
    }
    
    companion object {
        /**
         * All 32 playable dark squares in row-major order.
         */
        val PLAYABLE_SQUARES: List<Position> = run {
            val squares = mutableListOf<Position>()
            for (row in 0..7) {
                for (col in 0..7) {
                    if ((row + col) % 2 != 0) {
                        squares.add(Position(row, col))
                    }
                }
            }
            squares
        }
    }
}