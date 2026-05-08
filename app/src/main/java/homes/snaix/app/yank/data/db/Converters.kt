package homes.snaix.app.yank.data.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun sourceToString(s: Source): String = s.name
    @TypeConverter fun stringToSource(s: String): Source = Source.valueOf(s)
}
