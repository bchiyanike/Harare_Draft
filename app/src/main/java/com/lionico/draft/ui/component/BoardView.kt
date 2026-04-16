// File: app/src/main/java/com/lionico/draft/ui/component/BoardView.kt
package com.lionico.draft.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lionico.draft.data.engine.Board
import com.lionico.draft.data.model.Piece
import com.lionico.draft.data.model.Position
import com.lionico.draft.ui.theme.DarkSquareColor
import com.lionico.draft.ui.theme.LightSquareColor

/**
 * Main board view that renders the 8x8 board and all pieces.
 */
@Composable
fun BoardView(
    board: Board,
    selectedPosition: Position?,
    validMovePositions: Set<Position>,
    onSquareClick: (Position) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        val squareSize = constraints.maxWidth / 8
        val squareSizeDp = with(LocalDensity.current) { squareSize.toDp() }
        
        // Draw the board squares
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (row in 0..7) {
                for (col in 0..7) {
                    val isDark = (row + col) % 2 != 0
                    val color = if (isDark) DarkSquareColor else LightSquareColor
                    
                    drawRect(
                        color = color,
                        topLeft = Offset(col * squareSize, row * squareSize),
                        size = androidx.compose.ui.geometry.Size(squareSize, squareSize)
                    )
                }
            }
            
            // Highlight selected square
            selectedPosition?.let { pos ->
                val row = pos.row()
                val col = pos.col()
                drawRect(
                    color = Color.Yellow.copy(alpha = 0.3f),
                    topLeft = Offset(col * squareSize, row * squareSize),
                    size = androidx.compose.ui.geometry.Size(squareSize, squareSize)
                )
            }
        }
        
        // Draw pieces on top of the board
        Box(modifier = Modifier.fillMaxSize()) {
            for (row in 0..7) {
                for (col in 0..7) {
                    // Only dark squares contain pieces
                    if ((row + col) % 2 != 0) {
                        val position = Position.fromRowCol(row, col)
                        if (position != null) {
                            val piece = board.getPieceAt(position)
                            val isValidMove = validMovePositions.contains(position)
                            val isSelected = selectedPosition == position
                            
                            if (piece != null) {
                                PieceView(
                                    piece = piece,
                                    isSelected = isSelected,
                                    modifier = Modifier
                                        .offset(
                                            x = (col * squareSize).toDp(),
                                            y = (row * squareSize).toDp()
                                        )
                                        .size(squareSizeDp),
                                    onClick = { onSquareClick(position) }
                                )
                            } else if (isValidMove) {
                                // Show valid move indicator on empty square
                                EmptySquareView(
                                    isValidMove = true,
                                    modifier = Modifier
                                        .offset(
                                            x = (col * squareSize).toDp(),
                                            y = (row * squareSize).toDp()
                                        )
                                        .size(squareSizeDp),
                                    onClick = { onSquareClick(position) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper extension to convert pixel value to Dp.
 */
@Composable
private fun Float.toDp(): Dp {
    return with(LocalDensity.current) { this@toDp.toDp() }
}