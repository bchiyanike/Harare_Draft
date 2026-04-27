// File: app/src/main/java/com/lionico/draft/ui/component/PlayerNameDialog.kt
package com.lionico.draft.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lionico.draft.data.ai.Difficulty

@Composable
fun PlayerNameDialog(
    currentPlayer1Name: String,
    currentPlayer2Name: String,
    currentDifficulty: Difficulty,
    isPlayerVsAI: Boolean,
    onConfirm: (String, String, Difficulty) -> Unit,
    onDismiss: () -> Unit
) {
    var player1Name by remember { mutableStateOf(currentPlayer1Name) }
    var player2Name by remember { mutableStateOf(currentPlayer2Name) }
    var difficulty by remember { mutableStateOf(currentDifficulty) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = player1Name,
                    onValueChange = { player1Name = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = player2Name,
                    onValueChange = { player2Name = it },
                    label = {
                        Text(if (isPlayerVsAI) "AI Opponent Name" else "Opponent Name")
                    },
                    singleLine = true,
                    enabled = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Difficulty selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Difficulty",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = {
                            difficulty = when (difficulty) {
                                Difficulty.EASY -> Difficulty.MEDIUM
                                Difficulty.MEDIUM -> Difficulty.HARD
                                Difficulty.HARD -> Difficulty.EASY
                            }
                        }
                    ) {
                        Text(text = difficulty.name)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        player1Name.ifBlank { currentPlayer1Name },
                        player2Name.ifBlank { currentPlayer2Name },
                        difficulty
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}