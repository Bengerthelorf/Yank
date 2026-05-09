package homes.snaix.app.yank.domain.capture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import homes.snaix.app.yank.data.db.Source

class ImagePickPipeline(
    private val context: Context,
    private val capture: CapturePipeline,
) {
    suspend fun process(uri: Uri): PipelineOutcome {
        val raw = decodeBitmap(uri) ?: return PipelineOutcome.TransportFailure(
            homes.snaix.app.yank.domain.vlm.VlmError.Http(-1, "image unreadable")
        )
        val downscaled = downscaleIfNeeded(raw, longEdgeMax = 1600)
        return capture.process(downscaled, source = Source.IMAGE_PICK)
    }

    private fun decodeBitmap(uri: Uri): Bitmap? = runCatching {
        // ImageDecoder handles modern formats (HEIC, AVIF, animated WebP first
        // frame) and applies EXIF orientation automatically — BitmapFactory.
        // decodeStream silently returns null on many of those.
        // ALLOCATOR_SOFTWARE is required because the bitmap later gets passed
        // to Bitmap.compress(PNG) and createScaledBitmap, both of which fail
        // on hardware-backed bitmaps that ImageDecoder otherwise produces.
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }.onFailure {
        Log.e(TAG, "decodeBitmap failed for $uri", it)
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

    companion object {
        private const val TAG = "ImagePickPipeline"
    }
}
