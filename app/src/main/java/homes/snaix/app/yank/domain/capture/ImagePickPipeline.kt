package homes.snaix.app.yank.domain.capture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import homes.snaix.app.yank.data.db.Source

class ImagePickPipeline(
    private val context: Context,
    private val capture: CapturePipeline,
) {
    suspend fun process(uri: Uri): PipelineOutcome {
        val raw = readBitmap(uri) ?: return PipelineOutcome.TransportFailure(
            homes.snaix.app.yank.domain.vlm.VlmError.Http(-1, "image unreadable")
        )
        val downscaled = downscaleIfNeeded(raw, longEdgeMax = 1600)
        return capture.process(downscaled, source = Source.IMAGE_PICK)
    }

    private fun readBitmap(uri: Uri): Bitmap? = runCatching {
        context.contentResolver.openInputStream(uri).use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    }.getOrNull()

    private fun downscaleIfNeeded(src: Bitmap, longEdgeMax: Int): Bitmap {
        val long = maxOf(src.width, src.height)
        if (long <= longEdgeMax) return src
        val scale = longEdgeMax.toFloat() / long
        val w = (src.width * scale).toInt()
        val h = (src.height * scale).toInt()
        val out = Bitmap.createScaledBitmap(src, w, h, true)
        if (out !== src) src.recycle()
        return out
    }
}
