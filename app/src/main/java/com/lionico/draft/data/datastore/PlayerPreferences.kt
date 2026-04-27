// File: app/src/main/java/com/lionico/draft/data/datastore/PlayerPreferences.kt
package com.lionico.draft.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lionico.draft.data.ai.Difficulty
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "player_prefs")

data class PlayerNames(
    val player1Name: String,
    val player2Name: String
)

@Singleton
class PlayerPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val PLAYER_1_NAME = stringPreferencesKey("player_1_name")
        private val PLAYER_2_NAME = stringPreferencesKey("player_2_name")
        private val DIFFICULTY = stringPreferencesKey("difficulty")

        val AI_NAMES_EASY = listOf("Mhofela", "Mukanya", "Mhukahuru")
        val AI_NAMES_MEDIUM = listOf("Mugabe", "Giribheti", "Tambaoga")
        val AI_NAMES_HARD = listOf("Tshaka", "Changamire", "Nkomo")

        fun randomAIName(difficulty: Difficulty): String {
            val pool = when (difficulty) {
                Difficulty.EASY -> AI_NAMES_EASY
                Difficulty.MEDIUM -> AI_NAMES_MEDIUM
                Difficulty.HARD -> AI_NAMES_HARD
            }
            return "${pool.random()} - AI"
        }
    }

    val playerNames: Flow<PlayerNames> = context.dataStore.data.map { prefs ->
        PlayerNames(
            player1Name = prefs[PLAYER_1_NAME] ?: "You",
            player2Name = prefs[PLAYER_2_NAME] ?: "Opponent"
        )
    }

    val difficulty: Flow<Difficulty> = context.dataStore.data.map { prefs ->
        when (prefs[DIFFICULTY]) {
            "easy" -> Difficulty.EASY
            "hard" -> Difficulty.HARD
            else -> Difficulty.MEDIUM
        }
    }

    suspend fun setPlayer1Name(name: String) {
        context.dataStore.edit { prefs ->
            prefs[PLAYER_1_NAME] = name
        }
    }

    suspend fun setPlayer2Name(name: String) {
        context.dataStore.edit { prefs ->
            prefs[PLAYER_2_NAME] = name
        }
    }

    suspend fun setDifficulty(difficulty: Difficulty) {
        context.dataStore.edit { prefs ->
            prefs[DIFFICULTY] = difficulty.name.lowercase()
        }
    }

    suspend fun resetToDefault() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}