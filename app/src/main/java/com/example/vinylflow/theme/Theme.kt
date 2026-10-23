package com.example.vinylflow.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VinylColorScheme = darkColorScheme(
    primary         = Color(0xFF7C6AF7),
    onPrimary       = Color.White,
    secondary       = Color(0xFFBBB0FF),
    background      = Color(0xFF060610),
    surface         = Color(0xFF0D0D1A),
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
