// File: app/src/main/java/com/lionico/draft/ui/component/PlayerNameDialog.kt
package com.lionico.draft.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun PlayerNameDialog(
    currentPlayer1Name: String,
    currentPlayer2Name: String,
    isPlayerVsAI: Boolean,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var player1Name by remember { mutableStateOf(currentPlayer1Name) }
    var player2Name by remember { mutableStateOf(currentPlayer2Name) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Player Names") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        player1Name.ifBlank { currentPlayer1Name },
                        player2Name.ifBlank { currentPlayer2Name }
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