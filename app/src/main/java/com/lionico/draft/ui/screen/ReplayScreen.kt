// File: app/src/main/java/com/lionico/draft/ui/screen/ReplayScreen.kt
package com.lionico.draft.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lionico.draft.data.model.Player
import com.lionico.draft.ui.component.BoardView
import com.lionico.draft.ui.theme.BlackSideColor
import com.lionico.draft.ui.theme.MoveCurrentBg
import com.lionico.draft.ui.theme.MovePlayedColor
import com.lionico.draft.ui.theme.MoveUpcomingColor
import com.lionico.draft.ui.theme.RedSideColor
import com.lionico.draft.ui.viewmodel.MoveEntry
import com.lionico.draft.ui.viewmodel.ReplayViewModel
import com.lionico.draft.ui.component.Arrow
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplayScreen(
    gameId: Long,
    initialMode: String,
    onNavigateBack: () -> Unit,
    onContinueWithAI: (gameId: Long, moveIndex: Int, humanSide: Player) -> Unit,
    viewModel: ReplayViewModel = hiltViewModel()
) {
    val boardState by viewModel.boardState.collectAsState()
    val currentMoveIndex by viewModel.currentMoveIndex.collectAsState()
    val totalMoves by viewModel.totalMoves.collectAsState()
    val moveList by viewModel.moveList.collectAsState()
    val gameResult by viewModel.gameResult.collectAsState()
    val analysisText by viewModel.analysisText.collectAsState()
    val playedArrow by viewModel.playedArrow.collectAsState()
    val bestArrow by viewModel.bestArrow.collectAsState()

    var selectedTab by remember { mutableStateOf(if (initialMode == "analysis") "analysis" else "replay") }
    var showSidePicker by remember { mutableStateOf(false) }
    var isAutoPlaying by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(gameId) {
        viewModel.loadGame(gameId)
    }

    // Inform ViewModel when tab changes to trigger analysis
    LaunchedEffect(selectedTab) {
        viewModel.setTab(selectedTab)
    }

    // Auto-scroll to current move
    LaunchedEffect(currentMoveIndex) {
        if (currentMoveIndex in 1..totalMoves) {
            listState.animateScrollToItem(currentMoveIndex - 1)
        }
    }

    // Auto-play logic
    LaunchedEffect(isAutoPlaying) {
        if (isAutoPlaying && currentMoveIndex < totalMoves) {
            while (isAutoPlaying && currentMoveIndex < totalMoves) {
                delay(1000)
                viewModel.nextMove()
            }
            isAutoPlaying = false
        }
    }

    // Side picker dialog
    if (showSidePicker) {
        AlertDialog(
            onDismissRequest = { showSidePicker = false },
            title = { Text("Choose your side") },
            text = { Text("You will play as:") },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            showSidePicker = false
                            viewModel.getContinuePosition()?.let { (id, idx) ->
                                onContinueWithAI(id, idx, Player.PLAYER_1)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedSideColor)
                    ) {
                        Text("Red", color = Color.White)
                    }
                    Button(
                        onClick = {
                            showSidePicker = false
                            viewModel.getContinuePosition()?.let { (id, idx) ->
                                onContinueWithAI(id, idx, Player.PLAYER_2)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlackSideColor)
                    ) {
                        Text("Black", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSidePicker = false }) {
                    Text("Cancel")
                }
            }
        )
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
                            contentDescription = "Back"
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
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { selectedTab = "replay" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == "replay")
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text("Replay")
                    }
                    Button(
                        onClick = { selectedTab = "analysis" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == "analysis")
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
            "replay" -> {
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
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(8.dp),
                        arrows = emptyList()  // no arrows in replay mode
                    )

                    Text(
                        text = "${currentMoveIndex} / $totalMoves",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(moveList, key = { it.index }) { entry ->
                            MoveListItem(
                                entry = entry,
                                currentMoveIndex = currentMoveIndex,
                                onClick = { viewModel.goToMove(entry.index) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.previousMove() },
                            enabled = currentMoveIndex > 1
                        ) {
                            Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous")
                        }
                        IconButton(
                            onClick = { isAutoPlaying = !isAutoPlaying }
                        ) {
                            Icon(
                                imageVector = if (isAutoPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = if (isAutoPlaying) "Stop" else "Auto-play"
                            )
                        }
                        IconButton(
                            onClick = { viewModel.nextMove() },
                            enabled = currentMoveIndex < totalMoves
                        ) {
                            Icon(Icons.Filled.SkipNext, contentDescription = "Next")
                        }
                    }

                    Button(
                        onClick = { showSidePicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Continue vs AI", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            "analysis" -> {
                val arrows = listOfNotNull(playedArrow, bestArrow)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BoardView(
                        board = boardState,
                        selectedPosition = null,
                        validMoves = emptyList(),
                        onSquareClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(8.dp),
                        arrows = arrows
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = analysisText,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MoveListItem(entry: MoveEntry, currentMoveIndex: Int, onClick: () -> Unit) {
    val isPlayed = entry.index < currentMoveIndex
    val backgroundColor = if (entry.isCurrent) MoveCurrentBg else Color.Transparent
    val textColor = when {
        entry.isCurrent -> MoveUpcomingColor
        isPlayed -> MovePlayedColor
        else -> MoveUpcomingColor
    }
    val fontWeight = if (entry.isCurrent) FontWeight.Bold else FontWeight.Normal

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${entry.index}.",
            color = textColor,
            fontWeight = fontWeight,
            fontSize = 14.sp,
            modifier = Modifier.width(24.dp)
        )
        Text(
            text = entry.notation,
            color = textColor,
            fontWeight = fontWeight,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = if (entry.player == Player.PLAYER_1) "Red" else "Black",
            color = textColor,
            fontSize = 12.sp,
            fontWeight = fontWeight
        )
    }
}