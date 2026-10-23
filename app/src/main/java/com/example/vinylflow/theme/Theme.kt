package com.example.vinylflow.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VinylColorScheme = darkColorScheme(
    primary         = Color(0xFFD97847),
    onPrimary       = Color.White,
    secondary       = Color(0xFFFFD7AA),
    background      = Color(0xFF241416),
    surface         = Color(0xFF38201F),
    onBackground    = Color.White,
    onSurface       = Color.White,
)

@Composable
fun VinylFlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VinylColorScheme,
        typography  = VinylTypography,
        content     = content
    )
}
