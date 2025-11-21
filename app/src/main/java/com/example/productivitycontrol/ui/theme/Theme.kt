package com.example.productivitycontrol.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// --- NEW CYBER-NATURE COLORS ---
private val TrueBlack = Color(0xFF050505)       // Almost pure black
private val NeonGreen = Color(0xFF4DFFB4)       // The glowing mint color
private val DarkGreen = Color(0xFF0F291E)       // Darker green for card backgrounds
private val PureWhite = Color(0xFFFFFFFF)

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = TrueBlack,
    secondary = NeonGreen,
    onSecondary = TrueBlack,
    background = TrueBlack,
    onBackground = PureWhite,
    surface = DarkGreen,
    onSurface = PureWhite,
    outline = NeonGreen
)

private val LightColorScheme = lightColorScheme(
    primary = TrueBlack,
    onPrimary = PureWhite,
    secondary = DarkGreen,
    onSecondary = PureWhite,
    background = PureWhite,
    onBackground = TrueBlack,
    surface = PureWhite,
    onSurface = TrueBlack,
    outline = Color.LightGray
)

@Composable
fun ProductivityTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(32.dp)
        ),
        content = content
    )
}