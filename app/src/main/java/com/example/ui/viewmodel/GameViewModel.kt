package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.ScoreEntity
import com.example.data.repository.ScoreRepository
import com.example.game.Difficulty
import com.example.game.GameEngine
import com.example.game.GameState
import com.example.game.ShipType
import com.example.data.repository.GamePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: ScoreRepository,
    val preferences: GamePreferences
) : ViewModel() {

    val gameEngine = GameEngine(repository, preferences, viewModelScope)

    val coins: StateFlow<Int> = preferences.coins
    val upgradeLevels: StateFlow<Map<String, Int>> = preferences.upgradeLevels
    val lastDailyClaim: StateFlow<Long> = preferences.lastDailyClaim
    val dailyStreak: StateFlow<Int> = preferences.dailyStreak
    val vibrationEnabled: StateFlow<Boolean> = preferences.vibrationEnabled
    val sfxEnabled: StateFlow<Boolean> = preferences.sfxEnabled
    val musicEnabled: StateFlow<Boolean> = preferences.musicEnabled
    val screenShakeEnabled: StateFlow<Boolean> = preferences.screenShakeEnabled
    val unlockedShips: StateFlow<Set<String>> = preferences.unlockedShips
    val onboardingCompleted: StateFlow<Boolean> = preferences.onboardingCompleted

    fun setOnboardingCompleted(completed: Boolean) {
        preferences.setOnboardingCompleted(completed)
    }

    fun upgradeStat(stat: String, cost: Int): Boolean {
        val success = preferences.upgradeStat(stat, cost)
        if (success) {
            gameEngine.playSimulatedSFX(android.media.ToneGenerator.TONE_SUP_CONFIRM, 150)
            preferences.vibrate(80)
        } else {
            gameEngine.playSimulatedSFX(android.media.ToneGenerator.TONE_SUP_ERROR, 150)
        }
        return success
    }

    fun claimDailyReward(reward: Int) {
        preferences.claimDailyReward(reward)
        gameEngine.playSimulatedSFX(android.media.ToneGenerator.TONE_SUP_CONFIRM, 250)
        preferences.vibrate(150)
    }

    fun toggleVibration(enabled: Boolean) {
        preferences.setVibrationEnabled(enabled)
        if (enabled) {
            preferences.vibrate(100)
        }
    }

    fun toggleSFX(enabled: Boolean) {
        preferences.setSfxEnabled(enabled)
        if (enabled) {
            gameEngine.playSimulatedSFX(android.media.ToneGenerator.TONE_PROP_ACK, 100)
        }
    }

    fun toggleMusic(enabled: Boolean) {
        preferences.setMusicEnabled(enabled)
    }

    fun toggleScreenShake(enabled: Boolean) {
        preferences.setScreenShakeEnabled(enabled)
    }

    fun unlockShip(shipType: ShipType, cost: Int): Boolean {
        val success = preferences.unlockShip(shipType, cost)
        if (success) {
            gameEngine.playSimulatedSFX(android.media.ToneGenerator.TONE_SUP_CONFIRM, 250)
            preferences.vibrate(120)
        } else {
            gameEngine.playSimulatedSFX(android.media.ToneGenerator.TONE_SUP_ERROR, 150)
        }
        return success
    }

    fun resetGameProgress() {
        preferences.resetProgress()
        gameEngine.playSimulatedSFX(android.media.ToneGenerator.TONE_SUP_ERROR, 300)
    }

    // Leaderboard filter (All, Recruit, Veteran, Elite)
    private val _selectedDifficultyFilter = MutableStateFlow("All")
    val selectedDifficultyFilter: StateFlow<String> = _selectedDifficultyFilter.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val leaderboardScores: StateFlow<List<ScoreEntity>> = _selectedDifficultyFilter
        .flatMapLatest { filter ->
            if (filter == "All") {
                repository.topScores
            } else {
                repository.getTopScoresByDifficulty(filter)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectDifficultyFilter(filter: String) {
        _selectedDifficultyFilter.value = filter
    }

    fun setDifficulty(difficulty: Difficulty) {
        gameEngine.currentDifficulty = difficulty
    }

    fun setShipType(shipType: ShipType) {
        gameEngine.selectedShip = shipType
    }

    fun startGame() {
        gameEngine.setGameState(GameState.PLAYING)
    }

    fun changeGameState(state: GameState) {
        gameEngine.setGameState(state)
    }

    fun clearLeaderboard() {
        viewModelScope.launch {
            repository.clearAllScores()
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                val database = AppDatabase.getDatabase(context)
                val repository = ScoreRepository(database.scoreDao())
                val preferences = GamePreferences(context)
                @Suppress("UNCHECKED_CAST")
                return GameViewModel(repository, preferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
