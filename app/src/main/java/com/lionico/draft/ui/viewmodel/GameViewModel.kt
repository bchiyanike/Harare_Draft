// File: app/src/main/java/com/lionico/draft/ui/viewmodel/GameViewModel.kt
package com.lionico.draft.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lionico.draft.data.ai.Difficulty
import com.lionico.draft.data.engine.Board
import com.lionico.draft.data.engine.GameEngine
import com.lionico.draft.data.model.GameStatus
import com.lionico.draft.data.model.Move
import com.lionico.draft.data.model.Player
import com.lionico.draft.data.model.Position
import com.lionico.draft.domain.usecase.CheckGameOverUseCase
import com.lionico.draft.domain.usecase.ExecuteMoveUseCase
import com.lionico.draft.domain.usecase.GetAIMoveUseCase
import com.lionico.draft.domain.usecase.ValidateMoveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class GameMode {
    PLAYER_VS_PLAYER,
    PLAYER_VS_COMPUTER
}

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameEngine: GameEngine,
    private val validateMoveUseCase: ValidateMoveUseCase,
    private val executeMoveUseCase: ExecuteMoveUseCase,
    private val checkGameOverUseCase: CheckGameOverUseCase,
    private val getAIMoveUseCase: GetAIMoveUseCase
) : ViewModel() {

    private val _boardState = MutableStateFlow(gameEngine.getBoard())
    val boardState: StateFlow<Board> = _boardState.asStateFlow()

    private val _currentPlayer = MutableStateFlow(gameEngine.getCurrentPlayer())
    val currentPlayer: StateFlow<Player> = _currentPlayer.asStateFlow()

    private val _gameStatus = MutableStateFlow(GameStatus.ONGOING)
    val gameStatus: StateFlow<GameStatus> = _gameStatus.asStateFlow()

    private val _selectedPosition = MutableStateFlow<Position?>(null)
    val selectedPosition: StateFlow<Position?> = _selectedPosition.asStateFlow()

    private val _validMoves = MutableStateFlow<List<Move>>(emptyList())
    val validMoves: StateFlow<List<Move>> = _validMoves.asStateFlow()

    private val _validMovePositions = MutableStateFlow<Set<Position>>(emptySet())
    val validMovePositions: StateFlow<Set<Position>> = _validMovePositions.asStateFlow()

    private val _isAIThinking = MutableStateFlow(false)
    val isAIThinking: StateFlow<Boolean> = _isAIThinking.asStateFlow()

    private val _winner = MutableStateFlow<Player?>(null)
    val winner: StateFlow<Player?> = _winner.asStateFlow()

    private var gameMode = GameMode.PLAYER_VS_PLAYER
    private var aiDifficulty = Difficulty.MEDIUM

    fun setGameMode(mode: GameMode, difficulty: Difficulty = Difficulty.MEDIUM) {
        this.gameMode = mode
        this.aiDifficulty = difficulty
        resetGame()
    }

    fun onSquareClick(position: Position) {
        if (_isAIThinking.value) return
        if (_gameStatus.value != GameStatus.ONGOING) return

        if (gameMode == GameMode.PLAYER_VS_COMPUTER && 
            _currentPlayer.value == Player.PLAYER_2) {
            return
        }

        val selected = _selectedPosition.value

        when {
            selected == null -> {
                selectPieceAt(position)
            }
            selected == position -> {
                clearSelection()
            }
            else -> {
                tryMove(selected, position)
            }
        }
    }

    private fun selectPieceAt(position: Position) {
        val piece = _boardState.value.getPieceAt(position)
        if (piece?.player == _currentPlayer.value) {
            _selectedPosition.value = position
            val moves = validateMoveUseCase(position)
            _validMoves.value = moves
            _validMovePositions.value = moves.map { it.to }.toSet()
        }
    }

    private fun tryMove(from: Position, to: Position) {
        val move = _validMoves.value.find { it.from == from && it.to == to }
        
        if (move != null) {
            executeMove(move)
        } else {
            val piece = _boardState.value.getPieceAt(to)
            if (piece?.player == _currentPlayer.value) {
                selectPieceAt(to)
            } else {
                clearSelection()
            }
        }
    }

    private fun executeMove(move: Move) {
        val success = executeMoveUseCase(move)
        if (success) {
            updateUIState()
            clearSelection()

            if (gameMode == GameMode.PLAYER_VS_COMPUTER &&
                _currentPlayer.value == Player.PLAYER_2 &&
                _gameStatus.value == GameStatus.ONGOING) {
                makeAIMove()
            }
        }
    }

    private fun makeAIMove() {
        if (_isAIThinking.value) return

        viewModelScope.launch {
            _isAIThinking.value = true
            try {
                val move = getAIMoveUseCase(aiDifficulty)
                if (move != Move.NONE) {
                    executeMoveUseCase(move)
                    updateUIState()
                }
            } finally {
                _isAIThinking.value = false
            }
        }
    }

    private fun updateUIState() {
        _boardState.value = gameEngine.getBoard()
        _currentPlayer.value = gameEngine.getCurrentPlayer()
        _gameStatus.value = checkGameOverUseCase()
        _winner.value = checkGameOverUseCase.getWinner()
    }

    private fun clearSelection() {
        _selectedPosition.value = null
        _validMoves.value = emptyList()
        _validMovePositions.value = emptySet()
    }

    fun resetGame() {
        gameEngine.newGame()
        updateUIState()
        clearSelection()
        _isAIThinking.value = false
    }

    fun getStatusMessage(): String {
        return when (_gameStatus.value) {
            GameStatus.ONGOING -> {
                val playerText = if (_currentPlayer.value == Player.PLAYER_1) "Player 1" else "Player 2"
                "$playerText's Turn"
            }
            GameStatus.PLAYER_1_WINS -> "Player 1 Wins!"
            GameStatus.PLAYER_2_WINS -> "Player 2 Wins!"
            GameStatus.DRAW -> "Draw!"
        }
    }

    fun getPieceCounts(): Pair<Int, Int> {
        return gameEngine.getPieceCounts()
    }
}