// File: app/src/main/java/com/lionico/draft/ui/screen/ReplayScreen.kt
package com.lionico.draft.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lionico.draft.R
import com.lionico.draft.ui.component.BoardView
import com.lionico.draft.ui.viewmodel.ReplayViewModel

enum class ReplayTab { REPLAY, ANALYSIS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplayScreen(
    gameId: Long,
    initialMode: String,
    onNavigateBack: () -> Unit,
    viewModel: ReplayViewModel = hiltViewModel()
) {
    val boardState by viewModel.boardState.collectAsState()
    val currentMoveIndex by viewModel.currentMoveIndex.collectAsState()
    val totalMoves by viewModel.totalMoves.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val analysisText by viewModel.analysisText.collectAsState()
    val gameResult by viewModel.gameResult.collectAsState()

    var selectedTab by remember { mutableStateOf(if (initialMode == "analysis") ReplayTab.ANALYSIS else ReplayTab.REPLAY) }

    LaunchedEffect(gameId) {
        viewModel.loadGame(gameId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = gameResult?.let { "${it.player1Name} vs ${it.player2Name}" } ?: "Replay",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { selectedTab = ReplayTab.REPLAY },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == ReplayTab.REPLAY)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text("Replay")
                    }
                    Button(
                        onClick = { selectedTab = ReplayTab.ANALYSIS },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == ReplayTab.ANALYSIS)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text("Analysis")
                    }
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            ReplayTab.REPLAY -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    BoardView(
                        board = boardState,
                        selectedPosition = null,
                        validMoves = emptyList(),
                        onSquareClick = {},
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.previousMove() },
                            enabled = currentMoveIndex > 0
                        ) {
                            Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous")
                        }
                        Text(
                            text = "${currentMoveIndex}/${totalMoves}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(
                            onClick = { viewModel.nextMove() },
                            enabled = currentMoveIndex < totalMoves
                        ) {
                            Icon(Icons.Filled.SkipNext, contentDescription = "Next")
                        }
                        IconButton(onClick = { viewModel.toggleAutoPlay() }) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Stop" else "Auto-play"
                            )
                        }
                    }
                }
            }
            ReplayTab.ANALYSIS -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    BoardView(
                        board = boardState,
                        selectedPosition = null,
                        validMoves = emptyList(),
                        onSquareClick = {},
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    )

                    Text(
                        text = "Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        text = analysisText.ifEmpty { "Computing best move..." },
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}