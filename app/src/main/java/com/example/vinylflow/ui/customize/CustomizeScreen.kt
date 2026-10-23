package com.example.vinylflow.ui.customize

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vinylflow.domain.model.*
import com.example.vinylflow.ui.player.VinylRecord

@Composable
fun CustomizeScreen(
    currentTheme: VinylTheme,
    onThemeChange: (VinylTheme) -> Unit,
    widgetAccent: Long = 0xFFB6AFFF,
    widgetSurface: Long = 0xFF24242A,
    widgetShowControls: Boolean = true,
    widgetShowArtist: Boolean = true,
    onWidgetAppearanceChange: (Long, Long, Boolean, Boolean) -> Unit = { _, _, _, _ -> },
    onBack: () -> Unit = {}
) {
    var theme by remember(currentTheme) { mutableStateOf(currentTheme) }
    var wAccent by remember(widgetAccent) { mutableLongStateOf(widgetAccent) }
    var wSurface by remember(widgetSurface) { mutableLongStateOf(widgetSurface) }
    var wControls by remember(widgetShowControls) { mutableStateOf(widgetShowControls) }
    var wArtist by remember(widgetShowArtist) { mutableStateOf(widgetShowArtist) }
    val accent = Color(theme.accentColor)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF351D20), Color(0xFF241416))))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    "Customization",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { onThemeChange(theme) }) {
                    Text("Apply", color = accent, fontWeight = FontWeight.Bold)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SectionHeader("Widget Studio · Live Preview")
                    Row(
                        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large)
                            .background(Color(wSurface)).padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(72.dp).clip(CircleShape).background(Color(0xFF101014)), contentAlignment = Alignment.Center) {
                            VinylRecord(true, null, Color(0xFF101014), Color(0xFF29292E), Color(wAccent), Color(wAccent), true, 0.6f, modifier = Modifier.fillMaxSize().padding(3.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Nothing playing", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                            if (wArtist) Text("Artist name", color = Color.White.copy(alpha = .65f), maxLines = 1)
                            if (wControls) Text("◀       ▶       ▶", color = Color(wAccent), modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                    Text("Choose a widget style when adding it from your home screen. Resize it there; the layout adapts automatically.", color = Color.White.copy(alpha = .65f), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                }
                item {
                    SectionHeader("Widget Accent Color")
                    ColorPicker(listOf(0xFFB6AFFF, 0xFFE7A0B5, 0xFF86C9B2, 0xFFFFC477, 0xFF8CB8F5, 0xFFF1F0EA), wAccent) {
                        wAccent = it; onWidgetAppearanceChange(wAccent, wSurface, wControls, wArtist)
                    }
                }
                item {
                    SectionHeader("Widget Background")
                    ColorPicker(listOf(0xFF24242A, 0xFF111114, 0xFF51434B, 0xFF283A43, 0xFFF0EFEC, 0xFFDDD9D2), wSurface) {
                        wSurface = it; onWidgetAppearanceChange(wAccent, wSurface, wControls, wArtist)
                    }
                    ToggleRow("Show artist name", wArtist) { wArtist = it; onWidgetAppearanceChange(wAccent, wSurface, wControls, wArtist) }
                    ToggleRow("Show playback controls", wControls) { wControls = it; onWidgetAppearanceChange(wAccent, wSurface, wControls, wArtist) }
                }
                // Live preview
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        VinylRecord(
                            isPlaying       = true,
                            artwork         = null,
                            recordColor     = Color(theme.recordColor),
                            grooveColor     = Color(theme.grooveColor),
                            labelColor      = Color(theme.labelColor),
                            accentColor     = Color(theme.accentColor),
                            showGrooves     = theme.showGrooves,
                            grooveIntensity = theme.grooveIntensity,
                            modifier        = Modifier.size(180.dp)
                        )
                    }
                }

                // Preset gallery
                item {
                    SectionHeader("Presets")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(ThemePreset.values().toList()) { preset ->
                            PresetCard(
                                preset    = preset,
                                isSelected = theme.preset == preset,
                                onClick   = {
                                    theme = preset.defaultTheme()
                                }
                            )
                        }
                    }
                }

                // Record Color
                item {
                    SectionHeader("Record Color")
                    ColorPicker(
                        colors = listOf(
                            0xFF0D0D12L, 0xFF5C1320L, 0xFF2A1810L,
                            0xFF2A2A2AL, 0xFF5A3F70L, 0xFF003366L,
                            0xFF1A3A1AL, 0xFF2D1B00L
                        ),
                        selected = theme.recordColor,
                        onSelect = { theme = theme.copy(recordColor = it, preset = ThemePreset.CUSTOM) }
                    )
                }

                // Label Color
                item {
                    SectionHeader("Label Color")
                    ColorPicker(
                        colors = listOf(
                            0xFF1C1C2EL, 0xFFEFE0D0L, 0xFFF5E6C8L,
                            0xFFF0F0F0L, 0xFFE8D5F5L, 0xFFFFE4B5L,
                            0xFFB0E0E6L, 0xFFFFB6C1L
                        ),
                        selected = theme.labelColor,
                        onSelect = { theme = theme.copy(labelColor = it, preset = ThemePreset.CUSTOM) }
                    )
                }

                item {
                    SectionHeader("Player Background")
                    ColorPicker(
                        colors = listOf(0xFFF0F1EFL, 0xFFE8E2D8L, 0xFFE2E8F0L, 0xFFE7DDEBL, 0xFF17171CL, 0xFF201A2BL, 0xFF132128L, 0xFF24191BL),
                        selected = theme.backgroundColor,
                        onSelect = { theme = theme.copy(backgroundColor = it, darkMode = it < 0xFF800000L, preset = ThemePreset.CUSTOM) }
                    )
                    ToggleRow("Dark mode", theme.darkMode) { theme = theme.copy(darkMode = it, preset = ThemePreset.CUSTOM) }
                }

                // Toggles
                item {
                    SectionHeader("Options")
                    ToggleRow("Show Grooves", theme.showGrooves) {
                        theme = theme.copy(showGrooves = it, preset = ThemePreset.CUSTOM)
                    }
                    ToggleRow("Show Tonearm", theme.showToneArm) {
                        theme = theme.copy(showToneArm = it, preset = ThemePreset.CUSTOM)
                    }
                    ToggleRow("Show Album Art", theme.showArtwork) {
                        theme = theme.copy(showArtwork = it, preset = ThemePreset.CUSTOM)
                    }
                    ToggleRow("Show Track Info", theme.showTrackInfo) {
                        theme = theme.copy(showTrackInfo = it, preset = ThemePreset.CUSTOM)
                    }
                    ToggleRow("Show Controls", theme.showControls) {
                        theme = theme.copy(showControls = it, preset = ThemePreset.CUSTOM)
                    }
                    ToggleRow("Shadows", theme.shadows) {
                        theme = theme.copy(shadows = it, preset = ThemePreset.CUSTOM)
                    }
                }

                // Groove intensity
                item {
                    SectionHeader("Groove Intensity")
                    Slider(
                        value         = theme.grooveIntensity,
                        onValueChange = { theme = theme.copy(grooveIntensity = it, preset = ThemePreset.CUSTOM) },
                        colors        = SliderDefaults.colors(
                            thumbColor       = Color(theme.accentColor),
                            activeTrackColor = Color(theme.accentColor)
                        )
                    )
                }

                // Reset button
                item {
                    Button(
                        onClick = { theme = PRESET_MIDNIGHT_BLACK; onThemeChange(PRESET_MIDNIGHT_BLACK) },
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Rounded.RestartAlt, null, tint = Color.White.copy(alpha = 0.7f))
                        Spacer(Modifier.width(8.dp))
                        Text("Reset to Default", color = Color.White.copy(alpha = 0.7f))
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun PresetCard(preset: ThemePreset, isSelected: Boolean, onClick: () -> Unit) {
    val t = preset.defaultTheme()
    Column(
        modifier = Modifier
            .width(80.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, Color(0xFFD97847), MaterialTheme.shapes.medium)
                else Modifier
            )
            .background(Color.White.copy(alpha = 0.06f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(t.recordColor), CircleShape)
                .then(
                    if (isSelected) Modifier.border(2.dp, Color(0xFFD97847), CircleShape)
                    else Modifier
                )
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.Center)
                    .background(Color(t.labelColor), CircleShape)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text  = preset.name.replace("_", "\n"),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Color(0xFFD97847) else Color.White.copy(alpha = 0.65f),
            maxLines = 2,
            lineHeight = MaterialTheme.typography.labelSmall.fontSize * 1.3
        )
    }
}

@Composable
private fun ColorPicker(
    colors: List<Long>,
    selected: Long,
    onSelect: (Long) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(colors) { colorLong ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(colorLong))
                    .clickable { onSelect(colorLong) }
                    .then(
                        if (colorLong == selected)
                            Modifier.border(3.dp, Color(0xFFD97847), CircleShape)
                        else
                            Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    )
            )
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
        Switch(
            checked         = checked,
            onCheckedChange = onToggle,
            colors          = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFD97847))
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = Color.White.copy(alpha = 0.5f),
        modifier = Modifier.padding(bottom = 4.dp)
    )
}
