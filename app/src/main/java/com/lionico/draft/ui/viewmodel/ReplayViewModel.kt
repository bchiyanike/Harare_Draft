// File: app/src/main/java/com/lionico/draft/ui/viewmodel/ReplayViewModel.kt
package com.lionico.draft.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lionico.draft.data.engine.Board
import com.lionico.draft.data.engine.GameEngine
import com.lionico.draft.data.model.GameMove
import com.lionico.draft.data.model.GameResult
import com.lionico.draft.data.model.Player
import com.lionico.draft.data.repository.GameHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MoveEntry(
    val index: Int,            // 1-based
    val notation: String,      // e.g., "22-18"
    val player: Player,
    val isCurrent: Boolean
)

@HiltViewModel
class ReplayViewModel @Inject constructor(
    private val gameEngine: GameEngine,
    private val historyRepository: GameHistoryRepository
) : ViewModel() {

    private val _boardState = MutableStateFlow(Board())
    val boardState: StateFlow<Board> = _boardState.asStateFlow()

    private val _currentMoveIndex = MutableStateFlow(1) // start at move 1
    val currentMoveIndex: StateFlow<Int> = _currentMoveIndex.asStateFlow()

    private val _totalMoves = MutableStateFlow(0)
    val totalMoves: StateFlow<Int> = _totalMoves.asStateFlow()

    private val _moveList = MutableStateFlow<List<MoveEntry>>(emptyList())
    val moveList: StateFlow<List<MoveEntry>> = _moveList.asStateFlow()

    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult: StateFlow<GameResult?> = _gameResult.asStateFlow()

    private val _analysisText = MutableStateFlow("Analysis on hold")
    val analysisText: StateFlow<String> = _analysisText.asStateFlow()

    private var moves = emptyList<GameMove>()
    private var boardStates = mutableListOf<Board>()  // index 0 = initial, index i = after move i

    fun loadGame(gameId: Long) {
        viewModelScope.launch {
            val result = historyRepository.getGameById(gameId) ?: return@launch
            _gameResult.value = result
            moves = GameMove.deserialize(result.movesJson)
            _totalMoves.value = moves.size

            // Reconstruct board states
            gameEngine.newGame()
            boardStates.clear()
            boardStates.add(gameEngine.getBoard()) // initial
            for (move in moves) {
                gameEngine.executeMove(GameMove.toDomainMove(move))
                boardStates.add(gameEngine.getBoard())
            }

            // Generate move list notation (1-based index)
            val entries = moves.mapIndexed { i: Int, gm: GameMove ->
                val domainMove = GameMove.toDomainMove(gm)
                MoveEntry(
                    index = i + 1,
                    notation = domainMove.toAlgebraicNotation(),
                    player = gm.player,
                    isCurrent = (i + 1) == 1 // first move is current initially
                )
            }
            _moveList.value = entries

            goToMove(1)
        }
    }

    fun setTab(tab: String) {
        // no-op for now, analysis tab placeholder
    }

    fun goToMove(index: Int) {
        val clamped = index.coerceIn(1, moves.size)
        _currentMoveIndex.value = clamped
        // Update board from boardStates (index corresponds to state after that move)
        _boardState.value = boardStates[clamped]

        // Update move list isCurrent flags
        _moveList.value = _moveList.value.map { it.copy(isCurrent = it.index == clamped) }
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

    /**
     * Returns the game ID and move index to be used for continuing from this position.
     * The calling screen will handle the side selection dialog.
     */
    fun getContinuePosition(): Pair<Long, Int>? {
        val gameId = _gameResult.value?.id ?: return null
        return Pair(gameId, _currentMoveIndex.value)
    }

    /**
     * Returns which player's turn it is at the current replay position.
     */
    fun getCurrentTurnPlayer(): Player {
        // After move `index`, the turn switches. So we look at the move at index to see
        // who moved, and the opponent is now to move.
        if (_currentMoveIndex.value == 0) return Player.PLAYER_1 // should not happen
        val moveIndex = _currentMoveIndex.value - 1 // 0-based index
        if (moveIndex < moves.size) {
            val playerWhoMoved = moves[moveIndex].player
            return playerWhoMoved.opponent()
        }
        // If at the end, the game is over; no current player. Return the last player's opponent (not critical).
        return moves.last().player.opponent()
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
    }
}