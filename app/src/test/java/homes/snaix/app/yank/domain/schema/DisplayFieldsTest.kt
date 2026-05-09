package homes.snaix.app.yank.domain.schema

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DisplayFieldsTest {
    @Test fun `pickup primary is number, secondary is brand`() {
        val r = Recognition.Pickup(number = "A123", brand = "瑞幸", store = "街道口店")
        assertThat(r.displayPrimary()).isEqualTo("A123")
        assertThat(r.displaySecondary()).isEqualTo("瑞幸 · 街道口店")
    }

    @Test fun `train ticket primary is trainNo, secondary is route`() {
        val r = Recognition.Ticket(trainNo = "G123", fromStation = "北京", toStation = "上海")
        assertThat(r.displayPrimary()).isEqualTo("G123")
        assertThat(r.displaySecondary()).isEqualTo("北京 → 上海")
    }

    @Test fun `note primary is title or first body line prefix`() {
        val r1 = Recognition.Note(title = "标题", body = "正文")
        val r2 = Recognition.Note(body = "只有正文这是个长文本")
        assertThat(r1.displayPrimary()).isEqualTo("标题")
        assertThat(r2.displayPrimary()).startsWith("只有正文")
    }

    @Test fun `note secondary previews body when title is set`() {
        val r = Recognition.Note(title = "M3E 设计", body = "总结这张截图的关键信息和上下文。")
        assertThat(r.displaySecondary()).isEqualTo("总结这张截图的关键信息和上下文。")
    }
}
