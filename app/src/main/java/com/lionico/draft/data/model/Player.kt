// File: app/src/main/java/com/lionico/draft/data/model/Player.kt
package com.lionico.draft.data.model

/**
 * Represents the two players in the game.
 * PLAYER_1 is typically the bottom player (moves up).
 * PLAYER_2 is typically the top player (moves down).
 */
enum class Player {
    PLAYER_1,
    PLAYER_2;
    
    /**
     * Returns the opponent of the current player.
     */
    fun opponent(): Player {
        return if (this == PLAYER_1) PLAYER_2 else PLAYER_1
    }
}