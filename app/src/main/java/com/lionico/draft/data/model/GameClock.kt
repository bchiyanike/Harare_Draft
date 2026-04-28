// File: app/src/main/java/com/lionico/draft/data/model/GameClock.kt
package com.lionico.draft.data.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ClockState(
    val player1TimeSeconds: Int,
    val player2TimeSeconds: Int,
    val isRunning: Boolean,
    val activePlayer: Player?
)

class GameClock(
    timeControl: TimeControl = TimeControl.PRESETS.first()
) {
    private val incrementSeconds = timeControl.incrementSeconds

    private val _state = MutableStateFlow(
        ClockState(
            player1TimeSeconds = timeControl.totalSeconds,
            player2TimeSeconds = timeControl.totalSeconds,
            isRunning = false,
            activePlayer = null
        )
    )
    val state: StateFlow<ClockState> = _state.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    fun start(player: Player) {
        _state.value = _state.value.copy(
            isRunning = true,
            activePlayer = player
        )
    }

    fun pause() {
        _state.value = _state.value.copy(
            isRunning = false,
            activePlayer = null
        )
    }

    /**
     * Switches the active clock. Applies the increment to the player
     * who just completed their move (the current active player).
     */
    fun switchTo(player: Player) {
        val current = _state.value
        val previousPlayer = current.activePlayer
        // Apply increment to the player who just moved
        val updatedState = when (previousPlayer) {
            Player.PLAYER_1 -> current.copy(
                player1TimeSeconds = current.player1TimeSeconds + incrementSeconds
            )
            Player.PLAYER_2 -> current.copy(
                player2TimeSeconds = current.player2TimeSeconds + incrementSeconds
            )
            else -> current
        }
        _state.value = updatedState.copy(activePlayer = player)
    }

    fun tick() {
        val current = _state.value
        if (!current.isRunning || current.activePlayer == null) return

        _state.value = when (current.activePlayer) {
            Player.PLAYER_1 -> current.copy(
                player1TimeSeconds = (current.player1TimeSeconds - 1).coerceAtLeast(0)
            )
            Player.PLAYER_2 -> current.copy(
                player2TimeSeconds = (current.player2TimeSeconds - 1).coerceAtLeast(0)
            )
            else -> current
        }
    }

    fun isTimeOut(): Player? {
        val current = _state.value
        return when {
            current.player1TimeSeconds <= 0 -> Player.PLAYER_1
            current.player2TimeSeconds <= 0 -> Player.PLAYER_2
            else -> null
        }
    }

    fun reset(timeControl: TimeControl = TimeControl.PRESETS.first()) {
        timerJob?.cancel()
        _state.value = ClockState(
            player1TimeSeconds = timeControl.totalSeconds,
            player2TimeSeconds = timeControl.totalSeconds,
            isRunning = false,
            activePlayer = null
        )
    }

    fun formatTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%02d:%02d".format(mins, secs)
    }
}