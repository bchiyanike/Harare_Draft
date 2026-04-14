// File: app/src/main/java/com/lionico/draft/data/model/Position.kt
package com.lionico.draft.data.model

/**
 * Represents a position on the 8x8 board using only the 32 dark squares.
 * Index values range from 0 to 31, mapping to playable dark squares only.
 * 
 * Board layout (dark squares only, numbered 0-31):
 * 
 * Row 0 (top):    0   1   2   3
 * Row 1:          4   5   6   7
 * Row 2:          8   9  10  11
 * Row 3:         12  13  14  15
 * Row 4:         16  17  18  19
 * Row 5:         20  21  22  23
 * Row 6:         24  25  26  27
 * Row 7 (bottom):28  29  30  31
 */
data class Position(
    val index: Int  // 0-31 representing dark squares only
) {
    init {
        require(index in 0..31) { "Position index must be between 0 and 31" }
    }
    
    /**
     * Returns the board row (0-7) for this position.
     */
    fun row(): Int = index / 4
    
    /**
     * Returns the board column (0-7) for this position.
     */
    fun col(): Int {
        val r = row()
        val offset = index % 4
        // Dark squares are offset based on row parity
        return if (r % 2 == 0) offset * 2 + 1 else offset * 2
    }
    
    companion object {
        /**
         * Creates a Position from row and column coordinates.
         * Returns null if the square is not a dark square.
         */
        fun fromRowCol(row: Int, col: Int): Position? {
            if (row !in 0..7 || col !in 0..7) return null
            if ((row + col) % 2 == 0) return null // Light square - not playable
            
            val baseIndex = row * 4
            val offset = if (row % 2 == 0) (col - 1) / 2 else col / 2
            return Position(baseIndex + offset)
        }
        
        /**
         * Checks if the given row and column represent a valid dark square.
         */
        fun isValid(row: Int, col: Int): Boolean {
            return row in 0..7 && col in 0..7 && (row + col) % 2 != 0
        }
    }
}