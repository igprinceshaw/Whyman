package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.GameState
import com.example.ui.screens.GameOverScreen
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LevelCompleteScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShipSelectScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.LoadingScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize our local repository and VM with constructor injection
                val viewModel: GameViewModel = viewModel(
                    factory = GameViewModel.Factory(applicationContext)
                )

                val gameState by viewModel.gameEngine.gameState.collectAsState()
                val stars = viewModel.gameEngine.stars

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(
                            targetState = gameState,
                            animationSpec = tween(400),
                            label = "screen_transition"
                        ) { state ->
                            when (state) {
                                GameState.SPLASH -> {
                                    SplashScreen(
                                        viewModel = viewModel
                                    )
                                }
                                GameState.LOADING -> {
                                    LoadingScreen(
                                        viewModel = viewModel
                                    )
                                }
                                GameState.ONBOARDING -> {
                                    OnboardingScreen(
                                        viewModel = viewModel
                                    )
                                }
                                GameState.MENU -> {
                                    MenuScreen(
                                        viewModel = viewModel,
                                        stars = stars
                                    )
                                }
                                GameState.SETTINGS -> {
                                    SettingsScreen(
                                        viewModel = viewModel
                                    )
                                }
                                GameState.SHIP_SELECT -> {
                                    ShipSelectScreen(
                                        viewModel = viewModel
                                    )
                                }
                                GameState.LEADERBOARD -> {
                                    LeaderboardScreen(
                                        viewModel = viewModel
                                    )
                                }
                                GameState.PLAYING, GameState.PAUSED -> {
                                    GamePlayScreen(
                                        viewModel = viewModel
                                    )
                                }
                                GameState.LEVEL_COMPLETE -> {
                                    GamePlayScreen(
                                        viewModel = viewModel
                                    )
                                    LevelCompleteScreen(
                                        viewModel = viewModel
                                    )
                                }
                                GameState.GAME_OVER -> {
                                    // Draw gameplay canvas in the background of the gameover overlay
                                    GamePlayScreen(
                                        viewModel = viewModel
                                    )
                                    GameOverScreen(
                                        viewModel = viewModel
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
