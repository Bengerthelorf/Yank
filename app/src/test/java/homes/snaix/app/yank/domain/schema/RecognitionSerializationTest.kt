// app/src/test/java/homes/snaix/app/yank/domain/schema/RecognitionSerializationTest.kt
package homes.snaix.app.yank.domain.schema

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class RecognitionSerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun `parses 取餐 object`() {
        val raw = """{"type":"取餐","number":"A123","brand":"瑞幸","store":"街道口店","product":"生椰拿铁","price":"15"}"""
        val r = json.decodeFromString<Recognition>(raw)
        assertThat(r).isInstanceOf(Recognition.Pickup::class.java)
        val p = r as Recognition.Pickup
        assertThat(p.number).isEqualTo("A123")
        assertThat(p.brand).isEqualTo("瑞幸")
    }

    @Test fun `parses 待办 with required title and time`() {
        val raw = """{"type":"待办","title":"项目周会","date":"2026-05-09","time":"14:00","pinLeadMinutes":30}"""
        val r = json.decodeFromString<Recognition>(raw) as Recognition.Todo
        assertThat(r.title).isEqualTo("项目周会")
        assertThat(r.pinLeadMinutes).isEqualTo(30)
    }

    @Test fun `parses 票券 火车 fields`() {
        val raw = """{"type":"票券","trainNo":"G123","fromStation":"北京","toStation":"上海","trainDate":"2026-05-15","trainDepartTime":"08:00"}"""
        val r = json.decodeFromString<Recognition>(raw) as Recognition.Ticket
        assertThat(r.trainNo).isEqualTo("G123")
        assertThat(r.flightNo).isNull()
    }

    @Test fun `parses notes with optional fields (current body wire name)`() {
        val raw = """{"type":"notes","title":"abc","body":"summary"}"""
        val r = json.decodeFromString<Recognition>(raw) as Recognition.Note
        assertThat(r.body).isEqualTo("summary")
    }

    @Test fun `parses notes with legacy number wire name (backward compat alias)`() {
        val raw = """{"type":"notes","title":"abc","number":"summary"}"""
        val r = json.decodeFromString<Recognition>(raw) as Recognition.Note
        assertThat(r.body).isEqualTo("summary")
    }

    @Test fun `decodes 排队 券码 快递`() {
        val q = json.decodeFromString<Recognition>("""{"type":"排队","number":"A01"}""")
        val v = json.decodeFromString<Recognition>("""{"type":"券码","number":"周末早茶"}""")
        val e = json.decodeFromString<Recognition>("""{"type":"快递","number":"1-2-3456"}""")
        assertThat(q).isInstanceOf(Recognition.Queue::class.java)
        assertThat(v).isInstanceOf(Recognition.Voucher::class.java)
        assertThat(e).isInstanceOf(Recognition.Express::class.java)
    }
}
