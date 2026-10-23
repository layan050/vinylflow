package com.example.vinylflow.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.widget.RemoteViews
import com.example.vinylflow.MainActivity
import com.example.vinylflow.R
import com.example.vinylflow.media.VinylNotificationListenerService

/** A real launcher widget, backed by Android media sessions. */
class VinylWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateWidget(context, manager, it) }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, VinylWidgetProvider::class.java))
            ids.forEach { updateWidget(context, manager, it) }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_vinyl)
            val controller = activeController(context)
            val metadata = controller?.metadata
            val state = controller?.playbackState
            val title = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE)
                ?.takeIf(String::isNotBlank)
            val artist = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST)
                ?.takeIf(String::isNotBlank)
                ?: metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                    ?.takeIf(String::isNotBlank)

            views.setTextViewText(R.id.widget_track_title, title ?: "Open a music app")
            views.setTextViewText(R.id.widget_track_artist, artist ?: "Your record will show here")
            views.setTextViewText(
                R.id.widget_source,
                if (controller != null) "NOW SPINNING" else "VINYLFLOW · READY"
            )

            val artwork = metadata?.getBitmap(android.media.MediaMetadata.METADATA_KEY_ART)
                ?: metadata?.getBitmap(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART)
            if (artwork != null && !artwork.isRecycled) {
                views.setImageViewBitmap(R.id.widget_artwork, circularArtwork(artwork))
            } else {
                views.setImageViewResource(R.id.widget_artwork, R.drawable.widget_artwork_placeholder)
            }

            val isPlaying = state?.state == PlaybackState.STATE_PLAYING
            views.setImageViewResource(
                R.id.widget_play_pause,
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )
            views.setInt(R.id.widget_play_pause, "setColorFilter", 0xFFFFF2E4.toInt())
            views.setInt(R.id.widget_previous, "setColorFilter", 0xFFFFF2E4.toInt())
            views.setInt(R.id.widget_next, "setColorFilter", 0xFFFFF2E4.toInt())

            views.setOnClickPendingIntent(R.id.widget_root, openApp(context))
            views.setOnClickPendingIntent(R.id.widget_previous, action(context, id, VinylWidgetActionReceiver.PREVIOUS))
            views.setOnClickPendingIntent(R.id.widget_play_pause, action(context, id, VinylWidgetActionReceiver.TOGGLE))
            views.setOnClickPendingIntent(R.id.widget_next, action(context, id, VinylWidgetActionReceiver.NEXT))
            manager.updateAppWidget(id, views)
        }

        private fun activeController(context: Context): MediaController? = try {
            val manager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val sessions = manager.getActiveSessions(VinylNotificationListenerService.componentName(context))
            sessions.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
                ?: sessions.firstOrNull { it.metadata != null }
                ?: sessions.firstOrNull()
        } catch (_: Exception) {
            null
        }

        private fun openApp(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java)
            return PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun action(context: Context, widgetId: Int, name: String): PendingIntent {
            val intent = Intent(context, VinylWidgetActionReceiver::class.java).apply {
                action = name
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            return PendingIntent.getBroadcast(
                context, widgetId * 31 + name.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun circularArtwork(source: Bitmap): Bitmap {
            val side = 144
            val output = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            val shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            val crop = minOf(source.width, source.height)
            val left = (source.width - crop) / 2
            val top = (source.height - crop) / 2
            shader.setLocalMatrix(android.graphics.Matrix().apply {
                setScale(side.toFloat() / crop, side.toFloat() / crop)
                postTranslate(-left * side.toFloat() / crop, -top * side.toFloat() / crop)
            })
            paint.shader = shader
            Canvas(output).drawCircle(side / 2f, side / 2f, side / 2f, paint)
            return output
        }
    }
}

class VinylWidgetActionReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val manager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val controller = manager.getActiveSessions(
                VinylNotificationListenerService.componentName(context)
            ).firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
                ?: manager.getActiveSessions(VinylNotificationListenerService.componentName(context)).firstOrNull()
            when (intent.action) {
                PREVIOUS -> controller?.transportControls?.skipToPrevious()
                NEXT -> controller?.transportControls?.skipToNext()
                TOGGLE -> if (controller?.playbackState?.state == PlaybackState.STATE_PLAYING) {
                    controller.transportControls.pause()
                } else {
                    controller?.transportControls?.play()
                }
            }
        } catch (_: Exception) {
            // Permission may be off or the source app may have closed.
        }
        VinylWidgetProvider.updateAll(context)
    }

    companion object {
        const val PREVIOUS = "com.example.vinylflow.widget.PREVIOUS"
        const val TOGGLE = "com.example.vinylflow.widget.TOGGLE"
        const val NEXT = "com.example.vinylflow.widget.NEXT"
    }
}
