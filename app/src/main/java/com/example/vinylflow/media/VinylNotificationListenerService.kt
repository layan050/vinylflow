package com.example.vinylflow.media

import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.util.Log

/**
 * Grants VinylFlow access to active MediaSessions via the Notification
 * Listener permission. The user must enable it once in:
 *   Settings → Apps → Special app access → Notification access → VinylFlow
 *
 * This service does NOT read notification content — it only provides the
 * ComponentName required by MediaSessionManager.getActiveSessions().
 */
class VinylNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "VinylNLS"

        @Volatile
        var isConnected: Boolean = false
            private set

        fun componentName(context: android.content.Context): ComponentName =
            ComponentName(context, VinylNotificationListenerService::class.java)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isConnected = true
        Log.d(TAG, "Listener connected — initialising MediaSessionRepository")
        try {
            MediaSessionRepository.getInstance(applicationContext)
                .initialize(componentName(applicationContext))
        } catch (e: Exception) {
            Log.e(TAG, "Error initialising on connect: ${e.message}")
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isConnected = false
        Log.d(TAG, "Listener disconnected")
    }
}
