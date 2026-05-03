// File: app/src/main/java/com/lionico/draft/ui/viewmodel/SettingsViewModel.kt
package com.lionico.draft.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lionico.draft.data.ai.AiStrengthProfile
import com.lionico.draft.data.ai.Difficulty
import com.lionico.draft.data.datastore.PlayerPreferences
import com.lionico.draft.domain.QuoteManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: PlayerPreferences,
    quoteManager: QuoteManager
) : ViewModel() {

    val player1Name = preferences.playerNames
        .map { it.player1Name }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "You")

    val player2Name = preferences.playerNames
        .map { it.player2Name }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Opponent")

    val difficulty = preferences.difficulty
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Difficulty.MEDIUM)

    val selectedAiRating = preferences.selectedAiRating
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AiStrengthProfile.DEFAULT.eloRating)

    val soundEnabled = preferences.soundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val hapticEnabled = preferences.hapticEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val currentQuote: StateFlow<String> = quoteManager.currentQuote

    fun setPlayer1Name(name: String) {
        viewModelScope.launch { preferences.setPlayer1Name(name) }
    }

    fun setPlayer2Name(name: String) {
        viewModelScope.launch { preferences.setPlayer2Name(name) }
    }

    fun setDifficulty(difficulty: Difficulty) {
        viewModelScope.launch { preferences.setDifficulty(difficulty) }
    }

    fun setSelectedAiRating(rating: Int) {
        viewModelScope.launch { preferences.setSelectedAiRating(rating) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setSoundEnabled(enabled) }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setHapticEnabled(enabled) }
    }
}