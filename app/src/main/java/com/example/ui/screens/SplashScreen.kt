package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameState
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    // Entrance animations
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }
    
    // Subtitle blinking animation
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    // Ship floating movement
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    // Cosmic background color shift
    val bgShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgShift"
    )

    LaunchedEffect(Unit) {
        // Trigger retro launch chime
        try {
            viewModel.gameEngine.playSimulatedSFX(android.media.ToneGenerator.TONE_SUP_CONFIRM, 150)
            delay(150)
            viewModel.gameEngine.playSimulatedSFX(android.media.ToneGenerator.TONE_PROP_ACK, 200)
        } catch (e: Exception) {
            // Ignore if tone generator unavailable
        }

        // Animate intro elements
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alpha.animateTo(1.0f, tween(1000))

        // Wait and navigate to Loading screen
        delay(1800)
        viewModel.changeGameState(GameState.LOADING)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable { viewModel.changeGameState(GameState.LOADING) } // Tap anywhere to skip
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF03001e),
                        Color(0xFF0C0028).copy(alpha = 1f - bgShift * 0.1f),
                        Color(0xFF1B003A).copy(alpha = 0.8f + bgShift * 0.2f),
                        Color(0xFF03001e)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Starfield
        val stars = remember {
            List(40) {
                Offset(
                    x = (100..900).random().toFloat() / 1000f,
                    y = (100..900).random().toFloat() / 1000f
                ) to (2..5).random().toFloat()
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            stars.forEach { (pos, size) ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = size,
                    center = Offset(pos.x * size.times(1000f) % size.times(1000f) + size, pos.y * size.times(1000f) % size.times(1000f) + size)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // Animated Neo-Retro Ship Emblem
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .offset(y = floatAnim.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    // Draw neon trail lines
                    drawLine(
                        color = Color(0xFFFF007F).copy(alpha = 0.6f),
                        start = Offset(w * 0.5f, h * 0.9f),
                        end = Offset(w * 0.5f, h * 1.2f),
                        strokeWidth = 6f
                    )
                    drawLine(
                        color = Color(0xFF00E5FF).copy(alpha = 0.4f),
                        start = Offset(w * 0.4f, h * 0.85f),
                        end = Offset(w * 0.4f, h * 1.15f),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = Color(0xFF00E5FF).copy(alpha = 0.4f),
                        start = Offset(w * 0.6f, h * 0.85f),
                        end = Offset(w * 0.6f, h * 1.15f),
                        strokeWidth = 4f
                    )

                    // Draw glowing retro-style spaceship triangle
                    val path = Path().apply {
                        moveTo(w * 0.5f, h * 0.1f)
                        lineTo(w * 0.85f, h * 0.75f)
                        lineTo(w * 0.65f, h * 0.68f)
                        lineTo(w * 0.5f, h * 0.8f)
                        lineTo(w * 0.35f, h * 0.68f)
                        lineTo(w * 0.15f, h * 0.75f)
                        close()
                    }
                    drawPath(path, color = Color(0xFF00E5FF).copy(alpha = 0.15f))
                    drawPath(path, color = Color(0xFF00E5FF), style = Stroke(width = 5f))
                    
                    // Core reactor glow
                    drawCircle(
                        color = Color(0xFFFF007F),
                        radius = 12f,
                        center = Offset(w * 0.5f, h * 0.6f)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 6f,
                        center = Offset(w * 0.5f, h * 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Glowing Heading
            Text(
                text = "COSMIC",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 10.sp,
                    shadow = Shadow(
                        color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                        offset = Offset(0f, 0f),
                        blurRadius = 35f
                    )
                )
            )

            Text(
                text = "DODGE",
                style = MaterialTheme.typography.displayMedium.copy(
                    color = Color(0xFFFF007F),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 14.sp,
                    shadow = Shadow(
                        color = Color(0xFFFF007F).copy(alpha = 0.8f),
                        offset = Offset(0f, 0f),
                        blurRadius = 40f
                    )
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "RETRO SYNTHWAVE EXPEDITION",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Pulse text loading indicator
            Text(
                text = "INITIALIZING SYSTEM...",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFEA00).copy(alpha = blinkAlpha),
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "TAP ANYWHERE TO SKIP",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.3f),
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}
