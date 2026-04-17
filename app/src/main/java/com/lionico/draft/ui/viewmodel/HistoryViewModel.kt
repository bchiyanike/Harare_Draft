// File: app/src/main/java/com/lionico/draft/ui/viewmodel/HistoryViewModel.kt
package com.lionico.draft.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.lionico.draft.data.model.GameResult
import com.lionico.draft.data.repository.GameHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: GameHistoryRepository
) : ViewModel() {
    
    val history: StateFlow<List<GameResult>> = repository.getAllResults()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}