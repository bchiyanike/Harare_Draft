// File: app/src/main/java/com/lionico/draft/data/model/Move.kt
package com.lionico.draft.data.model

/**
 * Represents a single move or capture sequence in the game.
 * 
 * @property from Starting position of the piece
 * @property to Destination position after the move
 * @property capturedPositions List of positions where opponent pieces were captured
 * @property promotedToKing Whether this move results in a king promotion
 */
data class Move(
    val from: Position,
    val to: Position,
    val capturedPositions: List<Position> = emptyList(),
    val promotedToKing: Boolean = false
) {
    /**
     * Whether this move includes at least one capture.
     */
    val isCapture: Boolean = capturedPositions.isNotEmpty()
    
    /**
     * The number of pieces captured in this move.
     */
    val captureCount: Int = capturedPositions.size
    
    companion object {
        /**
         * Returns an empty/invalid move. Used as a placeholder.
         */
        fun none(): Move = Move(
            from = Position(0),
            to = Position(0),
            capturedPositions = emptyList(),
            promotedToKing = false
        )
        
        val NONE = none()
    }
}