// File: app/src/main/java/com/lionico/draft/data/ai/Difficulty.kt
package com.lionico.draft.data.ai

/**
 * AI difficulty levels for Player vs Computer mode.
 * 
 * @property depth Search depth for the minimax algorithm
 */
enum class Difficulty(val depth: Int) {
    /** Easy - Searches 2 moves ahead. Fast, beginner-friendly. */
    EASY(2),
    
    /** Medium - Searches 4 moves ahead. Balanced challenge. */
    MEDIUM(4),
    
    /** Hard - Searches 6 moves ahead. Strong tactical play. */
    HARD(6);
    
    companion object {
        /**
         * Returns a Difficulty from a string name (case-insensitive).
         */
        fun fromString(name: String): Difficulty {
            return when (name.lowercase()) {
                "easy" -> EASY
                "hard" -> HARD
                else -> MEDIUM
            }
        }
    }
}