// File: app/src/main/java/com/lionico/draft/ui/component/GameStatusBar.kt
package com.lionico.draft.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lionico.draft.R
import com.lionico.draft.data.model.Player
import com.lionico.draft.ui.theme.Player1PieceColor
import com.lionico.draft.ui.theme.Player2PieceColor

@Composable
fun GameStatusBar(
    currentPlayer: Player,
    player1Pieces: Int,
    player2Pieces: Int,
    isAIThinking: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerInfo(
                playerName = stringResource(R.string.player_1_short),
                pieceCount = player1Pieces,
                pieceColor = Player1PieceColor,
                isActive = currentPlayer == Player.PLAYER_1
            )

            TurnIndicator(
                currentPlayer = currentPlayer,
                isAIThinking = isAIThinking
            )

            PlayerInfo(
                playerName = stringResource(R.string.player_2_short),
                pieceCount = player2Pieces,
                pieceColor = Player2PieceColor,
                isActive = currentPlayer == Player.PLAYER_2
            )
        }
    }
}

@Composable
private fun PlayerInfo(
    playerName: String,
    pieceCount: Int,
    pieceColor: Color,
    isActive: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(
                modifier = Modifier.size(16.dp)
            ) {
                drawCircle(
                    color = pieceColor,
                    radius = size.minDimension / 2
                )
                if (isActive) {
                    drawCircle(
                        color = Color.Yellow,
                        radius = size.minDimension / 2,
                        style = Stroke(
                            width = 3.dp.toPx()
                        )
                    )
                }
            }
            
            Text(
                text = playerName,
                modifier = Modifier.padding(start = 8.dp),
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        
        Text(
            text = "$pieceCount",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun TurnIndicator(
    currentPlayer: Player,
    isAIThinking: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isAIThinking) {
                stringResource(R.string.ai_thinking)
            } else {
                stringResource(R.string.turn)
            },
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (currentPlayer == Player.PLAYER_1) {
                        Player1PieceColor.copy(alpha = 0.3f)
                    } else {
                        Player2PieceColor.copy(alpha = 0.3f)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (currentPlayer == Player.PLAYER_1) {
                    stringResource(R.string.player_1_short)
                } else {
                    stringResource(R.string.player_2_short)
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (currentPlayer == Player.PLAYER_1) {
                    Player1PieceColor
                } else {
                    Player2PieceColor
                }
            )
        }
    }
}