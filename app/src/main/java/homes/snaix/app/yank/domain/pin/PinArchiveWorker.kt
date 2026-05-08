package homes.snaix.app.yank.domain.pin

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import homes.snaix.app.yank.YankApp

class PinArchiveWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val historyId = inputData.getString(KEY_HISTORY_ID) ?: return Result.failure()
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, -1)
        val app = applicationContext as YankApp
        if (notificationId > 0) app.di.pinPublisher.cancel(notificationId)
        app.di.historyRepo.setArchived(historyId)
        return Result.success()
    }
    companion object {
        const val KEY_HISTORY_ID = "history_id"
        const val KEY_NOTIFICATION_ID = "notification_id"
    }
}
