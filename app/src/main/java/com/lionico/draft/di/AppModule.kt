// File: app/src/main/java/com/lionico/draft/di/AppModule.kt
package com.lionico.draft.di

import android.content.Context
import androidx.room.Room
import com.lionico.draft.data.ai.AIPlayer
import com.lionico.draft.data.datastore.PlayerPreferences
import com.lionico.draft.data.engine.GameEngine
import com.lionico.draft.data.repository.AppDatabase
import com.lionico.draft.data.repository.GameHistoryDao
import com.lionico.draft.data.repository.GameHistoryRepository
import com.lionico.draft.domain.usecase.CheckGameOverUseCase
import com.lionico.draft.domain.usecase.ExecuteMoveUseCase
import com.lionico.draft.domain.usecase.GetAIMoveUseCase
import com.lionico.draft.domain.usecase.ValidateMoveUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- Game Engine & AI ---

    @Provides
    @Singleton
    fun provideGameEngine(): GameEngine = GameEngine()

    @Provides
    @Singleton
    fun provideAIPlayer(): AIPlayer = AIPlayer()

    // --- Use Cases ---

    @Provides
    fun provideValidateMoveUseCase(gameEngine: GameEngine): ValidateMoveUseCase =
        ValidateMoveUseCase(gameEngine)

    @Provides
    fun provideExecuteMoveUseCase(gameEngine: GameEngine): ExecuteMoveUseCase =
        ExecuteMoveUseCase(gameEngine)

    @Provides
    fun provideCheckGameOverUseCase(gameEngine: GameEngine): CheckGameOverUseCase =
        CheckGameOverUseCase(gameEngine)

    @Provides
    fun provideGetAIMoveUseCase(
        gameEngine: GameEngine,
        aiPlayer: AIPlayer
    ): GetAIMoveUseCase = GetAIMoveUseCase(gameEngine, aiPlayer)

    // --- DataStore ---

    @Provides
    @Singleton
    fun providePlayerPreferences(
        @ApplicationContext context: Context
    ): PlayerPreferences = PlayerPreferences(context)

    // --- Room Database ---

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "draft_history.db"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()
    }

    @Provides
    fun provideGameHistoryDao(database: AppDatabase): GameHistoryDao =
        database.gameHistoryDao()

    @Provides
    @Singleton
    fun provideGameHistoryRepository(dao: GameHistoryDao): GameHistoryRepository =
        GameHistoryRepository(dao)
}