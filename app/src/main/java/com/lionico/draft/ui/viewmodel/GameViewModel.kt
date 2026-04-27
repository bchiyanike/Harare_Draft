// File: app/src/main/java/com/lionico/draft/ui/viewmodel/GameViewModel.kt
package com.lionico.draft.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lionico.draft.data.ai.Difficulty
import com.lionico.draft.data.datastore.PlayerPreferences
import com.lionico.draft.data.engine.Board
import com.lionico.draft.data.engine.GameEngine
import com.lionico.draft.data.model.GameClock
import com.lionico.draft.data.model.GameMove
import com.lionico.draft.data.model.GameResult
import com.lionico.draft.data.model.GameStatus
import com.lionico.draft.data.model.Move
import com.lionico.draft.data.model.Player
import com.lionico.draft.data.model.Position
import com.lionico.draft.data.model.TimeControl
import com.lionico.draft.data.repository.GameHistoryRepository
import com.lionico.draft.domain.usecase.CheckGameOverUseCase
import com.lionico.draft.domain.usecase.ExecuteMoveUseCase
import com.lionico.draft.domain.usecase.GetAIMoveUseCase
import com.lionico.draft.domain.usecase.ValidateMoveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val getAIMoveUseCase: GetAIMoveUseCase,
    private val playerPreferences: PlayerPreferences,
    private val historyRepository: GameHistoryRepository
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

    private val _isAIThinking = MutableStateFlow(false)
    val isAIThinking: StateFlow<Boolean> = _isAIThinking.asStateFlow()

    private val _winner = MutableStateFlow<Player?>(null)
    val winner: StateFlow<Player?> = _winner.asStateFlow()

    private val _player1Name = MutableStateFlow("Player 1")
    val player1Name: StateFlow<String> = _player1Name.asStateFlow()

    private val _player2Name = MutableStateFlow("Player 2")
    val player2Name: StateFlow<String> = _player2Name.asStateFlow()

    private var gameMode = GameMode.PLAYER_VS_PLAYER
    private var aiDifficulty = Difficulty.MEDIUM
    private var currentTimeControl = TimeControl.PRESETS.last() // will be set properly in startGame
    private var gameStartTime = 0L

    private val gameClock = GameClock()
    val clockState = gameClock.state
    private var clockTickJob: Job? = null

    init {
        loadPlayerNames()
    }

    private fun loadPlayerNames() {
        viewModelScope.launch {
            playerPreferences.playerNames.collect { names ->
                _player1Name.value = names.player1Name
                _player2Name.value = names.player2Name
            }
        }
    }

    /**
     * Start a new game with the chosen mode and time control.
     * Difficulty for AI is read from stored preferences.
     */
    fun startGame(mode: GameMode, timeControl: TimeControl) {
        this.gameMode = mode
        this.currentTimeControl = timeControl

        viewModelScope.launch {
            if (mode == GameMode.PLAYER_VS_COMPUTER) {
                aiDifficulty = playerPreferences.difficulty.value ?: Difficulty.MEDIUM
                val aiName = PlayerPreferences.randomAIName(aiDifficulty)
                _player2Name.value = aiName
                playerPreferences.setPlayer2Name(aiName)
            }
            resetGame()
        }
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
            _validMoves.value = validateMoveUseCase(position)
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

            gameClock.switchTo(_currentPlayer.value)

            if (gameMode == GameMode.PLAYER_VS_COMPUTER &&
                _currentPlayer.value == Player.PLAYER_2 &&
                _gameStatus.value == GameStatus.ONGOING) {
                makeAIMove()
            }

            checkAndHandleGameOver()
        }
    }

    private fun makeAIMove() {
        if (_isAIThinking.value) return

        viewModelScope.launch {
            _isAIThinking.value = true
            try {
                delay(300)
                val move = getAIMoveUseCase(aiDifficulty)
                if (move != Move.NONE) {
                    executeMoveUseCase(move)
                    updateUIState()
                    gameClock.switchTo(_currentPlayer.value)
                    checkAndHandleGameOver()
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
    }

    private fun checkAndHandleGameOver() {
        if (_gameStatus.value != GameStatus.ONGOING) {
            gameClock.pause()
            clockTickJob?.cancel()
            saveGameResult()
        }
    }

    private fun saveGameResult() {
        viewModelScope.launch {
            val winnerName = when (_gameStatus.value) {
                GameStatus.PLAYER_1_WINS -> _player1Name.value
                GameStatus.PLAYER_2_WINS -> _player2Name.value
                else -> "Draw"
            }

            val duration = ((System.currentTimeMillis() - gameStartTime) / 1000).toInt()

            val result = GameResult(
                player1Name = _player1Name.value,
                player2Name = _player2Name.value,
                winner = winnerName,
                gameMode = gameMode.name,
                durationSeconds = duration,
                player1PiecesRemaining = gameEngine.getPieceCounts().first,
                player2PiecesRemaining = gameEngine.getPieceCounts().second,
                movesJson = GameMove.serialize(gameEngine.getMoveHistory()),
                timeControlLabel = currentTimeControl.label()
            )

            historyRepository.saveResult(result)
        }
    }

    private fun startClock() {
        clockTickJob?.cancel()
        clockTickJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                gameClock.tick()
                gameClock.isTimeOut()?.let { player ->
                    handleTimeOut(player)
                }
            }
        }
    }

    private fun handleTimeOut(player: Player) {
        gameClock.pause()
        clockTickJob?.cancel()

        _gameStatus.value = if (player == Player.PLAYER_1) {
            GameStatus.PLAYER_2_WINS
        } else {
            GameStatus.PLAYER_1_WINS
        }
        _winner.value = if (player == Player.PLAYER_1) Player.PLAYER_2 else Player.PLAYER_1

        saveGameResult()
    }

    fun resetGame() {
        gameEngine.newGame()
        gameClock.reset(currentTimeControl)
        gameStartTime = System.currentTimeMillis()
        startClock()
        gameClock.start(Player.PLAYER_1)
        updateUIState()
        clearSelection()
        _isAIThinking.value = false
    }

    fun formatTime(seconds: Int): String = gameClock.formatTime(seconds)

    fun getStatusMessage(): String {
        return when (_gameStatus.value) {
            GameStatus.ONGOING -> {
                val playerName = if (_currentPlayer.value == Player.PLAYER_1) {
                    _player1Name.value
                } else {
                    _player2Name.value
                }
                "$playerName's Turn"
            }
            GameStatus.PLAYER_1_WINS -> "${_player1Name.value} Wins!"
            GameStatus.PLAYER_2_WINS -> "${_player2Name.value} Wins!"
            GameStatus.DRAW -> "Draw!"
        }
    }

    fun getPieceCounts(): Pair<Int, Int> = gameEngine.getPieceCounts()

    /**
     * Load a previously saved game and set up to continue playing vs AI
     * with the given time control.
     */
    suspend fun continueFromGame(gameId: Long, timeControl: TimeControl) {
        val gameResult = historyRepository.getGameById(gameId) ?: return
        val moves = GameMove.deserialize(gameResult.movesJson)
        gameEngine.newGame()
        for (gameMove in moves) {
            val move = GameMove.toDomainMove(gameMove)
            gameEngine.executeMove(move)
        }
        gameEngine.loadPosition(gameEngine.getBoard(), gameEngine.getCurrentPlayer())

        gameMode = GameMode.PLAYER_VS_COMPUTER
        this.currentTimeControl = timeControl
        aiDifficulty = playerPreferences.difficulty.value ?: Difficulty.MEDIUM

        viewModelScope.launch {
            val aiName = PlayerPreferences.randomAIName(aiDifficulty)
            _player2Name.value = aiName
            playerPreferences.setPlayer2Name(aiName)
        }

        gameStartTime = System.currentTimeMillis()
        gameClock.reset(timeControl)
        startClock()
        gameClock.start(gameEngine.getCurrentPlayer())

        updateUIState()
        clearSelection()
        _isAIThinking.value = false

        if (gameEngine.getCurrentPlayer() == Player.PLAYER_2) {
            makeAIMove()
        }
    }

    override fun onCleared() {
        super.onCleared()
        clockTickJob?.cancel()
    }
}