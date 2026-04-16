// File: app/src/main/java/com/lionico/draft/data/ai/AIPlayer.kt
package com.lionico.draft.data.ai

import com.lionico.draft.data.engine.Board
import com.lionico.draft.data.engine.MoveValidator
import com.lionico.draft.data.model.Move
import com.lionico.draft.data.model.PieceType
import com.lionico.draft.data.model.Player
import kotlin.math.max

/**
 * AI opponent using Negamax algorithm with Alpha-Beta pruning.
 * Provides challenging gameplay optimized for mobile performance.
 */
class AIPlayer {
    
    private val transpositionTable = mutableMapOf<Long, Pair<Int, Int>>()
    
    /**
     * Returns the best move for the current board position.
     */
    fun getBestMove(board: Board, player: Player, difficulty: Difficulty): Move {
        val validator = MoveValidator(board)
        val moves = validator.getValidMoves(player)
        
        if (moves.isEmpty()) return Move.NONE
        if (moves.size == 1) return moves.first()
        
        transpositionTable.clear()
        
        var bestMove = moves.first()
        var bestScore = Int.MIN_VALUE
        
        val sortedMoves = moves.sortedByDescending { it.captureCount }
        
        for (move in sortedMoves) {
            val newBoard = board.copy()
            applyMoveToBoard(newBoard, move)
            
            val score = -negamax(
                board = newBoard,
                player = player.opponent(),
                depth = difficulty.depth - 1,
                alpha = Int.MIN_VALUE,
                beta = Int.MAX_VALUE
            )
            
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        
        return bestMove
    }
    
    /**
     * Negamax algorithm with alpha-beta pruning.
     */
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
        
        val boardHash = computeBoardHash(board, player)
        transpositionTable[boardHash]?.let { (cachedDepth, cachedScore) ->
            if (cachedDepth >= depth) {
                return cachedScore
            }
        }
        
        var currentAlpha = alpha
        var maxScore = Int.MIN_VALUE
        
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
            
            maxScore = max(maxScore, score)
            currentAlpha = max(currentAlpha, score)
            
            if (currentAlpha >= beta) {
                break
            }
        }
        
        transpositionTable[boardHash] = Pair(depth, maxScore)
        return maxScore
    }
    
    /**
     * Applies a move to a board (used during AI simulation).
     */
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
    
    /**
     * Computes a simple hash of the board state for the transposition table.
     */
    private fun computeBoardHash(board: Board, player: Player): Long {
        var hash = 0L
        for (position in com.lionico.draft.data.model.Position.PLAYABLE_SQUARES) {
            val piece = board.getPieceAt(position)
            val pieceValue = when {
                piece == null -> 0
                piece.player == Player.PLAYER_1 && piece.type == PieceType.MAN -> 1
                piece.player == Player.PLAYER_1 && piece.type == PieceType.KING -> 2
                piece.player == Player.PLAYER_2 && piece.type == PieceType.MAN -> 3
                else -> 4
            }
            hash = hash * 5 + pieceValue
        }
        hash = hash * 2 + (if (player == Player.PLAYER_1) 0 else 1)
        return hash
    }
    
    /**
     * Returns a random move (for easy difficulty or fallback).
     */
    fun getRandomMove(board: Board, player: Player): Move {
        val validator = MoveValidator(board)
        val moves = validator.getValidMoves(player)
        return if (moves.isNotEmpty()) moves.random() else Move.NONE
    }
}