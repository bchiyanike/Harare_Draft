// File: app/src/main/java/com/lionico/draft/ui/viewmodel/MainMenuViewModel.kt
package com.lionico.draft.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lionico.draft.data.ai.Difficulty
import com.lionico.draft.data.datastore.PlayerPreferences
import com.lionico.draft.data.repository.GameHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainMenuViewModel @Inject constructor(
    private val preferences: PlayerPreferences,
    private val historyRepository: GameHistoryRepository
) : ViewModel() {

    val playerNames = preferences.playerNames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val difficulty = preferences.difficulty
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Difficulty.MEDIUM)

    val playerRating = preferences.playerRating
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1200f)

    val lastRatingDelta = preferences.lastRatingDelta
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val gameCount = historyRepository.getAllResults()
        .map { results -> results.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setPlayerNames(player1: String, player2: String) {
        viewModelScope.launch {
            preferences.setPlayer1Name(player1)
            preferences.setPlayer2Name(player2)
        }
    }

    fun setDifficulty(difficulty: Difficulty) {
        viewModelScope.launch {
            preferences.setDifficulty(difficulty)
        }
    }
}