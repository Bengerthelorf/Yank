package homes.snaix.app.yank

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import homes.snaix.app.yank.domain.pin.ChannelIds
import homes.snaix.app.yank.domain.pin.HistoryRetentionWorker
import homes.snaix.app.yank.trigger.CaptureActivity
import java.util.concurrent.TimeUnit

class YankApp : Application() {
    lateinit var di: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        di = AppContainer(this)
        ensureChannels()
        scheduleRetention()
        registerCaptureShortcut()
    }

    private fun ensureChannels() {
        val nm = getSystemService(NotificationManager::class.java) ?: return
        nm.createNotificationChannel(NotificationChannel(
            ChannelIds.SERVICE, "Yank Service", NotificationManager.IMPORTANCE_LOW
        ).apply { description = "FG service" })
        nm.createNotificationChannel(NotificationChannel(
            ChannelIds.PIN, "钉住的信息", NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "识别后的常驻通知" })
        nm.createNotificationChannel(NotificationChannel(
            ChannelIds.ERROR, "错误", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "解析失败 / 网络错误" })
    }

    private fun scheduleRetention() {
        val req = PeriodicWorkRequestBuilder<HistoryRetentionWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "yank_retention", ExistingPeriodicWorkPolicy.KEEP, req
        )
    }

    private fun registerCaptureShortcut() {
        val intent = Intent(this, CaptureActivity::class.java).apply {
            action = Intent.ACTION_VIEW
        }
        val shortcut = ShortcutInfoCompat.Builder(this, "yank_capture")
            .setShortLabel(getString(R.string.shortcut_capture_short))
            .setLongLabel(getString(R.string.shortcut_capture_long))
            .setIcon(IconCompat.createWithResource(this, R.drawable.ic_yank))
            .setIntent(intent)
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(this, shortcut)
    }
}
