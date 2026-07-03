package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameState
import com.example.ui.viewmodel.GameViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val scores by viewModel.leaderboardScores.collectAsState()
    val activeFilter by viewModel.selectedDifficultyFilter.collectAsState()
    val filters = listOf("All", "Recruit", "Veteran", "Elite")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "LEADERBOARD",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp,
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
                            contentDescription = "Back to main menu",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (scores.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearLeaderboard() },
                            modifier = Modifier.testTag("clear_leaderboard")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Wipe scores",
                                tint = Color(0xFFFF007F)
                            )
                        }
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
                .padding(16.dp)
        ) {
            // Horizontal difficulty filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filterName ->
                    val isSelected = activeFilter == filterName
                    val bg = if (isSelected) Color(0xFFFFEA00) else Color(0xFF140129)
                    val tc = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f)
                    val borderCol = if (isSelected) Color(0xFFFFEA00) else Color(0xFFFF007F).copy(alpha = 0.3f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(bg)
                            .border(1.dp, borderCol, RoundedCornerShape(20.dp))
                            .clickable { viewModel.selectDifficultyFilter(filterName) }
                            .padding(vertical = 10.dp)
                            .testTag("filter_$filterName"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filterName.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = tc,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }

            if (scores.isEmpty()) {
                // Immersive and gorgeous Empty State (following design guidelines)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "No trophies",
                        tint = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ARCHIVES EMPTY",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No mission records found for this difficulty tier.\nTake off, dodge obstacles, and secure your place in cosmic history!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    )
                }
            } else {
                // High score rankings list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("leaderboard_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(scores) { index, record ->
                        val rank = index + 1
                        val rankColor = when (rank) {
                            1 -> Color(0xFFFFD600) // Gold
                            2 -> Color(0xFFC0C0C0) // Silver
                            3 -> Color(0xFFCD7F32) // Bronze
                            else -> Color.White.copy(alpha = 0.4f)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF130030),
                                            Color(0xFF070014)
                                        )
                                    )
                                )
                                .border(
                                    1.dp,
                                    if (rank <= 3) rankColor.copy(alpha = 0.6f) else Color(0xFFFF007F).copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rank Circle Badge
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(rankColor.copy(alpha = 0.2f))
                                    .border(1.dp, rankColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$rank",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (rank <= 3) rankColor else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Name and date column
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = record.pilotName.uppercase(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                )
                                val dateStr = DateFormat.format("yyyy-MM-dd HH:mm", Date(record.timestamp)).toString()
                                Text(
                                    text = "DATE: $dateStr",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }

                            // Score and Difficulty Column
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = String.format("%06d", record.score),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Badge(
                                    containerColor = when (record.difficulty) {
                                        "Recruit" -> Color(0xFF00E676).copy(alpha = 0.1f)
                                        "Veteran" -> Color(0xFF00E5FF).copy(alpha = 0.1f)
                                        else -> Color(0xFFFF007F).copy(alpha = 0.1f)
                                    },
                                    contentColor = when (record.difficulty) {
                                        "Recruit" -> Color(0xFF00E676)
                                        "Veteran" -> Color(0xFF00E5FF)
                                        else -> Color(0xFFFF007F)
                                    }
                                ) {
                                    Text(
                                        text = record.difficulty.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
