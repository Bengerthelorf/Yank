package homes.snaix.app.yank.domain.routing

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import homes.snaix.app.yank.data.db.Source
import homes.snaix.app.yank.data.db.YankDatabase
import homes.snaix.app.yank.data.repo.HistoryRepository
import homes.snaix.app.yank.domain.schema.Recognition
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RouterTest {
    private lateinit var db: YankDatabase
    private lateinit var repo: HistoryRepository
    private val publisher = FakePublisher()
    private val scheduler = FakeScheduler()
    private var nidCounter = 0
    private val clock = homes.snaix.app.yank.domain.time.EventClock()
    private var fakeNow = 1_700_000_000_000L

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            YankDatabase::class.java
        ).allowMainThreadQueries().build()
        repo = HistoryRepository(db.historyDao(), db.dedupDao())
    }
    @After fun tearDown() = db.close()

    private fun router() = Router(
        repo = repo,
        publisher = publisher,
        scheduler = scheduler,
        clock = clock,
        nextNotificationId = { ++nidCounter },
        now = { fakeNow },
    )

    @Test fun pickup_publishes_pin_and_writes_history() = runTest {
        val event = router().route(
            listOf(Recognition.Pickup(number = "A123", brand = "瑞幸")),
            screenshotPath = null, zxingPayloads = emptyList(), source = Source.SCREENSHOT,
        )
        assertThat(event[0]).isInstanceOf(ResultEvent.PinPublished::class.java)
        assertThat(publisher.published).hasSize(1)
    }

    @Test fun second_capture_within_window_replaces_pin() = runTest {
        val r = Recognition.Pickup(number = "A123", brand = "瑞幸")
        router().route(listOf(r), null, emptyList(), Source.SCREENSHOT)
        fakeNow += 60_000L  // 1 min later
        val event = router().route(listOf(r), null, emptyList(), Source.SCREENSHOT)
        assertThat(event[0]).isInstanceOf(ResultEvent.DedupReplaced::class.java)
    }

    @Test fun expired_todo_archives_without_publishing() = runTest {
        val r = Recognition.Todo(title = "已过期", date = "2020-01-01", time = "08:00")
        val event = router().route(listOf(r), null, emptyList(), Source.SCREENSHOT)
        assertThat(event[0]).isInstanceOf(ResultEvent.TodoArchivedExpired::class.java)
        assertThat(publisher.published).isEmpty()
    }

    @Test fun future_todo_schedules_delayed_pin() = runTest {
        val futureMs = fakeNow + 24 * 60 * 60_000L
        val futureDate = java.time.LocalDate.ofInstant(
            java.time.Instant.ofEpochMilli(futureMs), java.time.ZoneId.systemDefault()
        ).toString()
        val r = Recognition.Todo(title = "明日", date = futureDate, time = "12:00", pinLeadMinutes = 30)
        val event = router().route(listOf(r), null, emptyList(), Source.SCREENSHOT)
        assertThat(event[0]).isInstanceOf(ResultEvent.TodoScheduled::class.java)
        assertThat(scheduler.todoScheduled).hasSize(1)
    }

    @Test fun note_saves_to_history_only() = runTest {
        val event = router().route(
            listOf(Recognition.Note(number = "hi")),
            null, emptyList(), Source.SCREENSHOT,
        )
        assertThat(event[0]).isInstanceOf(ResultEvent.NoteSaved::class.java)
        assertThat(publisher.published).isEmpty()
        assertThat(scheduler.todoScheduled).isEmpty()
    }
}

class FakePublisher : PinPublisher {
    val published = mutableListOf<Triple<String, Int, Recognition>>()
    val cancelled = mutableListOf<Int>()
    override suspend fun publish(history: homes.snaix.app.yank.data.db.HistoryEntity, notificationId: Int, recognition: Recognition, payload: String?) {
        published += Triple(history.id, notificationId, recognition)
    }
    override suspend fun cancel(notificationId: Int) { cancelled += notificationId }
}

class FakeScheduler : PinScheduler {
    val archiveScheduled = mutableListOf<Triple<String, Long, Int>>()
    val todoScheduled = mutableListOf<Triple<String, Long, Int>>()
    val todoCancelled = mutableListOf<String>()
    override suspend fun scheduleArchive(historyId: String, archiveAt: Long, notificationId: Int) {
        archiveScheduled += Triple(historyId, archiveAt, notificationId)
    }
    override suspend fun scheduleTodoPin(historyId: String, pinTime: Long, notificationId: Int) {
        todoScheduled += Triple(historyId, pinTime, notificationId)
    }
    override suspend fun cancelTodo(historyId: String) { todoCancelled += historyId }
}
