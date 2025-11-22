package com.example.productivitycontrol.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// --- LIQUID DARK PALETTE ---
val VoidBlack = Color(0xFF000000)
val DarkGlass = Color(0xFF1C1C1E).copy(alpha = 0.6f)
val DarkBorder = Color(0xFFFFFFFF).copy(alpha = 0.15f)
val WhiteText = Color(0xFFFFFFFF)

// --- LIQUID LIGHT PALETTE ---
val SilverMist = Color(0xFFF2F2F7) // iOS Light Background
val LightGlass = Color(0xFFFFFFFF).copy(alpha = 0.75f) // Frosted White
val LightBorder = Color(0xFF000000).copy(alpha = 0.05f) // Subtle grey border
val BlackText = Color(0xFF000000)

private val DarkColorScheme = darkColorScheme(
    primary = WhiteText,         // Text/Icons are White
    onPrimary = VoidBlack,
    background = VoidBlack,      // Background is Black
    surface = DarkGlass,         // Cards are Dark Glass
    onSurface = WhiteText,
    outline = DarkBorder         // Borders are Faint White
)

private val LightColorScheme = lightColorScheme(
    primary = BlackText,         // Text/Icons are Black
    onPrimary = SilverMist,
    background = SilverMist,     // Background is Silver
    surface = LightGlass,        // Cards are White Glass
    onSurface = BlackText,
    outline = LightBorder        // Borders are Faint Grey
)

@Composable
fun ProductivityTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    // This toggle switches between the two palettes
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = Shapes(
            small = RoundedCornerShape(16.dp),
            medium = RoundedCornerShape(24.dp),
            large = RoundedCornerShape(32.dp)
        ),
        content = content
    )
}