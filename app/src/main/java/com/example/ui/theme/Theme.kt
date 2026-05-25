package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PrepNexusColorScheme = darkColorScheme(
    primary = CyberPrimaryPurple,
    secondary = NeonElectricBlue,
    tertiary = CyberCyan,
    background = CosmicBackground,
    surface = DeepGlassCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PrepNexusColorScheme,
        typography = Typography,
        content = content
    )
}
