package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun OnboardingScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableStateOf(0) }
    val totalPages = 4

    // Interactive simulator animation loop for Slide 1 (dragging ship)
    val infiniteTransition = rememberInfiniteTransition(label = "onboardAnim")
    val shipOffsetAnim by infiniteTransition.animateFloat(
        initialValue = -80f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shipOffset"
    )

    // Pulse rate for instructions
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF03001C),
                        Color(0xFF100030),
                        Color(0xFF0A0021)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        // TOP LOGO BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ACADEMY COGNITION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF),
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "PILOT INDUCTION PROTOCOL",
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace
                )
            }

            TextButton(
                onClick = {
                    viewModel.setOnboardingCompleted(true)
                    viewModel.changeGameState(GameState.MENU)
                },
                modifier = Modifier.testTag("skip_onboarding_btn")
            ) {
                Text(
                    text = "SKIP",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        // CENTER MAIN CONTENT SLIDES
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.78f)
                .align(Alignment.Center)
                .padding(top = 64.dp),
            contentAlignment = Alignment.Center
        ) {
            when (currentPage) {
                0 -> SlideWelcome(shipOffset = shipOffsetAnim)
                1 -> SlideObstacles()
                2 -> SlidePowerUps(glow = glowAlpha)
                3 -> SlideBossWarning()
            }
        }

        // BOTTOM CONTROLS (STEPS, DOT INDICATORS & BUTTONS)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Neon Dot Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until totalPages) {
                    val isSelected = i == currentPage
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "dotWidth"
                    )
                    val dotColor = if (isSelected) Color(0xFFFF007F) else Color.White.copy(alpha = 0.2f)

                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            // Navigation Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // BACK button
                if (currentPage > 0) {
                    OutlinedButton(
                        onClick = { currentPage-- },
                        modifier = Modifier
                            .height(50.dp)
                            .width(110.dp)
                            .testTag("onboarding_back_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Back")
                        Text(
                            "BACK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(110.dp))
                }

                // NEXT / START Button
                val isLastPage = currentPage == totalPages - 1
                Button(
                    onClick = {
                        if (isLastPage) {
                            viewModel.setOnboardingCompleted(true)
                            viewModel.changeGameState(GameState.MENU)
                        } else {
                            currentPage++
                        }
                    },
                    modifier = Modifier
                        .height(50.dp)
                        .width(150.dp)
                        .testTag("onboarding_next_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLastPage) Color(0xFF00E5FF) else Color(0xFFFF007F)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isLastPage) "LAUNCH GAME" else "NEXT PROTOCOL",
                        fontSize = 11.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    if (!isLastPage) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next", tint = Color.Black)
                    } else {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.RocketLaunch, contentDescription = "Launch", tint = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun SlideWelcome(shipOffset: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "01. MANEUVER PROTOCOL",
            fontSize = 16.sp,
            color = Color(0xFFFFEA00),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Moving simulated spaceships
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw movement lane
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = Offset(40f, h * 0.5f),
                        end = Offset(w - 40f, h * 0.5f),
                        strokeWidth = 2f
                    )

                    // Draw left/right boundary indicators
                    drawCircle(Color(0xFF00E5FF).copy(alpha = 0.2f), radius = 10f, center = Offset(40f, h * 0.5f))
                    drawCircle(Color(0xFF00E5FF).copy(alpha = 0.2f), radius = 10f, center = Offset(w - 40f, h * 0.5f))

                    // Draw interactive spaceship position based on animated shipOffset
                    val cx = w * 0.5f + shipOffset * 2.5f
                    val cy = h * 0.5f

                    // Draw neat vector spaceship
                    val sPath = Path().apply {
                        moveTo(cx, cy - 25f)
                        lineTo(cx + 25f, cy + 20f)
                        lineTo(cx + 12f, cy + 15f)
                        lineTo(cx, cy + 22f)
                        lineTo(cx - 12f, cy + 15f)
                        lineTo(cx - 25f, cy + 20f)
                        close()
                    }
                    drawPath(sPath, color = Color(0xFF00E5FF).copy(alpha = 0.15f))
                    drawPath(sPath, color = Color(0xFF00E5FF), style = Stroke(width = 4f))

                    // Draw simulated thrust flame
                    drawCircle(Color(0xFFFF007F).copy(alpha = 0.7f), radius = 6f, center = Offset(cx, cy + 22f))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SLIDE TO FLY",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your pilot system has automatic firing activated. Drag your finger horizontally across the screen to slide the spaceship left and right to dodge hazards and catch objects.",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.65f),
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun SlideObstacles() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "02. COSMIC OBSTACLES",
            fontSize = 16.sp,
            color = Color(0xFFFFEA00),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Grid of unique asteroid hazards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color(0xFFFF1744).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💥", fontSize = 24.sp)
                    Text("EXPLOSIVE", fontSize = 11.sp, color = Color(0xFFFF1744), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Detonates into particles. Dodge to capture credits.", fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color(0xFFE040FB).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛸", fontSize = 24.sp)
                    Text("HUNTER", fontSize = 11.sp, color = Color(0xFFE040FB), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Homing target tracking. Tracks your flight path.", fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🌀", fontSize = 24.sp)
                    Text("WAVY", fontSize = 11.sp, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Zigzag descend movement. Sidewinds unpredictable paths.", fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "DODGE & DESTROY",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sectors contain dense normal and exotic asteroid flows. Safely graze asteroids close-by for a Close Dodge bonus score multiplier, or blast them directly with automatic projectile blasts.",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.65f),
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun SlidePowerUps(glow: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "03. DRIFTING POWER-UPS",
            fontSize = 16.sp,
            color = Color(0xFFFFEA00),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD500F9).copy(alpha = 0.15f))
                        .border(1.5.dp, Color(0xFFD500F9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛡️", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("SHIELD", fontSize = 9.sp, color = Color.White, fontFamily = FontFamily.Monospace)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676).copy(alpha = 0.15f))
                        .border(1.5.dp, Color(0xFF00E676), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⏱️", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("SLOW-MO", fontSize = 9.sp, color = Color.White, fontFamily = FontFamily.Monospace)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5722).copy(alpha = 0.15f))
                        .border(1.5.dp, Color(0xFFFF5722), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧲", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("MAGNET", fontSize = 9.sp, color = Color.White, fontFamily = FontFamily.Monospace)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD600).copy(alpha = 0.15f))
                        .border(1.5.dp, Color(0xFFFFD600), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⭐", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("MULTIPLY", fontSize = 9.sp, color = Color.White, fontFamily = FontFamily.Monospace)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                        .border(1.5.dp, Color(0xFF00E5FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("HYPER", fontSize = 9.sp, color = Color.White, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "UPGRADE TO MAXIMUM CAPACITY",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Drifting quantum capsules grant massive power upgrades. Gather credits inside the sector, then visit the Hangar to upgrade your primary attributes: Ship Speed, Magnet Range, Deflection Duration, Weapon Fire Rate, Bullet Damage, and Hull Armor.",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.65f),
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun SlideBossWarning() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "04. SECTOR REACTOR BOSSES",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.sp,
                color = Color(0xFFFF1744),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                shadow = Shadow(
                    color = Color(0xFFFF1744).copy(alpha = 0.5f),
                    blurRadius = 10f
                )
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .border(1.5.dp, Color(0xFFFF1744).copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF160007))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "⚠️ SECTOR WARNING: HOSTILE DREADNOUGHT APPROACHING",
                    color = Color(0xFFFF1744),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { 1.0f },
                    color = Color(0xFFFF1744),
                    trackColor = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SURVIVE THE PROJECTILE STORM",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "DEFEAT THE BOSS REVERSE RECOVERY",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Survive normal obstacles to fill up your progress bar. Once full, the Sector Boss reactor will emerge. Evade complex radial plasma patterns while firing back automatically to destroy the boss reactor cores and earn massive pilot reward ranks!",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.65f),
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}
