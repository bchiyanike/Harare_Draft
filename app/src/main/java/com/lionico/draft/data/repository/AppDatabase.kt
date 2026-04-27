// File: app/src/main/java/com/lionico/draft/data/repository/AppDatabase.kt
package com.lionico.draft.data.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lionico.draft.data.model.GameResult

@Database(
    entities = [GameResult::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameHistoryDao(): GameHistoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE game_history ADD COLUMN movesJson TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE game_history ADD COLUMN timeControlLabel TEXT NOT NULL DEFAULT ''"
                )
            }
        }
    }
}