package homes.snaix.app.yank.domain.time

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class EventClock(private val zone: ZoneId = ZoneId.systemDefault()) {

    fun toEpochMillis(date: String?, time: String?): Long? {
        if (date.isNullOrBlank() || time.isNullOrBlank()) return null
        return runCatching {
            val d = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val t = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
            d.atTime(t).atZone(zone).toInstant().toEpochMilli()
        }.getOrNull()
    }

    fun pinTime(eventEpochMs: Long, leadMinutes: Int): Long =
        eventEpochMs - leadMinutes * 60_000L
}
