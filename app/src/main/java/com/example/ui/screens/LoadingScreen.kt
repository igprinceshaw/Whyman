package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameState
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    // Progress state
    var progress by remember { mutableStateOf(0f) }
    // Terminal logs state
    val terminalLogs = remember { mutableStateListOf<String>() }
    
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()

    // Smooth progress bar animation
    val progressAnimate by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing),
        label = "progress"
    )

    // Log list strings
    val bootLogs = remember {
        listOf(
            "📡 CONNECTING TO NEBULA BEACON...",
            "⚙️ TESTING ANTI-MATTER INJECTORS...",
            "🛡️ CHARGING DEFLECTOR CORES (100%)...",
            "🚀 WARMING UP PHOTON THRUSTERS...",
            "🔋 SYNCING COGNITIVE PILOT PROTOCOLS...",
            "✅ ALL SYSTEMS NOMINAL. MISSION ACTIVE!"
        )
    }

    // Effect to increment progress and append terminal logs
    LaunchedEffect(Unit) {
        terminalLogs.add("[BOOT] INITIALIZING NEURAL SPACE DRIVERS...")
        delay(100)
        
        // Progress step loop
        while (progress < 1.0f) {
            val randomStep = (5..15).random() / 100f
            progress = (progress + randomStep).coerceAtLeast(0f).coerceAtMost(1f)
            
            // Add logs corresponding to progress milestones
            val logIndex = ((progress * bootLogs.size).toInt() - 1).coerceIn(0, bootLogs.size - 1)
            if (terminalLogs.size <= logIndex + 1) {
                terminalLogs.add(bootLogs[logIndex])
                // Short retro sfx tick on log append
                try {
                    viewModel.gameEngine.playSimulatedSFX(android.media.ToneGenerator.TONE_PROP_ACK, 40)
                } catch (e: Exception) {}
            }
            delay((150..300).random().toLong())
        }
        
        delay(500) // Hold briefly at 100%
        
        // Transition based on onboarding status
        if (onboardingCompleted) {
            viewModel.changeGameState(GameState.MENU)
        } else {
            viewModel.changeGameState(GameState.ONBOARDING)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF03001C),
                        Color(0xFF0E002B),
                        Color(0xFF03001C)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Futuristic Header Text
            Text(
                text = "HYPER-DRIVE INTERLINK",
                fontSize = 13.sp,
                color = Color(0xFF00E5FF),
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Sci-fi Terminal logs Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = "--- RETRO-SECTOR-CONSOLE ---",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Display newest logs at bottom
                    terminalLogs.takeLast(6).forEach { log ->
                        Text(
                            text = log,
                            fontSize = 11.sp,
                            color = if (log.startsWith("✅")) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.8f),
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 2.dp),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Percentage Status Header
            Text(
                text = "LOADING PROTOCOLS... ${(progressAnimate * 100).toInt()}%",
                fontSize = 12.sp,
                color = Color(0xFFFF007F),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Custom glowing premium Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color(0xFFFF007F).copy(alpha = 0.3f), RoundedCornerShape(7.dp))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressAnimate)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF007F),
                                    Color(0xFF9D00FF),
                                    Color(0xFF00E5FF)
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "DO NOT POWER OFF MISSION SYSTEM",
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.3f),
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
