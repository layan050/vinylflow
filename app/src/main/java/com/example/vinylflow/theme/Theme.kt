package com.example.vinylflow.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VinylDarkColorScheme = darkColorScheme(
    primary         = Color(0xFFD97847),
    onPrimary       = Color.White,
    secondary       = Color(0xFFFFD7AA),
    background      = Color(0xFF241416),
    surface         = Color(0xFF38201F),
    onBackground    = Color.White,
    onSurface       = Color.White,
)

private val VinylLightColorScheme = lightColorScheme(
    primary = Color(0xFF5D57C9), onPrimary = Color.White,
    secondary = Color(0xFF6A6598), background = Color(0xFFF0F1EF),
    surface = Color.White, onBackground = Color(0xFF24252B), onSurface = Color(0xFF24252B)
)

@Composable
fun VinylFlowTheme(darkMode: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkMode) VinylDarkColorScheme else VinylLightColorScheme,
        typography  = VinylTypography,
        content     = content
    )
}
