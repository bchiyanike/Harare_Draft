// File: app/src/main/java/com/lionico/draft/data/model/GameState.kt
package com.lionico.draft.data.model

/**
 * Represents the current status of the game.
 */
enum class GameStatus {
    ONGOING,         // Game is still in progress
    PLAYER_1_WINS,   // Player 1 has won
    PLAYER_2_WINS,   // Player 2 has won
    DRAW             // Game ended in a draw
}

/**
 * Represents the complete state of a game at a point in time.
 * 
 * @property status Current game status (ongoing, win, draw)
 * @property currentPlayer The player whose turn it is
 * @property winner The winning player (null if game is ongoing or draw)
 */
data class GameState(
    val status: GameStatus = GameStatus.ONGOING,
    val currentPlayer: Player = Player.PLAYER_1,
    val winner: Player? = null
) {
    /**
     * Whether the game is still in progress.
     */
    val isOngoing: Boolean = status == GameStatus.ONGOING
    
    /**
     * Whether the game has ended (win or draw).
     */
    val isGameOver: Boolean = status != GameStatus.ONGOING
    
    /**
     * Returns a descriptive message about the current game state.
     */
    fun getStatusMessage(): String {
        return when (status) {
            GameStatus.ONGOING -> "${currentPlayer.name}'s Turn"
            GameStatus.PLAYER_1_WINS -> "Player 1 Wins!"
            GameStatus.PLAYER_2_WINS -> "Player 2 Wins!"
            GameStatus.DRAW -> "Game Ended in a Draw"
        }
    }
}