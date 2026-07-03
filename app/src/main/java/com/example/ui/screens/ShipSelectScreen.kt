package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameState
import com.example.game.ShipType
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipSelectScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val selectedShip = viewModel.gameEngine.selectedShip
    val totalCoins by viewModel.coins.collectAsState()
    val unlockedShips by viewModel.unlockedShips.collectAsState()
    val upgradeLevels by viewModel.upgradeLevels.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Fleet Hangar, 1: Armory Upgrades

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (activeTab == 0) "THE HANGAR" else "THE ARMORY",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.changeGameState(GameState.MENU) },
                        modifier = Modifier.testTag("back_to_menu")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to menu",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "🪙 $totalCoins",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFFFFD600),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF03001e),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF03001e),
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Unified M3 TabRow
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color(0xFF0D0024),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = Color(0xFF00FFCC)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .padding(bottom = 12.dp)
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Text(
                            "FLEET",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    selectedContentColor = Color(0xFF00FFCC),
                    unselectedContentColor = Color.Gray
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Text(
                            "UPGRADES",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    selectedContentColor = Color(0xFF00FFCC),
                    unselectedContentColor = Color.Gray
                )
            }

            if (activeTab == 0) {
                // TAB 0: FLEET HANGAR (Ships selection & skin unlocks)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Live Preview Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.1f)
                            .border(1.dp, selectedShip.color.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF0D0024)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // High-performance vector rendering inside top half of preview
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(130.dp)) {
                                    val w = size.width
                                    val h = size.height
                                    val cx = w / 2
                                    val cy = h / 2

                                    when (selectedShip) {
                                        ShipType.SPEEDSTER -> {
                                            val shipPath = Path().apply {
                                                moveTo(cx, cy - h * 0.4f)
                                                lineTo(cx + w * 0.2f, cy + h * 0.2f)
                                                lineTo(cx + w * 0.08f, cy + h * 0.12f)
                                                lineTo(cx - w * 0.08f, cy + h * 0.12f)
                                                lineTo(cx - w * 0.2f, cy + h * 0.2f)
                                                close()
                                            }
                                            drawCircle(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(Color(0xFF00E5FF), Color.Transparent),
                                                    center = Offset(cx, cy + h * 0.25f),
                                                    radius = 35f
                                                ),
                                                radius = 35f,
                                                center = Offset(cx, cy + h * 0.25f)
                                            )
                                            drawPath(shipPath, Color(0xFF00E5FF), style = Stroke(width = 5f))
                                            drawPath(shipPath, Color(0xFF00E5FF).copy(alpha = 0.25f))
                                        }
                                        ShipType.SHIELD_DEFENDER -> {
                                            val shipPath = Path().apply {
                                                moveTo(cx, cy - h * 0.35f)
                                                lineTo(cx + w * 0.3f, cy - h * 0.1f)
                                                lineTo(cx + w * 0.2f, cy + h * 0.25f)
                                                lineTo(cx + w * 0.08f, cy + h * 0.15f)
                                                lineTo(cx - w * 0.08f, cy + h * 0.15f)
                                                lineTo(cx - w * 0.2f, cy + h * 0.25f)
                                                lineTo(cx - w * 0.3f, cy - h * 0.1f)
                                                close()
                                            }
                                            drawCircle(
                                                color = Color(0xFFFF007F).copy(alpha = 0.12f),
                                                radius = w * 0.45f,
                                                center = Offset(cx, cy)
                                            )
                                            drawCircle(
                                                color = Color(0xFFFF007F).copy(alpha = 0.4f),
                                                radius = w * 0.45f,
                                                center = Offset(cx, cy),
                                                style = Stroke(width = 2.5f)
                                            )
                                            drawPath(shipPath, Color(0xFFFF007F), style = Stroke(width = 5f))
                                            drawPath(shipPath, Color(0xFFFF007F).copy(alpha = 0.25f))
                                        }
                                        ShipType.VANGUARD -> {
                                            val shipPath = Path().apply {
                                                moveTo(cx, cy - h * 0.42f)
                                                lineTo(cx + w * 0.15f, cy - h * 0.1f)
                                                lineTo(cx + w * 0.35f, cy + h * 0.25f)
                                                lineTo(cx + w * 0.1f, cy + h * 0.1f)
                                                lineTo(cx, cy + h * 0.25f)
                                                lineTo(cx - w * 0.1f, cy + h * 0.1f)
                                                lineTo(cx - w * 0.35f, cy + h * 0.25f)
                                                lineTo(cx - w * 0.15f, cy - h * 0.1f)
                                                close()
                                            }
                                            drawCircle(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(Color(0xFFFFEA00), Color.Transparent),
                                                    center = Offset(cx, cy + h * 0.32f),
                                                    radius = 45f
                                                ),
                                                radius = 45f,
                                                center = Offset(cx, cy + h * 0.32f)
                                            )
                                            drawPath(shipPath, Color(0xFFFFEA00), style = Stroke(width = 5f))
                                            drawPath(shipPath, Color(0xFFFFEA00).copy(alpha = 0.25f))
                                        }
                                        ShipType.VOID_STALKER -> {
                                            val shipPath = Path().apply {
                                                moveTo(cx, cy - h * 0.44f)
                                                lineTo(cx + w * 0.15f, cy)
                                                lineTo(cx + w * 0.28f, cy + h * 0.3f)
                                                lineTo(cx + w * 0.12f, cy + h * 0.15f)
                                                lineTo(cx, cy + h * 0.22f)
                                                lineTo(cx - w * 0.12f, cy + h * 0.15f)
                                                lineTo(cx - w * 0.28f, cy + h * 0.3f)
                                                lineTo(cx - w * 0.15f, cy)
                                                close()
                                            }
                                            drawCircle(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(Color(0xFFBB86FC), Color.Transparent),
                                                    center = Offset(cx, cy + h * 0.28f),
                                                    radius = 35f
                                                ),
                                                radius = 35f,
                                                center = Offset(cx, cy + h * 0.28f)
                                            )
                                            drawPath(shipPath, Color(0xFFBB86FC), style = Stroke(width = 5f))
                                            drawPath(shipPath, Color(0xFFBB86FC).copy(alpha = 0.25f))
                                        }
                                        ShipType.CHRONO_KEEPER -> {
                                            val shipPath = Path().apply {
                                                moveTo(cx, cy - h * 0.4f)
                                                lineTo(cx + w * 0.22f, cy + h * 0.15f)
                                                lineTo(cx + w * 0.08f, cy + h * 0.1f)
                                                lineTo(cx - w * 0.08f, cy + h * 0.1f)
                                                lineTo(cx - w * 0.22f, cy + h * 0.15f)
                                                close()
                                            }
                                            drawCircle(
                                                color = Color(0xFF00FF88).copy(alpha = 0.08f),
                                                radius = w * 0.36f,
                                                center = Offset(cx, cy)
                                            )
                                            drawCircle(
                                                color = Color(0xFF00FF88).copy(alpha = 0.35f),
                                                radius = w * 0.36f,
                                                center = Offset(cx, cy),
                                                style = Stroke(width = 2f)
                                            )
                                            drawPath(shipPath, Color(0xFF00FF88), style = Stroke(width = 5f))
                                            drawPath(shipPath, Color(0xFF00FF88).copy(alpha = 0.25f))
                                        }
                                        ShipType.NEBULA_REAVER -> {
                                            val shipPath = Path().apply {
                                                moveTo(cx, cy - h * 0.42f)
                                                lineTo(cx + w * 0.1f, cy - h * 0.1f)
                                                lineTo(cx + w * 0.33f, cy - h * 0.15f)
                                                lineTo(cx + w * 0.21f, cy + h * 0.24f)
                                                lineTo(cx, cy + h * 0.15f)
                                                lineTo(cx - w * 0.21f, cy + h * 0.24f)
                                                lineTo(cx - w * 0.33f, cy - h * 0.15f)
                                                lineTo(cx - w * 0.1f, cy - h * 0.1f)
                                                close()
                                            }
                                            drawCircle(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(Color(0xFFFF3D00), Color.Transparent),
                                                    center = Offset(cx, cy + h * 0.2f),
                                                    radius = 45f
                                                ),
                                                radius = 45f,
                                                center = Offset(cx, cy + h * 0.2f)
                                            )
                                            drawPath(shipPath, Color(0xFFFF3D00), style = Stroke(width = 5f))
                                            drawPath(shipPath, Color(0xFFFF3D00).copy(alpha = 0.25f))
                                        }
                                    }
                                }
                            }

                            // Ship stats details
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = selectedShip.displayName.uppercase(),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = selectedShip.color,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 2.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = selectedShip.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                // Dynamic metrics
                                StatBar(label = "SPEED", ratio = selectedShip.speedFactor / 1.25f, color = Color(0xFF00E5FF))
                                Spacer(modifier = Modifier.height(4.dp))
                                StatBar(label = "ARMOR", ratio = selectedShip.initialHealth / 200f, color = Color(0xFFFF007F))

                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = selectedShip.color.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Text(
                                        text = "PERK: ${selectedShip.perk}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = selectedShip.color,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 1.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Selector List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.9f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ShipType.values()) { ship ->
                            val isCurrent = selectedShip == ship
                            val isUnlocked = ship.coinCost == 0 || unlockedShips.contains(ship.name)
                            val borderAlpha = if (isCurrent) 1.0f else 0.22f
                            val borderStroke = if (isCurrent) 2.dp else 1.dp

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isCurrent) Color(0xFF190D38) else Color(0xFF0C001C))
                                    .border(
                                        borderStroke,
                                        ship.color.copy(alpha = borderAlpha),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        if (isUnlocked) {
                                            viewModel.setShipType(ship)
                                        } else {
                                            viewModel.unlockShip(ship, ship.coinCost)
                                        }
                                    }
                                    .padding(14.dp)
                                    .testTag("ship_${ship.name.lowercase()}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = ship.displayName.uppercase(),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = if (isUnlocked) ship.color else Color.Gray,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                letterSpacing = 1.sp
                                            )
                                        )
                                        if (ship.coinCost >= 1200) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFF3D00).copy(alpha = 0.15f)),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    "LEGENDARY",
                                                    color = Color(0xFFFF3D00),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontFamily = FontFamily.Monospace,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = if (isUnlocked) {
                                            "Armor: ${ship.initialHealth.toInt()} | ${ship.perk}"
                                        } else {
                                            "LOCKED SCHEMATIC (Click to buy)"
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White.copy(alpha = 0.5f)
                                        )
                                    )
                                }

                                if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = ship.color
                                    )
                                } else if (!isUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFFEA00).copy(alpha = 0.12f))
                                            .border(1.dp, Color(0xFFFFEA00), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "🪙 ${ship.coinCost}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFFFEA00),
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
            } else {
                // TAB 1: ARMORY UPGRADES (Character upgrade system)
                val upgradeList = listOf(
                    UpgradeItem("speed", "ENGINE BOOSTERS", "⚡", "Increases thruster velocity and dodge handling (+10% per level)."),
                    UpgradeItem("magnet", "MAGNETIC CONDENSER", "🧲", "Draws credits automatically from a larger range (+65px radius per level)."),
                    UpgradeItem("shield", "SHIELD OVERCHARGER", "🛡️", "Extends defensive invulnerability window (+1.5s per level)."),
                    UpgradeItem("firerate", "BLASTER COILS", "🔫", "Reduces automated blaster shoot interval (-50ms per level)."),
                    UpgradeItem("damage", "PLASMA CHAMBER", "💥", "Augments laser hit points and unlocks double & triple fire streams."),
                    UpgradeItem("maxhealth", "TITANIUM HULL", "❤️", "Amplifies maximum armor shielding limits (+20 HP per level).")
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(upgradeList) { item ->
                        val currentLevel = upgradeLevels[item.key] ?: 0
                        val cost = if (currentLevel < 5) {
                            if (item.key == "firerate" || item.key == "damage") {
                                150 + currentLevel * 150
                            } else {
                                100 + currentLevel * 100
                            }
                        } else {
                            0
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0C001C))
                                .border(1.dp, Color(0xFF2563EB).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${item.emoji} ${item.name}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }
                                Text(
                                    text = item.desc,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.55f),
                                    lineHeight = 14.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                // Render level segment blocks
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (i in 1..5) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 24.dp, height = 6.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(
                                                    if (i <= currentLevel) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.15f)
                                                )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$currentLevel/5",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentLevel == 5) Color(0xFF00FFCC) else Color.Gray,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            if (currentLevel < 5) {
                                Button(
                                    onClick = {
                                        viewModel.upgradeStat(item.key, cost)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2563EB)
                                    ),
                                    modifier = Modifier.testTag("upgrade_${item.key}")
                                ) {
                                    Text(
                                        text = "🪙 $cost",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF00FFCC).copy(alpha = 0.15f))
                                        .border(1.dp, Color(0xFF00FFCC), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "MAXED",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF00FFCC),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Done / Return Button
            Button(
                onClick = { viewModel.changeGameState(GameState.MENU) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("select_ship_done"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 0) selectedShip.color else Color(0xFF00FFCC)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "CONFIRM & BACK TO COMMAND",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
        }
    }
}

data class UpgradeItem(
    val key: String,
    val name: String,
    val emoji: String,
    val desc: String
)

@Composable
fun StatBar(label: String, ratio: Float, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.5f),
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(60.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { ratio.coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}
