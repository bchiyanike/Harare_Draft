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
 * King multi-square movement fully implemented.
 */
class MoveValidator(private val board: Board) {

    /**
     * Returns all valid moves for the specified player.
     * If any capture moves exist, only capture moves are returned (compulsory capture rule).
     */
    fun getValidMoves(player: Player): List<Move> {
        val captureMoves = mutableListOf<Move>()
        val regularMoves = mutableListOf<Move>()

        for (position in Position.PLAYABLE_SQUARES) {
            if (board.isPlayerPiece(position, player)) {
                captureMoves.addAll(getCaptureMovesFrom(position))
            }
        }

        if (captureMoves.isNotEmpty()) {
            return captureMoves
        }

        for (position in Position.PLAYABLE_SQUARES) {
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
     * Handles both men (single jump) and kings (multi-square jumps).
     */
    fun getCaptureMovesFrom(position: Position): List<Move> {
        val piece = board.getPieceAt(position) ?: return emptyList()
        val directions = Rules.getDirections(piece.type, piece.player)
        val captureMoves = mutableListOf<Move>()

        for (dir in directions) {
            when (piece.type) {
                PieceType.MAN -> {
                    val capture = findManCapture(position, dir, piece)
                    if (capture != null) captureMoves.add(capture)
                }
                PieceType.KING -> {
                    captureMoves.addAll(findKingCaptures(position, dir, piece))
                }
            }
        }

        return captureMoves
    }

    /**
     * Finds a man's capture in a single direction.
     * Men jump exactly 2 squares and land immediately behind the captured piece.
     */
    private fun findManCapture(start: Position, dir: Rules.Direction, piece: Piece): Move? {
        val jumped = start.step(dir.dr, dir.dc) ?: return null
        val landing = start.jump(dir.dr, dir.dc) ?: return null

        val jumpedPiece = board.getPieceAt(jumped) ?: return null
        if (jumpedPiece.player == piece.player) return null

        if (!board.isEmpty(landing)) return null

        val promoted = Rules.isKingRow(landing, piece.player)
        return Move(start, landing, listOf(jumped), promoted)
    }

    /**
     * Finds all king captures in a single direction.
     * Kings can jump from any distance, landing on any empty square behind the captured piece.
     * Cannot jump over multiple pieces in one move.
     */
    private fun findKingCaptures(start: Position, dir: Rules.Direction, piece: Piece): List<Move> {
        val captures = mutableListOf<Move>()
        var current = start.step(dir.dr, dir.dc) ?: return emptyList()
        var foundOpponent = false
        var jumpedPosition: Position? = null

        while (current.row in 0..7 && current.col in 0..7) {
            val currentPiece = board.getPieceAt(current)

            when {
                currentPiece == null -> {
                    if (foundOpponent) {
                        val promoted = Rules.isKingRow(current, piece.player)
                        captures.add(Move(start, current, listOf(jumpedPosition!!), promoted))
                    }
                }
                currentPiece.player == piece.player -> {
                    break
                }
                currentPiece.player != piece.player -> {
                    if (foundOpponent) {
                        break
                    } else {
                        foundOpponent = true
                        jumpedPosition = current
                    }
                }
            }

            current = current.step(dir.dr, dir.dc) ?: break
        }

        return captures
    }

    /**
     * Checks if a piece at the given position has any capture moves available.
     */
    fun hasCaptureMoveFrom(position: Position): Boolean {
        return getCaptureMovesFrom(position).isNotEmpty()
    }

    /**
     * Returns all regular (non-capture) moves available from a specific position.
     * Men move 1 square. Kings move any number of squares along empty diagonals.
     */
    private fun getRegularMovesFrom(position: Position): List<Move> {
        val piece = board.getPieceAt(position) ?: return emptyList()
        val directions = Rules.getDirections(piece.type, piece.player)
        val moves = mutableListOf<Move>()

        for (dir in directions) {
            when (piece.type) {
                PieceType.MAN -> {
                    val landing = position.step(dir.dr, dir.dc) ?: continue
                    if (board.isEmpty(landing)) {
                        val promoted = Rules.isKingRow(landing, piece.player)
                        moves.add(Move(position, landing, emptyList(), promoted))
                    }
                }
                PieceType.KING -> {
                    var current = position.step(dir.dr, dir.dc) ?: continue
                    while (current.row in 0..7 && current.col in 0..7) {
                        if (board.isEmpty(current)) {
                            moves.add(Move(position, current, emptyList(), false))
                            current = current.step(dir.dr, dir.dc) ?: break
                        } else {
                            break
                        }
                    }
                }
            }
        }

        return moves
    }

    /**
     * Returns the player who owns the piece at the given position.
     */
    fun getPiecePlayer(position: Position): Player? {
        return board.getPieceAt(position)?.player
    }
}