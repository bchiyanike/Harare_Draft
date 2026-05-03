// File: app/src/main/java/com/lionico/draft/ui/screen/GameScreen.kt
package com.lionico.draft.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lionico.draft.R
import com.lionico.draft.data.model.GameStatus
import com.lionico.draft.data.model.Player
import com.lionico.draft.data.model.TimeControl
import com.lionico.draft.ui.component.BoardView
import com.lionico.draft.ui.component.ClockView
import com.lionico.draft.ui.component.GameControls
import com.lionico.draft.ui.component.GameStatusBar
import com.lionico.draft.ui.theme.RatingNegativeRed
import com.lionico.draft.ui.theme.RatingNeutralGray
import com.lionico.draft.ui.theme.RatingPositiveGreen
import com.lionico.draft.ui.viewmodel.GameMode
import com.lionico.draft.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameMode: GameMode,
    timeControl: TimeControl,
    gameId: Long? = null,
    continueMoveIndex: Int? = null,
    humanSide: Player? = null,
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
    val resolvedHumanSide by viewModel.humanSide.collectAsState()
    val pieceCounts = viewModel.getPieceCounts()

    // Rating states
    val playerRating by viewModel.playerRating.collectAsState()
    val opponentRating by viewModel.opponentRating.collectAsState()
    val ratingDelta by viewModel.ratingDelta.collectAsState()
    val showRatingAnimation by viewModel.showRatingAnimation.collectAsState()

    LaunchedEffect(gameMode, timeControl, gameId, continueMoveIndex, humanSide) {
        if (gameId != null && continueMoveIndex != null && humanSide != null) {
            viewModel.continueFromPosition(gameId, continueMoveIndex, humanSide, timeControl)
        } else if (gameId != null) {
            viewModel.continueFromGame(gameId, timeControl)
        } else {
            viewModel.startGame(gameMode, timeControl)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            gameId != null -> stringResource(R.string.play_vs_computer)
                            gameMode == GameMode.PLAYER_VS_PLAYER -> stringResource(R.string.play_vs_player)
                            else -> stringResource(R.string.play_vs_computer)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                ClockView(
                    player1Time = viewModel.formatTime(clockState.player1TimeSeconds),
                    player2Time = viewModel.formatTime(clockState.player2TimeSeconds),
                    activePlayer = clockState.activePlayer,
                    player1Name = player1Name,
                    player2Name = player2Name,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Player ratings row
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${playerRating.roundToInt()} Elo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${opponentRating.roundToInt()} Elo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

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
                        .padding(8.dp),
                    flipped = (resolvedHumanSide == Player.PLAYER_2)
                )

                GameControls(
                    onNewGame = { viewModel.resetGame() },
                    onBack = onNavigateBack,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Rating animation overlay
            if (showRatingAnimation) {
                RatingAnimationOverlay(
                    delta = ratingDelta,
                    onDismiss = { viewModel.dismissRatingAnimation() }
                )
            }

            // Game over dialog (appears after rating animation dismissed)
            if (gameStatus != GameStatus.ONGOING && !showRatingAnimation) {
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
}

@Composable
private fun RatingAnimationOverlay(
    delta: Int,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(2500)
        visible = false
        delay(300) // Allow exit animation
        onDismiss()
    }

    val color = when {
        delta > 0 -> RatingPositiveGreen
        delta < 0 -> RatingNegativeRed
        else -> RatingNeutralGray
    }

    val deltaText = when {
        delta > 0 -> stringResource(R.string.rating_delta_positive, delta)
        delta < 0 -> stringResource(R.string.rating_delta_negative, delta)
        else -> stringResource(R.string.rating_delta_zero)
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color.copy(alpha = 0.85f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = deltaText,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onError,
                textAlign = TextAlign.Center
            )
        }
    }
}