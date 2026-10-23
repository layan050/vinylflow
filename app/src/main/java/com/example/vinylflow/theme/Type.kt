package com.example.vinylflow.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val VinylTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold,    fontSize = 32.sp, letterSpacing = (-0.5).sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, letterSpacing = (-0.2).sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.Bold,    fontSize = 20.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 16.sp, letterSpacing = 0.15.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 14.sp, letterSpacing = 0.1.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 12.sp, letterSpacing = 0.1.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 13.sp, letterSpacing = 0.8.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 10.sp, letterSpacing = 0.4.sp),
)
