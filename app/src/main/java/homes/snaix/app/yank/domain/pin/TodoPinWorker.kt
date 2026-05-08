package homes.snaix.app.yank.domain.pin

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import homes.snaix.app.yank.YankApp
import homes.snaix.app.yank.domain.schema.Recognition
import kotlinx.serialization.json.Json

class TodoPinWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val historyId = inputData.getString(KEY_HISTORY_ID) ?: return Result.failure()
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, -1)
        if (notificationId < 0) return Result.failure()

        val app = applicationContext as YankApp
        val entity = app.di.historyRepo.get(historyId) ?: return Result.failure()
        if (entity.archived) return Result.success()

        val r = Json { ignoreUnknownKeys = true }.decodeFromString<Recognition>(entity.rawJson)
        app.di.pinPublisher.publish(entity, notificationId, r, payload = null)
        return Result.success()
    }
    companion object {
        const val KEY_HISTORY_ID = "history_id"
        const val KEY_NOTIFICATION_ID = "notification_id"
    }
}
