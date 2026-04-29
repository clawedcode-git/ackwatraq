package com.ackwatraq.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AnimeColorScheme = lightColorScheme(
    primary = Color(0xFF7EC8E3),
    onPrimary = Color.White,
    secondary = Color(0xFFFFB6C1),
    onSecondary = Color.White,
    tertiary = Color(0xFFDDA0DD),
    background = Color(0xFFF0F8FF),
    surface = Color.White,
    onSurface = Color(0xFF2C3E50)
)

@Composable
fun AckwatraqTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AnimeColorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
