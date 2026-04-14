// File: app/src/main/java/com/lionico/draft/data/ai/AIPlayer.kt
package com.lionico.draft.data.ai

import com.lionico.draft.data.engine.Board
import com.lionico.draft.data.engine.MoveValidator
import com.lionico.draft.data.model.Move
import com.lionico.draft.data.model.PieceType
import com.lionico.draft.data.model.Player
import kotlin.math.max
import kotlin.math.min

/**
 * AI opponent using Negamax algorithm with Alpha-Beta pruning.
 * Provides challenging gameplay optimized for mobile performance.
 */
class AIPlayer {
    
    // Transposition table to cache evaluated positions
    private val transpositionTable = mutableMapOf<Long, Pair<Int, Int>>()
    
    /**
     * Returns the best move for the current board position.
     * 
     * @param board Current board state
     * @param player The AI player (usually PLAYER_2)
     * @param difficulty Difficulty level determining search depth
     * @return The best move found
     */
    fun getBestMove(board: Board, player: Player, difficulty: Difficulty): Move {
        val validator = MoveValidator(board)
        val moves = validator.getValidMoves(player)
        
        if (moves.isEmpty()) return Move.NONE
        if (moves.size == 1) return moves.first()
        
        // Clear transposition table for new search
        transpositionTable.clear()
        
        var bestMove = moves.first()
        var bestScore = Int.MIN_VALUE
        
        // Sort moves for better alpha-beta pruning (captures first)
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
     * Returns the score from the perspective of the current player.
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
        
        // Terminal conditions
        if (depth == 0 || moves.isEmpty()) {
            return Evaluation.evaluate(board, player)
        }
        
        // Check transposition table
        val boardHash = computeBoardHash(board, player)
        transpositionTable[boardHash]?.let { (cachedDepth, cachedScore) ->
            if (cachedDepth >= depth) {
                return cachedScore
            }
        }
        
        var currentAlpha = alpha
        var maxScore = Int.MIN_VALUE
        
        // Sort moves for better pruning (captures first)
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
                break // Alpha-beta cutoff
            }
        }
        
        // Store in transposition table
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
        for (i in 0..31) {
            hash = hash * 31 + board.squares[i]
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