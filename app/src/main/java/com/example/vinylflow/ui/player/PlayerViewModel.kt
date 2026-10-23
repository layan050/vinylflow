package com.example.vinylflow.ui.player

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vinylflow.domain.model.*
import com.example.vinylflow.media.MediaSessionRepository
import com.example.vinylflow.media.VinylNotificationListenerService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlayerViewModel(private val context: Context) : ViewModel() {

    private val repo = MediaSessionRepository.getInstance(context)

    val mediaState: StateFlow<MediaState> = repo.mediaState
    val availableSessions: StateFlow<List<SessionSource>> = repo.availableSessions

    // Live position ticker (updates every second when playing)
    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position.asStateFlow()

    init {
        // Try initialising immediately (in case service is already connected)
        tryInitialize()

        // Keep position up-to-date
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                val s = mediaState.value
                if (s.isPlaying) {
                    _position.value = _position.value + 1_000
                }
            }
        }

        // Sync position whenever state changes
        viewModelScope.launch {
            mediaState.collect { state ->
                _position.value = state.positionMs
            }
        }
    }

    private fun tryInitialize() {
        if (VinylNotificationListenerService.isConnected) {
            repo.initialize(VinylNotificationListenerService.componentName(context))
        } else {
            repo.onPermissionDenied()
        }
    }

    fun refresh() = tryInitialize()

    fun play()              = repo.play()
    fun pause()             = repo.pause()
    fun next()              = repo.next()
    fun previous()          = repo.previous()
    fun seekTo(ms: Long)    = repo.seekTo(ms)
    fun selectSource(pkg: String) = repo.selectSource(pkg)

    override fun onCleared() {
        super.onCleared()
        repo.cleanup()
    }

    // ── Factory ────────────────────────────────────────────────────────────────

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PlayerViewModel(context.applicationContext) as T
    }
}
