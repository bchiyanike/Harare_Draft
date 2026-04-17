// File: app/src/main/java/com/lionico/draft/data/repository/AppDatabase.kt
package com.lionico.draft.data.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lionico.draft.data.model.GameResult

@Database(
    entities = [GameResult::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameHistoryDao(): GameHistoryDao
}