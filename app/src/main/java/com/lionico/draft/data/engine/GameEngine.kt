// File: app/src/main/java/com/lionico/draft/data/engine/GameEngine.kt
package com.lionico.draft.data.engine

import com.lionico.draft.data.model.GameStatus
import com.lionico.draft.data.model.Move
import com.lionico.draft.data.model.Player
import com.lionico.draft.data.model.Position

/**
 * Main game logic coordinator.
 * Manages the board state, turn flow, move execution, and game over detection.
 */
class GameEngine {
    
    private var board = Board()
    private var currentPlayer = Player.PLAYER_1
    private var gameStatus = GameStatus.ONGOING
    private var winner: Player? = null
    
    // Track if the current player is in the middle of a multi-capture sequence
    private var isMultiCaptureInProgress = false
    private var multiCapturePiecePosition: Position? = null
    
    /**
     * Returns a copy of the current board state.
     */
    fun getBoard(): Board = board.copy()
    
    /**
     * Returns the player whose turn it currently is.
     */
    fun getCurrentPlayer(): Player = currentPlayer
    
    /**
     * Returns the current game status.
     */
    fun getGameStatus(): GameStatus = gameStatus
    
    /**
     * Returns the winner (null if game is ongoing or draw).
     */
    fun getWinner(): Player? = winner
    
    /**
     * Returns all valid moves for the specified player.
     */
    fun getValidMoves(player: Player): List<Move> {
        if (gameStatus != GameStatus.ONGOING) return emptyList()
        if (player != currentPlayer) return emptyList()
        
        val validator = MoveValidator(board)
        
        // If in multi-capture, only return captures from the active piece
        return if (isMultiCaptureInProgress && multiCapturePiecePosition != null) {
            validator.getCaptureMovesFrom(multiCapturePiecePosition!!)
        } else {
            validator.getValidMoves(player)
        }
    }
    
    /**
     * Executes a move on the board.
     * @return true if the move was valid and executed
     */
    fun executeMove(move: Move): Boolean {
        if (gameStatus != GameStatus.ONGOING) return false
        
        val piece = board.getPieceAt(move.from) ?: return false
        if (piece.player != currentPlayer) return false
        
        // Validate the move
        val validator = MoveValidator(board)
        val validMoves = if (isMultiCaptureInProgress && multiCapturePiecePosition != null) {
            validator.getCaptureMovesFrom(multiCapturePiecePosition!!)
        } else {
            validator.getValidMoves(currentPlayer)
        }
        
        if (!validMoves.any { it.from == move.from && it.to == move.to }) {
            return false
        }
        
        // Execute the move
        board.setPieceAt(move.from, null)
        move.capturedPositions.forEach { board.setPieceAt(it, null) }
        
        val finalPiece = if (move.promotedToKing) {
            piece.promote()
        } else {
            piece
        }
        board.setPieceAt(move.to, finalPiece)
        
        // Check for additional captures from the new position
        val additionalCaptures = if (move.isCapture) {
            validator.getCaptureMovesFrom(move.to)
        } else {
            emptyList()
        }
        
        if (additionalCaptures.isNotEmpty() && move.isCapture) {
            // Multi-capture in progress - same player continues
            isMultiCaptureInProgress = true
            multiCapturePiecePosition = move.to
        } else {
            // Turn complete - switch player
            isMultiCaptureInProgress = false
            multiCapturePiecePosition = null
            currentPlayer = currentPlayer.opponent()
        }
        
        // Check if the game is over
        updateGameStatus()
        
        return true
    }
    
    /**
     * Checks if the current player has any capture moves available.
     */
    fun canCurrentPlayerCapture(): Boolean {
        val validator = MoveValidator(board)
        return validator.getValidMoves(currentPlayer).any { it.isCapture }
    }
    
    /**
     * Checks if a multi-capture sequence is in progress.
     */
    fun isMultiCaptureInProgress(): Boolean = isMultiCaptureInProgress
    
    /**
     * Returns the position of the piece that must continue capturing.
     */
    fun getMultiCapturePiecePosition(): Position? = multiCapturePiecePosition
    
    /**
     * Updates the game status based on the current board state.
     */
    private fun updateGameStatus() {
        val validator = MoveValidator(board)
        
        // Check if current player has no valid moves
        if (!validator.hasAnyValidMove(currentPlayer)) {
            gameStatus = if (currentPlayer == Player.PLAYER_1) {
                GameStatus.PLAYER_2_WINS
            } else {
                GameStatus.PLAYER_1_WINS
            }
            winner = if (currentPlayer == Player.PLAYER_1) Player.PLAYER_2 else Player.PLAYER_1
            return
        }
        
        // Check if either player has no pieces
        val player1Pieces = board.countPieces(Player.PLAYER_1)
        val player2Pieces = board.countPieces(Player.PLAYER_2)
        
        when {
            player1Pieces == 0 -> {
                gameStatus = GameStatus.PLAYER_2_WINS
                winner = Player.PLAYER_2
            }
            player2Pieces == 0 -> {
                gameStatus = GameStatus.PLAYER_1_WINS
                winner = Player.PLAYER_1
            }
        }
    }
    
    /**
     * Starts a new game.
     */
    fun newGame() {
        board = Board()
        currentPlayer = Player.PLAYER_1
        gameStatus = GameStatus.ONGOING
        winner = null
        isMultiCaptureInProgress = false
        multiCapturePiecePosition = null
    }
    
    /**
     * Returns the count of pieces for each player.
     */
    fun getPieceCounts(): Pair<Int, Int> {
        return Pair(
            board.countPieces(Player.PLAYER_1),
            board.countPieces(Player.PLAYER_2)
        )
    }
}