package homes.snaix.app.yank.domain.pin

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import homes.snaix.app.yank.domain.routing.PinScheduler
import java.util.concurrent.TimeUnit

class WorkPinScheduler(context: Context) : PinScheduler {
    private val wm = WorkManager.getInstance(context)

    override suspend fun scheduleArchive(historyId: String, archiveAt: Long, notificationId: Int) {
        val delay = (archiveAt - System.currentTimeMillis()).coerceAtLeast(0)
        val req = OneTimeWorkRequestBuilder<PinArchiveWorker>()
            .setInputData(Data.Builder()
                .putString(PinArchiveWorker.KEY_HISTORY_ID, historyId)
                .putInt(PinArchiveWorker.KEY_NOTIFICATION_ID, notificationId)
                .build())
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        wm.enqueueUniqueWork("pin_archive_$historyId", ExistingWorkPolicy.REPLACE, req)
    }

    override suspend fun scheduleTodoPin(historyId: String, pinTime: Long, notificationId: Int) {
        val delay = (pinTime - System.currentTimeMillis()).coerceAtLeast(0)
        val req = OneTimeWorkRequestBuilder<TodoPinWorker>()
            .setInputData(Data.Builder()
                .putString(TodoPinWorker.KEY_HISTORY_ID, historyId)
                .putInt(TodoPinWorker.KEY_NOTIFICATION_ID, notificationId)
                .build())
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        wm.enqueueUniqueWork("todo_pin_$historyId", ExistingWorkPolicy.REPLACE, req)
    }

    override suspend fun cancelTodo(historyId: String) {
        wm.cancelUniqueWork("todo_pin_$historyId")
    }
}
