package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GameColorScheme = darkColorScheme(
    primary = RunnerPrimary,
    onPrimary = RunnerOnPrimary,
    secondary = RunnerSecondary,
    onSecondary = Color.White,
    tertiary = RunnerTertiary,
    onTertiary = Color.Black,
    background = RunnerBackground,
    onBackground = RunnerOnSurface,
    surface = RunnerSurface,
    onSurface = RunnerOnSurface,
    surfaceVariant = LightNavyCard,
    onSurfaceVariant = Color(0xFFCCC8E5),
    error = LaserCrimson,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = GameColorScheme,
        typography = Typography,
        content = content
    )
}
