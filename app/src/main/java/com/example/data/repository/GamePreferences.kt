package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.game.ShipType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GamePreferences(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("cosmic_dodge_prefs", Context.MODE_PRIVATE)

    fun vibrate(durationMs: Long) {
        if (!_vibrationEnabled.value) return
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator?.vibrate(android.os.VibrationEffect.createOneShot(durationMs, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val _coins = MutableStateFlow(prefs.getInt(KEY_COINS, 0))
    val coins: StateFlow<Int> = _coins.asStateFlow()

    private val _unlockedShips = MutableStateFlow(
        prefs.getStringSet(KEY_UNLOCKED_SHIPS, setOf(ShipType.SPEEDSTER.name)) ?: setOf(ShipType.SPEEDSTER.name)
    )
    val unlockedShips: StateFlow<Set<String>> = _unlockedShips.asStateFlow()

    private val _sensitivity = MutableStateFlow(prefs.getFloat(KEY_SENSITIVITY, 1.0f))
    val sensitivity: StateFlow<Float> = _sensitivity.asStateFlow()

    private val _screenShakeEnabled = MutableStateFlow(prefs.getBoolean(KEY_SCREEN_SHAKE, true))
    val screenShakeEnabled: StateFlow<Boolean> = _screenShakeEnabled.asStateFlow()

    private val _sfxEnabled = MutableStateFlow(prefs.getBoolean(KEY_SFX, true))
    val sfxEnabled: StateFlow<Boolean> = _sfxEnabled.asStateFlow()

    private val _musicEnabled = MutableStateFlow(prefs.getBoolean(KEY_MUSIC, true))
    val musicEnabled: StateFlow<Boolean> = _musicEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(prefs.getBoolean(KEY_VIBRATION, true))
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _upgradeLevels = MutableStateFlow(
        mapOf(
            "speed" to prefs.getInt("upg_speed", 0),
            "magnet" to prefs.getInt("upg_magnet", 0),
            "shield" to prefs.getInt("upg_shield", 0),
            "firerate" to prefs.getInt("upg_firerate", 0),
            "damage" to prefs.getInt("upg_damage", 0),
            "maxhealth" to prefs.getInt("upg_maxhealth", 0)
        )
    )
    val upgradeLevels: StateFlow<Map<String, Int>> = _upgradeLevels.asStateFlow()

    private val _lastDailyClaim = MutableStateFlow(prefs.getLong(KEY_LAST_DAILY_CLAIM, 0L))
    val lastDailyClaim: StateFlow<Long> = _lastDailyClaim.asStateFlow()

    private val _dailyStreak = MutableStateFlow(prefs.getInt(KEY_DAILY_STREAK, 0))
    val dailyStreak: StateFlow<Int> = _dailyStreak.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        _onboardingCompleted.value = completed
    }

    fun addCoins(amount: Int) {
        val newCoins = _coins.value + amount
        prefs.edit().putInt(KEY_COINS, newCoins).apply()
        _coins.value = newCoins
    }

    fun deductCoins(amount: Int): Boolean {
        if (_coins.value >= amount) {
            val newCoins = _coins.value - amount
            prefs.edit().putInt(KEY_COINS, newCoins).apply()
            _coins.value = newCoins
            return true
        }
        return false
    }

    fun isShipUnlocked(shipType: ShipType): Boolean {
        return _unlockedShips.value.contains(shipType.name)
    }

    fun unlockShip(shipType: ShipType, cost: Int): Boolean {
        if (isShipUnlocked(shipType)) return true
        if (deductCoins(cost)) {
            val updated = _unlockedShips.value.toMutableSet().apply { add(shipType.name) }
            prefs.edit().putStringSet(KEY_UNLOCKED_SHIPS, updated).apply()
            _unlockedShips.value = updated
            return true
        }
        return false
    }

    fun setSensitivity(value: Float) {
        prefs.edit().putFloat(KEY_SENSITIVITY, value).apply()
        _sensitivity.value = value
    }

    fun setScreenShakeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SCREEN_SHAKE, enabled).apply()
        _screenShakeEnabled.value = enabled
    }

    fun setSfxEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SFX, enabled).apply()
        _sfxEnabled.value = enabled
    }

    fun setMusicEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MUSIC, enabled).apply()
        _musicEnabled.value = enabled
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
        _vibrationEnabled.value = enabled
    }

    fun getUpgradeLevel(stat: String): Int {
        return _upgradeLevels.value[stat] ?: 0
    }

    fun upgradeStat(stat: String, cost: Int): Boolean {
        val currentLvl = getUpgradeLevel(stat)
        if (currentLvl >= 5) return false
        if (deductCoins(cost)) {
            val newLvl = currentLvl + 1
            prefs.edit().putInt("upg_$stat", newLvl).apply()
            val updatedMap = _upgradeLevels.value.toMutableMap().apply { put(stat, newLvl) }
            _upgradeLevels.value = updatedMap
            return true
        }
        return false
    }

    fun claimDailyReward(rewardAmount: Int) {
        val now = System.currentTimeMillis()
        prefs.edit()
            .putLong(KEY_LAST_DAILY_CLAIM, now)
            .putInt(KEY_DAILY_STREAK, (_dailyStreak.value % 7) + 1)
            .apply()
        _lastDailyClaim.value = now
        _dailyStreak.value = (_dailyStreak.value % 7) + 1
        addCoins(rewardAmount)
    }

    fun resetDailyStreak() {
        prefs.edit().putInt(KEY_DAILY_STREAK, 0).apply()
        _dailyStreak.value = 0
    }

    fun resetProgress() {
        prefs.edit().clear().apply()
        _coins.value = 0
        val defaultUnlocked = setOf(ShipType.SPEEDSTER.name)
        _unlockedShips.value = defaultUnlocked
        _sensitivity.value = 1.0f
        _screenShakeEnabled.value = true
        _sfxEnabled.value = true
        _musicEnabled.value = true
        _vibrationEnabled.value = true
        _onboardingCompleted.value = false
        _upgradeLevels.value = mapOf(
            "speed" to 0, "magnet" to 0, "shield" to 0, "firerate" to 0, "damage" to 0, "maxhealth" to 0
        )
        _lastDailyClaim.value = 0L
        _dailyStreak.value = 0
        prefs.edit()
            .putInt(KEY_COINS, 0)
            .putStringSet(KEY_UNLOCKED_SHIPS, defaultUnlocked)
            .putFloat(KEY_SENSITIVITY, 1.0f)
            .putBoolean(KEY_SCREEN_SHAKE, true)
            .putBoolean(KEY_SFX, true)
            .putBoolean(KEY_MUSIC, true)
            .putBoolean(KEY_VIBRATION, true)
            .putBoolean(KEY_ONBOARDING_COMPLETED, false)
            .putInt("upg_speed", 0)
            .putInt("upg_magnet", 0)
            .putInt("upg_shield", 0)
            .putInt("upg_firerate", 0)
            .putInt("upg_damage", 0)
            .putInt("upg_maxhealth", 0)
            .putLong(KEY_LAST_DAILY_CLAIM, 0L)
            .putInt(KEY_DAILY_STREAK, 0)
            .apply()
    }

    companion object {
        private const val KEY_COINS = "coins"
        private const val KEY_UNLOCKED_SHIPS = "unlocked_ships"
        private const val KEY_SENSITIVITY = "sensitivity"
        private const val KEY_SCREEN_SHAKE = "screen_shake"
        private const val KEY_SFX = "sfx"
        private const val KEY_MUSIC = "music"
        private const val KEY_VIBRATION = "vibration"
        private const val KEY_LAST_DAILY_CLAIM = "last_daily_claim"
        private const val KEY_DAILY_STREAK = "daily_streak"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
