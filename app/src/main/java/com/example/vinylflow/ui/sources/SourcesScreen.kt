package com.example.vinylflow.ui.sources

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vinylflow.domain.model.SessionSource
import com.example.vinylflow.ui.player.PlayerViewModel

@Composable
fun SourcesScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val vm: PlayerViewModel = viewModel(factory = PlayerViewModel.Factory(context))
    val mediaState by vm.mediaState.collectAsStateWithLifecycle()
    val sessions   by vm.availableSessions.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF351D20), Color(0xFF241416)))
            )
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
                    "Media Sources",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { vm.refresh() }) {
                    Icon(Icons.Rounded.Refresh, "Refresh", tint = Color.White.copy(alpha = 0.7f))
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Permission card
                item {
                    PermissionCard(isGranted = mediaState.permissionGranted)
                }

                // Current source
                if (mediaState.source != null) {
                    item {
                        SectionHeader("Now Playing From")
                        SourceRow(
                            source = mediaState.source!!,
                            isActive = true,
                            isSelected = true,
                            onSelect = {}
                        )
                    }
                }

                // All active sessions
                if (sessions.isNotEmpty()) {
                    item { SectionHeader("Active Sessions") }
                    items(sessions) { source ->
                        SourceRow(
                            source     = source,
                            isActive   = source.packageName == mediaState.source?.packageName,
                            isSelected = source.packageName == mediaState.source?.packageName,
                            onSelect   = { vm.selectSource(source.packageName) }
                        )
                    }
                } else {
                    item {
                        EmptySessionsCard()
                    }
                }

                // Compatibility note
                item {
                    Spacer(Modifier.height(8.dp))
                    CompatibilityNote()
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(isGranted: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted)
                Color(0xFF1A3A1A) else Color(0xFF3A1A1A)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                if (isGranted) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF4CAF50) else Color(0xFFFF9800),
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isGranted) "Notification Access: Enabled" else "Notification Access: Required",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    if (isGranted)
                        "VinylFlow can detect media sessions"
                    else
                        "Go to Settings → Apps → Special access → Notification access",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
private fun SourceRow(
    source: SessionSource,
    isActive: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                Color(0xFFD97847).copy(alpha = 0.18f)
            else
                Color.White.copy(alpha = 0.06f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // App icon placeholder
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFD97847).copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.MusicNote, null, tint = Color(0xFFD97847), modifier = Modifier.size(22.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    source.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    source.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }

            if (isActive) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                    )
                    Text("Active", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                }
            }

            if (isSelected) {
                Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFFD97847), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = Color.White.copy(alpha = 0.5f),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun EmptySessionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Rounded.MusicOff, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                "No active media sessions",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Text(
                "Start playing music in any media app",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.35f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CompatibilityNote() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171313))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.Info, null, tint = Color(0xFFD97847), modifier = Modifier.size(18.dp))
                Text("Compatibility", style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "VinylFlow works with any app that exposes an Android MediaSession:\n" +
                "• YouTube Music ✓\n• Spotify ✓\n• Pocket Casts ✓\n• VLC ✓\n• Audible ✓\n\n" +
                "Note: Apps must have active playback and expose MediaSession controls.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )
        }
    }
}
