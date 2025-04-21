package com.example.imageloader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Orange500,
    onPrimary = White,
    primaryContainer = Orange100,
    onPrimaryContainer = Orange500,
    secondary = Orange200,
    onSecondary = Orange500,
    background = LightBackground,
    surface = LightBackground,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = ErrorRed
)

@Composable
fun ImageLoaderTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
