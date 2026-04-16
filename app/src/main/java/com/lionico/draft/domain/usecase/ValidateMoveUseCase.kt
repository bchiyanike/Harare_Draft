// File: app/src/main/java/com/lionico/draft/domain/usecase/ValidateMoveUseCase.kt
package com.lionico.draft.domain.usecase

import com.lionico.draft.data.engine.GameEngine
import com.lionico.draft.data.model.Move
import com.lionico.draft.data.model.Position
import javax.inject.Inject

class ValidateMoveUseCase @Inject constructor(
    private val gameEngine: GameEngine
) {
    operator fun invoke(position: Position): List<Move> {
        val currentPlayer = gameEngine.getCurrentPlayer()
        val allValidMoves = gameEngine.getValidMoves(currentPlayer)
        return allValidMoves.filter { it.from == position }
    }
}