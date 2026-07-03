package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.game.GameState
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val preferences = viewModel.preferences
    val sfxEnabled by preferences.sfxEnabled.collectAsState()
    val musicEnabled by preferences.musicEnabled.collectAsState()
    val screenShakeEnabled by preferences.screenShakeEnabled.collectAsState()
    val vibrationEnabled by preferences.vibrationEnabled.collectAsState()
    val sensitivity by preferences.sensitivity.collectAsState()

    var showResetConfirmation by remember { mutableStateOf(false) }
    var resetSuccessMsg by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "SYSTEM SETTINGS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp,
                            shadow = Shadow(
                                color = Color(0xFF00E5FF).copy(alpha = 0.5f),
                                blurRadius = 10f
                            )
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.changeGameState(GameState.MENU) },
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to menu",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0F002D)
                )
            )
        },
        containerColor = Color(0xFF0F002D),
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F002D),
                            Color(0xFF1F0042)
                        )
                    )
                )
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Screen title description
            Text(
                text = "CONFIGURE YOUR SHIP AND CONTROLLER PILOT SCHEMATICS FOR MAXIMUM EFFICIENCY.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            // Category 1: Audio and SFX
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF150036)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Audio settings icon",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AUDIO SIMULATOR",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Divider(color = Color(0xFF00E5FF).copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "8-Bit Retro Synthesizer SFX",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            Text(
                                text = "Enables real-time retro game sound generator loops",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                        Switch(
                            checked = sfxEnabled,
                            onCheckedChange = {
                                preferences.setSfxEnabled(it)
                                if (it) {
                                    // Trigger test beep
                                    viewModel.gameEngine.playSimulatedSFX(android.media.ToneGenerator.TONE_PROP_BEEP, 80)
                                }
                            },
                            modifier = Modifier.testTag("sfx_toggle"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00E5FF),
                                checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.4f),
                                uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                                uncheckedTrackColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "8-Bit Space Ambient Music",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            Text(
                                text = "Continuous looping 8-bit chip-tune procedural BGM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                        Switch(
                            checked = musicEnabled,
                            onCheckedChange = {
                                preferences.setMusicEnabled(it)
                            },
                            modifier = Modifier.testTag("music_toggle"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00E5FF),
                                checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.4f),
                                uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                                uncheckedTrackColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            // Category 2: Screen Shake and Haptics
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF150036)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFF007F).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Haptics settings icon",
                            tint = Color(0xFFFF007F),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "IMMERSIVE EFFECTS",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Divider(color = Color(0xFFFF007F).copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Screen Shake on Impact",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            Text(
                                text = "Dynamic screen rumble on obstacle collision events",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                        Switch(
                            checked = screenShakeEnabled,
                            onCheckedChange = { preferences.setScreenShakeEnabled(it) },
                            modifier = Modifier.testTag("shake_toggle"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFF007F),
                                checkedTrackColor = Color(0xFFFF007F).copy(alpha = 0.4f),
                                uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                                uncheckedTrackColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Haptic Vibration Feedback",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            Text(
                                text = "Enables physical vibration feedback on collisions & buttons",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = {
                                viewModel.toggleVibration(it)
                            },
                            modifier = Modifier.testTag("vibration_toggle"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFF007F),
                                checkedTrackColor = Color(0xFFFF007F).copy(alpha = 0.4f),
                                uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                                uncheckedTrackColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            // Category 3: Controller Drag Sensitivity
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF150036)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFFEA00).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Sensitivity settings icon",
                            tint = Color(0xFFFFEA00),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "CONTROLLER CALIBRATION",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Divider(color = Color(0xFFFFEA00).copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 12.dp))

                    Text(
                        text = "Touch Sensitivity: ${"%.2f".format(sensitivity)}x",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )

                    Slider(
                        value = sensitivity,
                        onValueChange = { preferences.setSensitivity(it) },
                        valueRange = 0.5f..2.5f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("sensitivity_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFEA00),
                            activeTrackColor = Color(0xFFFFEA00),
                            inactiveTrackColor = Color(0xFFFFEA00).copy(alpha = 0.2f)
                        )
                    )

                    Text(
                        text = "Adjust the multiplier for touch drag movements. Higher sensitivity allows faster evasion response times.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 14.sp
                        )
                    )
                }
            }

            // Category 4: Danger Zone - Clear Scores & Progress
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B0016)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFF1744).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Danger zone icon",
                            tint = Color(0xFFFF1744),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "DANGER ZONE",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color(0xFFFF1744),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Divider(color = Color(0xFFFF1744).copy(alpha = 0.15f), modifier = Modifier.padding(bottom = 12.dp))

                    Text(
                        text = "Wipe Progress Schematics",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    )

                    Text(
                        text = "Deletes all persistent high scores, resets coin balance to 0, and locks all premium ship classes.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 14.sp
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (!showResetConfirmation) {
                        Button(
                            onClick = { showResetConfirmation = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("wipe_progress_button")
                        ) {
                            Text(
                                text = "WIPE PROGRESS",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Are you absolutely sure? This action is IRREVERSIBLE.",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFFFF1744),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showResetConfirmation = false },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Text(
                                        text = "CANCEL",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Button(
                                    onClick = {
                                        preferences.resetProgress()
                                        viewModel.clearLeaderboard()
                                        showResetConfirmation = false
                                        resetSuccessMsg = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                                    modifier = Modifier.weight(1f).height(40.dp).testTag("confirm_wipe_button")
                                ) {
                                    Text(
                                        text = "YES, RESET",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = resetSuccessMsg) {
                        Surface(
                            color = Color(0xFF00E676).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = borderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Success info",
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "All system progress successfully wiped.",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF00E676),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)
