// File: app/src/main/java/com/lionico/draft/data/engine/GameEngine.kt
package com.lionico.draft.data.engine

import com.lionico.draft.data.model.GameStatus
import com.lionico.draft.data.model.Move
import com.lionico.draft.data.model.PieceType
import com.lionico.draft.data.model.Player
import com.lionico.draft.data.model.Position

class GameEngine {

    private var board = Board()
    private var currentPlayer = Player.PLAYER_1
    private var gameStatus = GameStatus.ONGOING
    private var winner: Player? = null

    var onGameEnd: ((winner: Player?, status: GameStatus) -> Unit)? = null

    fun getBoard(): Board = board.copy()
    fun getCurrentPlayer(): Player = currentPlayer
    fun getGameStatus(): GameStatus = gameStatus
    fun getWinner(): Player? = winner

    fun getValidMoves(player: Player): List<Move> {
        if (gameStatus != GameStatus.ONGOING) return emptyList()
        if (player != currentPlayer) return emptyList()
        return MoveValidator(board).getValidMoves(player)
    }

    fun executeMove(move: Move): Boolean {
        if (gameStatus != GameStatus.ONGOING) return false

        val piece = board.getPieceAt(move.from) ?: return false
        if (piece.player != currentPlayer) return false

        val validator = MoveValidator(board)
        val validMoves = validator.getValidMoves(currentPlayer)
        
        if (!validMoves.any { it.from == move.from && it.to == move.to }) {
            return false
        }

        // Apply the complete move (entire sequence already encoded)
        board.setPieceAt(move.from, null)
        move.capturedPositions.forEach { board.setPieceAt(it, null) }
        
        val finalPiece = if (move.promotedToKing) {
            piece.copy(type = PieceType.KING)
        } else {
            piece
        }
        board.setPieceAt(move.to, finalPiece)

        // Always advance turn - MoveValidator already returned complete sequence
        currentPlayer = currentPlayer.opponent()

        updateGameStatus()
        return true
    }

    fun canCurrentPlayerCapture(): Boolean {
        val validator = MoveValidator(board)
        return validator.getValidMoves(currentPlayer).any { it.isCapture }
    }

    fun isMultiCaptureInProgress(): Boolean = false
    fun getMultiCapturePiecePosition(): Position? = null

    private fun updateGameStatus() {
        val validator = MoveValidator(board)
        val previousStatus = gameStatus

        if (!validator.hasAnyValidMove(currentPlayer)) {
            gameStatus = if (currentPlayer == Player.PLAYER_1) {
                GameStatus.PLAYER_2_WINS
            } else {
                GameStatus.PLAYER_1_WINS
            }
            winner = currentPlayer.opponent()
        }

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

        if (previousStatus == GameStatus.ONGOING && gameStatus != GameStatus.ONGOING) {
            onGameEnd?.invoke(winner, gameStatus)
        }
    }

    fun newGame() {
        board = Board()
        currentPlayer = Player.PLAYER_1
        gameStatus = GameStatus.ONGOING
        winner = null
    }

    fun getPieceCounts(): Pair<Int, Int> {
        return Pair(
            board.countPieces(Player.PLAYER_1),
            board.countPieces(Player.PLAYER_2)
        )
    }
}