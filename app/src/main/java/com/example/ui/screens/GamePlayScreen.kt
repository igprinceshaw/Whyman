package com.example.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.AsteroidType
import com.example.game.GameState
import com.example.game.ShipType
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GamePlayScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameEngine = viewModel.gameEngine
    val gameState by gameEngine.gameState.collectAsState()
    val score by gameEngine.score.collectAsState()
    val multiplier by gameEngine.multiplier.collectAsState()
    val health by gameEngine.health.collectAsState()
    val maxHealth by gameEngine.maxHealth.collectAsState()
    val shieldActive by gameEngine.shieldActive.collectAsState()
    val shieldTimeLeft by gameEngine.shieldTimeLeft.collectAsState()
    val magnetActive by gameEngine.magnetActive.collectAsState()
    val magnetTimeLeft by gameEngine.magnetTimeLeft.collectAsState()
    val speedBoostActive by gameEngine.speedBoostActive.collectAsState()
    val speedBoostTimeLeft by gameEngine.speedBoostTimeLeft.collectAsState()
    val multiplierTimeLeft by gameEngine.multiplierTimeLeft.collectAsState()
    val slowMoActive by gameEngine.slowMoActive.collectAsState()
    val screenShake by gameEngine.screenShake.collectAsState()

    val currentLevel by gameEngine.currentLevel.collectAsState()
    val levelProgress by gameEngine.levelProgress.collectAsState()
    val levelTimeLeftMs by gameEngine.levelTimeLeftMs.collectAsState()
    val isBossActive by gameEngine.isBossActive.collectAsState()
    val bossHealth by gameEngine.bossHealth.collectAsState()
    val bossMaxHealth by gameEngine.bossMaxHealth.collectAsState()
    val bossName by gameEngine.bossName.collectAsState()

    // Frame trigger state to force redraw of Canvas on each frame tick
    val frameTick = remember { mutableStateOf(0L) }

    // Synchronous High-performance Game loop coroutine
    LaunchedEffect(gameState) {
        if (gameState == GameState.PLAYING) {
            var lastTime = System.currentTimeMillis()
            while (isActive) {
                withFrameMillis<Unit> { frameTime ->
                    val now = System.currentTimeMillis()
                    val dt = (now - lastTime).coerceAtLeast(1L)
                    lastTime = now

                    // Drive Game physics & updates
                    gameEngine.update(
                        width = gameEngine.stars.firstOrNull()?.let { 1080f } ?: 1080f, // Fallback width
                        height = 1920f, // Fallback height
                        dtMs = dt
                    )
                    frameTick.value++
                }
            }
        }
    }

    // Capture screen shake translation offset
    val shakeOffset = remember(screenShake, frameTick.value) {
        if (screenShake > 0f) {
            val r = Random
            Offset(
                (r.nextFloat() - 0.5f) * screenShake * 35f,
                (r.nextFloat() - 0.5f) * screenShake * 35f
            )
        } else {
            Offset.Zero
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF03001e))
            .pointerInput(Unit) {
                // Support both drag to move and fast tap/warp teleportation
                detectTapGestures { offset ->
                    gameEngine.warpShip(offset.x, offset.y)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    gameEngine.moveShip(dragAmount.x, dragAmount.y)
                }
            }
            .testTag("game_play_area")
    ) {
        // High-Performance Graphics Rendering Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = shakeOffset.x
                    translationY = shakeOffset.y
                }
        ) {
            // Read frameTick to trigger Canvas draw block redraw
            val _trigger = frameTick.value

            val canvasWidth = size.width
            val canvasHeight = size.height

            // 1. Draw Starfield
            for (star in gameEngine.stars) {
                drawCircle(
                    color = Color.White.copy(alpha = star.alpha),
                    radius = star.size,
                    center = Offset(star.x, star.y)
                )
            }

            // 2. Draw Particles
            for (p in gameEngine.particles) {
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.size,
                    center = Offset(p.x, p.y)
                )
            }

            // 3. Draw Power-Ups
            for (pu in gameEngine.powerUps) {
                // Glowing outer aura
                drawCircle(
                    color = pu.type.color.copy(alpha = 0.3f),
                    radius = pu.radius * 1.5f,
                    center = Offset(pu.x, pu.y)
                )
                // Solid core
                drawCircle(
                    color = pu.type.color,
                    radius = pu.radius,
                    center = Offset(pu.x, pu.y),
                    style = Stroke(width = 4f)
                )

                // Render powerup symbol icon inside
                drawContext.canvas.nativeCanvas.drawText(
                    pu.type.symbol,
                    pu.x - 22f,
                    pu.y + 14f,
                    Paint().apply {
                        textSize = 45f
                        textAlign = Paint.Align.LEFT
                    }
                )
            }

            // 4. Draw Asteroids
            for (ast in gameEngine.asteroids) {
                val baseColor = when (ast.type) {
                    AsteroidType.NORMAL -> Color(0xFFFF7A00) // Neon Orange
                    AsteroidType.GOLD -> Color(0xFFFFD600)   // Solar Yellow Gold
                    AsteroidType.EXPLOSIVE -> Color(0xFFFF1744) // Crimson Red
                    AsteroidType.HUNTER -> Color(0xFFE040FB) // Electric Magenta
                    AsteroidType.WAVY -> Color(0xFF00E5FF) // Neon Cyan
                }

                // High score retro vector polygon drawing of asteroids
                val path = Path()
                val numVertices = 8
                val angleInc = (2 * Math.PI / numVertices)
                for (i in 0 until numVertices) {
                    val angle = i * angleInc + Math.toRadians(ast.rotation.toDouble())
                    // Deform circle slightly to make it look jagged
                    val offsetRadius = ast.radius * (0.8f + 0.3f * sin(i * 1.5f + ast.id))
                    val px = ast.x + (cos(angle) * offsetRadius).toFloat()
                    val py = ast.y + (sin(angle) * offsetRadius).toFloat()

                    if (i == 0) {
                        path.moveTo(px, py)
                    } else {
                        path.lineTo(px, py)
                    }
                }
                path.close()

                // Outline vector drawing
                drawPath(
                    path = path,
                    color = baseColor,
                    style = Stroke(width = 5f)
                )
                // Transparent fill
                drawPath(
                    path = path,
                    color = baseColor.copy(alpha = 0.15f)
                )

                // Inner crater lines for visual retro fidelity
                drawLine(
                    color = baseColor.copy(alpha = 0.4f),
                    start = Offset(ast.x - ast.radius * 0.3f, ast.y - ast.radius * 0.2f),
                    end = Offset(ast.x + ast.radius * 0.2f, ast.y - ast.radius * 0.4f),
                    strokeWidth = 3f
                )
                drawLine(
                    color = baseColor.copy(alpha = 0.4f),
                    start = Offset(ast.x - ast.radius * 0.4f, ast.y + ast.radius * 0.3f),
                    end = Offset(ast.x + ast.radius * 0.1f, ast.y + ast.radius * 0.1f),
                    strokeWidth = 3f
                )
            }

            // 4b. Draw Coins
            for (coin in gameEngine.coinsList) {
                val baseColor = Color(0xFFFFD600) // Solar Yellow Gold
                
                // Draw rotating hexagonal coin shape
                val coinPath = Path()
                val numVertices = 6
                val angleInc = (2 * Math.PI / numVertices)
                for (i in 0 until numVertices) {
                    val angle = i * angleInc + Math.toRadians(coin.rotation.toDouble())
                    val px = coin.x + (cos(angle) * coin.radius).toFloat()
                    val py = coin.y + (sin(angle) * coin.radius).toFloat()

                    if (i == 0) {
                        coinPath.moveTo(px, py)
                    } else {
                        coinPath.lineTo(px, py)
                    }
                }
                coinPath.close()

                // Outline vector drawing
                drawPath(
                    path = coinPath,
                    color = baseColor,
                    style = Stroke(width = 4f)
                )
                // Transparent fill
                drawPath(
                    path = coinPath,
                    color = baseColor.copy(alpha = 0.25f)
                )

                // Inner core ring
                drawCircle(
                    color = baseColor.copy(alpha = 0.6f),
                    radius = coin.radius * 0.45f,
                    center = Offset(coin.x, coin.y),
                    style = Stroke(width = 2f)
                )
            }

            // 4c. Draw Player Bullets
            for (b in gameEngine.playerBullets) {
                drawCircle(
                    color = Color(0xFF00FFCC),
                    radius = b.radius,
                    center = Offset(b.x, b.y)
                )
                drawCircle(
                    color = Color.White,
                    radius = b.radius * 0.5f,
                    center = Offset(b.x, b.y)
                )
            }

            // 4d. Draw Boss and Boss Bullets
            val activeBoss = gameEngine.boss
            if (gameEngine.isBossActive.value && activeBoss != null) {
                val bossColor = activeBoss.type.color
                val bPath = Path().apply {
                    moveTo(activeBoss.x, activeBoss.y - activeBoss.height * 0.45f)
                    lineTo(activeBoss.x + activeBoss.width * 0.45f, activeBoss.y - activeBoss.height * 0.15f)
                    lineTo(activeBoss.x + activeBoss.width * 0.3f, activeBoss.y + activeBoss.height * 0.4f)
                    lineTo(activeBoss.x, activeBoss.y + activeBoss.height * 0.15f)
                    lineTo(activeBoss.x - activeBoss.width * 0.3f, activeBoss.y + activeBoss.height * 0.4f)
                    lineTo(activeBoss.x - activeBoss.width * 0.45f, activeBoss.y - activeBoss.height * 0.15f)
                    close()
                }
                drawPath(bPath, bossColor, style = Stroke(width = 8f))
                drawPath(bPath, bossColor.copy(alpha = 0.2f))

                // Draw core reactor glow
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = 25f,
                    center = Offset(activeBoss.x, activeBoss.y)
                )
                drawCircle(
                    color = bossColor,
                    radius = 12f,
                    center = Offset(activeBoss.x, activeBoss.y)
                )
            }

            for (bb in gameEngine.bossBullets) {
                drawCircle(
                    color = bb.color,
                    radius = bb.radius,
                    center = Offset(bb.x, bb.y)
                )
                drawCircle(
                    color = Color.White,
                    radius = bb.radius * 0.5f,
                    center = Offset(bb.x, bb.y)
                )
            }

            // 5. Draw Player Ship
            val p = gameEngine.player
            val shipType = gameEngine.selectedShip
            val shipColor = shipType.color

            // Glow aura
            drawCircle(
                color = shipColor.copy(alpha = 0.2f),
                radius = p.width * 0.6f,
                center = Offset(p.x, p.y)
            )

            // Drawing custom neon spaceship vector geometries
            val shipPath = Path()
            when (shipType) {
                ShipType.SPEEDSTER -> {
                    shipPath.moveTo(p.x, p.y - p.height * 0.45f)                  // Nose
                    shipPath.lineTo(p.x + p.width * 0.35f, p.y + p.height * 0.3f)   // Right wingtip
                    shipPath.lineTo(p.x + p.width * 0.12f, p.y + p.height * 0.2f)   // Engine bay right
                    shipPath.lineTo(p.x - p.width * 0.12f, p.y + p.height * 0.2f)   // Engine bay left
                    shipPath.lineTo(p.x - p.width * 0.35f, p.y + p.height * 0.3f)   // Left wingtip
                    shipPath.close()
                }
                ShipType.SHIELD_DEFENDER -> {
                    shipPath.moveTo(p.x, p.y - p.height * 0.4f)
                    shipPath.lineTo(p.x + p.width * 0.45f, p.y - p.height * 0.1f)
                    shipPath.lineTo(p.x + p.width * 0.3f, p.y + p.height * 0.35f)
                    shipPath.lineTo(p.x + p.width * 0.12f, p.y + p.height * 0.22f)
                    shipPath.lineTo(p.x - p.width * 0.12f, p.y + p.height * 0.22f)
                    shipPath.lineTo(p.x - p.width * 0.3f, p.y + p.height * 0.35f)
                    shipPath.lineTo(p.x - p.width * 0.45f, p.y - p.height * 0.1f)
                    shipPath.close()
                }
                ShipType.VANGUARD -> {
                    shipPath.moveTo(p.x, p.y - p.height * 0.48f)
                    shipPath.lineTo(p.x + p.width * 0.18f, p.y - p.height * 0.12f)
                    shipPath.lineTo(p.x + p.width * 0.42f, p.y + p.height * 0.32f)
                    shipPath.lineTo(p.x + p.width * 0.15f, p.y + p.height * 0.18f)
                    shipPath.lineTo(p.x, p.y + p.height * 0.35f)
                    shipPath.lineTo(p.x - p.width * 0.15f, p.y + p.height * 0.18f)
                    shipPath.lineTo(p.x - p.width * 0.42f, p.y + p.height * 0.32f)
                    shipPath.lineTo(p.x - p.width * 0.18f, p.y - p.height * 0.12f)
                    shipPath.close()
                }
                ShipType.VOID_STALKER -> {
                    shipPath.moveTo(p.x, p.y - p.height * 0.5f)
                    shipPath.lineTo(p.x + p.width * 0.25f, p.y - p.height * 0.1f)
                    shipPath.lineTo(p.x + p.width * 0.45f, p.y + p.height * 0.4f)
                    shipPath.lineTo(p.x + p.width * 0.1f, p.y + p.height * 0.15f)
                    shipPath.lineTo(p.x - p.width * 0.1f, p.y + p.height * 0.15f)
                    shipPath.lineTo(p.x - p.width * 0.45f, p.y + p.height * 0.4f)
                    shipPath.lineTo(p.x - p.width * 0.25f, p.y - p.height * 0.1f)
                    shipPath.close()
                }
                ShipType.CHRONO_KEEPER -> {
                    shipPath.moveTo(p.x, p.y - p.height * 0.45f)
                    shipPath.lineTo(p.x + p.width * 0.38f, p.y - p.height * 0.2f)
                    shipPath.lineTo(p.x + p.width * 0.38f, p.y + p.height * 0.2f)
                    shipPath.lineTo(p.x, p.y + p.height * 0.45f)
                    shipPath.lineTo(p.x - p.width * 0.38f, p.y + p.height * 0.2f)
                    shipPath.lineTo(p.x - p.width * 0.38f, p.y - p.height * 0.2f)
                    shipPath.close()
                }
                ShipType.NEBULA_REAVER -> {
                    shipPath.moveTo(p.x, p.y - p.height * 0.52f)
                    shipPath.lineTo(p.x + p.width * 0.15f, p.y - p.height * 0.25f)
                    shipPath.lineTo(p.x + p.width * 0.48f, p.y - p.height * 0.25f)
                    shipPath.lineTo(p.x + p.width * 0.25f, p.y + p.height * 0.35f)
                    shipPath.lineTo(p.x, p.y + p.height * 0.1f)
                    shipPath.lineTo(p.x - p.width * 0.25f, p.y + p.height * 0.35f)
                    shipPath.lineTo(p.x - p.width * 0.48f, p.y - p.height * 0.25f)
                    shipPath.lineTo(p.x - p.width * 0.15f, p.y - p.height * 0.25f)
                    shipPath.close()
                }
            }

            drawPath(
                path = shipPath,
                color = shipColor,
                style = Stroke(width = 6f)
            )
            drawPath(
                path = shipPath,
                color = shipColor.copy(alpha = 0.25f)
            )

            // Outer Shield Barrier (if shield active)
            if (shieldActive) {
                drawCircle(
                    color = Color(0xFFD500F9).copy(alpha = 0.18f),
                    radius = p.width * 0.75f,
                    center = Offset(p.x, p.y)
                )
                drawCircle(
                    color = Color(0xFFD500F9),
                    radius = p.width * 0.75f,
                    center = Offset(p.x, p.y),
                    style = Stroke(width = 4f)
                )
            }

            // Magnet Attraction Forcefield (if magnet active)
            if (magnetActive) {
                drawCircle(
                    color = Color(0xFFFF5722).copy(alpha = 0.15f),
                    radius = p.width * 0.95f,
                    center = Offset(p.x, p.y)
                )
                drawCircle(
                    color = Color(0xFFFF5722).copy(alpha = 0.6f),
                    radius = p.width * 0.95f,
                    center = Offset(p.x, p.y),
                    style = Stroke(width = 3f)
                )
            }

            // Speed Boost Hyperdrive Streams (if speed boost active)
            if (speedBoostActive) {
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                    radius = p.width * 1.1f,
                    center = Offset(p.x, p.y)
                )
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.7f),
                    radius = p.width * 1.1f,
                    center = Offset(p.x, p.y),
                    style = Stroke(width = 4f)
                )
                // Wingtip warp streams extending downwards
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                    start = Offset(p.x - p.width * 0.3f, p.y + p.height * 0.4f),
                    end = Offset(p.x - p.width * 0.3f, p.y + p.height * 1.3f),
                    strokeWidth = 4f
                )
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                    start = Offset(p.x + p.width * 0.3f, p.y + p.height * 0.4f),
                    end = Offset(p.x + p.width * 0.3f, p.y + p.height * 1.3f),
                    strokeWidth = 4f
                )
            }

            // 6. Draw floating score/combo pops
            for (pop in gameEngine.textPops) {
                drawContext.canvas.nativeCanvas.drawText(
                    pop.text,
                    pop.x,
                    pop.y,
                    Paint().apply {
                        color = android.graphics.Color.argb(
                            (pop.life * 255).toInt(),
                            (pop.color.red * 255).toInt(),
                            (pop.color.green * 255).toInt(),
                            (pop.color.blue * 255).toInt()
                        )
                        textSize = 34f * pop.scale
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    }
                )
            }
        }

        // --- HUD OVERLAY COVERS (Jetpack Compose Overlays) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Score Box
                Column {
                    Text(
                        text = "SCORE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = String.format("%06d", score),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 26.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val coinsCollectedThisRun by gameEngine.coinsCollectedThisRun.collectAsState()
                    val totalCoins by viewModel.preferences.coins.collectAsState()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🪙 $coinsCollectedThisRun ($totalCoins)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFFFEA00),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }

                // Difficulty Mode Indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = 4.dp)
                ) {
                    val activeColor = when (gameEngine.currentDifficulty) {
                        com.example.game.Difficulty.EASY -> Color(0xFF00E676)
                        com.example.game.Difficulty.MEDIUM -> Color(0xFF00E5FF)
                        com.example.game.Difficulty.HARD -> Color(0xFFFF007F)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(activeColor.copy(alpha = 0.15f))
                            .border(1.dp, activeColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = gameEngine.currentDifficulty.displayName.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = activeColor,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Combo/Multiplier Bubble & Pause
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (multiplier > 1) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFFFEA00))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "x$multiplier",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }

                    // PAUSE BTN
                    IconButton(
                        onClick = { gameEngine.setGameState(GameState.PAUSED) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .testTag("pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = Color.White
                        )
                    }
                }
            }

            // Boss or Level Progress Indicator
            if (isBossActive && bossMaxHealth > 0f) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(1.5.dp, Color(0xFFFF1744).copy(alpha = 0.8f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B000C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️ WARNING: $bossName",
                            color = Color(0xFFFF1744),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val progressRatio = (bossHealth / bossMaxHealth).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFFFF1744),
                            trackColor = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "REACTOR HEALTH: ${(progressRatio * 100f).toInt()}%",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF061A16)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LEVEL $currentLevel PROGRESS",
                                color = Color(0xFF00FFCC),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { levelProgress },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF00FFCC),
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF00FFCC).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${(levelTimeLeftMs / 1000L).coerceAtLeast(0L)}S",
                                color = Color(0xFF00FFCC),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Central indicators (Slow-Mo warp flash)
            if (slowMoActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Badge(
                        containerColor = Color(0xFF00E676).copy(alpha = 0.2f),
                        contentColor = Color(0xFF00E676),
                        modifier = Modifier.border(1.dp, Color(0xFF00E676), RoundedCornerShape(20.dp))
                    ) {
                        Text(
                            text = "⏱️ SLOW-MOTION FIELD ACTIVE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Bottom stats row (Health meter and dynamic PowerUp timers)
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Active power-up progress bars
                if (shieldActive && shieldTimeLeft > 0f) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🛡️ SHIELD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFD500F9),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.width(70.dp)
                        )
                        LinearProgressIndicator(
                            progress = { shieldTimeLeft },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFFD500F9),
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                    }
                }

                if (magnetActive && magnetTimeLeft > 0f) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🧲 MAGNET",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFF5722),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.width(70.dp)
                        )
                        LinearProgressIndicator(
                            progress = { magnetTimeLeft },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFFFF5722),
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                    }
                }

                if (speedBoostActive && speedBoostTimeLeft > 0f) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "⚡ BOOST",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF00E5FF),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.width(70.dp)
                        )
                        LinearProgressIndicator(
                            progress = { speedBoostTimeLeft },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFF00E5FF),
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                    }
                }

                if (multiplier > 1 && multiplierTimeLeft > 0f && !slowMoActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "⭐ MULTI",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFFEA00),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.width(70.dp)
                        )
                        LinearProgressIndicator(
                            progress = { multiplierTimeLeft },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFFFFEA00),
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // HP Indicator row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "HULL INTEGRITY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            Text(
                                text = "${health.toInt()}/${maxHealth.toInt()} AP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (health < 40f) Color(0xFFFF1744) else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val progressRatio = (health / maxHealth).coerceIn(0f, 1f)
                        val barColor = when {
                            progressRatio < 0.3f -> Color(0xFFFF1744) // Red
                            progressRatio < 0.6f -> Color(0xFFFFC107) // Yellow
                            else -> Color(0xFF00E676) // Green
                        }
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, barColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                            color = barColor,
                            trackColor = Color(0xFF1E0C3F)
                        )
                    }
                }
            }
        }

        // --- PAUSE MENU OVERLAY ---
        if (gameState == GameState.PAUSED) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xD003001E)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0D0024))
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "FLIGHT PAUSED",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                    )

                    Text(
                        text = "Tactical shield is active while paused.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // RESUME
                    Button(
                        onClick = { gameEngine.setGameState(GameState.PLAYING) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("resume_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Resume Icon",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RESUME FLIGHT",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    // RESTART
                    Button(
                        onClick = { viewModel.startGame() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("restart_from_pause"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF007F)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "RESTART MISSION",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    // ABORT
                    OutlinedButton(
                        onClick = { gameEngine.setGameState(GameState.MENU) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("abort_from_pause"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            width = 1.dp
                        )
                    ) {
                        Text(
                            text = "ABORT MISSION",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }
        }
    }
}
