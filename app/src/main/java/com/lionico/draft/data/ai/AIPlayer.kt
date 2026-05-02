// File: app/src/main/java/com/lionico/draft/data/ai/AIPlayer.kt
package com.lionico.draft.data.ai

import com.lionico.draft.data.engine.Board
import com.lionico.draft.data.engine.MoveValidator
import com.lionico.draft.data.model.Move
import com.lionico.draft.data.model.PieceType
import com.lionico.draft.data.model.Player
import kotlin.random.Random

class AIPlayer {

    /**
     * Returns the best move for the given board and player, according to the
     * [profile] that controls search depth and human‑like mistake probability.
     */
    fun getBestMove(board: Board, player: Player, profile: AiStrengthProfile): Move {
        val validator = MoveValidator(board)
        val moves = validator.getValidMoves(player)

        if (moves.isEmpty()) return Move.NONE
        if (moves.size == 1) return moves.first()

        var bestMove = moves.first()
        var bestScore = Int.MIN_VALUE + 1

        val sortedMoves = moves.sortedByDescending { it.captureCount }

        for (move in sortedMoves) {
            val newBoard = board.copy()
            applyMoveToBoard(newBoard, move)

            val score = -negamax(
                board = newBoard,
                player = player.opponent(),
                depth = profile.maxDepth - 1,
                alpha = Int.MIN_VALUE + 1,
                beta = Int.MAX_VALUE
            )

            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }

        // Mistake: sometimes pick a random legal move instead of the best one
        if (shouldMakeMistake(profile.mistakeProbability)) {
            return moves.random()
        }

        return bestMove
    }

    /**
     * Returns a purely random legal move (no search).
     */
    fun getRandomMove(board: Board, player: Player): Move {
        val validator = MoveValidator(board)
        val moves = validator.getValidMoves(player)
        return if (moves.isNotEmpty()) moves.random() else Move.NONE
    }

    private fun negamax(
        board: Board,
        player: Player,
        depth: Int,
        alpha: Int,
        beta: Int
    ): Int {
        val validator = MoveValidator(board)
        val moves = validator.getValidMoves(player)

        if (depth == 0 || moves.isEmpty()) {
            return Evaluation.evaluate(board, player)
        }

        var currentAlpha = alpha
        var maxScore = Int.MIN_VALUE + 1

        val sortedMoves = moves.sortedByDescending { it.captureCount }

        for (move in sortedMoves) {
            val newBoard = board.copy()
            applyMoveToBoard(newBoard, move)

            val score = -negamax(
                board = newBoard,
                player = player.opponent(),
                depth = depth - 1,
                alpha = -beta,
                beta = -currentAlpha
            )

            if (score > maxScore) maxScore = score
            if (score > currentAlpha) currentAlpha = score

            if (currentAlpha >= beta) break
        }

        return maxScore
    }

    private fun applyMoveToBoard(board: Board, move: Move) {
        val piece = board.getPieceAt(move.from) ?: return
        board.setPieceAt(move.from, null)
        move.capturedPositions.forEach { board.setPieceAt(it, null) }
        val finalPiece = if (move.promotedToKing) {
            piece.copy(type = PieceType.KING)
        } else {
            piece
        }
        board.setPieceAt(move.to, finalPiece)
    }

    private fun shouldMakeMistake(probability: Float): Boolean {
        return Random.nextFloat() < probability
    }
}