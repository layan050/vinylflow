package com.example.vinylflow.ui.player

import android.content.Intent
import android.graphics.Bitmap
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vinylflow.domain.model.*
import kotlin.math.*

// ─────────────────────────────────────────────────────────────────────────────
// Player Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PlayerScreen(
    theme: VinylTheme = PRESET_MIDNIGHT_BLACK,
    onOpenSources: () -> Unit = {},
    onOpenCustomize: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val context   = LocalContext.current
    val vm: PlayerViewModel = viewModel(factory = PlayerViewModel.Factory(context))
    val state     by vm.mediaState.collectAsStateWithLifecycle()
    val positionMs by vm.position.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(theme.backgroundColor),
                        Color(theme.backgroundColor).copy(alpha = 0.85f),
                        Color(theme.recordColor).copy(alpha = 0.25f)
                    )
                )
            )
    ) {
        when {
            !state.permissionGranted -> PermissionScreen(onOpenSettings = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            })

            !state.isActive || state.track == null -> EmptyPlayerContent(
                theme = theme,
                onOpenSources  = onOpenSources,
                onOpenSettings = onOpenSettings
            )

            else -> ActivePlayerContent(
                state      = state,
                positionMs = positionMs,
                theme      = theme,
                onPlay     = vm::play,
                onPause    = vm::pause,
                onNext     = vm::next,
                onPrev     = vm::previous,
                onSeek     = vm::seekTo,
                onOpenSources   = onOpenSources,
                onOpenCustomize = onOpenCustomize,
                onOpenSettings  = onOpenSettings
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Active Player Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ActivePlayerContent(
    state: MediaState,
    positionMs: Long,
    theme: VinylTheme,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeek: (Long) -> Unit,
    onOpenSources: () -> Unit,
    onOpenCustomize: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val track = state.track ?: return
    val accent = Color(theme.accentColor)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        // Source chip
        SourceChip(source = state.source, accentColor = accent, onClick = onOpenSources)

        Spacer(Modifier.height(24.dp))

        // Vinyl record with optional tonearm
        Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
            if (theme.showToneArm) {
                ToneArm(isPlaying = state.isPlaying, armColor = Color(theme.toneArmColor))
            }
            VinylRecord(
                isPlaying     = state.isPlaying,
                artwork       = if (theme.showArtwork) track.artworkBitmap else null,
                recordColor   = Color(theme.recordColor),
                grooveColor   = Color(theme.grooveColor),
                labelColor    = Color(theme.labelColor),
                accentColor   = accent,
                showGrooves   = theme.showGrooves,
                grooveIntensity = theme.grooveIntensity,
                modifier      = Modifier.size(280.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        // Track info
        if (theme.showTrackInfo) {
            TrackInfoSection(track = track, accentColor = accent)
            Spacer(Modifier.height(20.dp))
        }

        // Seek bar
        if (MediaAction.SEEK in state.supportedActions && track.durationMs > 0L) {
            SeekBar(
                positionMs  = positionMs,
                durationMs  = track.durationMs,
                accentColor = accent,
                onSeek      = onSeek
            )
            Spacer(Modifier.height(12.dp))
        }

        // Controls
        if (theme.showControls) {
            PlayerControlsRow(
                state       = state,
                accentColor = accent,
                onPlay      = onPlay,
                onPause     = onPause,
                onNext      = onNext,
                onPrev      = onPrev
            )
        }

        Spacer(Modifier.height(24.dp))

        // Bottom actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onOpenSources) {
                Icon(Icons.Rounded.MusicNote, "Sources", tint = Color.White.copy(alpha = 0.6f))
            }
            IconButton(onClick = onOpenCustomize) {
                Icon(Icons.Rounded.Palette, "Customize", tint = Color.White.copy(alpha = 0.6f))
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Rounded.Settings, "Settings", tint = Color.White.copy(alpha = 0.6f))
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Vinyl Record Component
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun VinylRecord(
    isPlaying: Boolean,
    artwork: Bitmap?,
    recordColor: Color,
    grooveColor: Color,
    labelColor: Color,
    accentColor: Color,
    showGrooves: Boolean,
    grooveIntensity: Float,
    modifier: Modifier = Modifier
) {
    // Rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val rotation by infiniteTransition.animateFloat(
        initialValue   = 0f,
        targetValue    = 360f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val targetRotation = if (isPlaying) rotation else 0f
    val animatedRotation by animateFloatAsState(
        targetValue   = targetRotation,
        animationSpec = tween(durationMillis = 500, easing = EaseInOutSine),
        label         = "smoothRotation"
    )

    Canvas(
        modifier = modifier
            .rotate(animatedRotation)
            .shadow(elevation = 24.dp, shape = CircleShape, clip = false)
    ) {
        val r = size.minDimension / 2f
        val cx = size.center

        // Outer disc
        drawCircle(color = recordColor, radius = r, center = cx)

        // Groove rings
        if (showGrooves && grooveIntensity > 0f) {
            val numGrooves = 22
            val grooveStart = r * 0.38f
            val grooveEnd   = r * 0.94f
            for (i in 0..numGrooves) {
                val t = i.toFloat() / numGrooves
                val gr = grooveStart + t * (grooveEnd - grooveStart)
                drawCircle(
                    color  = grooveColor.copy(alpha = grooveIntensity * (0.3f + t * 0.3f)),
                    radius = gr,
                    center = cx,
                    style  = Stroke(width = 1.2f)
                )
            }
        }

        // Subtle highlight arc (light reflection)
        drawArc(
            brush       = Brush.radialGradient(
                colors  = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent),
                center  = Offset(cx.x - r * 0.3f, cx.y - r * 0.35f),
                radius  = r * 0.7f
            ),
            startAngle  = 200f,
            sweepAngle  = 130f,
            useCenter   = false,
            style       = Stroke(width = r * 0.6f)
        )

        // Center label
        val labelR = r * 0.30f
        drawCircle(color = labelColor, radius = labelR, center = cx)

        // Artwork inside label
        if (artwork != null) {
            drawCircle(
                brush  = ShaderBrush(
                    android.graphics.BitmapShader(
                        Bitmap.createScaledBitmap(
                            artwork,
                            (labelR * 2).toInt().coerceAtLeast(1),
                            (labelR * 2).toInt().coerceAtLeast(1),
                            true
                        ),
                        android.graphics.Shader.TileMode.CLAMP,
                        android.graphics.Shader.TileMode.CLAMP
                    ).apply {
                        setLocalMatrix(android.graphics.Matrix().apply {
                            setTranslate(cx.x - labelR, cx.y - labelR)
                        })
                    }
                ),
                radius = labelR,
                center = cx
            )
        }

        // Spindle hole
        drawCircle(
            color  = recordColor.copy(alpha = 0.9f),
            radius = r * 0.025f,
            center = cx
        )
        // Spindle ring
        drawCircle(
            color  = accentColor.copy(alpha = 0.4f),
            radius = r * 0.032f,
            center = cx,
            style  = Stroke(width = 1.5f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tonearm
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ToneArm(isPlaying: Boolean, armColor: Color) {
    val angle by animateFloatAsState(
        targetValue   = if (isPlaying) 28f else 12f,
        animationSpec = tween(durationMillis = 800, easing = EaseInOutCubic),
        label         = "tonearm"
    )

    Canvas(
        modifier = Modifier
            .size(200.dp)
            .offset(x = 55.dp, y = (-55).dp),
    ) {
        val pivotX = size.width * 0.88f
        val pivotY = size.height * 0.05f
        val pivotR = 14f

        val rad  = Math.toRadians(angle.toDouble())
        val armL = size.height * 0.85f
        val tipX = pivotX + armL * sin(rad).toFloat()
        val tipY = pivotY + armL * cos(rad).toFloat()

        // Arm shadow
        drawLine(
            color       = Color.Black.copy(alpha = 0.4f),
            start       = Offset(pivotX + 2f, pivotY + 2f),
            end         = Offset(tipX + 2f, tipY + 2f),
            strokeWidth = 5f,
            cap         = StrokeCap.Round
        )
        // Arm
        drawLine(
            color       = armColor,
            start       = Offset(pivotX, pivotY),
            end         = Offset(tipX, tipY),
            strokeWidth = 4f,
            cap         = StrokeCap.Round
        )
        // Pivot circle
        drawCircle(color = armColor, radius = pivotR, center = Offset(pivotX, pivotY))
        drawCircle(
            color  = Color.Black.copy(alpha = 0.4f),
            radius = pivotR * 0.45f,
            center = Offset(pivotX, pivotY)
        )
        // Needle
        drawCircle(color = armColor.copy(alpha = 0.9f), radius = 5f, center = Offset(tipX, tipY))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Track Info
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TrackInfoSection(track: TrackInfo, accentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = track.displayTitle,
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color      = Color.White,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text    = track.displayArtist,
            style   = MaterialTheme.typography.bodyLarge,
            color   = Color.White.copy(alpha = 0.72f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (track.displayAlbum.isNotBlank()) {
            Text(
                text    = track.displayAlbum,
                style   = MaterialTheme.typography.bodySmall,
                color   = accentColor.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Seek Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SeekBar(
    positionMs: Long,
    durationMs: Long,
    accentColor: Color,
    onSeek: (Long) -> Unit
) {
    val fraction = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    Column {
        Slider(
            value         = fraction,
            onValueChange = { onSeek((it * durationMs).toLong()) },
            colors        = SliderDefaults.colors(
                thumbColor          = accentColor,
                activeTrackColor    = accentColor,
                inactiveTrackColor  = Color.White.copy(alpha = 0.2f)
            )
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(positionMs), color = Color.White.copy(alpha = 0.55f), style = MaterialTheme.typography.labelSmall)
            Text(formatTime(durationMs), color = Color.White.copy(alpha = 0.55f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun formatTime(ms: Long): String {
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}

// ─────────────────────────────────────────────────────────────────────────────
// Player Controls
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlayerControlsRow(
    state: MediaState,
    accentColor: Color,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous
        IconButton(
            onClick = onPrev,
            enabled = MediaAction.PREVIOUS in state.supportedActions
        ) {
            Icon(
                Icons.Rounded.SkipPrevious,
                contentDescription = "Previous",
                tint    = if (MediaAction.PREVIOUS in state.supportedActions)
                    Color.White else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(36.dp)
            )
        }

        // Play / Pause big button
        Box(
            modifier = Modifier
                .size(68.dp)
                .background(accentColor, CircleShape)
                .clickable { if (state.isPlaying) onPause() else onPlay() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (state.isPlaying) "Pause" else "Play",
                tint     = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        // Next
        IconButton(
            onClick = onNext,
            enabled = MediaAction.NEXT in state.supportedActions
        ) {
            Icon(
                Icons.Rounded.SkipNext,
                contentDescription = "Next",
                tint    = if (MediaAction.NEXT in state.supportedActions)
                    Color.White else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Source Chip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SourceChip(source: SessionSource?, accentColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    if (source != null) accentColor else Color.Gray,
                    CircleShape
                )
        )
        Text(
            text  = source?.appName ?: "No Source",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
        Icon(
            Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
            tint   = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty State
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyPlayerContent(theme: VinylTheme, onOpenSources: () -> Unit, onOpenSettings: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Static vinyl
        VinylRecord(
            isPlaying       = false,
            artwork         = null,
            recordColor     = Color(theme.recordColor),
            grooveColor     = Color(theme.grooveColor),
            labelColor      = Color(theme.labelColor),
            accentColor     = Color(theme.accentColor),
            showGrooves     = theme.showGrooves,
            grooveIntensity = theme.grooveIntensity * 0.5f,
            modifier        = Modifier.size(220.dp).alpha(0.6f)
        )
        Spacer(Modifier.height(32.dp))
        Text(
            "Nothing Playing",
            style  = MaterialTheme.typography.headlineSmall,
            color  = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Start playback in a media app,\nthen open VinylFlow.",
            style  = MaterialTheme.typography.bodyMedium,
            color  = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = onOpenSources,
            border  = BorderStroke(1.dp, Color(theme.accentColor).copy(alpha = 0.6f))
        ) {
            Text("Choose Source", color = Color(theme.accentColor))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Permission Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PermissionScreen(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.MusicNote,
            contentDescription = null,
            tint     = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Notification Access Required",
            style  = MaterialTheme.typography.headlineSmall,
            color  = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "VinylFlow needs Notification Listener access to detect music from other apps. " +
            "Your notification content is never read — only the media session is used.",
            style  = MaterialTheme.typography.bodyMedium,
            color  = Color.White.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onOpenSettings,
            colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C6AF7))
        ) {
            Text("Open Notification Settings")
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Settings → Apps → Special access → Notification access → VinylFlow ✓",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}
