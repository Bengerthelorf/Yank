package homes.snaix.app.yank.domain.routing

import com.google.common.truth.Truth.assertThat
import homes.snaix.app.yank.domain.schema.Recognition
import org.junit.Test

class DedupKeyTest {
    @Test fun pickup_key_uses_brand_and_number() {
        val r = Recognition.Pickup(number = "A123", brand = "瑞幸", store = "x")
        assertThat(dedupKey(r)).isEqualTo("取餐|瑞幸|A123")
    }

    @Test fun train_key_uses_trainNo_and_date() {
        val r = Recognition.Ticket(trainNo = "G123", trainDate = "2026-05-15")
        assertThat(dedupKey(r)).isEqualTo("票券|火车|G123|2026-05-15")
    }

    @Test fun flight_key_branch() {
        val r = Recognition.Ticket(flightNo = "CA1234", departDate = "2026-05-15")
        assertThat(dedupKey(r)).isEqualTo("票券|航班|CA1234|2026-05-15")
    }

    @Test fun movie_key_branch() {
        val r = Recognition.Ticket(movie = "信条", date = "2026-05-15", time = "20:00")
        assertThat(dedupKey(r)).isEqualTo("票券|电影|信条|2026-05-15|20:00")
    }

    @Test fun todo_key_uses_title_date_time() {
        val r = Recognition.Todo(title = "周会", date = "2026-05-09", time = "14:00")
        assertThat(dedupKey(r)).isEqualTo("待办|周会|2026-05-09|14:00")
    }

    @Test fun note_key_is_unique_uuid_per_call() {
        val r = Recognition.Note(number = "x")
        val k1 = dedupKey(r); val k2 = dedupKey(r)
        assertThat(k1).isNotEqualTo(k2)
    }
}
