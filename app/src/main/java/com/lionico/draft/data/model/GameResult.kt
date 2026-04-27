// File: app/src/main/java/com/lionico/draft/data/model/GameResult.kt
package com.lionico.draft.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "game_history")
data class GameResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val player1Name: String,
    val player2Name: String,
    val winner: String,
    val gameMode: String,
    val date: Long = System.currentTimeMillis(),
    val durationSeconds: Int,
    val player1PiecesRemaining: Int,
    val player2PiecesRemaining: Int,
    val movesJson: String = "",
    val timeControlLabel: String = ""
) {
    val formattedDate: String
        get() = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            .format(Date(date))

    val formattedTime: String
        get() = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            .format(Date(date))
}