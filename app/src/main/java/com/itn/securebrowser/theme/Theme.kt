package com.itn.securebrowser.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Chrome Android baseline palette, taken from Chromium's Material 3 color resources.
 * Dynamic Material You colors are intentionally not used so the look stays consistent
 * with the reference Chrome UI instead of following the wallpaper.
 */
private val LightColors = lightColorScheme(
    primary = Color(0xFF1A73E8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD3E3FD),
    onPrimaryContainer = Color(0xFF041E49),
    secondary = Color(0xFF5F6368),
    surface = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF0F4F9),
    surfaceContainerHigh = Color(0xFFE9EEF6),
    surfaceTint = Color(0xFF1A73E8),
    onSurface = Color(0xFF1F1F1F),
    onSurfaceVariant = Color(0xFF444746),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1F1F1F),
    outline = Color(0xFFC4C7C5)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8C7FA),
    onPrimary = Color(0xFF062E6F),
    primaryContainer = Color(0xFF004A77),
    onPrimaryContainer = Color(0xFFD3E3FD),
    secondary = Color(0xFFC4C7C5),
    surface = Color(0xFF131314),
    surfaceContainer = Color(0xFF1E1F20),
    surfaceContainerHigh = Color(0xFF282A2C),
    surfaceTint = Color(0xFFA8C7FA),
    onSurface = Color(0xFFE3E3E3),
    onSurfaceVariant = Color(0xFFC4C7C5),
    background = Color(0xFF131314),
    onBackground = Color(0xFFE3E3E3),
    outline = Color(0xFF444746)
)

@Composable
fun ITNTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content
    )
}
