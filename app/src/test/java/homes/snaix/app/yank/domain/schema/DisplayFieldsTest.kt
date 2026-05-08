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

    @Test fun `note primary is title or number prefix`() {
        val r1 = Recognition.Note(title = "标题", number = "正文")
        val r2 = Recognition.Note(number = "只有正文这是个长文本")
        assertThat(r1.displayPrimary()).isEqualTo("标题")
        assertThat(r2.displayPrimary()).startsWith("只有正文")
    }
}
