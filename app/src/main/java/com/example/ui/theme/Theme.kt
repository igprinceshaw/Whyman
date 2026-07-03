package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00E5FF),     // Electric Cyan
    secondary = Color(0xFFFF007F),   // Neon Pink/Magenta
    tertiary = Color(0xFFFFEA00),    // Solar Yellow
    background = Color(0xFF03001e),  // Cosmic Space Dark Deep Blue/Black
    surface = Color(0xFF120024),     // Synthwave Purple Deep Card
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = DarkColorScheme // Always dark theme for space game

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for ultimate synthwave neon space arcade aesthetic
    dynamicColor: Boolean = false, // Enforce unified brand identity
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
