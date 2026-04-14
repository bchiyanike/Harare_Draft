// File: app/src/main/java/com/lionico/draft/data/model/Piece.kt
package com.lionico.draft.data.model

/**
 * Represents the type of a game piece.
 */
enum class PieceType {
    MAN,    // Regular piece - moves forward only
    KING    // Promoted piece - moves in all diagonal directions
}

/**
 * Represents a single game piece on the board.
 * 
 * @property player The player who owns this piece
 * @property type The type of piece (MAN or KING)
 */
data class Piece(
    val player: Player,
    val type: PieceType = PieceType.MAN
) {
    /**
     * Promotes this piece to a king.
     * Returns a new Piece instance with KING type.
     */
    fun promote(): Piece {
        return copy(type = PieceType.KING)
    }
    
    /**
     * Checks if this piece is a man (not a king).
     */
    fun isMan(): Boolean = type == PieceType.MAN
    
    /**
     * Checks if this piece is a king.
     */
    fun isKing(): Boolean = type == PieceType.KING
}