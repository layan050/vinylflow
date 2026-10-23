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
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.SystemClock
import android.widget.RemoteViews
import com.example.vinylflow.MainActivity
import com.example.vinylflow.R
import com.example.vinylflow.media.VinylNotificationListenerService

enum class WidgetStyle(
    val layout: Int,
    val label: Int,
    val accent: Int,
    val controlTint: Int,
    val playTint: Int
) {
    MINI(R.layout.widget_vinyl_mini, R.string.widget_mini_name, 0xFF6055C8.toInt(), 0xFF4942A9.toInt(), Color.WHITE),
    PLAYER(R.layout.widget_vinyl, R.string.widget_player_name, 0xFFE58350.toInt(), 0xFF59483E.toInt(), Color.WHITE),
    TURNTABLE(R.layout.widget_vinyl_turntable, R.string.widget_turntable_name, 0xFF5F62C9.toInt(), 0xFF30323A.toInt(), Color.WHITE)
}

abstract class VinylBaseWidgetProvider : AppWidgetProvider() {
    protected abstract val widgetStyle: WidgetStyle

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val controller = activeController(context)
        appWidgetIds.forEach { updateWidget(context, manager, it, widgetStyle, controller) }
    }

    companion object {
        private val widgetProviders = listOf(
            VinylMiniWidgetProvider::class.java,
            VinylPlayerWidgetProvider::class.java,
            VinylTurntableWidgetProvider::class.java
        )

        fun hasWidgets(context: Context): Boolean {
            val manager = AppWidgetManager.getInstance(context)
            return widgetProviders.any { provider ->
                manager.getAppWidgetIds(ComponentName(context, provider)).isNotEmpty()
            }
        }

        fun updateAll(context: Context, rotationDegrees: Float = currentRotation()) {
            val manager = AppWidgetManager.getInstance(context)
            val controller = activeController(context)
            widgetProviders.forEach { provider ->
                val ids = manager.getAppWidgetIds(ComponentName(context, provider))
                val style = when (provider) {
                    VinylMiniWidgetProvider::class.java -> WidgetStyle.MINI
                    VinylPlayerWidgetProvider::class.java -> WidgetStyle.PLAYER
                    else -> WidgetStyle.TURNTABLE
                }
                ids.forEach { updateWidget(context, manager, it, style, controller, rotationDegrees) }
            }
        }

        private fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            id: Int,
            style: WidgetStyle,
            controller: MediaController?,
            rotationDegrees: Float = currentRotation()
        ) {
            val views = RemoteViews(context.packageName, style.layout)
            val metadata = controller?.metadata
            val playback = controller?.playbackState
            val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
                ?.takeIf(String::isNotBlank)
            val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?.takeIf(String::isNotBlank)
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                    ?.takeIf(String::isNotBlank)

            views.setTextViewText(R.id.widget_track_title, title ?: "Nothing playing")
            views.setTextViewText(R.id.widget_track_artist, artist ?: "Start music to spin")
            views.setTextViewText(
                R.id.widget_source,
                if (controller != null) "NOW SPINNING" else context.getString(style.label).uppercase()
            )

            val artwork = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            views.setImageViewBitmap(
                R.id.widget_record,
                createSpinningRecord(artwork, rotationDegrees, style.accent)
            )

            val isPlaying = playback?.state == PlaybackState.STATE_PLAYING
            views.setImageViewResource(
                R.id.widget_play_pause,
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )
            views.setInt(R.id.widget_play_pause, "setColorFilter", style.playTint)
            views.setInt(R.id.widget_previous, "setColorFilter", style.controlTint)
            views.setInt(R.id.widget_next, "setColorFilter", style.controlTint)

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

        private fun currentRotation(): Float = (SystemClock.elapsedRealtime() * 0.12f) % 360f

        private fun openApp(context: Context): PendingIntent = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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

        private fun createSpinningRecord(artwork: Bitmap?, degrees: Float, accent: Int): Bitmap {
            val side = 256
            val center = side / 2f
            val record = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(record)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

            paint.color = Color.rgb(12, 12, 14)
            canvas.drawCircle(center, center, center - 2f, paint)
            for (index in 0..30) {
                val radius = side * (0.34f + index * 0.0047f)
                paint.color = if (index % 2 == 0) 0xFF27272B.toInt() else 0xFF141417.toInt()
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = if (index % 3 == 0) 1.5f else 0.8f
                canvas.drawCircle(center, center, radius, paint)
            }

            paint.style = Paint.Style.FILL
            val labelRadius = side * 0.29f
            if (artwork != null && !artwork.isRecycled) {
                val shader = BitmapShader(artwork, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                val crop = minOf(artwork.width, artwork.height).coerceAtLeast(1)
                val scale = labelRadius * 2f / crop
                shader.setLocalMatrix(Matrix().apply {
                    setScale(scale, scale)
                    postTranslate(center - artwork.width * scale / 2f, center - artwork.height * scale / 2f)
                })
                paint.shader = shader
                canvas.drawCircle(center, center, labelRadius, paint)
                paint.shader = null
            } else {
                paint.color = accent
                canvas.drawCircle(center, center, labelRadius, paint)
            }

            paint.color = 0xFF111114.toInt()
            canvas.drawCircle(center, center, 4f, paint)
            paint.color = 0x66FFFFFF
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawCircle(center, center, labelRadius + 3f, paint)
            paint.style = Paint.Style.FILL

            val rotated = Bitmap.createBitmap(
                record, 0, 0, side, side,
                Matrix().apply { postRotate(degrees, center, center) }, true
            )
            record.recycle()
            return rotated
        }
    }
}

class VinylMiniWidgetProvider : VinylBaseWidgetProvider() {
    protected override val widgetStyle = WidgetStyle.MINI
}

class VinylPlayerWidgetProvider : VinylBaseWidgetProvider() {
    protected override val widgetStyle = WidgetStyle.PLAYER
}

class VinylTurntableWidgetProvider : VinylBaseWidgetProvider() {
    protected override val widgetStyle = WidgetStyle.TURNTABLE
}

class VinylWidgetActionReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val manager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val sessions = manager.getActiveSessions(VinylNotificationListenerService.componentName(context))
            val controller = sessions.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
                ?: sessions.firstOrNull()
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
            // Notification access may be disabled, or the media app may have closed.
        }
        VinylBaseWidgetProvider.updateAll(context)
    }

    companion object {
        const val PREVIOUS = "com.example.vinylflow.widget.PREVIOUS"
        const val TOGGLE = "com.example.vinylflow.widget.TOGGLE"
        const val NEXT = "com.example.vinylflow.widget.NEXT"
    }
}
