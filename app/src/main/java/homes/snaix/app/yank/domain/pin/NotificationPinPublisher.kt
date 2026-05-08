package homes.snaix.app.yank.domain.pin

import android.app.NotificationManager
import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import homes.snaix.app.yank.data.db.HistoryEntity
import homes.snaix.app.yank.domain.routing.PinPublisher
import homes.snaix.app.yank.domain.schema.Recognition
import java.util.concurrent.TimeUnit

class NotificationPinPublisher(
    private val context: Context,
    private val builder: PinNotificationBuilder = PinNotificationBuilder(context),
) : PinPublisher {

    private val nm = context.getSystemService(NotificationManager::class.java)
    private val workManager = WorkManager.getInstance(context)

    override suspend fun publish(
        history: HistoryEntity,
        notificationId: Int,
        recognition: Recognition,
        payload: String?,
    ) {
        val now = System.currentTimeMillis()
        val pinTime = if (recognition is Recognition.Todo) {
            history.eventTime?.let { it - recognition.pinLeadMinutes * 60_000L }
        } else null
        val progress = ProgressStyleMapper.configFor(recognition, history.eventTime, pinTime, now)
        val notification = builder.build(recognition, progress, payload)
        nm?.notify(notificationId, notification)

        // Schedule a periodic refresh chain for time-based Pins.
        if (recognition is Recognition.Ticket || recognition is Recognition.Todo) {
            val event = history.eventTime ?: return
            val remaining = event - now
            if (remaining <= 0) return
            val nextDelay = if (remaining > 60 * 60_000L) 10 * 60_000L else 60_000L
            val req = OneTimeWorkRequestBuilder<PinRefreshWorker>()
                .setInputData(Data.Builder()
                    .putString(PinRefreshWorker.KEY_HISTORY_ID, history.id)
                    .putInt(PinRefreshWorker.KEY_NOTIFICATION_ID, notificationId)
                    .build())
                .setInitialDelay(nextDelay, TimeUnit.MILLISECONDS)
                .build()
            workManager.enqueueUniqueWork(
                "pin_refresh_${history.id}", ExistingWorkPolicy.REPLACE, req
            )
        }
    }

    override suspend fun cancel(notificationId: Int) {
        nm?.cancel(notificationId)
    }
}
