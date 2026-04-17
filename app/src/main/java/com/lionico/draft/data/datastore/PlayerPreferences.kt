// File: app/src/main/java/com/lionico/draft/data/datastore/PlayerPreferences.kt
package com.lionico.draft.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
        
        val AI_NAMES = listOf(
            "Tinsley", "Chinook", "Lafferty", "Baba", "Wyllie",
            "Sturges", "Anderson", "Jordan", "Stewart", "Cohen"
        )
        
        fun randomAIName(): String = AI_NAMES.random()
    }
    
    val playerNames: Flow<PlayerNames> = context.dataStore.data.map { prefs ->
        PlayerNames(
            player1Name = prefs[PLAYER_1_NAME] ?: "You",
            player2Name = prefs[PLAYER_2_NAME] ?: randomAIName()
        )
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
    
    suspend fun resetToDefault() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}