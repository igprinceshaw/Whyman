package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.Difficulty
import com.example.game.GameState
import com.example.game.WarpStar
import com.example.ui.viewmodel.GameViewModel

@Composable
fun MenuScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
    stars: List<WarpStar> = emptyList()
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF03001e),
                        Color(0xFF0F002D),
                        Color(0xFF1F0042)
                    )
                )
            )
    ) {
        // High-performance background Starfield rendering
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (star in stars) {
                drawCircle(
                    color = Color.White.copy(alpha = star.alpha),
                    radius = star.size,
                    center = Offset(star.x, star.y)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header / Title Block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text(
                    text = "COSMIC",
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 8.sp,
                        shadow = Shadow(
                            color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                            offset = Offset(0f, 0f),
                            blurRadius = 30f
                        )
                    )
                )
                Text(
                    text = "DODGE",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = Color(0xFFFF007F),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 12.sp,
                        shadow = Shadow(
                            color = Color(0xFFFF007F).copy(alpha = 0.8f),
                            offset = Offset(0f, 0f),
                            blurRadius = 35f
                        )
                    )
                )
                Text(
                    text = "RETRO RUNNER",
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color.White.copy(alpha = 0.6f),
                        letterSpacing = 4.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                val scores by viewModel.leaderboardScores.collectAsState()
                val bestScore = scores.firstOrNull()
                if (bestScore != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFEA00).copy(alpha = 0.12f))
                            .border(1.dp, Color(0xFFFFEA00).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🏆 BEST SCORE: ${String.format("%06d", bestScore.score)} BY ${bestScore.pilotName.uppercase()}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFFFFEA00),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }

            // Daily Reward Banner/Card
            val lastClaim by viewModel.lastDailyClaim.collectAsState()
            val streak by viewModel.dailyStreak.collectAsState()
            val now = System.currentTimeMillis()
            val rewardReady = lastClaim == 0L || (now - lastClaim) >= 86400000L
            val rewardAmount = when (streak) {
                0 -> 100
                1 -> 150
                2 -> 200
                3 -> 250
                4 -> 300
                5 -> 400
                else -> 600
            }

            if (rewardReady) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(vertical = 4.dp)
                        .clickable {
                            viewModel.claimDailyReward(rewardAmount)
                        }
                        .border(
                            width = 1.5.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFFEA00), Color(0xFFFF5722))
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEA00).copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "🎁 DAILY REWARD READY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFEA00),
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Day ${streak + 1} Reward: +$rewardAmount Credits",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFEA00))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "CLAIM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.04f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "✓ DAILY REWARD CLAIMED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00FFCC),
                                fontFamily = FontFamily.Monospace
                            )
                            val hrLeft = ((86400000L - (now - lastClaim)) / 3600000L).coerceAtLeast(0L)
                            val minLeft = (((86400000L - (now - lastClaim)) % 3600000L) / 60000L).coerceAtLeast(0L)
                            Text(
                                text = "Next in ${hrLeft}h ${minLeft}m | Current Streak: ${streak} days",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                        Text("⭐", fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Central Settings & Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SELECT DIFFICULTY",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Custom Neon Row for Difficulty selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A0033))
                        .border(1.dp, Color(0xFFFF007F).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Difficulty.values().forEach { diff ->
                        val isSelected = viewModel.gameEngine.currentDifficulty == diff
                        val itemBg = if (isSelected) Color(0xFFFF007F) else Color.Transparent
                        val itemTextColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(itemBg)
                                .clickable {
                                    viewModel.setDifficulty(diff)
                                }
                                .padding(vertical = 12.dp)
                                .testTag("diff_${diff.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = diff.displayName.uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = itemTextColor,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Multiplier indicator of chosen difficulty
                val mult = viewModel.gameEngine.currentDifficulty.scoreMultiplier
                Text(
                    text = "Difficulty Multiplier: x$mult",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFFFEA00),
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            // Action Buttons Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // START MISSION Button
                Button(
                    onClick = { viewModel.startGame() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("play_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play icon",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "START MISSION",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 3.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                // Row for Ship Select and Leaderboard
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // SHIP SELECT
                    OutlinedButton(
                        onClick = { viewModel.changeGameState(GameState.SHIP_SELECT) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("select_ship_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            width = 1.dp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = "Ship selection",
                            tint = Color(0xFFFF007F)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HANGAR",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    // LEADERBOARD
                    OutlinedButton(
                        onClick = { viewModel.changeGameState(GameState.LEADERBOARD) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("leaderboard_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            width = 1.dp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = "Leaderboard Icon",
                            tint = Color(0xFFFFEA00)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RANKS",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }

                // SETTINGS BUTTON
                OutlinedButton(
                    onClick = { viewModel.changeGameState(GameState.SETTINGS) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("settings_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        width = 1.dp
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings Icon",
                        tint = Color(0xFF00E5FF)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SETTINGS",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}
