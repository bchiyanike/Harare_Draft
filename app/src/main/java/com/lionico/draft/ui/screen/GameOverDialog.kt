// app/src/main/java/com/lionico/draft/ui/screen/GameOverDialog.kt
package com.lionico.draft.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lionico.draft.R
import com.lionico.draft.data.model.GameStatus
import com.lionico.draft.data.model.Player

@Composable
fun GameOverDialog(
    gameStatus: GameStatus,
    winner: Player?,
    player1Name: String,
    player2Name: String,
    currentQuote: String,
    onRematch: () -> Unit,
    onMainMenu: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    // Derive winner name defensively: prefer explicit winner, fall back to gameStatus
    val winnerName = when (winner) {
        Player.PLAYER_1 -> player1Name
        Player.PLAYER_2 -> player2Name
        null -> when (gameStatus) {
            GameStatus.PLAYER_1_WINS -> player1Name
            GameStatus.PLAYER_2_WINS -> player2Name
            else -> null
        }
    }

    val title = when (gameStatus) {
        GameStatus.PLAYER_1_WINS -> stringResource(R.string.game_over_title_player_wins, player1Name)
        GameStatus.PLAYER_2_WINS -> stringResource(R.string.game_over_title_player_wins, player2Name)
        GameStatus.DRAW -> stringResource(R.string.draw)
        else -> stringResource(R.string.game_over)
    }

    val message = when (gameStatus) {
        GameStatus.PLAYER_1_WINS -> stringResource(R.string.game_over_congratulations, player1Name)
        GameStatus.PLAYER_2_WINS -> stringResource(R.string.game_over_congratulations, player2Name)
        GameStatus.DRAW -> stringResource(R.string.draw_message)
        else -> ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message,
                    textAlign = TextAlign.Center
                )
                if (currentQuote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = currentQuote,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onRematch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(stringResource(R.string.rematch_button))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onMainMenu) {
                Text(stringResource(R.string.main_menu))
            }
        }
    )
}