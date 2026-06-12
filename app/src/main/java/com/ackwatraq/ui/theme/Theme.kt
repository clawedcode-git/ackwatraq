package com.ackwatraq.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Curator-Harmonized Color Schemes
private val PremiumLightColorScheme = lightColorScheme(
    primary = Color(0xFF0F62FE),          // Deep Tech Blue
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F0FE), // Ice Blue
    onPrimaryContainer = Color(0xFF0F62FE),
    secondary = Color(0xFF00B4DB),        // Hydration Cyan
    onSecondary = Color.White,
    tertiary = Color(0xFF8A3FFC),         // Neon Amethyst Purple
    background = Color(0xFFF4F7FC),       // Clean Grey-Blue
    onBackground = Color(0xFF161B22),
    surface = Color.White,
    onSurface = Color(0xFF161B22),
    surfaceVariant = Color(0xFFE8F0FE),
    onSurfaceVariant = Color(0xFF475569)  // Slate Grey
)

private val CyberDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00E5FF),          // Cyber Neon Cyan
    onPrimary = Color(0xFF0B0E14),
    primaryContainer = Color(0xFF083344), // Deep Cyan Container
    onPrimaryContainer = Color(0xFF00E5FF),
    secondary = Color(0xFFFF2D55),        // Cyber Coral Pink
    onSecondary = Color.White,
    tertiary = Color(0xFF8A3FFC),         // Violet Glow
    background = Color(0xFF0B0E14),       // Space Black
    onBackground = Color.White,
    surface = Color(0xFF161B22),          // Dark Slate Grey Card
    onSurface = Color.White,
    surfaceVariant = Color(0xFF21262D),
    onSurfaceVariant = Color(0xFF8B949E)  // Light Grey
)

// Premium Geometric Sans-Serif Layout Configs
val PremiumTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.15.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)

@Composable
fun AckwatraqTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CyberDarkColorScheme else PremiumLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PremiumTypography,
        content = content
    )
}
