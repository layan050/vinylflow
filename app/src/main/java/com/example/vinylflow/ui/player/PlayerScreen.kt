package com.example.vinylflow.ui.player

import android.content.Intent
import android.graphics.Bitmap
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vinylflow.domain.model.*
import kotlin.math.*
import kotlinx.coroutines.isActive

// ─────────────────────────────────────────────────────────────────────────────
// Player Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PlayerScreen(
    theme: VinylTheme = PRESET_VINTAGE_CREAM,
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

            else -> VinylPlayerConsole(
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

@Composable
private fun VinylPlayerConsole(
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
    val track = state.track
    val accent = Color(theme.accentColor)
    val ink = if (theme.darkMode) Color(0xFFF3F1F5) else Color(0xFF24252B)
    val muted = if (theme.darkMode) Color(0xFFB6B2BC) else Color(0xFF777981)

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(if (theme.darkMode) listOf(Color(0xFF17171C), Color(0xFF22222A), Color(0xFF151519)) else listOf(Color(theme.backgroundColor), Color(theme.backgroundColor).copy(alpha = 0.92f), Color(theme.backgroundColor).copy(alpha = 0.78f)))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "VINYLFLOW",
                        color = ink,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.2.sp
                    )
                    Text(
                        "YOUR MUSIC, ON VINYL",
                        color = muted,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.1.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(if (theme.darkMode) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.78f))
                            .clickable(onClick = onOpenSources)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Box(Modifier.size(8.dp).background(if (state.source != null) accent else muted.copy(alpha = 0.45f), CircleShape))
                        Text(state.source?.appName ?: "Choose music", color = ink, style = MaterialTheme.typography.labelMedium)
                    }
                    IconButton(onClick = onOpenCustomize) {
                        Icon(Icons.Rounded.Tune, "Customize player", tint = muted)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // The current track cover fills the turntable panel; the spinning disc sits over it.
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 430.dp)
                    .aspectRatio(1.03f)
                    .shadow(20.dp, RoundedCornerShape(30.dp))
                    .clip(RoundedCornerShape(30.dp))
                    .background(if (theme.darkMode) Color(0xFF222329) else Color(0xFFD5D5D2))
                    .border(1.dp, Color.White.copy(alpha = 0.88f), RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center
            ) {
                val cover = if (theme.showArtwork) track?.artworkBitmap else null
                if (cover != null) {
                    Image(cover.asImageBitmap(), "Album artwork background", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.16f), Color.Transparent, Color.Black.copy(alpha = 0.28f)))
                        )
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.linearGradient(
                                if (theme.darkMode) listOf(Color(0xFF42434A), Color(0xFF292A30), Color(0xFF1F2025))
                                else listOf(Color(0xFFF5F5F4), Color(0xFFD7D9DC), Color(0xFFBFC2C6))
                            )
                        )
                    )
                }
                Text(
                    "VINYLFLOW  ·  33⅓ RPM",
                    modifier = Modifier.align(Alignment.TopStart).padding(start = 18.dp, top = 16.dp),
                    color = if (cover != null) Color.White.copy(alpha = 0.92f) else Color(0xFF555860).copy(alpha = 0.75f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                val discSize = minOf(maxWidth * 0.82f, maxHeight * 0.84f)
                Box(
                    Modifier.size(discSize).offset(x = maxWidth * 0.15f),
                    contentAlignment = Alignment.Center
                ) {
                    VinylRecord(
                        isPlaying = state.isPlaying,
                        artwork = cover,
                        labelTitle = track?.displayTitle.orEmpty(),
                        labelArtist = track?.displayArtist.orEmpty(),
                        recordColor = Color(theme.recordColor), grooveColor = Color(theme.grooveColor),
                        labelColor = Color(theme.labelColor), accentColor = accent,
                        showGrooves = theme.showGrooves, grooveIntensity = theme.grooveIntensity,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (theme.showToneArm) ToneArm(isPlaying = state.isPlaying, armColor = Color(theme.toneArmColor))
                }
                Text(
                    "STEREO  ·  HI-FI",
                    modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp),
                    color = if (cover != null) Color.White.copy(alpha = 0.85f) else Color(0xFF555860).copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(Modifier.height(15.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        track?.displayTitle ?: "Nothing playing",
                        color = ink,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        track?.displayArtist ?: "Start a song and let the record spin",
                        color = muted,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (track != null && MediaAction.SEEK in state.supportedActions && track.durationMs > 0L) {
                Spacer(Modifier.height(7.dp))
                SeekBar(positionMs, track.durationMs, accent, onSeek, textColor = muted)
            }

            Spacer(Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrev,
                    enabled = MediaAction.PREVIOUS in state.supportedActions,
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        Icons.Rounded.SkipPrevious,
                        "Previous track",
                        tint = ink.copy(alpha = if (MediaAction.PREVIOUS in state.supportedActions) 1f else 0.35f),
                        modifier = Modifier.size(34.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 22.dp)
                        .size(76.dp)
                        .shadow(8.dp, CircleShape)
                        .background(accent, CircleShape)
                        .clickable(enabled = state.isActive) {
                            if (state.isPlaying) onPause() else onPlay()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        if (state.isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
                IconButton(
                    onClick = onNext,
                    enabled = MediaAction.NEXT in state.supportedActions,
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        Icons.Rounded.SkipNext,
                        "Next track",
                        tint = ink.copy(alpha = if (MediaAction.NEXT in state.supportedActions) 1f else 0.35f),
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(Modifier.height(5.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                PlayerNavAction(Icons.Rounded.LibraryMusic, "Sources", onOpenSources, ink)
                PlayerNavAction(Icons.Rounded.Palette, "Looks", onOpenCustomize, ink)
                PlayerNavAction(Icons.Rounded.Settings, "Settings", onOpenSettings, ink)
            }
        }
    }
}

@Composable
private fun PlayerNavAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, tint: Color = Color.White) {
    TextButton(onClick = onClick) {
        Icon(icon, contentDescription = null, tint = tint.copy(alpha = 0.72f), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(7.dp))
        Text(label, color = tint.copy(alpha = 0.72f), style = MaterialTheme.typography.labelLarge)
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
    labelTitle: String = "",
    labelArtist: String = "",
    modifier: Modifier = Modifier
) {
    // Stop the motor where it is when paused; do not animate a hidden/idle record.
    val rotation = remember { Animatable(0f) }
    val artworkShader = remember(artwork) {
        artwork?.let {
            android.graphics.BitmapShader(
                it,
                android.graphics.Shader.TileMode.CLAMP,
                android.graphics.Shader.TileMode.CLAMP
            )
        }
    }
    val artworkBrush = remember(artworkShader) { artworkShader?.let(::ShaderBrush) }
    LaunchedEffect(isPlaying) {
        while (isActive && isPlaying) {
            val remainder = 360f - (rotation.value % 360f)
            val duration = (3_000f * remainder / 360f).toInt().coerceAtLeast(120)
            rotation.animateTo(rotation.value + remainder, tween(duration, easing = LinearEasing))
            if (rotation.value >= 360f) rotation.snapTo(rotation.value % 360f)
        }
    }

    Canvas(
        modifier = modifier
            .rotate(rotation.value)
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
        if (artwork != null && artworkShader != null && artworkBrush != null) {
            val scale = labelR * 2f / minOf(artwork.width, artwork.height).coerceAtLeast(1)
            artworkShader.setLocalMatrix(android.graphics.Matrix().apply {
                setScale(scale, scale)
                postTranslate(cx.x - artwork.width * scale / 2f, cx.y - artwork.height * scale / 2f)
            })
            drawCircle(
                brush  = artworkBrush,
                radius = labelR,
                center = cx
            )
        }

        if (labelTitle.isNotBlank() || labelArtist.isNotBlank()) {
            drawContext.canvas.nativeCanvas.let { native ->
                val titlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (artwork == null) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = r * 0.082f
                    typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
                    setShadowLayer(r * 0.025f, 0f, r * 0.01f, android.graphics.Color.argb(130, 0, 0, 0))
                }
                val artistPaint = android.graphics.Paint(titlePaint).apply {
                    textSize = r * 0.063f
                    typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
                }
                native.drawText(labelTitle.take(10), cx.x, cx.y + r * 0.08f, titlePaint)
                native.drawText(labelArtist.take(14), cx.x, cx.y + r * 0.18f, artistPaint)
            }
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
fun ToneArm(
    isPlaying: Boolean,
    armColor: Color,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val angle by animateFloatAsState(
        targetValue   = if (isPlaying) 28f else 12f,
        animationSpec = tween(durationMillis = 800, easing = EaseInOutCubic),
        label         = "tonearm"
    )

    Canvas(
        modifier = modifier,
    ) {
        val pivotX = size.width * 0.84f
        val pivotY = size.height * 0.10f
        val pivotR = 12.dp.toPx()

        val rad  = Math.toRadians(angle.toDouble())
        val armL = size.minDimension * 0.67f
        val tipX = pivotX - armL * sin(rad).toFloat()
        val tipY = pivotY + armL * cos(rad).toFloat()

        // Arm shadow
        drawLine(
            color       = Color.Black.copy(alpha = 0.4f),
            start       = Offset(pivotX + 2f, pivotY + 2f),
            end         = Offset(tipX + 2f, tipY + 2f),
            strokeWidth = 6.dp.toPx(),
            cap         = StrokeCap.Round
        )
        // Arm
        drawLine(
            color       = armColor,
            start       = Offset(pivotX, pivotY),
            end         = Offset(tipX, tipY),
            strokeWidth = 4.dp.toPx(),
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
        drawRoundRect(
            color = armColor,
            topLeft = Offset(tipX - 8.dp.toPx(), tipY - 5.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(18.dp.toPx(), 10.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
        )
        drawCircle(color = Color(0xFF36383D), radius = 2.dp.toPx(), center = Offset(tipX - 2.dp.toPx(), tipY))
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
    onSeek: (Long) -> Unit,
    textColor: Color = Color.White
) {
    val fraction = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    Column {
        Slider(
            value         = fraction,
            onValueChange = { onSeek((it * durationMs).toLong()) },
            colors        = SliderDefaults.colors(
                thumbColor          = accentColor,
                activeTrackColor    = accentColor,
                inactiveTrackColor  = textColor.copy(alpha = 0.2f)
            )
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(positionMs), color = textColor.copy(alpha = 0.55f), style = MaterialTheme.typography.labelSmall)
            Text(formatTime(durationMs), color = textColor.copy(alpha = 0.55f), style = MaterialTheme.typography.labelSmall)
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
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFFE98850), Color(0xFFB8523B), Color(0xFF351D20)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(190.dp).clip(CircleShape)
                    .background(Color(0xFFFFB873)).padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                VinylRecord(
                    isPlaying = false,
                    artwork = null,
                    recordColor = Color(0xFF151519),
                    grooveColor = Color(0xFF38383C),
                    labelColor = Color(0xFFE58350),
                    accentColor = Color(0xFFFFE0C1),
                    showGrooves = true,
                    grooveIntensity = 0.65f,
                    modifier = Modifier.fillMaxSize()
                )
                Icon(
                    Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFFFFF3E5),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.height(26.dp))
            Text(
                "Let the music in",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFFFF3E5),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Allow notification access so VinylFlow can find the song playing in your music app and control it from the turntable.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFFFF3E5).copy(alpha = 0.78f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(25.dp))
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFF2E2),
                    contentColor = Color(0xFF71372D)
                ),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text("Connect to music", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "VinylFlow uses media-session details and does not read notification text.",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFFFF3E5).copy(alpha = 0.62f),
                textAlign = TextAlign.Center
            )
        }
    }
}
