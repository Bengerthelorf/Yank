package homes.snaix.app.yank.domain.schema

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RecognitionParserTest {
    private val parser = RecognitionParser()

    @Test fun `parses array with single item`() {
        val raw = """[{"type":"取餐","number":"A123"}]"""
        val r = parser.parse(raw).getOrThrow()
        assertThat(r).hasSize(1)
        assertThat(r[0]).isInstanceOf(Recognition.Pickup::class.java)
    }

    @Test fun `parses array with multiple items`() {
        val raw = """[{"type":"待办","title":"a","date":"2026-05-09","time":"10:00"},
                       {"type":"待办","title":"b","date":"2026-05-09","time":"14:00"}]"""
        val r = parser.parse(raw).getOrThrow()
        assertThat(r).hasSize(2)
    }

    @Test fun `strips markdown code fences`() {
        val raw = """```json
[{"type":"notes","number":"hi"}]
```"""
        val r = parser.parse(raw).getOrThrow()
        assertThat(r).hasSize(1)
    }

    @Test fun `fails when top-level is not array`() {
        val raw = """{"type":"取餐","number":"A123"}"""
        val res = parser.parse(raw)
        assertThat(res.isFailure).isTrue()
    }

    @Test fun `fails when type is not in closed enum`() {
        val raw = """[{"type":"unknown_type","number":"x"}]"""
        val res = parser.parse(raw)
        assertThat(res.isFailure).isTrue()
    }

    @Test fun `fails on malformed json`() {
        val res = parser.parse("not json at all")
        assertThat(res.isFailure).isTrue()
    }
}
