// File: app/src/main/java/com/lionico/draft/ui/viewmodel/MainMenuViewModel.kt
package com.lionico.draft.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lionico.draft.data.datastore.PlayerPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainMenuViewModel @Inject constructor(
    private val playerPreferences: PlayerPreferences
) : ViewModel() {
    
    val playerNames = playerPreferences.playerNames
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    fun setPlayerNames(player1: String, player2: String) {
        viewModelScope.launch {
            playerPreferences.setPlayer1Name(player1)
            playerPreferences.setPlayer2Name(player2)
        }
    }
    
    fun setPlayer1Name(name: String) {
        viewModelScope.launch {
            playerPreferences.setPlayer1Name(name)
        }
    }
    
    fun setPlayer2Name(name: String) {
        viewModelScope.launch {
            playerPreferences.setPlayer2Name(name)
        }
    }
    
    fun resetToDefaultNames() {
        viewModelScope.launch {
            playerPreferences.resetToDefault()
        }
    }
}