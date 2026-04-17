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
    private val initialTimeSeconds: Int = 300 // 5 minutes default
) {
    private val _state = MutableStateFlow(
        ClockState(
            player1TimeSeconds = initialTimeSeconds,
            player2TimeSeconds = initialTimeSeconds,
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
    
    fun switchTo(player: Player) {
        _state.value = _state.value.copy(activePlayer = player)
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
    
    fun reset(initialSeconds: Int = 300) {
        timerJob?.cancel()
        _state.value = ClockState(
            player1TimeSeconds = initialSeconds,
            player2TimeSeconds = initialSeconds,
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