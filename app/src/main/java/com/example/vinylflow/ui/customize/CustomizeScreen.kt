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
    onBack: () -> Unit = {}
) {
    var theme by remember(currentTheme) { mutableStateOf(currentTheme) }
    val accent = Color(theme.accentColor)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF060610), Color(0xFF0D0D1A))))
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
                if (isSelected) Modifier.border(2.dp, Color(0xFF7C6AF7), MaterialTheme.shapes.medium)
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
                    if (isSelected) Modifier.border(2.dp, Color(0xFF7C6AF7), CircleShape)
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
            color = if (isSelected) Color(0xFF7C6AF7) else Color.White.copy(alpha = 0.65f),
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
                            Modifier.border(3.dp, Color(0xFF7C6AF7), CircleShape)
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
            colors          = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF7C6AF7))
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
