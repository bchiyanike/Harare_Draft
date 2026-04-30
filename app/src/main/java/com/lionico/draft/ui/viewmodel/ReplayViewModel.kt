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
import com.lionico.draft.ui.component.Arrow
import com.lionico.draft.ui.theme.BestArrowColor
import com.lionico.draft.ui.theme.PlayedArrowColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class MoveEntry(
    val index: Int,
    val notation: String,
    val player: Player,
    val isCurrent: Boolean
)

@HiltViewModel
class ReplayViewModel @Inject constructor(
    private val gameEngine: GameEngine,
    private val historyRepository: GameHistoryRepository,
    private val getAIMoveUseCase: GetAIMoveUseCase
) : ViewModel() {

    private val _boardState = MutableStateFlow(Board())
    val boardState: StateFlow<Board> = _boardState.asStateFlow()

    private val _currentMoveIndex = MutableStateFlow(1)
    val currentMoveIndex: StateFlow<Int> = _currentMoveIndex.asStateFlow()

    private val _totalMoves = MutableStateFlow(0)
    val totalMoves: StateFlow<Int> = _totalMoves.asStateFlow()

    private val _moveList = MutableStateFlow<List<MoveEntry>>(emptyList())
    val moveList: StateFlow<List<MoveEntry>> = _moveList.asStateFlow()

    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult: StateFlow<GameResult?> = _gameResult.asStateFlow()

    private val _analysisText = MutableStateFlow("")
    val analysisText: StateFlow<String> = _analysisText.asStateFlow()

    // Arrows for played move and best AI move
    private val _playedArrow = MutableStateFlow<Arrow?>(null)
    val playedArrow: StateFlow<Arrow?> = _playedArrow.asStateFlow()

    private val _bestArrow = MutableStateFlow<Arrow?>(null)
    val bestArrow: StateFlow<Arrow?> = _bestArrow.asStateFlow()

    private var moves = emptyList<GameMove>()
    private var boardStates = mutableListOf<Board>()
    private var analysisJob: Job? = null

    fun loadGame(gameId: Long) {
        viewModelScope.launch {
            val result = historyRepository.getGameById(gameId) ?: return@launch
            _gameResult.value = result
            moves = GameMove.deserialize(result.movesJson)
            _totalMoves.value = moves.size

            // Reconstruct board states
            gameEngine.newGame()
            boardStates.clear()
            boardStates.add(gameEngine.getBoard())
            for (move in moves) {
                gameEngine.executeMove(GameMove.toDomainMove(move))
                boardStates.add(gameEngine.getBoard())
            }

            // Generate move list notation
            val entries = moves.mapIndexed { i, gm ->
                val domainMove = GameMove.toDomainMove(gm)
                MoveEntry(
                    index = i + 1,
                    notation = domainMove.toAlgebraicNotation(),
                    player = gm.player,
                    isCurrent = (i + 1) == 1
                )
            }
            _moveList.value = entries

            goToMove(1)
        }
    }

    fun setTab(tab: String) {
        if (tab == "analysis") {
            runAnalysis()
        } else {
            cancelAnalysis()
        }
    }

    fun goToMove(index: Int) {
        val clamped = index.coerceIn(1, moves.size)
        _currentMoveIndex.value = clamped
        _boardState.value = boardStates[clamped]

        _moveList.value = _moveList.value.map { it.copy(isCurrent = it.index == clamped) }

        // Update analysis for the new position
        runAnalysis()
    }

    fun previousMove() {
        if (_currentMoveIndex.value > 1) {
            goToMove(_currentMoveIndex.value - 1)
        }
    }

    fun nextMove() {
        if (_currentMoveIndex.value < moves.size) {
            goToMove(_currentMoveIndex.value + 1)
        }
    }

    fun getContinuePosition(): Pair<Long, Int>? {
        val gameId = _gameResult.value?.id ?: return null
        return Pair(gameId, _currentMoveIndex.value)
    }

    fun getCurrentTurnPlayer(): Player {
        if (_currentMoveIndex.value == 0) return Player.PLAYER_1
        val moveIndex = _currentMoveIndex.value - 1
        if (moveIndex < moves.size) {
            return moves[moveIndex].player.opponent()
        }
        return moves.last().player.opponent()
    }

    private fun runAnalysis() {
        cancelAnalysis()

        // Need at least one move to analyze the position after it.
        val index = _currentMoveIndex.value
        if (index < 1 || index > moves.size) return

        // The board state after this move, and the player to move next.
        val board = boardStates[index].copy()
        val playerToMove = if (index <= moves.size) moves[index - 1].player.opponent() else Player.PLAYER_1

        // Build the arrow for the move that was actually played.
        val nextMoveIndex = index  // move that was just performed
        var playedMoveArrow: Arrow? = null
        if (nextMoveIndex < moves.size) {
            val played = GameMove.toDomainMove(moves[nextMoveIndex])
            playedMoveArrow = Arrow(
                from = played.from,
                to = played.to,
                color = PlayedArrowColor,
                lengthFactor = 0.85f  // slightly shorter so the best arrow can be shown longer
            )
        }
        _playedArrow.value = playedMoveArrow

        analysisJob = viewModelScope.launch(Dispatchers.Default) {
            _analysisText.value = "Thinking…"
            _bestArrow.value = null

            try {
                // Set up engine for the current position
                gameEngine.loadPosition(board, playerToMove)

                // Search best move at depth 12 (HARD difficulty)
                val best = withContext(Dispatchers.Default) {
                    getAIMoveUseCase(Difficulty.HARD) // uses depth 12 internally
                }

                if (best != Move.NONE) {
                    _bestArrow.value = Arrow(
                        from = best.from,
                        to = best.to,
                        color = BestArrowColor,
                        lengthFactor = 1.0f
                    )
                    _analysisText.value = "Best: ${best.toAlgebraicNotation()}"
                } else {
                    _analysisText.value = "No moves available"
                    _bestArrow.value = null
                }
            } catch (e: Exception) {
                _analysisText.value = "Analysis error"
            }
        }
    }

    private fun cancelAnalysis() {
        analysisJob?.cancel()
        _analysisText.value = ""
        _playedArrow.value = null
        _bestArrow.value = null
    }

    private fun Move.toAlgebraicNotation(): String {
        val fromNum = positionToSquare(from)
        val toNum = positionToSquare(to)
        val sep = if (isCapture) "×" else "-"
        return "$fromNum$sep$toNum"
    }

    private fun positionToSquare(pos: com.lionico.draft.data.model.Position): Int {
        return (pos.row * 4) + (pos.col / 2) + 1
    }

    override fun onCleared() {
        super.onCleared()
        cancelAnalysis()
    }
}