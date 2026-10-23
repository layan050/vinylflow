package com.example.vinylflow.media

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.vinylflow.domain.model.*
import com.example.vinylflow.widget.VinylBaseWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Android MediaSession discovery and control.
 *
 * Requires the user to grant Notification Listener permission via
 * Settings → Apps → Special app access → Notification access.
 *
 * Compatible with any app that exposes a MediaSession (YouTube Music,
 * Pocket Casts, VLC, Audible, Spotify, etc.).
 */
class MediaSessionRepository(private val context: Context) {

    companion object {
        private const val TAG = "MediaSessionRepo"

        @Volatile
        private var instance: MediaSessionRepository? = null

        fun getInstance(context: Context): MediaSessionRepository =
            instance ?: synchronized(this) {
                instance ?: MediaSessionRepository(context.applicationContext).also { instance = it }
            }
    }

    private val _mediaState = MutableStateFlow(MediaState.Empty)
    val mediaState: StateFlow<MediaState> = _mediaState.asStateFlow()

    private val _availableSessions = MutableStateFlow<List<SessionSource>>(emptyList())
    val availableSessions: StateFlow<List<SessionSource>> = _availableSessions.asStateFlow()

    private var sessionManager: MediaSessionManager? = null
    private var activeController: MediaController? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var widgetAnimationRunning = false
    private val widgetSpinTicker = object : Runnable {
        override fun run() {
            if (!_mediaState.value.isPlaying || !VinylBaseWidgetProvider.hasWidgets(context)) {
                widgetAnimationRunning = false
                return
            }
            VinylBaseWidgetProvider.updateAll(context, spinningOnly = true)
            mainHandler.postDelayed(this, 500L)
        }
    }

    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        onSessionsChanged(controllers ?: emptyList())
    }

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateFromController(activeController, metadata = metadata)
        }
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateFromController(activeController, playbackState = state)
        }
        override fun onSessionDestroyed() {
            Log.d(TAG, "Session destroyed")
            handleSessionDestroyed()
        }
    }

    // ── Initialisation ─────────────────────────────────────────────────────────

    fun initialize(listenerComponent: ComponentName) {
        try {
            sessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            val controllers = sessionManager?.getActiveSessions(listenerComponent) ?: emptyList()
            onSessionsChanged(controllers)
            sessionManager?.addOnActiveSessionsChangedListener(sessionListener, listenerComponent, mainHandler)
            _mediaState.value = _mediaState.value.copy(permissionGranted = true)
            refreshWidgets()
            Log.d(TAG, "Initialized with ${controllers.size} active sessions")
        } catch (e: SecurityException) {
            Log.w(TAG, "Permission denied: ${e.message}")
            _mediaState.value = MediaState.PermissionRequired
            refreshWidgets()
        } catch (e: Exception) {
            Log.e(TAG, "Init error: ${e.message}")
            _mediaState.value = MediaState.PermissionRequired
            refreshWidgets()
        }
    }

    fun onPermissionDenied() {
        _mediaState.value = MediaState.PermissionRequired
        refreshWidgets()
    }

    // ── Session Management ─────────────────────────────────────────────────────

    private fun onSessionsChanged(controllers: List<MediaController>) {
        val sources = controllers.mapNotNull { ctrl ->
            try { SessionSource(ctrl.packageName, getAppName(ctrl.packageName)) }
            catch (e: Exception) { null }
        }
        _availableSessions.value = sources

        // Pick the playing session first, otherwise first available
        val best = controllers.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        } ?: controllers.firstOrNull()

        switchToController(best)
    }

    private fun switchToController(controller: MediaController?) {
        if (controller?.sessionToken == activeController?.sessionToken) return
        activeController?.unregisterCallback(controllerCallback)
        activeController = controller
        controller?.registerCallback(controllerCallback, mainHandler)
        updateFromController(controller)
        Log.d(TAG, "Switched to: ${controller?.packageName ?: "none"}")
    }

    private fun updateFromController(
        controller: MediaController?,
        metadata: MediaMetadata? = null,
        playbackState: PlaybackState? = null
    ) {
        if (controller == null) {
            _mediaState.value = _mediaState.value.copy(isActive = false, track = null)
            refreshWidgets()
            return
        }

        val meta  = metadata      ?: controller.metadata
        val state = playbackState ?: controller.playbackState

        val trackInfo = meta?.let {
            TrackInfo(
                title  = it.getString(MediaMetadata.METADATA_KEY_TITLE),
                artist = it.getString(MediaMetadata.METADATA_KEY_ARTIST)
                    ?: it.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST),
                album  = it.getString(MediaMetadata.METADATA_KEY_ALBUM),
                artworkBitmap = it.getBitmap(MediaMetadata.METADATA_KEY_ART)
                    ?: it.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART),
                durationMs = it.getLong(MediaMetadata.METADATA_KEY_DURATION).coerceAtLeast(0L)
            )
        }

        _mediaState.value = MediaState(
            track           = trackInfo,
            playbackStatus  = mapState(state?.state),
            positionMs      = state?.position ?: 0L,
            supportedActions= mapActions(state?.actions ?: 0L),
            source          = SessionSource(controller.packageName, getAppName(controller.packageName)),
            isActive        = true,
            permissionGranted = true
        )
        refreshWidgets()
    }

    private fun handleSessionDestroyed() {
        activeController?.unregisterCallback(controllerCallback)
        activeController = null
        _mediaState.value = _mediaState.value.copy(isActive = false, track = null, source = null)
        refreshWidgets()
    }

    // ── Transport Controls ─────────────────────────────────────────────────────

    fun play()                  { activeController?.transportControls?.play() }
    fun pause()                 { activeController?.transportControls?.pause() }
    fun next()                  { activeController?.transportControls?.skipToNext() }
    fun previous()              { activeController?.transportControls?.skipToPrevious() }
    fun seekTo(ms: Long)        { activeController?.transportControls?.seekTo(ms) }

    fun selectSource(packageName: String) {
        try {
            val ctrl = sessionManager
                ?.getActiveSessions(null)
                ?.firstOrNull { it.packageName == packageName }
            if (ctrl != null) switchToController(ctrl)
        } catch (e: Exception) {
            Log.w(TAG, "Cannot select source: ${e.message}")
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun mapState(state: Int?): PlaybackStatus = when (state) {
        PlaybackState.STATE_PLAYING    -> PlaybackStatus.PLAYING
        PlaybackState.STATE_PAUSED     -> PlaybackStatus.PAUSED
        PlaybackState.STATE_BUFFERING,
        PlaybackState.STATE_CONNECTING -> PlaybackStatus.BUFFERING
        PlaybackState.STATE_STOPPED    -> PlaybackStatus.STOPPED
        PlaybackState.STATE_ERROR      -> PlaybackStatus.ERROR
        else                           -> PlaybackStatus.IDLE
    }

    private fun mapActions(actions: Long): Set<MediaAction> = buildSet {
        if (actions and PlaybackState.ACTION_PLAY != 0L)             add(MediaAction.PLAY)
        if (actions and PlaybackState.ACTION_PAUSE != 0L)            add(MediaAction.PAUSE)
        if (actions and PlaybackState.ACTION_SKIP_TO_NEXT != 0L)     add(MediaAction.NEXT)
        if (actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS != 0L) add(MediaAction.PREVIOUS)
        if (actions and PlaybackState.ACTION_SEEK_TO != 0L)          add(MediaAction.SEEK)
    }

    private fun getAppName(pkg: String): String = try {
        context.packageManager.getApplicationLabel(
            context.packageManager.getApplicationInfo(pkg, 0)
        ).toString()
    } catch (e: Exception) { pkg }

    private fun refreshWidgets() {
        try {
            val hasWidgets = VinylBaseWidgetProvider.hasWidgets(context)
            if (hasWidgets) VinylBaseWidgetProvider.updateAll(context)
            if (_mediaState.value.isPlaying && hasWidgets && !widgetAnimationRunning) {
                widgetAnimationRunning = true
                mainHandler.postDelayed(widgetSpinTicker, 500L)
            } else if ((!_mediaState.value.isPlaying || !hasWidgets) && widgetAnimationRunning) {
                mainHandler.removeCallbacks(widgetSpinTicker)
                widgetAnimationRunning = false
            }
        } catch (e: Exception) { Log.w(TAG, "Widget refresh failed: ${e.message}") }
    }

    fun cleanup() {
        mainHandler.removeCallbacks(widgetSpinTicker)
        widgetAnimationRunning = false
        try { sessionManager?.removeOnActiveSessionsChangedListener(sessionListener) }
        catch (e: Exception) { /* ignore */ }
        activeController?.unregisterCallback(controllerCallback)
        activeController = null
    }
}
