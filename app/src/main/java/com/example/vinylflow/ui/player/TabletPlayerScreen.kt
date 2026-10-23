package com.example.vinylflow.ui.player

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vinylflow.domain.model.*

/**
 * Tablet-optimised player screen for Xiaomi Pad 7 (11-inch, landscape-first).
 * Layout: left panel = vinyl record | right panel = info + controls + sources.
 */
@Composable
fun TabletPlayerScreen(
    theme: VinylTheme = PRESET_VINTAGE_CREAM,
    onOpenSources: () -> Unit = {},
    onOpenCustomize: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val vm: PlayerViewModel = viewModel(factory = PlayerViewModel.Factory(context))
    val state      by vm.mediaState.collectAsStateWithLifecycle()
    val positionMs by vm.position.collectAsStateWithLifecycle()
    val sessions   by vm.availableSessions.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE98850),
                        Color(0xFFB8523B),
                        Color(0xFF351D20)
                    ),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end   = androidx.compose.ui.geometry.Offset(2000f, 1200f)
                )
            )
    ) {
        when {
            !state.permissionGranted -> TabletPermissionScreen(
                onOpenSettings = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
            )
            else -> TabletMainLayout(
                state           = state,
                positionMs      = positionMs,
                sessions        = sessions,
                theme           = theme,
                onPlay          = vm::play,
                onPause         = vm::pause,
                onNext          = vm::next,
                onPrev          = vm::previous,
                onSeek          = vm::seekTo,
                onSelectSource  = vm::selectSource,
                onOpenSources   = onOpenSources,
                onOpenCustomize = onOpenCustomize,
                onOpenSettings  = onOpenSettings
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Main Two-Panel Layout
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TabletMainLayout(
    state: MediaState,
    positionMs: Long,
    sessions: List<SessionSource>,
    theme: VinylTheme,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeek: (Long) -> Unit,
    onSelectSource: (String) -> Unit,
    onOpenSources: () -> Unit,
    onOpenCustomize: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val track  = state.track
    val accent = Color(theme.accentColor)

    Row(modifier = Modifier.fillMaxSize()) {

        // ── Left Panel: Vinyl Record ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(0.48f)
                .fillMaxHeight()
                .padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (theme.showToneArm) {
                ToneArm(isPlaying = state.isPlaying, armColor = Color(theme.toneArmColor))
            }
            VinylRecord(
                isPlaying       = state.isPlaying,
                artwork         = if (theme.showArtwork) track?.artworkBitmap else null,
                recordColor     = Color(theme.recordColor),
                grooveColor     = Color(theme.grooveColor),
                labelColor      = Color(theme.labelColor),
                accentColor     = accent,
                showGrooves     = theme.showGrooves,
                grooveIntensity = theme.grooveIntensity,
                modifier        = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }

        // Divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .padding(vertical = 40.dp)
                .background(Color.White.copy(alpha = 0.1f))
        )

        // ── Right Panel: Info + Controls ──────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(0.52f)
                .fillMaxHeight()
                .padding(horizontal = 40.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Source + nav icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Source pill
                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable(onClick = onOpenSources)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(if (state.source != null) accent else Color.Gray, CircleShape)
                    )
                    Text(
                        state.source?.appName ?: "No Source",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Icon(Icons.Rounded.UnfoldMore, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                }

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onOpenCustomize) {
                        Icon(Icons.Rounded.Palette, "Customize", tint = Color.White.copy(alpha = 0.55f))
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Rounded.Settings, "Settings", tint = Color.White.copy(alpha = 0.55f))
                    }
                }
            }

            // Middle: Track info
            if (track != null && theme.showTrackInfo) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (track.displayAlbum.isNotBlank()) {
                        Text(
                            track.displayAlbum.uppercase(),
                            style     = MaterialTheme.typography.labelLarge,
                            color     = accent.copy(alpha = 0.8f),
                            maxLines  = 1,
                            overflow  = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        track.displayTitle,
                        style      = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis,
                        lineHeight = 40.sp
                    )
                    Text(
                        track.displayArtist,
                        style  = MaterialTheme.typography.titleMedium,
                        color  = Color.White.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else if (track == null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Nothing Playing",
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        "Start a media app to begin",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.3f)
                    )
                }
            }

            // Bottom: Seek + Controls
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Seek bar
                if (track != null && MediaAction.SEEK in state.supportedActions && track.durationMs > 0L) {
                    Column {
                        Slider(
                            value = if (track.durationMs > 0) (positionMs.toFloat() / track.durationMs).coerceIn(0f, 1f) else 0f,
                            onValueChange = { onSeek((it * track.durationMs).toLong()) },
                            colors = SliderDefaults.colors(
                                thumbColor         = accent,
                                activeTrackColor   = accent,
                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(formatMs(positionMs), color = Color.White.copy(alpha = 0.45f), style = MaterialTheme.typography.labelSmall)
                            Text(formatMs(track.durationMs), color = Color.White.copy(alpha = 0.45f), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Controls row — bigger for tablet
                if (theme.showControls) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick  = onPrev,
                            enabled  = MediaAction.PREVIOUS in state.supportedActions,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Rounded.SkipPrevious, "Previous",
                                tint     = if (MediaAction.PREVIOUS in state.supportedActions) Color.White else Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        // Big play/pause
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(accent, CircleShape)
                                .clickable { if (state.isPlaying) onPause() else onPlay() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (state.isPlaying) "Pause" else "Play",
                                tint     = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        IconButton(
                            onClick  = onNext,
                            enabled  = MediaAction.NEXT in state.supportedActions,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Rounded.SkipNext, "Next",
                                tint     = if (MediaAction.NEXT in state.supportedActions) Color.White else Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                // Active sessions sidebar
                if (sessions.isNotEmpty()) {
                    Divider(color = Color.White.copy(alpha = 0.08f))
                    Text("Active Sessions", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.4f))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        sessions.take(4).forEach { src ->
                            val isActive = src.packageName == state.source?.packageName
                            Row(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.large)
                                    .background(
                                        if (isActive) accent.copy(alpha = 0.2f)
                                        else Color.White.copy(alpha = 0.06f)
                                    )
                                    .clickable { onSelectSource(src.packageName) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isActive) {
                                    Box(Modifier.size(6.dp).background(accent, CircleShape))
                                }
                                Text(
                                    src.appName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isActive) accent else Color.White.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tablet Permission Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TabletPermissionScreen(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: decorative vinyl
        Box(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight()
                .background(Color.White.copy(alpha = 0.03f)),
            contentAlignment = Alignment.Center
        ) {
            VinylRecord(
                isPlaying       = false,
                artwork         = null,
                recordColor     = Color(0xFF171313),
                grooveColor     = Color(0xFF29201D),
                labelColor      = Color(0xFF563324),
                accentColor     = Color(0xFFD97847),
                showGrooves     = true,
                grooveIntensity = 0.4f,
                modifier        = Modifier.size(300.dp).alpha(0.5f)
            )
        }

        // Right: permission text
        Column(
            modifier = Modifier
                .weight(0.55f)
                .padding(56.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                Icons.Rounded.MusicNote, null,
                tint     = Color(0xFFD97847),
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "Notification Access\nRequired",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                lineHeight = 44.sp
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "VinylFlow needs Notification Listener access to detect\n" +
                "active media sessions from music and podcast apps.\n\n" +
                "Your notification content is never read.",
                style     = MaterialTheme.typography.bodyLarge,
                color     = Color.White.copy(alpha = 0.6f),
                lineHeight = 26.sp
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onOpenSettings,
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97847)),
                modifier = Modifier.height(52.dp)
            ) {
                Text("Open Notification Settings", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Settings → Apps → Special access → Notification access → VinylFlow ✓",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.35f)
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}
