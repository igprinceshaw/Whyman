package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameState
import com.example.ui.viewmodel.GameViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameOverScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val score = viewModel.gameEngine.score.collectAsState().value
    var pilotName by remember { mutableStateOf("") }

    // Multi-tier performance rank calculation
    val grade = remember(score) {
        when {
            score >= 2000 -> "S-RANK"
            score >= 1000 -> "A-RANK"
            score >= 500 -> "B-RANK"
            score >= 200 -> "C-RANK"
            else -> "RECRUIT"
        }
    }
    val gradeColor = remember(grade) {
        when (grade) {
            "S-RANK" -> Color(0xFFFFEA00) // Gold
            "A-RANK" -> Color(0xFF00E5FF) // Cyan
            "B-RANK" -> Color(0xFFFF007F) // Pink
            "C-RANK" -> Color(0xFFD500F9) // Purple
            else -> Color.White.copy(alpha = 0.5f)
        }
    }

    // List of sci-fi retro pilot callsigns
    val randomCallsigns = listOf(
        "Viper", "Phoenix", "StarLord", "Ace", "Nova", "CyberStar", "Echo",
        "Apex", "Aegis", "Cosmo", "Shadow", "Rogue", "Ghost", "Zenith", "Neon"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xE603001E)), // Dark overlay
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0D0024))
                .border(2.dp, Color(0xFFFF007F).copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "MISSION FAILED",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color(0xFFFF1744),
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            )

            Text(
                text = "YOUR SHIP DETONATED IN THE ASTEROID FIELD",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Grade Display
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(gradeColor.copy(alpha = 0.15f))
                    .border(1.dp, gradeColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = grade,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = gradeColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Score details
            Text(
                text = "FINAL SCORE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
            )

            Text(
                text = String.format("%06d", score),
                style = MaterialTheme.typography.displayMedium.copy(
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Custom Callsign Input
            Text(
                text = "RECORD YOUR CALLSIGN",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Start
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Outlined Text Field
                TextField(
                    value = pilotName,
                    onValueChange = { if (it.length <= 15) pilotName = it },
                    placeholder = {
                        Text(
                            "Enter Callsign",
                            style = TextStyle(color = Color.White.copy(alpha = 0.3f), fontFamily = FontFamily.Monospace)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pilot_name_input"),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E0C3F),
                        unfocusedContainerColor = Color(0xFF12002E),
                        focusedIndicatorColor = Color(0xFFFF007F),
                        unfocusedIndicatorColor = Color(0xFFFF007F).copy(alpha = 0.3f),
                        cursorColor = Color(0xFFFF007F)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Randomize Button
                IconButton(
                    onClick = {
                        val base = randomCallsigns[Random.nextInt(randomCallsigns.size)]
                        val num = Random.nextInt(10, 99)
                        pilotName = "$base-$num"
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E0C3F))
                        .border(1.dp, Color(0xFFFF007F).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .testTag("random_pilot_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Random callsign",
                        tint = Color(0xFFFFD600)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Submit Score Button
                Button(
                    onClick = {
                        viewModel.gameEngine.submitHighScore(pilotName)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_score_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFEA00)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Save score",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "UPLOAD RECORD",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                // Restart Mission Button
                Button(
                    onClick = { viewModel.startGame() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("restart_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REDEPLOY FLIGHT",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                // Return to Command Menu
                OutlinedButton(
                    onClick = { viewModel.changeGameState(GameState.MENU) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("menu_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        width = 1.dp
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "ABANDON TO MAIN MENU",
                        style = MaterialTheme.typography.labelMedium.copy(
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
