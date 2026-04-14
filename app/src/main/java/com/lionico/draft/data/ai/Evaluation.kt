// File: app/src/main/java/com/lionico/draft/data/ai/Evaluation.kt
package com.lionico.draft.data.ai

import com.lionico.draft.data.engine.Board
import com.lionico.draft.data.engine.MoveValidator
import com.lionico.draft.data.model.PieceType
import com.lionico.draft.data.model.Player
import com.lionico.draft.data.model.Position

/**
 * Board evaluation utility for AI scoring.
 * Provides static methods to evaluate how favorable a board position is for a given player.
 */
object Evaluation {
    
    // Piece values
    private const val MAN_VALUE = 100
    private const val KING_VALUE = 175
    
    // Positional bonuses
    private const val CENTER_BONUS = 10
    private const val EDGE_PENALTY = -5
    private const val BACK_ROW_BONUS = 8
    
    // Mobility bonus (per available move)
    private const val MOBILITY_BONUS = 5
    
    /**
     * Evaluates the board from the perspective of the specified player.
     * Returns a positive score if the player is winning, negative if losing.
     */
    fun evaluate(board: Board, player: Player): Int {
        var score = 0
        
        // Material evaluation
        score += evaluateMaterial(board, player)
        
        // Positional evaluation
        score += evaluatePosition(board, player)
        
        // Mobility evaluation
        score += evaluateMobility(board, player)
        
        return score
    }
    
    /**
     * Evaluates material advantage (pieces and kings).
     */
    private fun evaluateMaterial(board: Board, player: Player): Int {
        var score = 0
        
        for (index in 0..31) {
            val position = Position(index)
            val piece = board.getPieceAt(position) ?: continue
            
            val pieceValue = if (piece.type == PieceType.MAN) MAN_VALUE else KING_VALUE
            val multiplier = if (piece.player == player) 1 else -1
            
            score += pieceValue * multiplier
        }
        
        return score
    }
    
    /**
     * Evaluates positional advantages.
     * - Center control is good
     * - Pieces on the edge are vulnerable
     * - Men on the back row are safe from capture
     */
    private fun evaluatePosition(board: Board, player: Player): Int {
        var score = 0
        
        for (index in 0..31) {
            val position = Position(index)
            val piece = board.getPieceAt(position) ?: continue
            
            val multiplier = if (piece.player == player) 1 else -1
            val row = position.row()
            val col = position.col()
            
            // Center control (columns 2-5, rows 2-5)
            if (row in 2..5 && col in 2..5) {
                score += CENTER_BONUS * multiplier
            }
            
            // Edge penalty (columns 0 or 7)
            if (col == 0 || col == 7) {
                score += EDGE_PENALTY * multiplier
            }
            
            // Back row bonus for men (safe from capture)
            if (piece.type == PieceType.MAN) {
                val isBackRow = when (piece.player) {
                    Player.PLAYER_1 -> row == 7  // Bottom row for player 1
                    Player.PLAYER_2 -> row == 0  // Top row for player 2
                }
                if (isBackRow) {
                    score += BACK_ROW_BONUS * multiplier
                }
            }
        }
        
        return score
    }
    
    /**
     * Evaluates mobility (number of available moves).
     * More available moves = better position.
     */
    private fun evaluateMobility(board: Board, player: Player): Int {
        val validator = MoveValidator(board)
        
        val playerMoves = validator.getValidMoves(player).size
        val opponentMoves = validator.getValidMoves(player.opponent()).size
        
        return (playerMoves - opponentMoves) * MOBILITY_BONUS
    }
    
    /**
     * Quick evaluation for endgame situations.
     * Used when time is limited or for shallow searches.
     */
    fun quickEvaluate(board: Board, player: Player): Int {
        var score = 0
        
        for (index in 0..31) {
            val position = Position(index)
            val piece = board.getPieceAt(position) ?: continue
            
            val pieceValue = if (piece.type == PieceType.MAN) MAN_VALUE else KING_VALUE
            val multiplier = if (piece.player == player) 1 else -1
            
            score += pieceValue * multiplier
        }
        
        return score
    }
}