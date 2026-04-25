// File: app/src/main/java/com/lionico/draft/MainActivity.kt
package com.lionico.draft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lionico.draft.data.ai.Difficulty
import com.lionico.draft.ui.screen.DebugScreen
import com.lionico.draft.ui.screen.GameScreen
import com.lionico.draft.ui.screen.MainMenuScreen
import com.lionico.draft.ui.theme.LionicoTheme
import com.lionico.draft.ui.viewmodel.GameMode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LionicoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "debug" // --- DEBUG: change back to "main_menu" before release ---
    ) {
        // --- DEBUG: remove this composable before release ---
        composable("debug") {
            DebugScreen(
                onContinue = {
                    navController.navigate("main_menu") {
                        popUpTo("debug") { inclusive = true }
                    }
                }
            )
        }
        // --- END DEBUG ---

        composable("main_menu") {
            MainMenuScreen(
                onPlayVsFriendSameDevice = {
                    navController.navigate("game/player_vs_player/medium")
                },
                onPlayVsAI = { difficulty ->
                    navController.navigate("game/player_vs_computer/${difficulty.name.lowercase()}")
                }
            )
        }

        composable(
            route = "game/{mode}/{difficulty}",
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("difficulty") {
                    type = NavType.StringType
                    defaultValue = "medium"
                }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "player_vs_player"
            val difficultyString = backStackEntry.arguments?.getString("difficulty") ?: "medium"

            val gameMode = when (mode) {
                "player_vs_computer" -> GameMode.PLAYER_VS_COMPUTER
                else -> GameMode.PLAYER_VS_PLAYER
            }

            val difficulty = when (difficultyString.lowercase()) {
                "easy" -> Difficulty.EASY
                "hard" -> Difficulty.HARD
                else -> Difficulty.MEDIUM
            }

            GameScreen(
                gameMode = gameMode,
                difficulty = difficulty,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
