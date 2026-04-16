// File: app/src/main/java/com/lionico/draft/ui/screen/GameScreen.kt
package com.lionico.draft.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lionico.draft.data.ai.Difficulty
import com.lionico.draft.data.model.GameStatus
import com.lionico.draft.ui.component.BoardView
import com.lionico.draft.ui.component.GameControls
import com.lionico.draft.ui.component.GameStatusBar
import com.lionico.draft.ui.viewmodel.GameMode
import com.lionico.draft.ui.viewmodel.GameViewModel

/**
 * Main game screen that displays the board and handles gameplay.
 */
@Composable
fun GameScreen(
    gameMode: GameMode,
    difficulty: Difficulty = Difficulty.MEDIUM,
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val boardState by viewModel.boardState.collectAsState()
    val currentPlayer by viewModel.currentPlayer.collectAsState()
    val gameStatus by viewModel.gameStatus.collectAsState()
    val winner by viewModel.winner.collectAsState()
    val selectedPosition by viewModel.selectedPosition.collectAsState()
    val validMovePositions by viewModel.validMovePositions.collectAsState()
    val isAIThinking by viewModel.isAIThinking.collectAsState()
    val pieceCounts = viewModel.getPieceCounts()
    
    // Initialize game mode when screen loads
    LaunchedEffect(gameMode, difficulty) {
        viewModel.setGameMode(gameMode, difficulty)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (gameMode) {
                            GameMode.PLAYER_VS_PLAYER -> "Player vs Player"
                            GameMode.PLAYER_VS_COMPUTER -> "Player vs Computer"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Game status bar
            GameStatusBar(
                currentPlayer = currentPlayer,
                player1Pieces = pieceCounts.first,
                player2Pieces = pieceCounts.second,
                isAIThinking = isAIThinking,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            // Game board
            BoardView(
                board = boardState,
                selectedPosition = selectedPosition,
                validMovePositions = validMovePositions,
                onSquareClick = viewModel::onSquareClick,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            )
            
            // Game controls
            GameControls(
                onNewGame = { viewModel.resetGame() },
                onBack = onNavigateBack,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Game over dialog
        if (gameStatus != GameStatus.ONGOING) {
            GameOverDialog(
                gameStatus = gameStatus,
                winner = winner,
                onNewGame = { viewModel.resetGame() },
                onMainMenu = onNavigateBack
            )
        }
    }
}