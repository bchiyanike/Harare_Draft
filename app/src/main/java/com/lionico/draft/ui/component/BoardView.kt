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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.lionico.draft.data.engine.Board
import com.lionico.draft.data.model.Move
import com.lionico.draft.data.model.Position
import com.lionico.draft.ui.theme.DarkSquareColor
import com.lionico.draft.ui.theme.LightSquareColor

data class Arrow(
    val from: Position,
    val to: Position,
    val color: Color,
    val lengthFactor: Float = 1.0f  // 1.0 = full square length, <1 for overlapping arrows
)

@Composable
fun BoardView(
    board: Board,
    selectedPosition: Position?,
    validMoves: List<Move>,
    onSquareClick: (Position) -> Unit,
    modifier: Modifier = Modifier,
    arrows: List<Arrow> = emptyList()
) {
    val validMovePositions = validMoves.map { it.to }.toSet()
    val capturePathPositions = validMoves.flatMap { move ->
        move.capturedPositions
    }.toSet()

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        val squareSize = constraints.maxWidth / 8f
        val squareSizeDp = with(LocalDensity.current) { squareSize.toDp() }

        // Arrows are drawn on a separate Canvas layer below pieces but above board
        if (arrows.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (arrow in arrows) {
                    drawArrow(
                        from = arrow.from,
                        to = arrow.to,
                        color = arrow.color,
                        squareSize = squareSize,
                        lengthFactor = arrow.lengthFactor
                    )
                }
            }
        }

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

            selectedPosition?.let { pos ->
                drawRect(
                    color = Color.Yellow.copy(alpha = 0.3f),
                    topLeft = Offset(pos.col * squareSize, pos.row * squareSize),
                    size = androidx.compose.ui.geometry.Size(squareSize, squareSize)
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            for (row in 0..7) {
                for (col in 0..7) {
                    if ((row + col) % 2 != 0) {
                        val position = Position(row, col)
                        val piece = board.getPieceAt(position)
                        val isValidMove = validMovePositions.contains(position)
                        val isCapturePath = capturePathPositions.contains(position)
                        val isSelected = selectedPosition == position

                        if (piece != null) {
                            PieceView(
                                piece = piece,
                                isSelected = isSelected,
                                modifier = Modifier
                                    .offset(
                                        x = (col * squareSizeDp.value).dp,
                                        y = (row * squareSizeDp.value).dp
                                    )
                                    .size(squareSizeDp),
                                onClick = { onSquareClick(position) }
                            )
                        } else if (isValidMove) {
                            EmptySquareView(
                                isValidMove = true,
                                isCapturePath = false,
                                modifier = Modifier
                                    .offset(
                                        x = (col * squareSizeDp.value).dp,
                                        y = (row * squareSizeDp.value).dp
                                    )
                                    .size(squareSizeDp),
                                onClick = { onSquareClick(position) }
                            )
                        } else if (isCapturePath) {
                            EmptySquareView(
                                isValidMove = false,
                                isCapturePath = true,
                                modifier = Modifier
                                    .offset(
                                        x = (col * squareSizeDp.value).dp,
                                        y = (row * squareSizeDp.value).dp
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

/**
 * Draws an arrow from the center of `from` square towards `to` square.
 * If `lengthFactor` < 1.0, the arrow stops short of the target center.
 */
private fun DrawScope.drawArrow(
    from: Position,
    to: Position,
    color: Color,
    squareSize: Float,
    lengthFactor: Float
) {
    val fromX = from.col * squareSize + squareSize / 2
    val fromY = from.row * squareSize + squareSize / 2
    val toX = to.col * squareSize + squareSize / 2
    val toY = to.row * squareSize + squareSize / 2

    val dx = toX - fromX
    val dy = toY - fromY
    val dist = kotlin.math.sqrt(dx * dx + dy * dy)
    if (dist == 0f) return

    // End point scaled by lengthFactor
    val endX = fromX + dx * lengthFactor
    val endY = fromY + dy * lengthFactor

    // Arrowhead dimensions
    val headLength = 12f
    val headAngle = kotlin.math.PI / 6  // 30°

    val angle = kotlin.math.atan2(dy, dx)
    val headX1 = endX - headLength * kotlin.math.cos(angle - headAngle).toFloat()
    val headY1 = endY - headLength * kotlin.math.sin(angle - headAngle).toFloat()
    val headX2 = endX - headLength * kotlin.math.cos(angle + headAngle).toFloat()
    val headY2 = endY - headLength * kotlin.math.sin(angle + headAngle).toFloat()

    // Draw line
    drawLine(
        color = color,
        start = Offset(fromX, fromY),
        end = Offset(endX, endY),
        strokeWidth = 4.dp.toPx()
    )

    // Draw arrowhead
    val path = Path().apply {
        moveTo(endX, endY)
        lineTo(headX1, headY1)
        lineTo(headX2, headY2)
        close()
    }
    drawPath(
        path = path,
        color = color
    )
}