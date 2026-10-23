package com.example.vinylflow.domain.model

import android.graphics.Bitmap

// ── Playback State ────────────────────────────────────────────────────────────

enum class PlaybackStatus { IDLE, PLAYING, PAUSED, BUFFERING, STOPPED, ERROR }

enum class MediaAction { PLAY, PAUSE, NEXT, PREVIOUS, SEEK }

// ── Session Source ─────────────────────────────────────────────────────────────

data class SessionSource(
    val packageName: String,
    val appName: String
)

// ── Track Info ─────────────────────────────────────────────────────────────────

data class TrackInfo(
    val title: String?,
    val artist: String?,
    val album: String?,
    val artworkBitmap: Bitmap?,
    val durationMs: Long = 0L
) {
    val displayTitle: String get() = title?.takeIf { it.isNotBlank() } ?: "Unknown Track"
    val displayArtist: String get() = artist?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
    val displayAlbum: String get() = album?.takeIf { it.isNotBlank() } ?: ""
}

// ── Media State ────────────────────────────────────────────────────────────────

data class MediaState(
    val track: TrackInfo? = null,
    val playbackStatus: PlaybackStatus = PlaybackStatus.IDLE,
    val positionMs: Long = 0L,
    val supportedActions: Set<MediaAction> = emptySet(),
    val source: SessionSource? = null,
    val isActive: Boolean = false,
    val permissionGranted: Boolean = false,
    val error: String? = null
) {
    val isPlaying: Boolean get() = playbackStatus == PlaybackStatus.PLAYING

    val progressFraction: Float
        get() {
            val dur = track?.durationMs ?: 0L
            return if (dur > 0L) (positionMs.toFloat() / dur).coerceIn(0f, 1f) else 0f
        }

    companion object {
        val Empty = MediaState()
        val PermissionRequired = MediaState(
            permissionGranted = false,
            error = "Notification Listener permission required"
        )
    }
}
