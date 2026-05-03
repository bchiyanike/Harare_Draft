// File: app/src/main/java/com/lionico/draft/data/datastore/PlayerPreferences.kt
package com.lionico.draft.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lionico.draft.data.ai.AiStrengthProfile
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
        private val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")

        private val PLAYER_RATING = floatPreferencesKey("player_rating")
        private val SELECTED_AI_RATING = intPreferencesKey("selected_ai_rating")
        private val LAST_RATING_DELTA = intPreferencesKey("last_rating_delta")

        val AI_NAMES_BY_RATING = mapOf(
            900  to listOf("Mhofela", "Mukanya", "Mhukahuru"),
            1125 to listOf("Gudo", "Shumba", "Dube"),
            1350 to listOf("Mugabe", "Giribheti", "Tambaoga"),
            1575 to listOf("Tshaka", "Changamire", "Nkomo"),
            1800 to listOf("Mwari", "Sekuru", "Mudzimu"),
            2025 to listOf("Gorilla", "Ngwena", "Mvuu"),
            2250 to listOf("Mhondoro", "Chaminuka", "Nehanda"),
            2475 to listOf("Kaguvi", "Lobengula", "Mzilikazi"),
            2700 to listOf("Monomotapa", "Changamire", "Rozvi")
        )

        fun randomAIName(rating: Int): String {
            val pool = AI_NAMES_BY_RATING[rating] ?: AI_NAMES_BY_RATING[1575]!!
            return "${pool.random()} (${rating})"
        }

        fun randomAIName(difficulty: Difficulty): String {
            val rating = when (difficulty) {
                Difficulty.EASY -> 900
                Difficulty.MEDIUM -> 1575
                Difficulty.HARD -> 2250
            }
            return randomAIName(rating)
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

    val playerRating: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[PLAYER_RATING] ?: 1200f
    }

    val selectedAiRating: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_AI_RATING] ?: AiStrengthProfile.DEFAULT.eloRating
    }

    val lastRatingDelta: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[LAST_RATING_DELTA] ?: 0
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SOUND_ENABLED] ?: true
    }

    val hapticEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAPTIC_ENABLED] ?: true
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

    suspend fun setPlayerRating(rating: Float) {
        context.dataStore.edit { prefs ->
            prefs[PLAYER_RATING] = rating
        }
    }

    suspend fun setSelectedAiRating(rating: Int) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_AI_RATING] = rating
        }
    }

    suspend fun setLastRatingDelta(delta: Int) {
        context.dataStore.edit { prefs ->
            prefs[LAST_RATING_DELTA] = delta
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun resetToDefault() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}// File: app/src/main/java/com/lionico/draft/data/datastore/PlayerPreferences.kt
package com.lionico.draft.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lionico.draft.data.ai.AiStrengthProfile
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
        private val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")

        private val PLAYER_RATING = floatPreferencesKey("player_rating")
        private val SELECTED_AI_RATING = intPreferencesKey("selected_ai_rating")
        private val LAST_RATING_DELTA = intPreferencesKey("last_rating_delta")

        val AI_NAMES_BY_RATING = mapOf(
            900  to listOf("Mhofela", "Mukanya", "Mhukahuru"),
            1125 to listOf("Gudo", "Shumba", "Dube"),
            1350 to listOf("Mugabe", "Giribheti", "Tambaoga"),
            1575 to listOf("Tshaka", "Changamire", "Nkomo"),
            1800 to listOf("Mwari", "Sekuru", "Mudzimu"),
            2025 to listOf("Gorilla", "Ngwena", "Mvuu"),
            2250 to listOf("Mhondoro", "Chaminuka", "Nehanda"),
            2475 to listOf("Kaguvi", "Lobengula", "Mzilikazi"),
            2700 to listOf("Monomotapa", "Changamire", "Rozvi")
        )

        fun randomAIName(rating: Int): String {
            val pool = AI_NAMES_BY_RATING[rating] ?: AI_NAMES_BY_RATING[1575]!!
            return "${pool.random()} (${rating})"
        }

        fun randomAIName(difficulty: Difficulty): String {
            val rating = when (difficulty) {
                Difficulty.EASY -> 900
                Difficulty.MEDIUM -> 1575
                Difficulty.HARD -> 2250
            }
            return randomAIName(rating)
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

    val playerRating: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[PLAYER_RATING] ?: 1200f
    }

    val selectedAiRating: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_AI_RATING] ?: AiStrengthProfile.DEFAULT.eloRating
    }

    val lastRatingDelta: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[LAST_RATING_DELTA] ?: 0
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SOUND_ENABLED] ?: true
    }

    val hapticEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAPTIC_ENABLED] ?: true
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

    suspend fun setPlayerRating(rating: Float) {
        context.dataStore.edit { prefs ->
            prefs[PLAYER_RATING] = rating
        }
    }

    suspend fun setSelectedAiRating(rating: Int) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_AI_RATING] = rating
        }
    }

    suspend fun setLastRatingDelta(delta: Int) {
        context.dataStore.edit { prefs ->
            prefs[LAST_RATING_DELTA] = delta
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun resetToDefault() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}