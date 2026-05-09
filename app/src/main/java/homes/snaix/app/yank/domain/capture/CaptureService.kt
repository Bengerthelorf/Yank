package homes.snaix.app.yank.domain.capture

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import homes.snaix.app.yank.R
import homes.snaix.app.yank.YankApp
import homes.snaix.app.yank.data.db.Source
import homes.snaix.app.yank.domain.pin.ChannelIds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class CaptureService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val capturer by lazy { ScreenCapturer(applicationContext) }

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        startForegroundCompat()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CAPTURE -> handleCapture()
            ACTION_STOP -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun handleCapture() {
        val projection = MediaProjectionHolder.projection ?: run {
            stopSelf(); return
        }
        scope.launch {
            try {
                val bitmap = capturer.captureOnce(projection)
                val app = applicationContext as YankApp
                val outcome = app.di.capturePipeline.process(bitmap, source = Source.SCREENSHOT)
                app.di.captureOutcomeBus.emit(outcome)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun ensureChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(
            ChannelIds.SERVICE, "Yank Service", NotificationManager.IMPORTANCE_LOW
        ).apply { description = "FG service for screen capture token caching" })
    }

    private fun startForegroundCompat() {
        val notification: Notification = NotificationCompat.Builder(this, ChannelIds.SERVICE)
            .setSmallIcon(R.drawable.ic_yank_tile)
            .setContentTitle(getString(R.string.fg_ready_title))
            .setContentText(getString(R.string.fg_ready_text))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                ChannelIds.SERVICE_NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(ChannelIds.SERVICE_NOTIFICATION_ID, notification)
        }
    }

    companion object {
        const val ACTION_CAPTURE = "homes.snaix.app.yank.action.CAPTURE"
        const val ACTION_STOP = "homes.snaix.app.yank.action.STOP"
    }
}
