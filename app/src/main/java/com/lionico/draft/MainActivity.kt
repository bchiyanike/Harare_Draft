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
import com.lionico.draft.ui.screen.HistoryScreen
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
                },
                onHistory = {
                    navController.navigate("history")
                }
            )
        }

        composable("history") {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onReplay = { gameId ->
                    navController.navigate("replay/$gameId?mode=replay")
                },
                onAnalyze = { gameId ->
                    navController.navigate("replay/$gameId?mode=analysis")
                },
                onContinueVsAI = { gameId ->
                    navController.navigate("game/player_vs_computer/medium?gameId=$gameId")
                }
            )
        }

        composable(
            route = "game/{mode}/{difficulty}?gameId={gameId}",
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("difficulty") {
                    type = NavType.StringType
                    defaultValue = "medium"
                },
                navArgument("gameId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "player_vs_player"
            val difficultyString = backStackEntry.arguments?.getString("difficulty") ?: "medium"
            val gameId = backStackEntry.arguments?.getLong("gameId") ?: -1L

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
                gameId = gameId.takeIf { it != -1L },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "replay/{gameId}?mode={mode}",
            arguments = listOf(
                navArgument("gameId") { type = NavType.LongType },
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = "replay"
                }
            )
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getLong("gameId") ?: return@composable
            val mode = backStackEntry.arguments?.getString("mode") ?: "replay"
            // Placeholder — ReplayScreen will be added in next step
            androidx.compose.material3.Text("Replay screen: gameId=$gameId mode=$mode")
        }
    }
}