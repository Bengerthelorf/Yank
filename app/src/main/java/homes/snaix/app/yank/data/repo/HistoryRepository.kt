package homes.snaix.app.yank.data.repo

import homes.snaix.app.yank.data.db.DedupDao
import homes.snaix.app.yank.data.db.DedupEntity
import homes.snaix.app.yank.data.db.HistoryDao
import homes.snaix.app.yank.data.db.HistoryEntity
import kotlinx.coroutines.flow.Flow

class HistoryRepository(
    private val historyDao: HistoryDao,
    private val dedupDao: DedupDao,
) {
    suspend fun upsert(entity: HistoryEntity) = historyDao.upsert(entity)
    suspend fun get(id: String): HistoryEntity? = historyDao.getById(id)
    suspend fun setArchived(id: String) = historyDao.setArchived(id)
    suspend fun delete(id: String) = historyDao.deleteById(id)
    suspend fun deleteArchivedBefore(before: Long): Int = historyDao.deleteArchivedBefore(before)

    fun observeRecords(typeFilter: String?, query: String?): Flow<List<HistoryEntity>> =
        historyDao.observeActivePinHistory(typeFilter, query)
    fun observeNotes(query: String?): Flow<List<HistoryEntity>> = historyDao.observeNotes(query)
    fun observeArchived(query: String?): Flow<List<HistoryEntity>> = historyDao.observeArchived(query)
    fun observeUpcoming(now: Long): Flow<List<HistoryEntity>> = historyDao.observeUpcoming(now)
    fun observeAnyRecord(): Flow<Boolean> = historyDao.observeAnyRecord()

    suspend fun findFreshDedup(key: String, since: Long): DedupEntity? =
        dedupDao.findFreshActive(key, since)
    suspend fun insertDedup(entry: DedupEntity) = dedupDao.insert(entry)
    suspend fun touchDedup(key: String, now: Long) = dedupDao.touch(key, now)
}
