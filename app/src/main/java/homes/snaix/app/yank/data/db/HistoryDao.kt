package homes.snaix.app.yank.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Upsert suspend fun upsert(entity: HistoryEntity)

    @Query("SELECT * FROM history WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): HistoryEntity?

    @Query("""
        SELECT * FROM history
        WHERE archived = 0
          AND type != 'notes'
          AND (:typeFilter IS NULL OR type = :typeFilter)
          AND (:query IS NULL OR rawText LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun observeActivePinHistory(typeFilter: String?, query: String?): Flow<List<HistoryEntity>>

    @Query("""
        SELECT * FROM history
        WHERE type = 'notes'
          AND (:query IS NULL OR rawText LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun observeNotes(query: String?): Flow<List<HistoryEntity>>

    @Query("""
        SELECT * FROM history
        WHERE archived = 1
          AND (:query IS NULL OR rawText LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun observeArchived(query: String?): Flow<List<HistoryEntity>>

    @Query("""
        SELECT * FROM history
        WHERE archived = 0
          AND eventTime IS NOT NULL
          AND eventTime > :now
        ORDER BY eventTime ASC
    """)
    fun observeUpcoming(now: Long): Flow<List<HistoryEntity>>

    /**
     * Whether the user has ever recorded *any* non-notes entry, regardless of
     * archive state. The Records screen uses this to suppress chip rendering
     * only on a truly pristine database — once anything has been recorded
     * (active or archived) the chips must stay reachable so the Archived
     * filter is never a dead end.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM history WHERE type != 'notes')")
    fun observeAnyRecord(): Flow<Boolean>

    @Query("UPDATE history SET archived = 1 WHERE id = :id")
    suspend fun setArchived(id: String)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM history WHERE archived = 1 AND archiveAt < :before")
    suspend fun deleteArchivedBefore(before: Long): Int
}
