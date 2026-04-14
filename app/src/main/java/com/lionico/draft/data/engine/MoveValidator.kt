// File: app/src/main/java/com/lionico/draft/data/engine/MoveValidator.kt
package com.lionico.draft.data.engine

import com.lionico.draft.data.model.Move
import com.lionico.draft.data.model.Piece
import com.lionico.draft.data.model.PieceType
import com.lionico.draft.data.model.Player
import com.lionico.draft.data.model.Position

/**
 * Validates and generates all possible moves for a given board state.
 * Handles both regular moves and captures, enforcing compulsory capture rules.
 */
class MoveValidator(private val board: Board) {
    
    /**
     * Returns all valid moves for the specified player.
     * If any capture moves exist, only capture moves are returned (compulsory capture rule).
     */
    fun getValidMoves(player: Player): List<Move> {
        val captureMoves = mutableListOf<Move>()
        val regularMoves = mutableListOf<Move>()
        
        // First, collect all capture moves
        for (index in 0..31) {
            val position = Position(index)
            if (board.isPlayerPiece(position, player)) {
                captureMoves.addAll(getCaptureMovesFrom(position))
            }
        }
        
        // If any captures exist, only return captures (compulsory rule)
        if (captureMoves.isNotEmpty()) {
            return captureMoves
        }
        
        // Otherwise, collect regular moves
        for (index in 0..31) {
            val position = Position(index)
            if (board.isPlayerPiece(position, player)) {
                regularMoves.addAll(getRegularMovesFrom(position))
            }
        }
        
        return regularMoves
    }
    
    /**
     * Checks if the specified player has any valid moves.
     */
    fun hasAnyValidMove(player: Player): Boolean {
        return getValidMoves(player).isNotEmpty()
    }
    
    /**
     * Returns all capture moves available from a specific position.
     */
    fun getCaptureMovesFrom(position: Position): List<Move> {
        val piece = board.getPieceAt(position) ?: return emptyList()
        val directions = getDirectionsForPiece(piece)
        
        return directions.mapNotNull { direction ->
            if (!Rules.isValidDirection(position, direction)) return@mapNotNull null
            
            val jumpIndex = position.index + direction
            val jumpPosition = Position(jumpIndex)
            
            // Check if there's an opponent piece to capture
            if (!board.isEmpty(jumpPosition) && 
                !board.isPlayerPiece(jumpPosition, piece.player)) {
                
                val landIndex = jumpIndex + direction
                if (landIndex !in 0..31) return@mapNotNull null
                
                val landPosition = Position(landIndex)
                
                // Check if landing square is empty
                if (board.isEmpty(landPosition) && 
                    Rules.isValidDirection(jumpPosition, direction)) {
                    
                    val promoted = piece.isMan() && 
                                  Rules.isKingRow(landPosition, piece.player)
                    Move(position, landPosition, listOf(jumpPosition), promoted)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }
    
    /**
     * Checks if a piece at the given position has any capture moves available.
     */
    fun hasCaptureMoveFrom(position: Position): Boolean {
        return getCaptureMovesFrom(position).isNotEmpty()
    }
    
    /**
     * Returns all regular (non-capture) moves available from a specific position.
     */
    private fun getRegularMovesFrom(position: Position): List<Move> {
        val piece = board.getPieceAt(position) ?: return emptyList()
        val directions = getDirectionsForPiece(piece)
        
        return directions.mapNotNull { direction ->
            if (!Rules.isValidDirection(position, direction)) return@mapNotNull null
            
            val toIndex = position.index + direction
            val toPosition = Position(toIndex)
            
            if (board.isEmpty(toPosition)) {
                val promoted = piece.isMan() && 
                              Rules.isKingRow(toPosition, piece.player)
                Move(position, toPosition, emptyList(), promoted)
            } else {
                null
            }
        }
    }
    
    /**
     * Returns the valid movement directions for a given piece.
     */
    private fun getDirectionsForPiece(piece: Piece): List<Int> {
        return when (piece.type) {
            PieceType.MAN -> Rules.getManDirections(piece.player)
            PieceType.KING -> Rules.kingDirections
        }
    }
    
    /**
     * Returns the player who owns the piece at the given position.
     */
    fun getPiecePlayer(position: Position): Player? {
        return board.getPieceAt(position)?.player
    }
}