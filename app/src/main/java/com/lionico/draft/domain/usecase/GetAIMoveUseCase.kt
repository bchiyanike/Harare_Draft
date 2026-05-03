// File: app/src/main/java/com/lionico/draft/domain/usecase/GetAIMoveUseCase.kt
package com.lionico.draft.domain.usecase

import com.lionico.draft.data.ai.AIPlayer
import com.lionico.draft.data.ai.AiStrengthProfile
import com.lionico.draft.data.engine.GameEngine
import com.lionico.draft.data.model.Move
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for getting the AI's next move.
 * Runs the AI computation on a background thread to avoid blocking the UI.
 */
class GetAIMoveUseCase @Inject constructor(
    private val gameEngine: GameEngine,
    private val aiPlayer: AIPlayer
) {

    /**
     * Computes and returns the best move for the AI.
     * This is a suspend function that runs on a background dispatcher.
     *
     * @param profile The AI strength profile (Elo rating, depth, mistake probability)
     * @return The best move found, or Move.NONE if no moves are available
     */
    suspend operator fun invoke(profile: AiStrengthProfile): Move {
        return withContext(Dispatchers.Default) {
            val board = gameEngine.getBoard()
            val currentPlayer = gameEngine.getCurrentPlayer()

            aiPlayer.getBestMove(
                board = board,
                player = currentPlayer,
                profile = profile
            )
        }
    }

    /**
     * Returns a random move for the AI (useful for easy difficulty or testing).
     */
    suspend fun getRandomMove(): Move {
        return withContext(Dispatchers.Default) {
            val board = gameEngine.getBoard()
            val currentPlayer = gameEngine.getCurrentPlayer()

            aiPlayer.getRandomMove(board, currentPlayer)
        }
    }
}