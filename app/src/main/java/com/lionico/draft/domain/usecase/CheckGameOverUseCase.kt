// File: app/src/main/java/com/lionico/draft/domain/usecase/CheckGameOverUseCase.kt
package com.lionico.draft.domain.usecase

import com.lionico.draft.data.engine.GameEngine
import com.lionico.draft.data.model.GameStatus
import javax.inject.Inject

/**
 * Use case for checking if the game has ended.
 * Returns the current game status (ongoing, win, or draw).
 */
class CheckGameOverUseCase @Inject constructor(
    private val gameEngine: GameEngine
) {
    
    /**
     * Returns the current game status.
     * 
     * @return GameStatus indicating if the game is ongoing or has ended
     */
    operator fun invoke(): GameStatus {
        return gameEngine.getGameStatus()
    }
    
    /**
     * Returns the winning player, if any.
     * 
     * @return The winning player, or null if game is ongoing or a draw
     */
    fun getWinner() = gameEngine.getWinner()
    
    /**
     * Checks if the game is still in progress.
     */
    fun isOngoing(): Boolean = gameEngine.getGameStatus() == GameStatus.ONGOING
    
    /**
     * Checks if the game has ended.
     */
    fun isGameOver(): Boolean = gameEngine.getGameStatus() != GameStatus.ONGOING
}