package homes.snaix.app.yank

import android.content.Context
import androidx.room.Room
import homes.snaix.app.yank.data.db.YankDatabase
import homes.snaix.app.yank.data.prefs.ConfigStore
import homes.snaix.app.yank.data.prefs.EncryptedKeyStore
import homes.snaix.app.yank.data.repo.ConfigRepository
import homes.snaix.app.yank.data.repo.HistoryRepository
import homes.snaix.app.yank.domain.capture.CapturePipeline
import homes.snaix.app.yank.domain.routing.PinPublisher
import homes.snaix.app.yank.domain.routing.PinScheduler
import homes.snaix.app.yank.domain.routing.Router
import homes.snaix.app.yank.domain.time.EventClock
import homes.snaix.app.yank.domain.vlm.VlmClient
import homes.snaix.app.yank.domain.zxing.ZxingDecoder
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AppContainer(private val ctx: Context) {

    val database: YankDatabase by lazy {
        Room.databaseBuilder(ctx, YankDatabase::class.java, "yank.db").build()
    }
    val configStore by lazy { ConfigStore(ctx) }
    val keyStore by lazy { EncryptedKeyStore(ctx) }
    val configRepo by lazy { ConfigRepository(configStore, keyStore) }
    val historyRepo by lazy { HistoryRepository(database.historyDao(), database.dedupDao()) }

    val zxing by lazy { ZxingDecoder() }
    val vlmClient by lazy { VlmClient() }

    // Phase 11 will replace these with real implementations.
    var pinPublisher: PinPublisher = NoopPublisher()
    var pinScheduler: PinScheduler = NoopScheduler()

    private var notificationCounter: Int = 2000
    val nextNotificationId: () -> Int = { ++notificationCounter }

    val router: Router by lazy {
        Router(
            repo = historyRepo,
            publisher = pinPublisher,
            scheduler = pinScheduler,
            clock = EventClock(),
            nextNotificationId = nextNotificationId,
        )
    }

    val capturePipeline: CapturePipeline by lazy {
        CapturePipeline(ctx, zxing, vlmClient, router, configRepo)
    }

    private val _outcomes = MutableSharedFlow<homes.snaix.app.yank.domain.capture.PipelineOutcome>(
        replay = 0, extraBufferCapacity = 8, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val captureOutcomeBus: MutableSharedFlow<homes.snaix.app.yank.domain.capture.PipelineOutcome> = _outcomes
    val captureOutcomes: SharedFlow<homes.snaix.app.yank.domain.capture.PipelineOutcome> = _outcomes.asSharedFlow()
}

private class NoopPublisher : PinPublisher {
    override suspend fun publish(history: homes.snaix.app.yank.data.db.HistoryEntity, notificationId: Int, recognition: homes.snaix.app.yank.domain.schema.Recognition, payload: String?) = Unit
    override suspend fun cancel(notificationId: Int) = Unit
}
private class NoopScheduler : PinScheduler {
    override suspend fun scheduleArchive(historyId: String, archiveAt: Long, notificationId: Int) = Unit
    override suspend fun scheduleTodoPin(historyId: String, pinTime: Long, notificationId: Int) = Unit
    override suspend fun cancelTodo(historyId: String) = Unit
}
