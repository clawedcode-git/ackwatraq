package com.ackwatraq.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import com.ackwatraq.R

private val AnimeColorScheme = lightColorScheme(
    primary = Color(0xFF7EC8E3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB3E5FC),
    onPrimaryContainer = Color(0xFF01579B),
    secondary = Color(0xFFFFB6C1),
    onSecondary = Color.White,
    tertiary = Color(0xFFDDA0DD),
    background = Color(0xFFF0F8FF),
    onBackground = Color(0xFF2C3E50),
    surface = Color.White,
    onSurface = Color(0xFF2C3E50),
    surfaceVariant = Color(0xFFE3F2FD),
    onSurfaceVariant = Color(0xFF424242)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7EC8E3),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF01579B),
    onPrimaryContainer = Color(0xFFE1F5FE),
    secondary = Color(0xFFFFB6C1),
    onSecondary = Color.Black,
    tertiary = Color(0xFFDDA0DD),
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0BEC5)
)

// Custom Inter typography removed – using default Material3 typography
// If you want custom fonts, ensure they are packaged correctly.


@Composable
fun AckwatraqTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else AnimeColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
