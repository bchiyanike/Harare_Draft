// File: app/src/main/java/com/lionico/draft/ui/viewmodel/ReplayViewModel.kt
package com.lionico.draft.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lionico.draft.data.ai.Difficulty
import com.lionico.draft.data.engine.Board
import com.lionico.draft.data.engine.GameEngine
import com.lionico.draft.data.model.GameMove
import com.lionico.draft.data.model.GameResult
import com.lionico.draft.data.model.Move
import com.lionico.draft.data.model.Player
import com.lionico.draft.data.repository.GameHistoryRepository
import com.lionico.draft.domain.usecase.GetAIMoveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReplayViewModel @Inject constructor(
    private val gameEngine: GameEngine,
    private val historyRepository: GameHistoryRepository,
    private val getAIMoveUseCase: GetAIMoveUseCase
) : ViewModel() {

    private val _boardState = MutableStateFlow(Board())
    val boardState: StateFlow<Board> = _boardState.asStateFlow()

    private val _currentMoveIndex = MutableStateFlow(-1)
    val currentMoveIndex: StateFlow<Int> = _currentMoveIndex.asStateFlow()

    private val _totalMoves = MutableStateFlow(0)
    val totalMoves: StateFlow<Int> = _totalMoves.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _analysisText = MutableStateFlow("")
    val analysisText: StateFlow<String> = _analysisText.asStateFlow()

    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult: StateFlow<GameResult?> = _gameResult.asStateFlow()

    private var moves = emptyList<GameMove>()
    private var boardStates = mutableListOf<Board>()
    private var autoPlayJob: Job? = null
    private var selectedTab: String = "replay"

    fun loadGame(gameId: Long) {
        viewModelScope.launch {
            val result = historyRepository.getGameById(gameId) ?: return@launch
            _gameResult.value = result
            moves = GameMove.deserialize(result.movesJson)
            _totalMoves.value = moves.size

            // Reconstruct all board states
            gameEngine.newGame()
            boardStates.clear()
            boardStates.add(gameEngine.getBoard()) // initial state
            for (move in moves) {
                gameEngine.executeMove(GameMove.toDomainMove(move))
                boardStates.add(gameEngine.getBoard())
            }

            // Start at the end
            goToMove(moves.size)
            selectedTab = "replay"
            runAnalysis()
        }
    }

    fun setTab(tab: String) {
        selectedTab = tab
        if (tab == "analysis") {
            runAnalysis()
        }
    }

    private fun goToMove(index: Int) {
        val clamped = index.coerceIn(0, moves.size)
        _currentMoveIndex.value = clamped
        _boardState.value = boardStates[clamped]
    }

    fun previousMove() {
        stopAutoPlay()
        goToMove(_currentMoveIndex.value - 1)
    }

    fun nextMove() {
        stopAutoPlay()
        goToMove(_currentMoveIndex.value + 1)
    }

    fun toggleAutoPlay() {
        if (_isPlaying.value) {
            stopAutoPlay()
        } else {
            startAutoPlay()
        }
    }

    private fun startAutoPlay() {
        if (_currentMoveIndex.value == moves.size) {
            goToMove(0)
        }
        _isPlaying.value = true
        autoPlayJob?.cancel()
        autoPlayJob = viewModelScope.launch {
            while (_currentMoveIndex.value < moves.size) {
                delay(1000)
                goToMove(_currentMoveIndex.value + 1)
            }
            _isPlaying.value = false
        }
    }

    private fun stopAutoPlay() {
        autoPlayJob?.cancel()
        _isPlaying.value = false
    }

    private fun runAnalysis() {
        if (moves.isEmpty()) return
        // Use the board state after the last move
        val currentBoard = boardStates.last()
        // Determine the player to move from the last stored move
        val lastPlayer = moves.last().player
        val playerToMove = lastPlayer.opponent()

        // AI analyzes best move for the player whose turn it is
        viewModelScope.launch {
            gameEngine.loadPosition(currentBoard.copy(), playerToMove)
            val bestMove = getAIMoveUseCase(Difficulty.HARD) // use hardest for analysis

            if (bestMove != Move.NONE) {
                // Analyze up to 3 half-moves (plies)
                val moves = mutableListOf<Move>()
                var board = currentBoard.copy()
                var player = playerToMove

                // 1st ply: best move for playerToMove
                gameEngine.loadPosition(board.copy(), player)
                val move1 = getAIMoveUseCase(Difficulty.HARD)
                if (move1 != Move.NONE) {
                    moves.add(move1)
                    board = applyMoveToBoard(board, move1)
                    player = player.opponent()

                    // 2nd ply: best reply by opponent
                    gameEngine.loadPosition(board.copy(), player)
                    val move2 = getAIMoveUseCase(Difficulty.HARD)
                    if (move2 != Move.NONE) {
                        moves.add(move2)
                        board = applyMoveToBoard(board, move2)
                        player = player.opponent()

                        // 3rd ply: best response by playerToMove
                        gameEngine.loadPosition(board.copy(), player)
                        val move3 = getAIMoveUseCase(Difficulty.HARD)
                        if (move3 != Move.NONE) {
                            moves.add(move3)
                        }
                    }
                }

                _analysisText.value = formatAnalysis(moves, playerToMove)
            } else {
                _analysisText.value = "No moves available"
            }
        }
    }

    private fun applyMoveToBoard(board: Board, move: Move): Board {
        val b = board.copy()
        b.setPieceAt(move.from, null)
        move.capturedPositions.forEach { b.setPieceAt(it, null) }
        val piece = board.getPieceAt(move.from)?.let {
            if (move.promotedToKing) it.copy(type = com.lionico.draft.data.model.PieceType.KING) else it
        }
        b.setPieceAt(move.to, piece)
        return b
    }

    private fun formatAnalysis(moves: List<Move>, playerToMove: Player): String {
        if (moves.isEmpty()) return "No moves"
        val notation = moves.joinToString(", then ") { move -> move.toAlgebraicNotation() }
        return "Best: $notation"
    }

    private fun Move.toAlgebraicNotation(): String {
        // Convert to draughts square numbering 1-32 (dark squares only, row-major)
        val fromNum = positionToSquare(from)
        val toNum = positionToSquare(to)
        val sep = if (isCapture) "×" else "-"
        return "$fromNum$sep$toNum"
    }

    private fun positionToSquare(pos: com.lionico.draft.data.model.Position): Int {
        // Dark squares are numbered 1-32, row-major, starting from top-left (row 0)
        // Square number = (row * 4) + (col / 2) + 1  for dark squares
        // Since only dark squares are playable, col is always odd, so col/2 integer division works
        return (pos.row * 4) + (pos.col / 2) + 1
    }

    override fun onCleared() {
        super.onCleared()
        autoPlayJob?.cancel()
    }
}