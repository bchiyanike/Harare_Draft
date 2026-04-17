// File: app/src/main/java/com/lionico/draft/data/engine/Rules.kt
package com.lionico.draft.data.engine

import com.lionico.draft.data.model.PieceType
import com.lionico.draft.data.model.Player
import com.lionico.draft.data.model.Position

object Rules {

    data class Direction(val dr: Int, val dc: Int)
    
    // Men can move forward only, but capture in ALL four diagonals
    fun getManMoveDirections(player: Player): List<Direction> {
        val dr = when (player) {
            Player.PLAYER_1 -> -1
            Player.PLAYER_2 -> 1
        }
        return listOf(
            Direction(dr, -1),
            Direction(dr, 1)
        )
    }
    
    // Men can capture in all four diagonal directions
    val manCaptureDirections = listOf(
        Direction(-1, -1),
        Direction(-1, 1),
        Direction(1, -1),
        Direction(1, 1)
    )
    
    val kingDirections = listOf(
        Direction(-1, -1),
        Direction(-1, 1),
        Direction(1, -1),
        Direction(1, 1)
    )
    
    fun getMoveDirections(pieceType: PieceType, player: Player): List<Direction> {
        return when (pieceType) {
            PieceType.MAN -> getManMoveDirections(player)
            PieceType.KING -> kingDirections
        }
    }
    
    fun getCaptureDirections(pieceType: PieceType): List<Direction> {
        return when (pieceType) {
            PieceType.MAN -> manCaptureDirections
            PieceType.KING -> kingDirections
        }
    }
    
    fun isKingRow(position: Position, player: Player): Boolean {
        return when (player) {
            Player.PLAYER_1 -> position.row == 0
            Player.PLAYER_2 -> position.row == 7
        }
    }
    
    fun isValidCaptureMove(start: Position, end: Position): Boolean {
        val dr = end.row - start.row
        val dc = end.col - start.col
        return kotlin.math.abs(dr) == 2 && 
               kotlin.math.abs(dc) == 2 && 
               end.isPlayable
    }
    
    fun isValidRegularMove(start: Position, end: Position): Boolean {
        val dr = end.row - start.row
        val dc = end.col - start.col
        return kotlin.math.abs(dr) == 1 && 
               kotlin.math.abs(dc) == 1 && 
               end.isPlayable
    }
    
    const val STARTING_PIECE_COUNT = 12
    const val BOARD_SIZE = 8
}