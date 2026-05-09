package homes.snaix.app.yank.data.repo

import homes.snaix.app.yank.data.db.DedupDao
import homes.snaix.app.yank.data.db.DedupEntity
import homes.snaix.app.yank.data.db.HistoryDao
import homes.snaix.app.yank.data.db.HistoryEntity
import homes.snaix.app.yank.domain.schema.Recognition
import homes.snaix.app.yank.domain.schema.RecognitionJson
import homes.snaix.app.yank.domain.schema.displayPrimary
import homes.snaix.app.yank.domain.schema.displaySecondary
import homes.snaix.app.yank.domain.schema.rawTextBlob
import homes.snaix.app.yank.domain.schema.withPrimary
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString

class HistoryRepository(
    private val historyDao: HistoryDao,
    private val dedupDao: DedupDao,
) {
    suspend fun upsert(entity: HistoryEntity) = historyDao.upsert(entity)
    suspend fun get(id: String): HistoryEntity? = historyDao.getById(id)
    suspend fun setArchived(id: String) = historyDao.setArchived(id)
    suspend fun delete(id: String) = historyDao.deleteById(id)
    suspend fun deleteArchivedBefore(before: Long): Int = historyDao.deleteArchivedBefore(before)

    suspend fun updatePrimary(entity: HistoryEntity, newPrimary: String) {
        val r = RecognitionJson.decodeFromString<Recognition>(entity.rawJson).withPrimary(newPrimary)
        upsert(entity.copy(
            displayPrimary = r.displayPrimary(),
            displaySecondary = r.displaySecondary(),
            rawJson = RecognitionJson.encodeToString(r),
            rawText = r.rawTextBlob(),
        ))
    }

    suspend fun updateNote(
        entity: HistoryEntity,
        title: String?,
        body: String,
        date: String?,
        time: String?,
    ) {
        val r = Recognition.Note(
            title = title?.takeIf { it.isNotBlank() },
            body = body,
            date = date?.takeIf { it.isNotBlank() },
            time = time?.takeIf { it.isNotBlank() },
        )
        upsert(entity.copy(
            displayPrimary = r.displayPrimary(),
            displaySecondary = r.displaySecondary(),
            rawJson = RecognitionJson.encodeToString<Recognition>(r),
            rawText = r.rawTextBlob(),
        ))
    }

    fun observeRecords(typeFilter: String?, query: String?): Flow<List<HistoryEntity>> =
        historyDao.observeActivePinHistory(typeFilter, query)
    fun observeNotes(query: String?): Flow<List<HistoryEntity>> = historyDao.observeNotes(query)
    fun observeArchived(query: String?): Flow<List<HistoryEntity>> = historyDao.observeArchived(query)
    fun observeUpcoming(now: Long): Flow<List<HistoryEntity>> = historyDao.observeUpcoming(now)
    fun observeAnyRecord(): Flow<Boolean> = historyDao.observeAnyRecord()

    suspend fun findFreshDedup(key: String, since: Long): DedupEntity? =
        dedupDao.findFreshActive(key, since)
    suspend fun findDedupByHistoryId(historyId: String): DedupEntity? =
        dedupDao.findByHistoryId(historyId)
    suspend fun insertDedup(entry: DedupEntity) = dedupDao.insert(entry)
    suspend fun touchDedup(key: String, now: Long) = dedupDao.touch(key, now)
}
