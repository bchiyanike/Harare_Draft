// File: app/src/main/java/com/lionico/draft/data/engine/MoveValidator.kt
package com.lionico.draft.data.engine

import com.lionico.draft.data.model.Move
import com.lionico.draft.data.model.Piece
import com.lionico.draft.data.model.PieceType
import com.lionico.draft.data.model.Player
import com.lionico.draft.data.model.Position

class MoveValidator(private val board: Board) {

    fun getValidMoves(player: Player): List<Move> {
        val allMoves = mutableListOf<Move>()

        for (position in Position.PLAYABLE_SQUARES) {
            if (board.isPlayerPiece(position, player)) {
                val piece = board.getPieceAt(position)!!
                val captureSequences = findAllCaptureSequences(position, piece, player)
                if (captureSequences.isNotEmpty()) {
                    allMoves.addAll(captureSequences)
                }
            }
        }

        if (allMoves.isNotEmpty()) {
            return allMoves
        }

        for (position in Position.PLAYABLE_SQUARES) {
            if (board.isPlayerPiece(position, player)) {
                val piece = board.getPieceAt(position)!!
                allMoves.addAll(getRegularMovesFrom(position, piece))
            }
        }

        return allMoves
    }

    private fun findAllCaptureSequences(
        start: Position,
        piece: Piece,
        player: Player,
        currentBoard: Board = board,
        visitedPositions: Set<Position> = emptySet()
    ): List<Move> {
        // Cycle detection
        if (start in visitedPositions) return emptyList()

        val sequences = mutableListOf<Move>()
        val directions = Rules.getCaptureDirections(piece.type)

        for (dir in directions) {
            when (piece.type) {
                PieceType.MAN -> {
                    val singleCapture = findManCapture(start, dir, piece, currentBoard)
                    if (singleCapture != null) {
                        // Promotion ends the turn
                        if (singleCapture.promotedToKing) {
                            sequences.add(singleCapture)
                            continue
                        }

                        val newBoard = currentBoard.copy()
                        applyMoveToBoard(newBoard, singleCapture)
                        val newPiece = newBoard.getPieceAt(singleCapture.to) ?: continue

                        val furtherCaptures = findAllCaptureSequences(
                            start = singleCapture.to,
                            piece = newPiece,
                            player = player,
                            currentBoard = newBoard,
                            visitedPositions = visitedPositions + start
                        )

                        if (furtherCaptures.isEmpty()) {
                            sequences.add(singleCapture)
                        } else {
                            for (further in furtherCaptures) {
                                sequences.add(
                                    Move(
                                        from = start,
                                        to = further.to,
                                        capturedPositions = singleCapture.capturedPositions + further.capturedPositions,
                                        promotedToKing = further.promotedToKing
                                    )
                                )
                            }
                        }
                    }
                }
                PieceType.KING -> {
                    val kingCaptures = findKingCaptures(start, dir, piece, currentBoard)
                    if (kingCaptures.isEmpty()) continue

                    // Check each landing: does it allow further capture?
                    val withContinuation = mutableListOf<Move>()
                    val withoutContinuation = mutableListOf<Move>()

                    for (capture in kingCaptures) {
                        val newBoard = currentBoard.copy()
                        applyMoveToBoard(newBoard, capture)
                        val newPiece = newBoard.getPieceAt(capture.to) ?: continue

                        val further = findAllCaptureSequences(
                            start = capture.to,
                            piece = newPiece,
                            player = player,
                            currentBoard = newBoard,
                            visitedPositions = visitedPositions + start
                        )

                        if (further.isEmpty()) {
                            withoutContinuation.add(capture)
                        } else {
                            withContinuation.add(capture)
                            for (f in further) {
                                sequences.add(
                                    Move(
                                        from = start,
                                        to = f.to,
                                        capturedPositions = capture.capturedPositions + f.capturedPositions,
                                        promotedToKing = f.promotedToKing
                                    )
                                )
                            }
                        }
                    }

                    // Only accept short landings if no long landings exist
                    if (withContinuation.isEmpty()) {
                        sequences.addAll(withoutContinuation)
                    }
                }
            }
        }

        return sequences
    }

    private fun findManCapture(
        start: Position,
        dir: Rules.Direction,
        piece: Piece,
        currentBoard: Board = board
    ): Move? {
        val jumped = start.step(dir.dr, dir.dc) ?: return null
        val landing = start.jump(dir.dr, dir.dc) ?: return null

        val jumpedPiece = currentBoard.getPieceAt(jumped) ?: return null
        if (jumpedPiece.player == piece.player) return null
        if (!currentBoard.isEmpty(landing)) return null

        val promoted = piece.isMan() && Rules.isKingRow(landing, piece.player)
        return Move(start, landing, listOf(jumped), promoted)
    }

    private fun findKingCaptures(
        start: Position,
        dir: Rules.Direction,
        piece: Piece,
        currentBoard: Board = board
    ): List<Move> {
        val captures = mutableListOf<Move>()
        var current = start.step(dir.dr, dir.dc) ?: return emptyList()
        var foundOpponent = false
        var jumpedPosition: Position? = null

        while (current.row in 0..7 && current.col in 0..7) {
            val currentPiece = currentBoard.getPieceAt(current)

            when {
                currentPiece == null -> {
                    if (foundOpponent) {
                        captures.add(Move(start, current, listOf(jumpedPosition!!), false))
                    }
                }
                currentPiece.player == piece.player -> break
                currentPiece.player != piece.player -> {
                    if (foundOpponent) break
                    foundOpponent = true
                    jumpedPosition = current
                }
            }
            current = current.step(dir.dr, dir.dc) ?: break
        }

        return captures
    }

    private fun getRegularMovesFrom(position: Position, piece: Piece): List<Move> {
        val moves = mutableListOf<Move>()
        val directions = Rules.getMoveDirections(piece.type, piece.player)

        for (dir in directions) {
            when (piece.type) {
                PieceType.MAN -> {
                    val landing = position.step(dir.dr, dir.dc) ?: continue
                    if (board.isEmpty(landing)) {
                        val promoted = piece.isMan() && Rules.isKingRow(landing, piece.player)
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

    private fun applyMoveToBoard(board: Board, move: Move) {
        val piece = board.getPieceAt(move.from) ?: return
        board.setPieceAt(move.from, null)
        move.capturedPositions.forEach { board.setPieceAt(it, null) }
        val finalPiece = if (move.promotedToKing) piece.copy(type = PieceType.KING) else piece
        board.setPieceAt(move.to, finalPiece)
    }

    fun hasAnyValidMove(player: Player): Boolean = getValidMoves(player).isNotEmpty()

    fun getCaptureMovesFrom(position: Position): List<Move> {
        val piece = board.getPieceAt(position) ?: return emptyList()
        return findAllCaptureSequences(position, piece, piece.player)
    }

    fun hasCaptureMoveFrom(position: Position): Boolean = getCaptureMovesFrom(position).isNotEmpty()

    fun getPiecePlayer(position: Position): Player? = board.getPieceAt(position)?.player
}