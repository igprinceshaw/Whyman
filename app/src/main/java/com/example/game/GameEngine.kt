package com.example.game

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.data.database.ScoreEntity
import com.example.data.repository.ScoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sign
import kotlin.random.Random

import com.example.data.repository.GamePreferences
import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.flow.asStateFlow

class GameEngine(
    private val repository: ScoreRepository,
    val preferences: GamePreferences,
    private val scope: CoroutineScope
) {
    // Game state tracking
    private val _gameState = MutableStateFlow(GameState.SPLASH)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _multiplier = MutableStateFlow(1)
    val multiplier: StateFlow<Int> = _multiplier.asStateFlow()

    private val _health = MutableStateFlow(100f)
    val health: StateFlow<Float> = _health.asStateFlow()

    private val _maxHealth = MutableStateFlow(100f)
    val maxHealth: StateFlow<Float> = _maxHealth.asStateFlow()

    private val _shieldActive = MutableStateFlow(false)
    val shieldActive: StateFlow<Boolean> = _shieldActive.asStateFlow()

    private val _shieldTimeLeft = MutableStateFlow(0f) // Percentage 0..1
    val shieldTimeLeft: StateFlow<Float> = _shieldTimeLeft.asStateFlow()

    private val _multiplierTimeLeft = MutableStateFlow(0f) // Percentage 0..1
    val multiplierTimeLeft: StateFlow<Float> = _multiplierTimeLeft.asStateFlow()

    private val _slowMoActive = MutableStateFlow(false)
    val slowMoActive: StateFlow<Boolean> = _slowMoActive.asStateFlow()

    private val _magnetActive = MutableStateFlow(false)
    val magnetActive: StateFlow<Boolean> = _magnetActive.asStateFlow()

    private val _magnetTimeLeft = MutableStateFlow(0f) // Percentage 0..1
    val magnetTimeLeft: StateFlow<Float> = _magnetTimeLeft.asStateFlow()

    private val _speedBoostActive = MutableStateFlow(false)
    val speedBoostActive: StateFlow<Boolean> = _speedBoostActive.asStateFlow()

    private val _speedBoostTimeLeft = MutableStateFlow(0f) // Percentage 0..1
    val speedBoostTimeLeft: StateFlow<Float> = _speedBoostTimeLeft.asStateFlow()

    private val _screenShake = MutableStateFlow(0f)
    val screenShake: StateFlow<Float> = _screenShake.asStateFlow()

    private val _coinsCollectedThisRun = MutableStateFlow(0)
    val coinsCollectedThisRun: StateFlow<Int> = _coinsCollectedThisRun.asStateFlow()

    private val _currentLevel = MutableStateFlow(1)
    val currentLevel: StateFlow<Int> = _currentLevel.asStateFlow()

    private val _levelProgress = MutableStateFlow(0f)
    val levelProgress: StateFlow<Float> = _levelProgress.asStateFlow()

    private val _levelTimeLeftMs = MutableStateFlow(30000L) // 30s levels
    val levelTimeLeftMs: StateFlow<Long> = _levelTimeLeftMs.asStateFlow()

    private val _isBossActive = MutableStateFlow(false)
    val isBossActive: StateFlow<Boolean> = _isBossActive.asStateFlow()

    private val _bossHealth = MutableStateFlow(0f)
    val bossHealth: StateFlow<Float> = _bossHealth.asStateFlow()

    private val _bossMaxHealth = MutableStateFlow(1f)
    val bossMaxHealth: StateFlow<Float> = _bossMaxHealth.asStateFlow()

    private val _bossName = MutableStateFlow("")
    val bossName: StateFlow<String> = _bossName.asStateFlow()

    private val _coinsEarnedThisLevel = MutableStateFlow(0)
    val coinsEarnedThisLevel: StateFlow<Int> = _coinsEarnedThisLevel.asStateFlow()

    private val _scoreEarnedThisLevel = MutableStateFlow(0)
    val scoreEarnedThisLevel: StateFlow<Int> = _scoreEarnedThisLevel.asStateFlow()

    private val _comboCount = MutableStateFlow(0)
    val comboCount: StateFlow<Int> = _comboCount.asStateFlow()

    private val _comboTimeLeft = MutableStateFlow(0f)
    val comboTimeLeft: StateFlow<Float> = _comboTimeLeft.asStateFlow()

    private val _damageFlashAlpha = MutableStateFlow(0f)
    val damageFlashAlpha: StateFlow<Float> = _damageFlashAlpha.asStateFlow()

    private var playerComboTimeLeftMs = 0L
    private val COMBO_DURATION_MS = 3000L

    // Configurable Game Parameters
    var currentDifficulty = Difficulty.MEDIUM
    var selectedShip = ShipType.SPEEDSTER

    // Viewport sizes
    private var viewportWidth = 0f
    private var viewportHeight = 0f

    // Live Game entities
    var player = PlayerShip()
    val stars = mutableListOf<WarpStar>()
    val asteroids = mutableListOf<Asteroid>()
    val powerUps = mutableListOf<PowerUp>()
    val coinsList = mutableListOf<Coin>()
    val particles = mutableListOf<Particle>()
    val playerBullets = mutableListOf<PlayerBullet>()
    val bossBullets = mutableListOf<BossBullet>()
    var boss: Boss? = null

    // Tone generator for 8-bit retro sounds
    private val toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 60)
    } catch (e: Exception) {
        null
    }

    fun playSimulatedSFX(toneType: Int, durationMs: Int) {
        if (preferences.sfxEnabled.value) {
            scope.launch {
                try {
                    toneGenerator?.startTone(toneType, durationMs)
                } catch (e: Exception) {
                    // Ignore sound device busy/not ready exceptions gracefully
                }
            }
        }
    }

    // Text pops for high score/combo visuals
    data class TextPop(
        val text: String,
        val color: Color,
        var x: Float,
        var y: Float,
        var life: Float = 1.0f, // 1.0 down to 0.0
        val scale: Float = 1.0f
    )
    val textPops = mutableListOf<TextPop>()

    // Timers
    private var lastSpawnTime = 0L
    private var nextPowerUpSpawnScore = 200
    private var idCounter = 0L
    private var lastPlayerShootTime = 0L

    // Dynamic difficulty tracking (increases every 30s)
    private var gameStartTime = 0L
    private var lastDifficultyIncreaseTime = 0L
    private var dynamicDifficultyMultiplier = 1.0f

    // Procedural Music Player
    private val musicPlayer = RetroMusicPlayer()

    init {
        // Initialize starfield coordinates
        generateStarfield(1080f, 1920f)

        // Dynamic music listener
        scope.launch {
            preferences.musicEnabled.collect { enabled ->
                if (enabled && _gameState.value == GameState.PLAYING) {
                    musicPlayer.start(scope)
                } else {
                    musicPlayer.stop()
                }
            }
        }
    }

    fun setGameState(state: GameState) {
        _gameState.value = state
        if (state == GameState.PLAYING) {
            startNewGame()
            if (preferences.musicEnabled.value) {
                musicPlayer.start(scope)
            }
        } else {
            musicPlayer.stop()
        }
    }

    private fun generateStarfield(width: Float, height: Float) {
        stars.clear()
        val r = Random(42)
        for (i in 0..60) {
            stars.add(
                WarpStar(
                    x = r.nextFloat() * width,
                    y = r.nextFloat() * height,
                    speed = 2f + r.nextFloat() * 10f,
                    size = 1f + r.nextFloat() * 5f,
                    alpha = 0.2f + r.nextFloat() * 0.8f
                )
            )
        }
    }

    private var lastCoinSpawnTime = 0L

    private fun startNewGame() {
        _score.value = 0
        _multiplier.value = 1
        _slowMoActive.value = false
        _shieldActive.value = false
        _magnetActive.value = false
        _speedBoostActive.value = false
        _magnetTimeLeft.value = 0f
        _speedBoostTimeLeft.value = 0f
        _screenShake.value = 0f
        _coinsCollectedThisRun.value = 0

        _currentLevel.value = 1
        _levelProgress.value = 0f
        _levelTimeLeftMs.value = 30000L
        _isBossActive.value = false
        _coinsEarnedThisLevel.value = 0
        _scoreEarnedThisLevel.value = 0

        _comboCount.value = 0
        _comboTimeLeft.value = 0f
        playerComboTimeLeftMs = 0L
        _damageFlashAlpha.value = 0f

        gameStartTime = System.currentTimeMillis()
        lastDifficultyIncreaseTime = System.currentTimeMillis()
        dynamicDifficultyMultiplier = 1.0f

        val maxHealthLvl = preferences.getUpgradeLevel("maxhealth")
        val upgradeMaxHealth = selectedShip.initialHealth + maxHealthLvl * 20f

        player = PlayerShip(
            health = upgradeMaxHealth,
            maxHealth = upgradeMaxHealth
        )
        _health.value = player.health
        _maxHealth.value = player.maxHealth

        asteroids.clear()
        powerUps.clear()
        coinsList.clear()
        particles.clear()
        textPops.clear()
        playerBullets.clear()
        bossBullets.clear()
        boss = null
        lastPlayerShootTime = 0L

        lastSpawnTime = System.currentTimeMillis()
        lastCoinSpawnTime = System.currentTimeMillis()
        nextPowerUpSpawnScore = 250

        // Play launch beep sequence
        playSimulatedSFX(ToneGenerator.TONE_CDMA_PIP, 250)
    }

    // Input handlers
    fun moveShip(dx: Float, dy: Float) {
        if (_gameState.value != GameState.PLAYING) return

        val speedLvl = preferences.getUpgradeLevel("speed")
        val speedUpgradeFactor = 1.0f + speedLvl * 0.10f // +10% per upgrade level
        val speedMod = selectedShip.speedFactor * preferences.sensitivity.value * speedUpgradeFactor
        player.x = (player.x + dx * speedMod).coerceIn(0f, viewportWidth)
        player.y = (player.y + dy * speedMod).coerceIn(0f, viewportHeight)
    }

    fun warpShip(targetX: Float, targetY: Float) {
        if (_gameState.value != GameState.PLAYING) return
        // Smoothly interpolate ship position on tap/warp
        player.x = targetX.coerceIn(0f, viewportWidth)
        player.y = targetY.coerceIn(0f, viewportHeight)
    }

    // Core high-performance loop logic called on each Frame
    fun update(width: Float, height: Float, dtMs: Long) {
        if (viewportWidth != width || viewportHeight != height) {
            viewportWidth = width
            viewportHeight = height
            // Center the ship initially if viewport changes
            if (player.x == 0f && player.y == 0f) {
                player.x = width / 2
                player.y = height * 0.8f
            }
            generateStarfield(width, height)
        }

        updateStarfield(height)
        updateParticles()
        updateTextPops()

        if (_gameState.value != GameState.PLAYING) return

        // Screen Shake Decay
        if (_screenShake.value > 0f) {
            _screenShake.value = (_screenShake.value - (dtMs.toFloat() / 300f)).coerceAtLeast(0f)
        }

        // Combo Timer Decay
        if (playerComboTimeLeftMs > 0L) {
            playerComboTimeLeftMs -= dtMs
            if (playerComboTimeLeftMs <= 0L) {
                playerComboTimeLeftMs = 0L
                _comboCount.value = 0
                _comboTimeLeft.value = 0f
            } else {
                _comboTimeLeft.value = playerComboTimeLeftMs.toFloat() / COMBO_DURATION_MS
            }
        } else {
            _comboTimeLeft.value = 0f
        }

        // Damage Flash decay
        if (_damageFlashAlpha.value > 0f) {
            _damageFlashAlpha.value = (_damageFlashAlpha.value - (dtMs.toFloat() / 400f)).coerceAtLeast(0f)
        }

        // Active Powerup Timers update
        updatePowerUpTimers(dtMs)

        val slowMoFactor = if (_slowMoActive.value) 0.5f else 1.0f

        updateLevelProgression(dtMs)
        updatePlayerBulletsAndCombat(dtMs, slowMoFactor)
        updateBossAndCombat(dtMs, slowMoFactor)

        // Entity Spawning Logic with 30s Difficulty Ramp
        val now = System.currentTimeMillis()
        if (now - lastDifficultyIncreaseTime > 30000L) {
            dynamicDifficultyMultiplier += 0.15f
            lastDifficultyIncreaseTime = now
            addTextPop("DIFFICULTY UP! SPEED +15%", Color(0xFFFF3D00), player.x, player.y - 120f, scale = 1.4f)
            playSimulatedSFX(ToneGenerator.TONE_CDMA_HIGH_L, 150)
        }

        val speedFactorWithRamp = currentDifficulty.speedMultiplier * dynamicDifficultyMultiplier
        val adjustedSpawnInterval = (currentDifficulty.spawnIntervalMs / speedFactorWithRamp).toLong()
        val finalSpawnInterval = (adjustedSpawnInterval / slowMoFactor).toLong()

        if (!_isBossActive.value && now - lastSpawnTime > finalSpawnInterval && asteroids.size < currentDifficulty.maxConcurrentAsteroids) {
            spawnAsteroid()
            lastSpawnTime = now
        }

        // Power-up score gate spawning
        if (_score.value >= nextPowerUpSpawnScore) {
            spawnPowerUp()
            nextPowerUpSpawnScore += 300 + Random.nextInt(200)
        }

        // Coin Spawning
        val coinSpawnInterval = (1800L / slowMoFactor).toLong()
        if (now - lastCoinSpawnTime > coinSpawnInterval && coinsList.size < 6) {
            spawnCoin()
            lastCoinSpawnTime = now
        }

        // Update Asteroid movements
        val asteroidIterator = asteroids.iterator()
        while (asteroidIterator.hasNext()) {
            val asteroid = asteroidIterator.next()
            val finalSpeed = asteroid.speed * currentDifficulty.speedMultiplier * slowMoFactor * dynamicDifficultyMultiplier
            asteroid.y += finalSpeed
            asteroid.rotation += asteroid.rotationSpeed

            // Smarter enemy AI movement mechanics
            if (asteroid.type == AsteroidType.HUNTER) {
                val dx = player.x - asteroid.x
                val chaseSpeed = 2.0f * currentDifficulty.speedMultiplier * slowMoFactor * dynamicDifficultyMultiplier
                if (abs(dx) > 6f) {
                    asteroid.x += sign(dx) * chaseSpeed
                }
            } else if (asteroid.type == AsteroidType.WAVY) {
                val waveAmplitude = 5f * dynamicDifficultyMultiplier
                val waveFrequency = 0.03f
                asteroid.x += sin(asteroid.y * waveFrequency) * waveAmplitude
            }

            // Keep asteroids bounded horizontally
            asteroid.x = asteroid.x.coerceIn(asteroid.radius, viewportWidth - asteroid.radius)

            // Check if off-screen
            if (asteroid.y - asteroid.radius > viewportHeight) {
                asteroidIterator.remove()
                // Successfully dodged!
                incrementScore(10)
                continue
            }

            // Check near miss/close dodge mechanic
            val dist = distance(player.x, player.y, asteroid.x, asteroid.y)
            val shipRadius = player.width * 0.35f
            val nearMissDistance = shipRadius + asteroid.radius + 60f

            if (dist < nearMissDistance && dist >= (shipRadius + asteroid.radius)) {
                // High risk close dodge gives score pop and combo boost!
                if (Random.nextInt(50) == 0) { // Throttle pops slightly to avoid visual spam
                    addTextPop("CLOSE MISS! +25", Color(0xFFFFD600), asteroid.x, asteroid.y - 20f, scale = 1.3f)
                    incrementScore(25)
                    // Temporary shield boost/health boost chance on hard dodges
                    if (selectedShip == ShipType.SPEEDSTER && Random.nextInt(10) == 0) {
                        player.health = (player.health + 5f).coerceAtMost(player.maxHealth)
                        _health.value = player.health
                    }
                }
            }

            // Real physical collision check
            if (dist < (shipRadius + asteroid.radius)) {
                val collisionX = (player.x + asteroid.x) / 2
                val collisionY = (player.y + asteroid.y) / 2

                // SPEED BOOST: Ship is invincible and smashes obstacles
                if (player.speedBoostActive) {
                    spawnExplosion(collisionX, collisionY, asteroid.type == AsteroidType.GOLD, color = Color(0xFF00E5FF), count = 25)
                    incrementScore(50)
                    addTextPop("SMASH! +50", Color(0xFF00E5FF), asteroid.x, asteroid.y - 20f, scale = 1.3f)
                    playSimulatedSFX(ToneGenerator.TONE_CDMA_PIP, 100)
                    if (preferences.screenShakeEnabled.value) {
                        _screenShake.value = 0.5f
                    }
                    asteroidIterator.remove()
                    continue
                }

                // Trigger explosion particles at collision point
                spawnExplosion(collisionX, collisionY, asteroid.type == AsteroidType.GOLD)

                if (player.shieldActive) {
                    // Shield absorbs impact
                    player.shieldActive = false
                    _shieldActive.value = false
                    player.shieldTimeLeftMs = 0L
                    addTextPop("SHIELD ABSORBED!", Color(0xFFD500F9), player.x, player.y - 60f, scale = 1.2f)
                    playSimulatedSFX(ToneGenerator.TONE_CDMA_PIP, 150)
                } else {
                    // Reduce health
                    val damage = when (asteroid.type) {
                        AsteroidType.NORMAL -> 25f
                        AsteroidType.GOLD -> 15f
                        AsteroidType.EXPLOSIVE -> 40f
                        AsteroidType.HUNTER -> 30f
                        AsteroidType.WAVY -> 20f
                    }
                    player.health = (player.health - damage).coerceAtLeast(0f)
                    _health.value = player.health
                    
                    if (preferences.screenShakeEnabled.value) {
                        _screenShake.value = 1.0f // Trigger shake
                    }

                    // Reset combo on crash
                    _comboCount.value = 0
                    playerComboTimeLeftMs = 0L
                    _comboTimeLeft.value = 0f

                    // Trigger screen damage flash
                    _damageFlashAlpha.value = 0.5f

                    addTextPop("CRASH! -${damage.toInt()}HP", Color(0xFFFF1744), player.x, player.y - 60f, scale = 1.4f)
                    playSimulatedSFX(ToneGenerator.TONE_SUP_ERROR, 200)

                    // Reset multiplier on crash
                    _multiplier.value = 1

                    if (player.health <= 0f) {
                        // Big destruction explosion
                        spawnExplosion(player.x, player.y, true, count = 40)
                        _gameState.value = GameState.GAME_OVER
                        playSimulatedSFX(ToneGenerator.TONE_PROP_PROMPT, 500)
                    }
                }
                asteroidIterator.remove()
            }
        }

        // Update Power-ups
        val pIterator = powerUps.iterator()
        while (pIterator.hasNext()) {
            val powerUp = pIterator.next()
            powerUp.y += powerUp.speed * slowMoFactor

            if (powerUp.y - powerUp.radius > viewportHeight) {
                pIterator.remove()
                continue
            }

            // Collision check with Ship
            val dist = distance(player.x, player.y, powerUp.x, powerUp.y)
            val shipRadius = player.width * 0.4f
            if (dist < (shipRadius + powerUp.radius)) {
                activatePowerUp(powerUp.type)
                spawnExplosion(powerUp.x, powerUp.y, true, color = powerUp.type.color, count = 15)
                playSimulatedSFX(ToneGenerator.TONE_CDMA_PIP, 180)
                pIterator.remove()
            }
        }

        // Update Coins
        val coinIterator = coinsList.iterator()
        while (coinIterator.hasNext()) {
            val coin = coinIterator.next()

            val magnetLvl = preferences.getUpgradeLevel("magnet")
            val finalMagnetActive = player.magnetActive || (magnetLvl > 0 && distance(player.x, player.y, coin.x, coin.y) < (player.width * 0.4f + magnetLvl * 65f))

            if (finalMagnetActive) {
                val dx = player.x - coin.x
                val dy = player.y - coin.y
                val distToShip = distance(player.x, player.y, coin.x, coin.y)
                if (distToShip > 0) {
                    val basePull = if (player.magnetActive) 18f else 10f
                    val pullSpeed = (basePull + magnetLvl * 3.5f) * slowMoFactor
                    coin.x += (dx / distToShip) * pullSpeed
                    coin.y += (dy / distToShip) * pullSpeed
                }
            } else {
                coin.y += coin.speed * slowMoFactor
            }
            coin.rotation += coin.rotationSpeed

            if (coin.y - coin.radius > viewportHeight) {
                coinIterator.remove()
                continue
            }

            // Collision check with Ship
            val dist = distance(player.x, player.y, coin.x, coin.y)
            val shipRadius = player.width * 0.4f
            if (dist < (shipRadius + coin.radius)) {
                _coinsCollectedThisRun.value += 1
                preferences.addCoins(1)
                incrementScore(50)
                spawnExplosion(coin.x, coin.y, isGold = true, color = Color(0xFFFFD600), count = 12)
                addTextPop("+1 COIN", Color(0xFFFFEA00), coin.x, coin.y - 40f, scale = 1.3f)
                playSimulatedSFX(ToneGenerator.TONE_PROP_BEEP, 80)
                coinIterator.remove()
            }
        }

        // Add thrust engine particles
        if (Random.nextInt(3) == 0) {
            val flameX = player.x + (Random.nextFloat() - 0.5f) * 30f
            val flameY = player.y + player.height * 0.4f
            particles.add(
                Particle(
                    x = flameX,
                    y = flameY,
                    vx = (Random.nextFloat() - 0.5f) * 2f,
                    vy = 5f + Random.nextFloat() * 5f,
                    color = selectedShip.color.copy(alpha = 0.7f),
                    size = 4f + Random.nextFloat() * 6f,
                    decay = 0.08f
                )
            )
        }
    }

    private fun updateStarfield(height: Float) {
        val warpModifier = if (_gameState.value == GameState.PLAYING) {
            if (_slowMoActive.value) 1.5f else 3.5f
        } else {
            1.0f
        }
        for (star in stars) {
            star.y += star.speed * warpModifier
            if (star.y > height) {
                star.y = 0f
                star.x = Random.nextFloat() * viewportWidth
            }
        }
    }

    private fun updateParticles() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx
            p.y += p.vy
            p.life -= p.decay
            p.alpha = p.life.coerceIn(0f, 1f)
            if (p.life <= 0f) {
                iterator.remove()
            }
        }
    }

    private fun updateTextPops() {
        val iterator = textPops.iterator()
        while (iterator.hasNext()) {
            val pop = iterator.next()
            pop.y -= 2.0f // Float upwards
            pop.life -= 0.02f // Fade out
            if (pop.life <= 0f) {
                iterator.remove()
            }
        }
    }

    private fun updatePowerUpTimers(dtMs: Long) {
        if (player.shieldActive) {
            player.shieldTimeLeftMs -= dtMs
            if (player.shieldTimeLeftMs <= 0) {
                player.shieldActive = false
                _shieldActive.value = false
                addTextPop("SHIELD DEPLETED", Color(0xFFD500F9), player.x, player.y - 60f)
            } else {
                _shieldTimeLeft.value = player.shieldTimeLeftMs.toFloat() / PowerUpType.SHIELD.durationMs
            }
        }

        if (_slowMoActive.value) {
            player.multiplierTimeLeftMs -= dtMs
            if (player.multiplierTimeLeftMs <= 0) {
                _slowMoActive.value = false
                addTextPop("TIME NORMALIZED", Color(0xFF00E676), player.x, player.y - 60f)
            } else {
                _multiplierTimeLeft.value = player.multiplierTimeLeftMs.toFloat() / PowerUpType.SLOW_MO.durationMs
            }
        }

        if (_multiplier.value > 1 && player.multiplierTimeLeftMs > 0 && !_slowMoActive.value) {
            player.multiplierTimeLeftMs -= dtMs
            if (player.multiplierTimeLeftMs <= 0) {
                _multiplier.value = 1
                addTextPop("MULTIPLIER RESET", Color(0xFFFFD600), player.x, player.y - 60f)
            } else {
                _multiplierTimeLeft.value = player.multiplierTimeLeftMs.toFloat() / PowerUpType.MULTIPLIER.durationMs
            }
        }

        if (_magnetActive.value) {
            player.magnetTimeLeftMs -= dtMs
            if (player.magnetTimeLeftMs <= 0) {
                _magnetActive.value = false
                player.magnetActive = false
                addTextPop("MAGNET EXPIRED", Color(0xFFFF5722), player.x, player.y - 60f)
            } else {
                _magnetTimeLeft.value = player.magnetTimeLeftMs.toFloat() / PowerUpType.MAGNET.durationMs
            }
        }

        if (_speedBoostActive.value) {
            player.speedBoostTimeLeftMs -= dtMs
            if (player.speedBoostTimeLeftMs <= 0) {
                _speedBoostActive.value = false
                player.speedBoostActive = false
                addTextPop("SPEED NORMALIZED", Color(0xFF00E5FF), player.x, player.y - 60f)
            } else {
                _speedBoostTimeLeft.value = player.speedBoostTimeLeftMs.toFloat() / PowerUpType.SPEED_BOOST.durationMs
            }
        }
    }

    private fun spawnAsteroid() {
        val radius = 40f + Random.nextFloat() * 70f
        val x = radius + Random.nextFloat() * (viewportWidth - radius * 2)
        val y = -radius

        val speed = 6f + Random.nextFloat() * 8f
        val rotSpeed = -5f + Random.nextFloat() * 10f

        val typeRand = Random.nextFloat()
        val type = when {
            typeRand < 0.12f -> AsteroidType.GOLD
            typeRand < 0.22f -> AsteroidType.EXPLOSIVE
            typeRand < 0.35f -> AsteroidType.WAVY
            typeRand < 0.48f -> AsteroidType.HUNTER
            else -> AsteroidType.NORMAL
        }

        asteroids.add(
            Asteroid(
                id = ++idCounter,
                x = x,
                y = y,
                radius = radius,
                speed = speed,
                rotation = 0f,
                rotationSpeed = rotSpeed,
                type = type
            )
        )
    }

    private fun spawnPowerUp() {
        val x = 50f + Random.nextFloat() * (viewportWidth - 100f)
        val y = -50f
        val types = PowerUpType.values()
        val type = types[Random.nextInt(types.size)]

        powerUps.add(
            PowerUp(
                id = ++idCounter,
                type = type,
                x = x,
                y = y
            )
        )
    }

    private fun spawnCoin() {
        val radius = 30f
        if (viewportWidth <= 0f) return
        val x = radius + Random.nextFloat() * (viewportWidth - radius * 2)
        val y = -radius
        val speed = 5f + Random.nextFloat() * 4f
        val rotSpeed = 3f + Random.nextFloat() * 4f
        coinsList.add(
            Coin(
                id = ++idCounter,
                x = x,
                y = y,
                radius = radius,
                speed = speed,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = rotSpeed
            )
        )
    }

    private fun activatePowerUp(type: PowerUpType) {
        addTextPop("${type.symbol} ${type.name}!", type.color, player.x, player.y - 80f, scale = 1.3f)

        val shieldLvl = preferences.getUpgradeLevel("shield")
        var baseDuration = type.durationMs
        if (type == PowerUpType.SHIELD) {
            baseDuration += shieldLvl * 1500L
        }
        if (selectedShip == ShipType.CHRONO_KEEPER) {
            baseDuration = (baseDuration * 1.30f).toLong()
        }

        when (type) {
            PowerUpType.SHIELD -> {
                player.shieldActive = true
                _shieldActive.value = true
                player.shieldTimeLeftMs = baseDuration
                _shieldTimeLeft.value = 1.0f
            }
            PowerUpType.SLOW_MO -> {
                _slowMoActive.value = true
                player.multiplierTimeLeftMs = baseDuration
                _multiplierTimeLeft.value = 1.0f
            }
            PowerUpType.HEALTH -> {
                val healAmt = when (selectedShip) {
                    ShipType.SHIELD_DEFENDER -> 50f
                    else -> 40f
                }
                player.health = (player.health + healAmt).coerceAtMost(player.maxHealth)
                _health.value = player.health
            }
            PowerUpType.MULTIPLIER -> {
                _multiplier.value = _multiplier.value * 2
                player.multiplierTimeLeftMs = baseDuration
                _multiplierTimeLeft.value = 1.0f
            }
            PowerUpType.MAGNET -> {
                player.magnetActive = true
                _magnetActive.value = true
                player.magnetTimeLeftMs = baseDuration
                _magnetTimeLeft.value = 1.0f
            }
            PowerUpType.SPEED_BOOST -> {
                player.speedBoostActive = true
                _speedBoostActive.value = true
                player.speedBoostTimeLeftMs = baseDuration
                _speedBoostTimeLeft.value = 1.0f
                playSimulatedSFX(ToneGenerator.TONE_SUP_CONFIRM, 250)
            }
        }
    }

    private fun incrementScore(amount: Int) {
        val comboMult = if (_comboCount.value > 1) 1 + _comboCount.value / 5 else 1
        val total = amount * currentDifficulty.scoreMultiplier * _multiplier.value * comboMult
        _score.value += total
    }

    private fun spawnExplosion(x: Float, y: Float, isGold: Boolean, color: Color? = null, count: Int = 20) {
        val r = Random
        val actualColor = color ?: if (isGold) Color(0xFFFFD600) else Color(0xFFFF6D00)
        for (i in 0 until count) {
            val angle = r.nextFloat() * 2 * Math.PI
            val speed = 2f + r.nextFloat() * 10f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = (Math.cos(angle) * speed).toFloat(),
                    vy = (Math.sin(angle) * speed).toFloat(),
                    color = actualColor.copy(alpha = 0.8f + r.nextFloat() * 0.2f),
                    size = 5f + r.nextFloat() * 12f,
                    decay = 0.02f + r.nextFloat() * 0.04f
                )
            )
        }
    }

    private fun addTextPop(text: String, color: Color, x: Float, y: Float, scale: Float = 1.0f) {
        textPops.add(TextPop(text, color, x, y, scale = scale))
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }

    private fun updateLevelProgression(dtMs: Long) {
        if (_isBossActive.value) {
            val currentBoss = boss
            if (currentBoss != null) {
                _levelProgress.value = (currentBoss.health / currentBoss.maxHealth).coerceIn(0f, 1f)
            }
            return
        }

        val timeLeft = _levelTimeLeftMs.value - dtMs
        _levelTimeLeftMs.value = timeLeft
        val pct = ((30000L - timeLeft).toFloat() / 30000f).coerceIn(0f, 1f)
        _levelProgress.value = pct

        if (timeLeft <= 0) {
            completeCurrentLevel()
        }
    }

    fun completeCurrentLevel() {
        val level = _currentLevel.value
        val completionBonus = level * 60
        preferences.addCoins(completionBonus)
        _coinsCollectedThisRun.value += completionBonus
        _coinsEarnedThisLevel.value = completionBonus
        _scoreEarnedThisLevel.value = level * 1000
        incrementScore(level * 1000)

        _slowMoActive.value = false
        _shieldActive.value = false
        _magnetActive.value = false
        _speedBoostActive.value = false
        asteroids.clear()
        powerUps.clear()
        coinsList.clear()
        playerBullets.clear()
        bossBullets.clear()
        boss = null

        preferences.vibrate(300)
        playSimulatedSFX(ToneGenerator.TONE_SUP_CONFIRM, 400)

        setGameState(GameState.LEVEL_COMPLETE)
    }

    fun proceedToNextLevel() {
        val nextLvl = _currentLevel.value + 1
        _currentLevel.value = nextLvl
        _coinsEarnedThisLevel.value = 0
        _scoreEarnedThisLevel.value = 0

        if (nextLvl % 5 == 0) {
            _isBossActive.value = true
            _levelProgress.value = 1.0f
            val bossTypeIndex = ((nextLvl / 5 - 1) % 5).coerceIn(0, 4)
            val type = BossType.values()[bossTypeIndex]
            val maxH = type.maxHealth * (1.0f + (nextLvl / 5 - 1) * 0.4f)
            boss = Boss(
                type = type,
                x = viewportWidth / 2f,
                y = -150f,
                health = maxH,
                maxHealth = maxH
            )
            _bossHealth.value = maxH
            _bossMaxHealth.value = maxH
            _bossName.value = type.displayName
            
            addTextPop("WARNING: BOSS APPROACHING", Color(0xFFFF1744), viewportWidth / 2f, viewportHeight / 2f - 100f, scale = 1.6f)
            playSimulatedSFX(ToneGenerator.TONE_CDMA_HIGH_L, 500)
            preferences.vibrate(500)
        } else {
            _isBossActive.value = false
            _levelTimeLeftMs.value = 30000L
            _levelProgress.value = 0f
            addTextPop("LEVEL $nextLvl", Color(0xFF00FFCC), viewportWidth / 2f, viewportHeight / 2f, scale = 1.5f)
            playSimulatedSFX(ToneGenerator.TONE_CDMA_PIP, 200)
        }

        setGameState(GameState.PLAYING)
    }

    private fun updatePlayerBulletsAndCombat(dtMs: Long, slowMoFactor: Float) {
        val now = System.currentTimeMillis()
        val fireRateLvl = preferences.getUpgradeLevel("firerate")
        var shootInterval = maxOf(130L, 380L - fireRateLvl * 50L)
        if (selectedShip == ShipType.VOID_STALKER) {
            shootInterval = (shootInterval * 0.75f).toLong()
        }

        if (now - lastPlayerShootTime > shootInterval && viewportWidth > 0f) {
            val dmgLvl = preferences.getUpgradeLevel("damage")
            val damage = 25f + dmgLvl * 10f
            val finalDamage = if (selectedShip == ShipType.NEBULA_REAVER) damage * 1.4f else damage
            
            if (dmgLvl >= 4) {
                playerBullets.add(PlayerBullet(id = ++idCounter, x = player.x - 30f, y = player.y - 20f, damage = finalDamage))
                playerBullets.add(PlayerBullet(id = ++idCounter, x = player.x, y = player.y - 45f, damage = finalDamage))
                playerBullets.add(PlayerBullet(id = ++idCounter, x = player.x + 30f, y = player.y - 20f, damage = finalDamage))
            } else if (dmgLvl >= 2) {
                playerBullets.add(PlayerBullet(id = ++idCounter, x = player.x - 20f, y = player.y - 30f, damage = finalDamage))
                playerBullets.add(PlayerBullet(id = ++idCounter, x = player.x + 20f, y = player.y - 30f, damage = finalDamage))
            } else {
                playerBullets.add(PlayerBullet(id = ++idCounter, x = player.x, y = player.y - 40f, damage = finalDamage))
            }
            lastPlayerShootTime = now
            playSimulatedSFX(ToneGenerator.TONE_PROP_ACK, 40)
        }

        val bulletIterator = playerBullets.iterator()
        while (bulletIterator.hasNext()) {
            val bullet = bulletIterator.next()
            bullet.y += bullet.speed * slowMoFactor

            if (bullet.y < -20f) {
                bulletIterator.remove()
                continue
            }

            var bulletDestroyed = false
            val asteroidIterator = asteroids.iterator()
            while (asteroidIterator.hasNext()) {
                val asteroid = asteroidIterator.next()
                val dist = distance(bullet.x, bullet.y, asteroid.x, asteroid.y)
                if (dist < (bullet.radius + asteroid.radius)) {
                    val asteroidColor = when (asteroid.type) {
                        AsteroidType.NORMAL -> Color(0xFFFF7A00)
                        AsteroidType.GOLD -> Color(0xFFFFD600)
                        AsteroidType.EXPLOSIVE -> Color(0xFFFF1744)
                        AsteroidType.HUNTER -> Color(0xFFE040FB)
                        AsteroidType.WAVY -> Color(0xFF00E5FF)
                    }

                    // Increment Combo
                    _comboCount.value += 1
                    playerComboTimeLeftMs = COMBO_DURATION_MS
                    _comboTimeLeft.value = 1.0f

                    // Spawn a punchy explosion
                    spawnExplosion(bullet.x, bullet.y, asteroid.type == AsteroidType.GOLD, color = asteroidColor, count = 25)

                    val baseAmount = 30
                    val comboMult = if (_comboCount.value > 1) 1 + _comboCount.value / 5 else 1
                    val finalAddedScore = baseAmount * currentDifficulty.scoreMultiplier * _multiplier.value * comboMult

                    val comboText = if (_comboCount.value >= 2) " x${_comboCount.value}" else ""
                    addTextPop("+$finalAddedScore$comboText", asteroidColor, asteroid.x, asteroid.y - 20f, scale = if (_comboCount.value > 5) 1.5f else 1.1f)

                    if (_comboCount.value % 5 == 0) {
                        playSimulatedSFX(ToneGenerator.TONE_CDMA_PIP, 100)
                    }

                    incrementScore(baseAmount)
                    
                    if (preferences.screenShakeEnabled.value) {
                        _screenShake.value = maxOf(_screenShake.value, 0.25f)
                    }
                    preferences.vibrate(35)

                    asteroidIterator.remove()
                    bulletDestroyed = true
                    break
                }
            }

            if (bulletDestroyed) {
                bulletIterator.remove()
                continue
            }

            val activeBoss = boss
            if (_isBossActive.value && activeBoss != null) {
                val bXDist = abs(bullet.x - activeBoss.x)
                val bYDist = abs(bullet.y - activeBoss.y)
                if (bXDist < activeBoss.width * 0.45f && bYDist < activeBoss.height * 0.45f) {
                    activeBoss.health -= bullet.damage
                    _bossHealth.value = maxOf(0f, activeBoss.health)
                    
                    spawnExplosion(bullet.x, bullet.y, false, color = activeBoss.type.color, count = 5)
                    playSimulatedSFX(ToneGenerator.TONE_CDMA_PIP, 50)
                    
                    if (preferences.screenShakeEnabled.value) {
                        _screenShake.value = maxOf(_screenShake.value, 0.2f)
                    }
                    preferences.vibrate(40)

                    bulletIterator.remove()

                    if (activeBoss.health <= 0f) {
                        defeatBoss(activeBoss)
                    }
                    break
                }
            }
        }
    }

    private fun defeatBoss(activeBoss: Boss) {
        spawnExplosion(activeBoss.x, activeBoss.y, true, color = activeBoss.type.color, count = 50)
        spawnExplosion(activeBoss.x - 40f, activeBoss.y + 20f, true, color = Color(0xFFFFEA00), count = 25)
        spawnExplosion(activeBoss.x + 40f, activeBoss.y - 20f, true, color = Color(0xFF00FFCC), count = 25)

        val rewardCoins = 200 + _currentLevel.value * 20
        preferences.addCoins(rewardCoins)
        _coinsCollectedThisRun.value += rewardCoins
        _coinsEarnedThisLevel.value = rewardCoins
        _scoreEarnedThisLevel.value = _currentLevel.value * 2000
        incrementScore(_currentLevel.value * 2000)

        _slowMoActive.value = false
        _shieldActive.value = false
        _magnetActive.value = false
        _speedBoostActive.value = false

        asteroids.clear()
        powerUps.clear()
        coinsList.clear()
        playerBullets.clear()
        bossBullets.clear()
        boss = null
        _isBossActive.value = false

        playSimulatedSFX(ToneGenerator.TONE_SUP_CONFIRM, 600)
        preferences.vibrate(600)

        setGameState(GameState.LEVEL_COMPLETE)
    }

    private fun updateBossAndCombat(dtMs: Long, slowMoFactor: Float) {
        val activeBoss = boss
        if (!_isBossActive.value || activeBoss == null) {
            bossBullets.clear()
            return
        }

        if (activeBoss.y < 180f) {
            activeBoss.y += 2.0f * slowMoFactor
        } else {
            activeBoss.x += activeBoss.directionX * 3.0f * slowMoFactor
            if (activeBoss.x < activeBoss.width * 0.6f || activeBoss.x > viewportWidth - activeBoss.width * 0.6f) {
                activeBoss.directionX = -activeBoss.directionX
                activeBoss.x = activeBoss.x.coerceIn(activeBoss.width * 0.6f, viewportWidth - activeBoss.width * 0.6f)
            }
        }

        val now = System.currentTimeMillis()
        val attackInterval = when (activeBoss.type) {
            BossType.NEBULA_DREADNOUGHT -> 1800L
            BossType.VOID_REAVER -> 2200L
            BossType.STAR_DEVOURER -> 2500L
            BossType.QUANTUM_PHANTOM -> 3000L
            BossType.CHAOS_SINGULARITY -> 2400L
        }

        if (now - activeBoss.lastAttackTime > attackInterval && activeBoss.y >= 170f) {
            activeBoss.lastAttackTime = now
            val bossColor = activeBoss.type.color

            when (activeBoss.type) {
                BossType.NEBULA_DREADNOUGHT -> {
                    for (i in 0 until 5) {
                        val angle = Math.PI * (0.3f + i * 0.1f)
                        val vx = (Math.cos(angle) * 7f).toFloat()
                        val vy = (Math.sin(angle) * 7f).toFloat()
                        bossBullets.add(BossBullet(id = ++idCounter, x = activeBoss.x, y = activeBoss.y + 30f, vx = vx, vy = vy, color = bossColor))
                    }
                    playSimulatedSFX(ToneGenerator.TONE_SUP_CONFIRM, 120)
                }
                BossType.VOID_REAVER -> {
                    scope.launch {
                        for (burst in 0 until 3) {
                            if (!_isBossActive.value || boss == null) break
                            bossBullets.add(BossBullet(id = ++idCounter, x = activeBoss.x - 30f, y = activeBoss.y + 30f, vx = -1f, vy = 11f, color = bossColor))
                            bossBullets.add(BossBullet(id = ++idCounter, x = activeBoss.x + 30f, y = activeBoss.y + 30f, vx = 1f, vy = 11f, color = bossColor))
                            playSimulatedSFX(ToneGenerator.TONE_CDMA_PIP, 60)
                            kotlinx.coroutines.delay(120)
                        }
                    }
                }
                BossType.STAR_DEVOURER -> {
                    val dx = if (player.x > activeBoss.x) 2.5f else -2.5f
                    bossBullets.add(BossBullet(id = ++idCounter, x = activeBoss.x - 40f, y = activeBoss.y + 20f, vx = dx, vy = 5f, radius = 20f, damage = 20f, color = bossColor))
                    bossBullets.add(BossBullet(id = ++idCounter, x = activeBoss.x + 40f, y = activeBoss.y + 20f, vx = -dx, vy = 5f, radius = 20f, damage = 20f, color = bossColor))
                    playSimulatedSFX(ToneGenerator.TONE_SUP_CONFIRM, 150)
                }
                BossType.QUANTUM_PHANTOM -> {
                    activeBoss.x = activeBoss.width * 0.6f + Random.nextFloat() * (viewportWidth - activeBoss.width * 1.2f)
                    spawnExplosion(activeBoss.x, activeBoss.y, false, color = bossColor, count = 12)
                    
                    for (i in 0 until 7) {
                        val angle = Math.PI * (0.25f + i * 0.08f)
                        val vx = (Math.cos(angle) * 8.5f).toFloat()
                        val vy = (Math.sin(angle) * 8.5f).toFloat()
                        bossBullets.add(BossBullet(id = ++idCounter, x = activeBoss.x, y = activeBoss.y + 30f, vx = vx, vy = vy, color = bossColor))
                    }
                    playSimulatedSFX(ToneGenerator.TONE_SUP_CONFIRM, 150)
                }
                BossType.CHAOS_SINGULARITY -> {
                    for (i in 0 until 10) {
                        val angle = (2 * Math.PI / 10) * i
                        val vx = (Math.cos(angle) * 6.5f).toFloat()
                        val vy = (Math.sin(angle) * 6.5f).toFloat()
                        bossBullets.add(BossBullet(id = ++idCounter, x = activeBoss.x, y = activeBoss.y, vx = vx, vy = vy, color = bossColor))
                    }
                    playSimulatedSFX(ToneGenerator.TONE_SUP_CONFIRM, 200)
                }
            }
        }

        val bIterator = bossBullets.iterator()
        while (bIterator.hasNext()) {
            val bullet = bIterator.next()
            
            if (activeBoss.type == BossType.STAR_DEVOURER && bullet.radius > 15f) {
                val dx = player.x - bullet.x
                bullet.vx = (bullet.vx + sign(dx) * 0.1f).coerceIn(-4f, 4f)
            }

            bullet.x += bullet.vx * slowMoFactor
            bullet.y += bullet.vy * slowMoFactor

            if (bullet.y > viewportHeight + 20f || bullet.y < -50f || bullet.x < -50f || bullet.x > viewportWidth + 50f) {
                bIterator.remove()
                continue
            }

            val dist = distance(bullet.x, bullet.y, player.x, player.y)
            val shipRadius = player.width * 0.35f
            if (dist < (shipRadius + bullet.radius)) {
                bIterator.remove()

                if (player.speedBoostActive) {
                    continue
                }

                if (player.shieldActive) {
                    player.shieldActive = false
                    _shieldActive.value = false
                    player.shieldTimeLeftMs = 0L
                    addTextPop("SHIELD BROKEN!", Color(0xFFD500F9), player.x, player.y - 60f, scale = 1.2f)
                    playSimulatedSFX(ToneGenerator.TONE_CDMA_PIP, 150)
                } else {
                    player.health = (player.health - bullet.damage).coerceAtLeast(0f)
                    _health.value = player.health
                    
                    if (preferences.screenShakeEnabled.value) {
                        _screenShake.value = 0.8f
                    }
                    preferences.vibrate(180)

                    // Reset combo on boss bullet hit
                    _comboCount.value = 0
                    playerComboTimeLeftMs = 0L
                    _comboTimeLeft.value = 0f

                    // Trigger screen damage flash
                    _damageFlashAlpha.value = 0.5f

                    addTextPop("-${bullet.damage.toInt()}HP", Color(0xFFFF1744), player.x, player.y - 60f, scale = 1.3f)
                    playSimulatedSFX(ToneGenerator.TONE_SUP_ERROR, 150)

                    if (player.health <= 0f) {
                        spawnExplosion(player.x, player.y, true, count = 45)
                        setGameState(GameState.GAME_OVER)
                        playSimulatedSFX(ToneGenerator.TONE_PROP_PROMPT, 500)
                    }
                }
            }
        }
    }

    fun submitHighScore(pilotName: String) {
        val finalPilot = pilotName.trim().ifEmpty { "Pilot Ace" }
        scope.launch {
            repository.insertScore(
                ScoreEntity(
                    pilotName = finalPilot,
                    score = _score.value,
                    difficulty = currentDifficulty.displayName
                )
            )
            // Go back to leaderboard or menu
            setGameState(GameState.LEADERBOARD)
        }
    }
}
