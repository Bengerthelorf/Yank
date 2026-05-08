package homes.snaix.app.yank.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [HistoryEntity::class, DedupEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class YankDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun dedupDao(): DedupDao
}
