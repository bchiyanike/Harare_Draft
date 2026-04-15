// File: app/src/main/java/com/lionico/draft/domain/usecase/ExecuteMoveUseCase.kt
package com.lionico.draft.domain.usecase

import com.lionico.draft.data.engine.GameEngine
import com.lionico.draft.data.model.Move
import javax.inject.Inject

/**
 * Use case for executing a move on the game board.
 * Handles the move execution and triggers game state updates.
 */
class ExecuteMoveUseCase @Inject constructor(
    private val gameEngine: GameEngine
) {
    
    /**
     * Executes the specified move.
     * 
     * @param move The move to execute
     * @return true if the move was valid and successfully executed, false otherwise
     */
    operator fun invoke(move: Move): Boolean {
        return gameEngine.executeMove(move)
    }
}