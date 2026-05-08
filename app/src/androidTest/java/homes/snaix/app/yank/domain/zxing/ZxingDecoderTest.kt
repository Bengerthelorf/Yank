package homes.snaix.app.yank.domain.zxing

import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ZxingDecoderTest {
    private val decoder = ZxingDecoder()

    @Test fun decodes_qr_from_asset() {
        val ctx = InstrumentationRegistry.getInstrumentation().context
        val stream = ctx.assets.open("qr_test.png")
        val bitmap = BitmapFactory.decodeStream(stream)
        val result = decoder.decodeAll(bitmap)
        assertThat(result.map { it.text }).contains("https://yank.test/abc")
    }

    @Test fun returns_empty_for_blank_image() {
        val bitmap = android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888)
        val result = decoder.decodeAll(bitmap)
        assertThat(result).isEmpty()
    }
}
