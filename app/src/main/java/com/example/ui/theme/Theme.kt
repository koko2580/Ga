package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NexoraColorScheme = darkColorScheme(
    primary = NexoraPurpleAccent,
    onPrimary = Color.White,
    primaryContainer = NexoraPurpleGlow,
    onPrimaryContainer = NexoraPurpleLight,
    secondary = NexoraBlueHighlight,
    onSecondary = Color.Black,
    secondaryContainer = NexoraSurfaceElevated,
    onSecondaryContainer = NexoraBlueHighlight,
    background = NexoraBackground,
    onBackground = NexoraTextPrimary,
    surface = NexoraSurface,
    onSurface = NexoraTextPrimary,
    surfaceVariant = NexoraSurfaceElevated,
    onSurfaceVariant = NexoraTextSecondary,
    outline = NexoraSurfaceBorder,
    outlineVariant = NexoraSurfaceBorder,
    error = NexoraRedAlert,
    onError = Color.White
)

@Composable
fun NexoraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NexoraColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    NexoraTheme(content = content)
}
