package homes.snaix.app.yank.domain.capture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import homes.snaix.app.yank.data.db.Source
import java.io.File

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

    // Direct ImageDecoder fails on some Google Photos cloud URIs that return
    // partial streams. Cache-file path buffers the bytes first as a recovery.
    // ALLOCATOR_SOFTWARE is mandatory: hardware bitmaps fail compress(PNG)
    // and createScaledBitmap downstream.
    private fun decodeBitmap(uri: Uri): Bitmap? {
        runCatching { decodeViaImageDecoder(uri) }
            .onSuccess { return it }
            .onFailure { Log.w(TAG, "ImageDecoder direct path failed for $uri, retrying via cache", it) }

        return runCatching { decodeViaCacheFile(uri) }
            .onFailure { Log.e(TAG, "decodeBitmap fallback failed for $uri", it) }
            .getOrNull()
    }

    private fun decodeViaImageDecoder(uri: Uri): Bitmap {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        return ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }

    private fun decodeViaCacheFile(uri: Uri): Bitmap {
        val tmp = File.createTempFile("imgpick_", ".bin", context.cacheDir)
        try {
            context.contentResolver.openInputStream(uri).use { input ->
                checkNotNull(input) { "openInputStream returned null for $uri" }
                tmp.outputStream().use { input.copyTo(it) }
            }
            check(tmp.length() > 0) { "downloaded $uri is empty (${tmp.length()} bytes)" }
            val source = ImageDecoder.createSource(tmp)
            return ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } finally {
            tmp.delete()
        }
    }

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
