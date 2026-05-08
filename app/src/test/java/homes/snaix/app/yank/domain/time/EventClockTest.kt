package homes.snaix.app.yank.domain.time

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class EventClockTest {
    private val zone = ZoneId.of("Asia/Shanghai")
    private val clock = EventClock(zone)

    @Test fun parses_date_time_pair() {
        val ms = clock.toEpochMillis("2026-05-09", "14:30")!!
        val ldt = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(ms), zone)
        assertThat(ldt.year).isEqualTo(2026)
        assertThat(ldt.hour).isEqualTo(14)
    }

    @Test fun null_when_either_field_missing() {
        assertThat(clock.toEpochMillis(null, "14:30")).isNull()
        assertThat(clock.toEpochMillis("2026-05-09", null)).isNull()
    }

    @Test fun null_when_malformed() {
        assertThat(clock.toEpochMillis("not-a-date", "14:30")).isNull()
        assertThat(clock.toEpochMillis("2026-05-09", "25:00")).isNull()
    }

    @Test fun pinTime_subtracts_lead_minutes() {
        val event = clock.toEpochMillis("2026-05-09", "14:30")!!
        val pin = clock.pinTime(event, leadMinutes = 30)
        assertThat(event - pin).isEqualTo(30L * 60 * 1000)
    }
}
