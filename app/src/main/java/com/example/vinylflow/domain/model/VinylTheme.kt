package com.example.vinylflow.domain.model

// ── Theme Preset ──────────────────────────────────────────────────────────────

enum class ThemePreset {
    VINTAGE_CREAM, BURGUNDY_RECORD, MIDNIGHT_BLACK, MINIMAL_MONO, SOFT_PASTEL, CUSTOM
}

enum class LabelShape { CIRCLE, SQUARE, ROUNDED }

// ── Vinyl Theme ───────────────────────────────────────────────────────────────

data class VinylTheme(
    val preset: ThemePreset = ThemePreset.MIDNIGHT_BLACK,
    val recordColor: Long = 0xFF0D0D12L,
    val grooveColor: Long = 0xFF1A1A22L,
    val labelColor: Long = 0xFF1C1C2EL,
    val labelShape: LabelShape = LabelShape.CIRCLE,
    val showGrooves: Boolean = true,
    val showToneArm: Boolean = true,
    val toneArmColor: Long = 0xFFB0B8C8L,
    val backgroundColor: Long = 0xFF060610L,
    val accentColor: Long = 0xFF7C6AF7L,
    val showArtwork: Boolean = true,
    val showTrackInfo: Boolean = true,
    val showControls: Boolean = true,
    val grooveIntensity: Float = 0.55f,
    val shadows: Boolean = true
)

// ── Presets ────────────────────────────────────────────────────────────────────

val PRESET_MIDNIGHT_BLACK = VinylTheme(
    preset = ThemePreset.MIDNIGHT_BLACK,
    recordColor = 0xFF0D0D12L,
    grooveColor = 0xFF1A1A22L,
    labelColor = 0xFF1C1C2EL,
    toneArmColor = 0xFFB0B8C8L,
    backgroundColor = 0xFF060610L,
    accentColor = 0xFF7C6AF7L,
    grooveIntensity = 0.55f
)

val PRESET_BURGUNDY = VinylTheme(
    preset = ThemePreset.BURGUNDY_RECORD,
    recordColor = 0xFF5C1320L,
    grooveColor = 0xFF6E1D2BL,
    labelColor = 0xFFEFE0D0L,
    toneArmColor = 0xFFD4A853L,
    backgroundColor = 0xFF180508L,
    accentColor = 0xFFD4A853L,
    grooveIntensity = 0.65f
)

val PRESET_VINTAGE_CREAM = VinylTheme(
    preset = ThemePreset.VINTAGE_CREAM,
    recordColor = 0xFF2A1810L,
    grooveColor = 0xFF3C2618L,
    labelColor = 0xFFF5E6C8L,
    toneArmColor = 0xFFD4A853L,
    backgroundColor = 0xFF190E08L,
    accentColor = 0xFFF5C842L,
    grooveIntensity = 0.7f
)

val PRESET_MINIMAL_MONO = VinylTheme(
    preset = ThemePreset.MINIMAL_MONO,
    recordColor = 0xFF2A2A2AL,
    grooveColor = 0xFF363636L,
    labelColor = 0xFFF0F0F0L,
    toneArmColor = 0xFFD0D0D0L,
    backgroundColor = 0xFF181818L,
    accentColor = 0xFFFFFFFFL,
    showGrooves = false,
    showToneArm = false,
    grooveIntensity = 0f,
    shadows = false
)

val PRESET_SOFT_PASTEL = VinylTheme(
    preset = ThemePreset.SOFT_PASTEL,
    recordColor = 0xFF5A3F70L,
    grooveColor = 0xFF6B4E82L,
    labelColor = 0xFFE8D5F5L,
    toneArmColor = 0xFFBEAAD4L,
    backgroundColor = 0xFF261535L,
    accentColor = 0xFFCBA3E8L,
    grooveIntensity = 0.5f
)

fun ThemePreset.defaultTheme(): VinylTheme = when (this) {
    ThemePreset.MIDNIGHT_BLACK  -> PRESET_MIDNIGHT_BLACK
    ThemePreset.BURGUNDY_RECORD -> PRESET_BURGUNDY
    ThemePreset.VINTAGE_CREAM   -> PRESET_VINTAGE_CREAM
    ThemePreset.MINIMAL_MONO    -> PRESET_MINIMAL_MONO
    ThemePreset.SOFT_PASTEL     -> PRESET_SOFT_PASTEL
    ThemePreset.CUSTOM          -> PRESET_MIDNIGHT_BLACK.copy(preset = ThemePreset.CUSTOM)
}
