// File: app/src/main/java/com/lionico/draft/ui/screen/GameOverDialog.kt
package com.lionico.draft.ui.screen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.lionico.draft.R
import com.lionico.draft.data.model.GameStatus
import com.lionico.draft.data.model.Player

@Composable
fun GameOverDialog(
    gameStatus: GameStatus,
    winner: Player?,
    onNewGame: () -> Unit,
    onMainMenu: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    val title = when (gameStatus) {
        GameStatus.PLAYER_1_WINS -> stringResource(R.string.player_1_wins)
        GameStatus.PLAYER_2_WINS -> stringResource(R.string.player_2_wins)
        GameStatus.DRAW -> stringResource(R.string.draw)
        else -> stringResource(R.string.game_over)
    }
    
    val message = when (gameStatus) {
        GameStatus.PLAYER_1_WINS -> stringResource(R.string.congratulations_p1)
        GameStatus.PLAYER_2_WINS -> stringResource(R.string.congratulations_p2)
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
            Text(text = message)
        },
        confirmButton = {
            Button(
                onClick = onNewGame,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(stringResource(R.string.new_game))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onMainMenu) {
                Text(stringResource(R.string.main_menu))
            }
        }
    )
}