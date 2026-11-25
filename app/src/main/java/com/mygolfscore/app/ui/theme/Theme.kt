package com.mygolfscore.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GolfDarkEmerald = Color(0xFF0A2E23)
val GolfGreenDark = Color(0xFF104E3B)
val GolfGreenMid = Color(0xFF1B6B52)
val GolfAppIconGreen = Color(0xFF14A665)
val GolfGreenLight = Color(0xFFE6F4EE)
val GolfAccent = Color(0xFF10B981)
val GoldAccent = Color(0xFFF59E0B)
val GolfSurface = Color(0xFFF3F7F5)

val BirdiePink = Color(0xFFEC4899)
val BirdieBg = Color(0xFFFCE7F3)
val ParGreen = Color(0xFF10B981)
val ParBg = Color(0xFFD1FAE5)
val BogeyOrange = Color(0xFFF59E0B)
val BogeyBg = Color(0xFFFEF3C7)
val DoubleBogeyRed = Color(0xFFEF4444)
val DoubleBogeyBg = Color(0xFFFEE2E2)
val TripleRed = Color(0xFF991B1B)
val TripleBg = Color(0xFFFEE2E2)

private val GolfColorScheme = lightColorScheme(
    primary = GolfGreenDark,
    onPrimary = Color.White,
    primaryContainer = GolfGreenLight,
    onPrimaryContainer = GolfDarkEmerald,
    secondary = GolfGreenMid,
    onSecondary = Color.White,
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFEAEFF0),
    onSurfaceVariant = Color(0xFF334155)
)

@Composable
fun MyGolfScoreTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GolfColorScheme,
        content = content
    )
}
