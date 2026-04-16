// File: app/src/main/java/com/lionico/draft/ui/component/PieceView.kt
package com.lionico.draft.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.lionico.draft.data.model.Piece
import com.lionico.draft.data.model.PieceType
import com.lionico.draft.data.model.Player
import com.lionico.draft.ui.theme.Player1PieceColor
import com.lionico.draft.ui.theme.Player2PieceColor
import com.lionico.draft.ui.theme.SelectedPieceBorder

/**
 * Composable that renders a single game piece.
 */
@Composable
fun PieceView(
    piece: Piece,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val pieceColor = when (piece.player) {
        Player.PLAYER_1 -> Player1PieceColor
        Player.PLAYER_2 -> Player2PieceColor
    }
    
    // Animate selection scale
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "piece_scale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(40.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2 - 4.dp.toPx()
            
            // Draw piece shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.3f),
                radius = radius,
                center = center.copy(y = center.y + 2.dp.toPx())
            )
            
            // Draw main piece
            drawCircle(
                color = pieceColor,
                radius = radius,
                center = center
            )
            
            // Draw highlight (3D effect)
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = radius * 0.7f,
                center = center.copy(
                    x = center.x - radius * 0.2f,
                    y = center.y - radius * 0.2f
                )
            )
            
            // Draw border
            drawCircle(
                color = Color.Black.copy(alpha = 0.5f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
            
            // Draw selection indicator
            if (isSelected) {
                drawCircle(
                    color = SelectedPieceBorder,
                    radius = radius + 3.dp.toPx(),
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
            
            // Draw king crown
            if (piece.type == PieceType.KING) {
                drawKingCrown(center, radius)
            }
        }
    }
}

/**
 * Draws a crown on a king piece.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawKingCrown(
    center: Offset,
    pieceRadius: Float
) {
    val crownRadius = pieceRadius * 0.35f
    val crownY = center.y - pieceRadius * 0.2f
    
    // Crown base
    drawCircle(
        color = Color.Yellow,
        radius = crownRadius,
        center = Offset(center.x, crownY)
    )
    
    // Crown points
    val pointOffset = crownRadius * 0.6f
    drawCircle(
        color = Color.Yellow,
        radius = crownRadius * 0.5f,
        center = Offset(center.x - pointOffset, crownY - pointOffset * 0.5f)
    )
    drawCircle(
        color = Color.Yellow,
        radius = crownRadius * 0.5f,
        center = Offset(center.x + pointOffset, crownY - pointOffset * 0.5f)
    )
    drawCircle(
        color = Color.Yellow,
        radius = crownRadius * 0.5f,
        center = Offset(center.x, crownY - pointOffset)
    )
    
    // Crown border
    drawCircle(
        color = Color.Black.copy(alpha = 0.5f),
        radius = crownRadius,
        center = Offset(center.x, crownY),
        style = Stroke(width = 1.dp.toPx())
    )
}

/**
 * Preview piece (empty square placeholder).
 */
@Composable
fun EmptySquareView(
    isValidMove: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.clickable(enabled = onClick != null) { onClick?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        if (isValidMove) {
            Canvas(modifier = Modifier.size(40.dp)) {
                drawCircle(
                    color = Color.Green.copy(alpha = 0.4f),
                    radius = size.width / 3,
                    center = Offset(size.width / 2, size.height / 2)
                )
            }
        }
    }
}