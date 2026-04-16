// File: app/src/main/java/com/lionico/draft/di/AppModule.kt
package com.lionico.draft.di

import com.lionico.draft.data.ai.AIPlayer
import com.lionico.draft.data.engine.GameEngine
import com.lionico.draft.domain.usecase.CheckGameOverUseCase
import com.lionico.draft.domain.usecase.ExecuteMoveUseCase
import com.lionico.draft.domain.usecase.GetAIMoveUseCase
import com.lionico.draft.domain.usecase.ValidateMoveUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGameEngine(): GameEngine = GameEngine()

    @Provides
    @Singleton
    fun provideAIPlayer(): AIPlayer = AIPlayer()

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
}