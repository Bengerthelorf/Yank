package homes.snaix.app.yank.domain.pin

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import homes.snaix.app.yank.YankApp
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.concurrent.TimeUnit

class HistoryRetentionWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as YankApp
        val days = app.di.configRepo.archiveRetentionDays().first()
        val cutoff = System.currentTimeMillis() - days * TimeUnit.DAYS.toMillis(1)
        val toDelete = app.di.database.historyDao().observeArchived(null).first()
            .filter { it.archiveAt < cutoff }
        toDelete.forEach { entry ->
            entry.screenshotPath?.let { runCatching { File(it).delete() } }
        }
        app.di.historyRepo.deleteArchivedBefore(cutoff)
        return Result.success()
    }
}
