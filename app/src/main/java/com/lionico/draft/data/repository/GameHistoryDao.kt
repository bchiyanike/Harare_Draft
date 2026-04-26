// File: app/src/main/java/com/lionico/draft/data/repository/GameHistoryDao.kt
package com.lionico.draft.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lionico.draft.data.model.GameResult
import kotlinx.coroutines.flow.Flow

@Dao
interface GameHistoryDao {
    @Insert
    suspend fun insert(result: GameResult)

    @Query("SELECT * FROM game_history ORDER BY date DESC")
    fun getAllResults(): Flow<List<GameResult>>

    @Query("SELECT * FROM game_history WHERE winner = :playerName ORDER BY date DESC")
    fun getWinsByPlayer(playerName: String): Flow<List<GameResult>>

    @Query("SELECT COUNT(*) FROM game_history WHERE winner = :playerName")
    suspend fun getWinCount(playerName: String): Int

    @Query("SELECT * FROM game_history WHERE id = :gameId")
    suspend fun getGameById(gameId: Long): GameResult?

    @Query("DELETE FROM game_history")
    suspend fun clearAll()
}