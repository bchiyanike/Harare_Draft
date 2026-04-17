// File: app/src/main/java/com/lionico/draft/ui/screen/GameScreen.kt
package com.lionico.draft.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lionico.draft.R
import com.lionico.draft.data.ai.Difficulty
import com.lionico.draft.data.model.GameStatus
import com.lionico.draft.ui.component.BoardView
import com.lionico.draft.ui.component.ClockView
import com.lionico.draft.ui.component.GameControls
import com.lionico.draft.ui.component.GameStatusBar
import com.lionico.draft.ui.viewmodel.GameMode
import com.lionico.draft.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    val validMoves by viewModel.validMoves.collectAsState()
    val isAIThinking by viewModel.isAIThinking.collectAsState()
    val clockState by viewModel.clockState.collectAsState()
    val player1Name by viewModel.player1Name.collectAsState()
    val player2Name by viewModel.player2Name.collectAsState()
    val pieceCounts = viewModel.getPieceCounts()
    
    LaunchedEffect(gameMode, difficulty) {
        viewModel.setGameMode(gameMode, difficulty)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (gameMode) {
                            GameMode.PLAYER_VS_PLAYER -> stringResource(R.string.play_vs_player)
                            GameMode.PLAYER_VS_COMPUTER -> stringResource(R.string.play_vs_computer)
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
            ClockView(
                player1Time = viewModel.formatTime(clockState.player1TimeSeconds),
                player2Time = viewModel.formatTime(clockState.player2TimeSeconds),
                activePlayer = clockState.activePlayer,
                player1Name = player1Name,
                player2Name = player2Name,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            GameStatusBar(
                currentPlayer = currentPlayer,
                player1Pieces = pieceCounts.first,
                player2Pieces = pieceCounts.second,
                player1Name = player1Name,
                player2Name = player2Name,
                isAIThinking = isAIThinking,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            BoardView(
                board = boardState,
                selectedPosition = selectedPosition,
                validMoves = validMoves,
                onSquareClick = viewModel::onSquareClick,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            )
            
            GameControls(
                onNewGame = { viewModel.resetGame() },
                onBack = onNavigateBack,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        if (gameStatus != GameStatus.ONGOING) {
            GameOverDialog(
                gameStatus = gameStatus,
                winner = winner,
                player1Name = player1Name,
                player2Name = player2Name,
                onNewGame = { viewModel.resetGame() },
                onMainMenu = onNavigateBack
            )
        }
    }
}