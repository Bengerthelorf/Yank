package homes.snaix.app.yank.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DedupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DedupEntity)

    @Query("UPDATE dedup SET createdAt = :now WHERE `key` = :key")
    suspend fun touch(key: String, now: Long)

    @Query("""
        SELECT d.* FROM dedup d
        INNER JOIN history h ON h.id = d.historyId
        WHERE d.`key` = :key AND d.createdAt >= :since AND h.archived = 0
        LIMIT 1
    """)
    suspend fun findFreshActive(key: String, since: Long): DedupEntity?
}
