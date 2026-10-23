package com.example.vinylflow.ui.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vinylflow.media.VinylNotificationListenerService

@Composable
fun SettingsScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val isConnected = VinylNotificationListenerService.isConnected

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
                    "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionHeader("Home screen widget")
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Rounded.Widgets,
                        iconTint = Color(0xFFD97847),
                        title = "Add VinylFlow player",
                        subtitle = "Long-press the home screen → Widgets → VinylFlow → drag the player onto your screen",
                        onClick = {}
                    )
                }

                // Permission status
                SectionHeader("Permissions")
                SettingsCard {
                    SettingsRow(
                        icon  = if (isConnected) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                        iconTint = if (isConnected) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        title = "Notification Listener",
                        subtitle = if (isConnected) "Enabled — media sessions accessible" else "Disabled — tap to enable",
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        }
                    )
                }

                // Xiaomi specific
                SectionHeader("Xiaomi / MIUI Settings")
                SettingsCard {
                    SettingsRow(
                        icon     = Icons.Rounded.PhoneAndroid,
                        iconTint = Color(0xFFFF6D00),
                        title    = "AutoStart (MIUI)",
                        subtitle = "Security → AutoStart → Enable VinylFlow",
                        onClick  = {
                            try {
                                val intent = Intent().apply {
                                    setClassName(
                                        "com.miui.securitycenter",
                                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                                    )
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                })
                            }
                        }
                    )
                    Divider(color = Color.White.copy(alpha = 0.08f))
                    SettingsRow(
                        icon     = Icons.Rounded.BatteryFull,
                        iconTint = Color(0xFF4CAF50),
                        title    = "Battery Saver (MIUI)",
                        subtitle = "Battery → App battery saver → No restrictions",
                        onClick  = {
                            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            })
                        }
                    )
                }

                // Troubleshooting
                SectionHeader("Troubleshooting")
                SettingsCard {
                    SettingsRow(
                        icon     = Icons.Rounded.HelpOutline,
                        iconTint = Color(0xFFD97847),
                        title    = "App not detecting music?",
                        subtitle = "1. Enable Notification Listener\n2. Enable AutoStart (Xiaomi)\n3. Play music then reopen app",
                        onClick  = {}
                    )
                    Divider(color = Color.White.copy(alpha = 0.08f))
                    SettingsRow(
                        icon     = Icons.Rounded.Info,
                        iconTint = Color(0xFF2196F3),
                        title    = "Compatible Apps",
                        subtitle = "YouTube Music, Spotify, VLC, Pocket Casts, Audible",
                        onClick  = {}
                    )
                }

                // About
                SectionHeader("About")
                SettingsCard {
                    SettingsRow(
                        icon     = Icons.Rounded.Album,
                        iconTint = Color(0xFFD97847),
                        title    = "VinylFlow",
                        subtitle = "Version 1.1.0 • Android Vinyl Player",
                        onClick  = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style    = MaterialTheme.typography.labelLarge,
        color    = Color.White.copy(alpha = 0.45f),
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
    }
}
