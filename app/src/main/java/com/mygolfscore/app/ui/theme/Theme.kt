@file:Suppress("unused")

package com.mygolfscore.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BrandPrimary = Color(0xFF14A665)
val BrandDarkCard = Color(0xFF1B4332)
val MintTint = Color(0xFFE8F6EF)
val MintText = Color(0xFF0D6E42)

val GolfDarkEmerald = Color(0xFF1B3A2D)
val GolfGreenDark = Color(0xFF0D6E42)
val GolfGreenMid = Color(0xFF14A665)
val GolfAppIconGreen = Color(0xFF14A665)
val GolfGreenLight = Color(0xFFE8F6EF)
val GolfAccent = Color(0xFF14A665)
val GoldAccent = Color(0xFFF59E0B)
val GolfSurface = Color(0xFFF7FAF8)

val NeutralBannerBg = Color(0xFFF2F5F3)
val TableHeaderBg = Color(0xFFF3F4F6)
val TextSecondary = Color(0xFF4B5563)
val TextTertiary = Color(0xFF7A8680)
val HairlineDivider = Color(0xFFE5E7EB)

val DestructiveRed = Color(0xFFDC2626)
val DestructiveBorder = Color(0xFFFCA5A5)

// Original Scorecard Image Viewer Tokens (v1.0.1)
val ViewerBackdrop = Color(0xFF0B0F17)
val ViewerOverlayBg = Color(0xCC111827)
val ViewerControlBarBg = Color(0xE61F2937)
val ViewerZoomBadgeBg = Color(0xFF10B981)
val ViewerPillBorder = Color(0xFF059669)
val ViewerPillBg = Color(0xFFECFDF5)

// 5-tier Score Semantics
val BirdieBlue = Color(0xFF2563EB)
val BirdieBg = Color(0xFFEFF6FF)
val ParGreen = Color(0xFF14A665)
val ParBg = Color(0xFFE6F6EE)
val BogeyAmber = Color(0xFFD97706)
val BogeyBg = Color(0xFFFEF3C7)
val DoubleBogeyOrange = Color(0xFFEA580C)
val DoubleBogeyBg = Color(0xFFFFEDD5)
val TripleRed = Color(0xFFDC2626)
val TripleBg = Color(0xFFFEE2E2)

private val GolfColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = MintTint,
    onPrimaryContainer = MintText,
    secondary = BrandDarkCard,
    onSecondary = Color.White,
    background = GolfSurface,
    surface = Color.White,
    surfaceVariant = TableHeaderBg,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MyGolfScoreTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GolfColorScheme,
        content = content
    )
}
