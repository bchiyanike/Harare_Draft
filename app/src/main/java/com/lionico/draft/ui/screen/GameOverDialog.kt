// File: app/src/main/java/com/lionico/draft/ui/screen/GameOverDialog.kt
package com.lionico.draft.ui.screen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.lionico.draft.data.model.GameStatus
import com.lionico.draft.data.model.Player

/**
 * Dialog displayed when the game ends.
 * Shows the winner and provides options to restart or return to menu.
 */
@Composable
fun GameOverDialog(
    gameStatus: GameStatus,
    winner: Player?,
    onNewGame: () -> Unit,
    onMainMenu: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    val title = when (gameStatus) {
        GameStatus.PLAYER_1_WINS -> "Player 1 Wins!"
        GameStatus.PLAYER_2_WINS -> "Player 2 Wins!"
        GameStatus.DRAW -> "It's a Draw!"
        else -> "Game Over"
    }
    
    val message = when (gameStatus) {
        GameStatus.PLAYER_1_WINS -> "Congratulations! Player 1 has won the game."
        GameStatus.PLAYER_2_WINS -> "Congratulations! Player 2 has won the game."
        GameStatus.DRAW -> "Neither player can force a win. The game ends in a draw."
        else -> "The game has ended."
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
            Text(text = message)
        },
        confirmButton = {
            Button(
                onClick = onNewGame,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("New Game")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onMainMenu) {
                Text("Main Menu")
            }
        }
    )
}