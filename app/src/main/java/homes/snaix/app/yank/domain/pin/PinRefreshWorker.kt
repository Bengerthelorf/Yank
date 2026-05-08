package homes.snaix.app.yank.domain.pin

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import homes.snaix.app.yank.YankApp
import homes.snaix.app.yank.domain.schema.Recognition
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

class PinRefreshWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val historyId = inputData.getString(KEY_HISTORY_ID) ?: return Result.failure()
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, -1)
        if (notificationId < 0) return Result.failure()

        val app = applicationContext as YankApp
        val entity = app.di.historyRepo.get(historyId) ?: return Result.success()
        if (entity.archived) return Result.success()

        val r = Json { ignoreUnknownKeys = true }.decodeFromString<Recognition>(entity.rawJson)
        app.di.pinPublisher.publish(entity, notificationId, r, payload = null)

        // Reschedule next refresh
        val now = System.currentTimeMillis()
        val event = entity.eventTime ?: return Result.success()
        val remaining = event - now
        if (remaining <= 0) return Result.success()

        val nextDelayMs = if (remaining > 60 * 60_000L) 10 * 60_000L else 60_000L
        val next = OneTimeWorkRequestBuilder<PinRefreshWorker>()
            .setInputData(Data.Builder().putString(KEY_HISTORY_ID, historyId).putInt(KEY_NOTIFICATION_ID, notificationId).build())
            .setInitialDelay(nextDelayMs, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "pin_refresh_$historyId",
            ExistingWorkPolicy.REPLACE,
            next,
        )
        return Result.success()
    }
    companion object {
        const val KEY_HISTORY_ID = "history_id"
        const val KEY_NOTIFICATION_ID = "notification_id"
    }
}
