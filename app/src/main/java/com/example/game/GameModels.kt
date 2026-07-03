package com.example.game

import androidx.compose.ui.graphics.Color

enum class GameState {
    MENU,
    SHIP_SELECT,
    LEADERBOARD,
    SETTINGS,
    PLAYING,
    PAUSED,
    GAME_OVER,
    LEVEL_COMPLETE,
    SPLASH,
    LOADING,
    ONBOARDING
}

enum class Difficulty(
    val displayName: String,
    val speedMultiplier: Float,
    val spawnIntervalMs: Long,
    val scoreMultiplier: Int,
    val maxConcurrentAsteroids: Int
) {
    EASY("Recruit", 0.7f, 1500L, 1, 6),
    MEDIUM("Veteran", 1.0f, 1000L, 2, 10),
    HARD("Elite", 1.4f, 650L, 3, 15)
}

enum class ShipType(
    val displayName: String,
    val description: String,
    val color: Color,
    val speedFactor: Float,
    val initialHealth: Float,
    val perk: String,
    val coinCost: Int = 0
) {
    SPEEDSTER(
        "Cosmic Swift",
        "Ultra-light fighter optimized for speed.",
        Color(0xFF00E5FF), // Cyan
        1.25f,
        100f,
        "+25% Ship Speed",
        0
    ),
    SHIELD_DEFENDER(
        "Aegis Vanguard",
        "Heavy interceptor with resilient shields.",
        Color(0xFFFF007F), // Neon Pink/Rose
        0.85f,
        150f,
        "+50% Armor Points",
        150
    ),
    VANGUARD(
        "Solar Phoenix",
        "Balanced dreadnought with a wide collision box.",
        Color(0xFFFFEA00), // Yellow/Gold
        1.0f,
        120f,
        "Balanced All-Rounder",
        300
    ),
    VOID_STALKER(
        "Void Stalker",
        "Stealth fighter with high bullet fire rate.",
        Color(0xFF9D00FF), // Violet
        1.15f,
        100f,
        "+25% Fire Rate",
        500
    ),
    CHRONO_KEEPER(
        "Chrono Keeper",
        "Time guardian; power-up durations increased.",
        Color(0xFF00FFCC), // Mint/Teal
        1.0f,
        110f,
        "+30% Powerup Duration",
        750
    ),
    NEBULA_REAVER(
        "Nebula Reaver",
        "Dreadnought carrying heavy laser cannons.",
        Color(0xFFFF3366), // Coral Pink
        0.9f,
        140f,
        "+40% Bullet Damage",
        1000
    )
}

enum class PowerUpType(
    val color: Color,
    val symbol: String,
    val durationMs: Long
) {
    SHIELD(Color(0xFFD500F9), "🛡️", 8000L),       // Purple shield
    SLOW_MO(Color(0xFF00E676), "⏱️", 6000L),      // Green slow motion
    HEALTH(Color(0xFFFF1744), "❤️", 0L),          // Red health pack
    MULTIPLIER(Color(0xFFFFD600), "⭐", 5000L),    // Gold double multiplier
    MAGNET(Color(0xFFFF5722), "🧲", 8000L),       // Orange coin magnet
    SPEED_BOOST(Color(0xFF00E5FF), "⚡", 6000L)   // Cyan hyper-speed boost
}

data class PlayerShip(
    var x: Float = 0f,
    var y: Float = 0f,
    val width: Float = 110f,
    val height: Float = 110f,
    var health: Float = 100f,
    var maxHealth: Float = 100f,
    var shieldActive: Boolean = false,
    var shieldTimeLeftMs: Long = 0L,
    var multiplierTimeLeftMs: Long = 0L,
    var magnetActive: Boolean = false,
    var magnetTimeLeftMs: Long = 0L,
    var speedBoostActive: Boolean = false,
    var speedBoostTimeLeftMs: Long = 0L
)

enum class AsteroidType {
    NORMAL,
    GOLD,        // Gives extra points on close dodge or collision absorb
    EXPLOSIVE,   // Detonates into particles on near collisions
    HUNTER,      // Chases player horizontally
    WAVY         // Zigzags sideways as it descends
}

data class Asteroid(
    val id: Long,
    var x: Float,
    var y: Float,
    val radius: Float,
    val speed: Float,
    var rotation: Float,
    val rotationSpeed: Float,
    val type: AsteroidType = AsteroidType.NORMAL
)

data class PowerUp(
    val id: Long,
    val type: PowerUpType,
    var x: Float,
    var y: Float,
    val radius: Float = 45f,
    val speed: Float = 5.0f
)

data class Particle(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    var alpha: Float = 1.0f,
    val size: Float,
    var life: Float = 1.0f, // 1.0 down to 0.0
    val decay: Float = 0.03f
)

data class WarpStar(
    var x: Float,
    var y: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float
)

data class Coin(
    val id: Long,
    var x: Float,
    var y: Float,
    val value: Int = 1,
    val radius: Float = 30f,
    val speed: Float = 5.0f,
    var rotation: Float = 0f,
    var rotationSpeed: Float = 2f
)

data class PlayerBullet(
    val id: Long,
    var x: Float,
    var y: Float,
    val radius: Float = 10f,
    val speed: Float = -22f,
    var damage: Float = 25f
)

enum class BossType(
    val displayName: String,
    val maxHealth: Float,
    val color: Color,
    val description: String
) {
    NEBULA_DREADNOUGHT("Nebula Dreadnought", 500f, Color(0xFFFF1744), "Fires dangerous sweeping red laser circles."),
    VOID_REAVER("Void Reaver", 800f, Color(0xFFE040FB), "Fires rapid triple-burst plasma pulses."),
    STAR_DEVOURER("Star Devourer", 1200f, Color(0xFFFF9100), "Spawns target-seeking homing space mines."),
    QUANTUM_PHANTOM("Quantum Phantom", 1600f, Color(0xFF2979FF), "Blinks and shoots high-density sweeping arcs."),
    CHAOS_SINGULARITY("Chaos Singularity", 2200f, Color(0xFF00E5FF), "Creates black holes and fires heavy radial storms.")
}

data class Boss(
    val type: BossType,
    var x: Float,
    var y: Float,
    var health: Float,
    var maxHealth: Float,
    val width: Float = 200f,
    val height: Float = 150f,
    var directionX: Float = 1f, // -1 or 1
    var lastAttackTime: Long = 0L,
    var specialStateTime: Long = 0L,
    var attackPhase: Int = 0
)

data class BossBullet(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float = 14f,
    val damage: Float = 15f,
    val color: Color = Color(0xFFFF1744)
)

