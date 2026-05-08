package homes.snaix.app.yank.domain.pin

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import homes.snaix.app.yank.R
import homes.snaix.app.yank.domain.schema.Recognition
import homes.snaix.app.yank.domain.schema.displayPrimary
import homes.snaix.app.yank.domain.schema.displaySecondary

class PinNotificationBuilder(private val ctx: Context) {

    fun build(
        recognition: Recognition,
        progress: ProgressConfig,
        qrPayload: String?,
    ): Notification {
        val title = recognition.displayPrimary().ifEmpty { "Yank" }
        val text = recognition.displaySecondary().orEmpty()

        val largeIcon: Bitmap? = qrPayload?.let { QrRenderer.render(it, sizePx = 512) }

        val compat = NotificationCompat.Builder(ctx, ChannelIds.PIN)
            .setSmallIcon(R.drawable.ic_yank)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setColorized(true)
            .setContentTitle(title)
            .setContentText(text)
            .apply { largeIcon?.let { setLargeIcon(it) } }
            .setStyle(NotificationCompat.BigTextStyle().bigText("$title\n$text"))
            .build()

        return if (Build.VERSION.SDK_INT >= 36) {
            applyProgressStyleIfPossible(compat, progress, title, text, largeIcon)
        } else {
            compat
        }
    }

    /**
     * Layer Notification.ProgressStyle on API 36+. Reflection-guarded:
     * if the OEM (e.g. Xiaomi HyperOS) stripped or moved the API, we
     * fall back silently to the BigTextStyle compat notification.
     */
    private fun applyProgressStyleIfPossible(
        fallback: Notification,
        progress: ProgressConfig,
        title: String,
        text: String,
        largeIcon: Bitmap?,
    ): Notification = try {
        val builder = Notification.Builder(ctx, ChannelIds.PIN)
            .setSmallIcon(R.drawable.ic_yank)
            .setOngoing(true)
            .setColorized(true)
            .setContentTitle(title)
            .setContentText(text)
            .apply { largeIcon?.let { setLargeIcon(it) } }

        val progressClass = Class.forName("android.app.Notification\$ProgressStyle")
        val progressInstance = progressClass.getConstructor().newInstance()
        progressClass.getMethod("setProgress", Int::class.javaPrimitiveType)
            .invoke(progressInstance, progress.percent.coerceAtLeast(0))
        Notification.Builder::class.java.getMethod("setStyle", Notification.Style::class.java)
            .invoke(builder, progressInstance)

        builder.build()
    } catch (e: Throwable) {
        fallback
    }
}
