// File: app/src/main/java/com/lionico/draft/ui/viewmodel/GameViewModel.kt
package com.lionico.draft.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lionico.draft.R
import com.lionico.draft.data.ai.AiStrengthProfile
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
import com.lionico.draft.domain.usecase.UpdateRatingUseCase
import com.lionico.draft.domain.usecase.ValidateMoveUseCase
import com.lionico.draft.ui.feedback.HapticManager
import com.lionico.draft.ui.feedback.SoundManager
import com.lionico.draft.ui.feedback.SoundType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

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
    private val updateRatingUseCase: UpdateRatingUseCase,
    private val playerPreferences: PlayerPreferences,
    private val historyRepository: GameHistoryRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager,
    @ApplicationContext private val appContext: Context
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

    private val _humanSide = MutableStateFlow<Player?>(null)
    val humanSide: StateFlow<Player?> = _humanSide.asStateFlow()

    // Elo rating state
    private val _playerRating = MutableStateFlow(1200f)
    val playerRating: StateFlow<Float> = _playerRating.asStateFlow()

    private val _opponentRating = MutableStateFlow(1200f)
    val opponentRating: StateFlow<Float> = _opponentRating.asStateFlow()

    private val _ratingDelta = MutableStateFlow(0)
    val ratingDelta: StateFlow<Int> = _ratingDelta.asStateFlow()

    private val _showRatingAnimation = MutableStateFlow(false)
    val showRatingAnimation: StateFlow<Boolean> = _showRatingAnimation.asStateFlow()

    private var gameMode = GameMode.PLAYER_VS_PLAYER
    private var aiProfile: AiStrengthProfile? = null
    private var currentTimeControl = TimeControl.PRESETS.last()
    private var gameStartTime = 0L
    private var aiPlayer: Player? = null

    // Session-only AI rating tracker (non-persistent)
    private var aiSessionRating = 0f
    private var playerRatingAtStart = 0f
    private var previousPlayerRating = 0f
    private var previousAiSessionRating = 0f

    private val gameClock = GameClock()
    val clockState = gameClock.state
    private var clockTickJob: Job? = null

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            // Combine names and rating flows
            launch {
                playerPreferences.playerNames.collect { names ->
                    _player1Name.value = names.player1Name
                    _player2Name.value = names.player2Name
                }
            }
            launch {
                playerPreferences.playerRating.collect { rating ->
                    _playerRating.value = rating
                }
            }
        }
    }

    fun startGame(mode: GameMode, timeControl: TimeControl) {
        this.gameMode = mode
        this.currentTimeControl = timeControl

        viewModelScope.launch {
            val names = playerPreferences.playerNames.first()
            if (mode == GameMode.PLAYER_VS_COMPUTER) {
                // Load selected AI rating from preferences
                val selectedAiRating = playerPreferences.selectedAiRating.first()
                aiProfile = AiStrengthProfile.forRating(selectedAiRating)
                aiSessionRating = selectedAiRating.toFloat()
                playerRatingAtStart = _playerRating.value
                previousPlayerRating = _playerRating.value
                previousAiSessionRating = aiSessionRating

                val aiName = PlayerPreferences.randomAIName(selectedAiRating)
                val humanName = names.player1Name
                val humanIsRed = Random.nextBoolean()
                if (humanIsRed) {
                    _player1Name.value = humanName
                    _player2Name.value = aiName
                    aiPlayer = Player.PLAYER_2
                    _humanSide.value = Player.PLAYER_1
                    _opponentRating.value = aiSessionRating
                } else {
                    _player1Name.value = aiName
                    _player2Name.value = humanName
                    aiPlayer = Player.PLAYER_1
                    _humanSide.value = Player.PLAYER_2
                    _opponentRating.value = aiSessionRating
                }
                playerPreferences.setPlayer2Name(aiName)
            } else {
                aiProfile = null
                aiPlayer = null
                aiSessionRating = 0f
                _humanSide.value = null
                _opponentRating.value = _playerRating.value // PvP both same player for now
            }
            resetGame()

            if (aiPlayer != null && _currentPlayer.value == aiPlayer &&
                _gameStatus.value == GameStatus.ONGOING) {
                makeAIMove()
            }
        }
    }

    fun onSquareClick(position: Position) {
        if (_isAIThinking.value) return
        if (_gameStatus.value != GameStatus.ONGOING) return
        if (aiPlayer != null && _currentPlayer.value == aiPlayer) return

        val selected = _selectedPosition.value
        when {
            selected == null -> selectPieceAt(position)
            selected == position -> clearSelection()
            else -> tryMove(selected, position)
        }
    }

    private fun selectPieceAt(position: Position) {
        val piece = _boardState.value.getPieceAt(position)
        if (piece?.player == _currentPlayer.value) {
            _selectedPosition.value = position
            _validMoves.value = validateMoveUseCase(position)
            viewModelScope.launch { hapticManager.selectPiece() }
        } else {
            viewModelScope.launch { hapticManager.invalidTap() }
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
                viewModelScope.launch { hapticManager.invalidTap() }
                clearSelection()
            }
        }
    }

    private fun executeMove(move: Move) {
        val wasPromotion = move.promotedToKing
        val capturedPositions = move.capturedPositions

        val success = executeMoveUseCase(move)
        if (success) {
            updateUIState()
            clearSelection()
            gameClock.switchTo(_currentPlayer.value)

            viewModelScope.launch {
                when {
                    wasPromotion -> {
                        soundManager.play(SoundType.PROMOTE)
                        hapticManager.movePiece()
                    }
                    capturedPositions.isNotEmpty() -> {
                        soundManager.play(SoundType.CAPTURE)
                        hapticManager.capture()
                    }
                    else -> {
                        soundManager.play(SoundType.MOVE)
                        hapticManager.movePiece()
                    }
                }
            }

            if (aiPlayer != null && _currentPlayer.value == aiPlayer &&
                _gameStatus.value == GameStatus.ONGOING) {
                makeAIMove()
            }

            checkAndHandleGameOver()
        }
    }

    private fun makeAIMove() {
        if (_isAIThinking.value) return
        val profile = aiProfile ?: return

        viewModelScope.launch {
            _isAIThinking.value = true
            try {
                delay(300)
                val move = getAIMoveUseCase(profile)
                if (move != Move.NONE) {
                    val wasPromotion = move.promotedToKing
                    val capturedPositions = move.capturedPositions

                    executeMoveUseCase(move)
                    updateUIState()
                    gameClock.switchTo(_currentPlayer.value)

                    when {
                        wasPromotion -> {
                            soundManager.play(SoundType.PROMOTE)
                            hapticManager.movePiece()
                        }
                        capturedPositions.isNotEmpty() -> {
                            soundManager.play(SoundType.CAPTURE)
                            hapticManager.capture()
                        }
                        else -> {
                            soundManager.play(SoundType.MOVE)
                            hapticManager.movePiece()
                        }
                    }

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

            viewModelScope.launch {
                when (_gameStatus.value) {
                    GameStatus.PLAYER_1_WINS -> soundManager.play(SoundType.WIN)
                    GameStatus.PLAYER_2_WINS -> soundManager.play(SoundType.LOSE)
                    GameStatus.DRAW -> soundManager.play(SoundType.DRAW)
                    else -> {}
                }
            }

            // Compute Elo update for AI games
            if (gameMode == GameMode.PLAYER_VS_COMPUTER) {
                computeRatingUpdate()
            }

            saveGameResult()
        }
    }

    private fun computeRatingUpdate() {
        // Determine outcome from perspective of player (human)
        val score = when {
            _gameStatus.value == GameStatus.PLAYER_1_WINS && _humanSide.value == Player.PLAYER_1 -> 1.0f
            _gameStatus.value == GameStatus.PLAYER_2_WINS && _humanSide.value == Player.PLAYER_2 -> 1.0f
            _gameStatus.value == GameStatus.DRAW -> 0.5f
            else -> 0.0f
        }

        // Update player rating (persistent)
        val (newPlayerRating, playerDelta) = updateRatingUseCase(
            _playerRating.value,
            aiSessionRating,
            score
        )
        _playerRating.value = newPlayerRating
        _ratingDelta.value = playerDelta
        previousPlayerRating = _playerRating.value

        // Update AI session rating (non-persistent)
        val aiScore = 1.0f - score
        val (newAiRating, aiDelta) = updateRatingUseCase(
            aiSessionRating,
            playerRatingAtStart,
            aiScore
        )
        aiSessionRating = newAiRating
        _opponentRating.value = aiSessionRating
        previousAiSessionRating = aiSessionRating

        // Persist player rating
        viewModelScope.launch {
            playerPreferences.setPlayerRating(newPlayerRating)
        }

        // Trigger rating animation
        _showRatingAnimation.value = true
    }

    fun dismissRatingAnimation() {
        _showRatingAnimation.value = false
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

        if (gameMode == GameMode.PLAYER_VS_COMPUTER) {
            computeRatingUpdate()
        }
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
        _ratingDelta.value = 0
        _showRatingAnimation.value = false
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
                appContext.getString(R.string.game_status_turn, playerName)
            }
            GameStatus.PLAYER_1_WINS -> appContext.getString(R.string.game_status_player_wins, _player1Name.value)
            GameStatus.PLAYER_2_WINS -> appContext.getString(R.string.game_status_player_wins, _player2Name.value)
            GameStatus.DRAW -> appContext.getString(R.string.game_status_draw)
        }
    }

    fun getPieceCounts(): Pair<Int, Int> = gameEngine.getPieceCounts()

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
        val selectedAiRating = playerPreferences.selectedAiRating.first()
        aiProfile = AiStrengthProfile.forRating(selectedAiRating)
        aiSessionRating = selectedAiRating.toFloat()
        playerRatingAtStart = _playerRating.value

        viewModelScope.launch {
            val aiName = PlayerPreferences.randomAIName(selectedAiRating)
            _player2Name.value = aiName
            _opponentRating.value = aiSessionRating
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

    suspend fun continueFromPosition(
        gameId: Long,
        moveIndex: Int,
        humanSide: Player,
        timeControl: TimeControl
    ) {
        val gameResult = historyRepository.getGameById(gameId) ?: return
        val moves = GameMove.deserialize(gameResult.movesJson)
        gameEngine.newGame()
        for (i in 0 until moveIndex.coerceAtMost(moves.size)) {
            gameEngine.executeMove(GameMove.toDomainMove(moves[i]))
        }
        gameEngine.loadPosition(gameEngine.getBoard(), gameEngine.getCurrentPlayer())

        gameMode = GameMode.PLAYER_VS_COMPUTER
        this.currentTimeControl = timeControl
        val selectedAiRating = playerPreferences.selectedAiRating.first()
        aiProfile = AiStrengthProfile.forRating(selectedAiRating)
        aiSessionRating = selectedAiRating.toFloat()
        playerRatingAtStart = _playerRating.value

        val aiName = PlayerPreferences.randomAIName(selectedAiRating)
        if (humanSide == Player.PLAYER_1) {
            _player1Name.value = gameResult.player1Name
            _player2Name.value = aiName
            aiPlayer = Player.PLAYER_2
        } else {
            _player1Name.value = aiName
            _player2Name.value = gameResult.player2Name
            aiPlayer = Player.PLAYER_1
        }
        _humanSide.value = humanSide
        _opponentRating.value = aiSessionRating
        viewModelScope.launch {
            playerPreferences.setPlayer2Name(aiName)
        }

        gameStartTime = System.currentTimeMillis()
        gameClock.reset(timeControl)
        startClock()
        gameClock.start(gameEngine.getCurrentPlayer())

        updateUIState()
        clearSelection()
        _isAIThinking.value = false

        if (gameEngine.getCurrentPlayer() == aiPlayer && _gameStatus.value == GameStatus.ONGOING) {
            makeAIMove()
        }
    }

    override fun onCleared() {
        super.onCleared()
        clockTickJob?.cancel()
    }
}