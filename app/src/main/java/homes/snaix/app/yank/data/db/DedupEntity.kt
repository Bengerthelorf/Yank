package homes.snaix.app.yank.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dedup")
data class DedupEntity(
    @PrimaryKey val key: String,
    val notificationId: Int,
    val historyId: String,
    val createdAt: Long,
)
