// File: app/src/main/java/com/lionico/draft/ui/viewmodel/HistoryViewModel.kt
package com.lionico.draft.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lionico.draft.data.model.GameResult
import com.lionico.draft.data.repository.GameHistoryRepository
import com.lionico.draft.domain.QuoteManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: GameHistoryRepository,
    quoteManager: QuoteManager
) : ViewModel() {

    val history: StateFlow<List<GameResult>> = repository.getAllResults()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentQuote: StateFlow<String> = quoteManager.currentQuote
}