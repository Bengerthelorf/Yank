package homes.snaix.app.yank.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryDaoTest {
    private lateinit var db: YankDatabase
    private lateinit var dao: HistoryDao
    private lateinit var dedup: DedupDao

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            YankDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.historyDao()
        dedup = db.dedupDao()
    }

    @After fun tearDown() { db.close() }

    @Test fun upsert_and_observeActivePinHistory_returns_inserted() = runTest {
        dao.upsert(sample(id = "1", type = "取餐", archived = false))
        val items = dao.observeActivePinHistory(null, null).first()
        assertThat(items.map { it.id }).containsExactly("1")
    }

    @Test fun observeActivePinHistory_excludes_archived_and_notes() = runTest {
        dao.upsert(sample(id = "a", type = "取餐", archived = false))
        dao.upsert(sample(id = "b", type = "取餐", archived = true))
        dao.upsert(sample(id = "c", type = "notes", archived = false))
        val items = dao.observeActivePinHistory(null, null).first()
        assertThat(items.map { it.id }).containsExactly("a")
    }

    @Test fun observeArchived_returns_only_archived() = runTest {
        dao.upsert(sample(id = "a", archived = true))
        dao.upsert(sample(id = "b", archived = false))
        val items = dao.observeArchived(null).first()
        assertThat(items.map { it.id }).containsExactly("a")
    }

    @Test fun setArchived_marks_entry() = runTest {
        dao.upsert(sample(id = "1", archived = false))
        dao.setArchived("1")
        val e = dao.getById("1")!!
        assertThat(e.archived).isTrue()
    }

    @Test fun dedup_findFreshActive_skips_archived() = runTest {
        dao.upsert(sample(id = "h1", archived = true))
        dedup.insert(DedupEntity(key = "k", notificationId = 1, historyId = "h1", createdAt = 1000L))
        val r = dedup.findFreshActive("k", 0L)
        assertThat(r).isNull()
    }

    @Test fun deleteArchivedBefore_purges() = runTest {
        dao.upsert(sample(id = "old", archived = true, archiveAt = 100L))
        dao.upsert(sample(id = "new", archived = true, archiveAt = 9999L))
        val deleted = dao.deleteArchivedBefore(1000L)
        assertThat(deleted).isEqualTo(1)
        assertThat(dao.getById("old")).isNull()
        assertThat(dao.getById("new")).isNotNull()
    }

    private fun sample(
        id: String = "x",
        type: String = "取餐",
        archived: Boolean = false,
        archiveAt: Long = 0L,
    ) = HistoryEntity(
        id = id, type = type,
        displayPrimary = "p", displaySecondary = null,
        rawText = "t", rawJson = "{}", zxingPayloads = null, screenshotPath = null,
        createdAt = 1000L, eventTime = null, archiveAt = archiveAt,
        archived = archived, source = Source.SCREENSHOT,
    )
}
