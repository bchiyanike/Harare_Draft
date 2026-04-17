// File: app/src/main/java/com/lionico/draft/data/repository/GameHistoryRepository.kt
package com.lionico.draft.data.repository

import com.lionico.draft.data.model.GameResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameHistoryRepository @Inject constructor(
    private val dao: GameHistoryDao
) {
    fun getAllResults(): Flow<List<GameResult>> = dao.getAllResults()
    
    suspend fun saveResult(result: GameResult) {
        dao.insert(result)
    }
    
    suspend fun getWinCount(playerName: String): Int = dao.getWinCount(playerName)
    
    suspend fun clearHistory() {
        dao.clearAll()
    }
}