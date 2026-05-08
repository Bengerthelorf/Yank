package homes.snaix.app.yank.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history",
    indices = [Index("type"), Index("createdAt"), Index("archived"), Index("eventTime")]
)
data class HistoryEntity(
    @PrimaryKey val id: String,
    val type: String,
    val displayPrimary: String,
    val displaySecondary: String?,
    val rawText: String,
    val rawJson: String,
    val zxingPayloads: String?,
    val screenshotPath: String?,
    val createdAt: Long,
    val eventTime: Long?,
    val archiveAt: Long,
    val archived: Boolean = false,
    @ColumnInfo(defaultValue = "SCREENSHOT") val source: Source = Source.SCREENSHOT,
    val tags: String = "",
)
