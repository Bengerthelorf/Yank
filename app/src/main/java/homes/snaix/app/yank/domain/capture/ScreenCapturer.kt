package homes.snaix.app.yank.domain.capture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.view.WindowManager
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class ScreenCapturer(private val context: Context) {

    suspend fun captureOnce(projection: MediaProjection): Bitmap = suspendCancellableCoroutine { cont ->
        val (width, height) = screenSize()
        val density = context.resources.displayMetrics.densityDpi

        val handlerThread = HandlerThread("ScreenCapturer-${System.nanoTime()}").also { it.start() }
        val handler = Handler(handlerThread.looper)

        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        var virtualDisplay: VirtualDisplay? = null
        val cleaned = AtomicBoolean(false)

        // Idempotent. Concurrent listener-fire and coroutine-cancellation can both call this.
        fun cleanup() {
            if (!cleaned.compareAndSet(false, true)) return
            try { virtualDisplay?.release() } catch (_: Exception) {}
            try { reader.close() } catch (_: Exception) {}
            handlerThread.quitSafely()
        }

        reader.setOnImageAvailableListener({ r ->
            val img = r.acquireLatestImage() ?: return@setOnImageAvailableListener
            try {
                // Build the bitmap with image still open; close image FIRST so its buffer slot
                // is returned before we tear down the ImageReader in cleanup().
                val cropped: Bitmap = try {
                    val plane = img.planes[0]
                    val rowStride = plane.rowStride
                    val pixelStride = plane.pixelStride
                    val rowPadding = rowStride - pixelStride * width
                    val bitmap = Bitmap.createBitmap(
                        width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888
                    )
                    bitmap.copyPixelsFromBuffer(plane.buffer)
                    if (rowPadding == 0) bitmap
                    else Bitmap.createBitmap(bitmap, 0, 0, width, height).also { bitmap.recycle() }
                } finally {
                    img.close()
                }
                cleanup()
                cont.resume(cropped)
            } catch (e: Exception) {
                cleanup()
                cont.resumeWithException(e)
            }
        }, handler)

        try {
            virtualDisplay = projection.createVirtualDisplay(
                "yank_capture",
                width, height, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                reader.surface, null, handler,
            )
        } catch (e: Exception) {
            cleanup()
            cont.resumeWithException(e)
        }

        cont.invokeOnCancellation { cleanup() }
    }

    private fun screenSize(): Pair<Int, Int> {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = wm.maximumWindowMetrics
            metrics.bounds.width() to metrics.bounds.height()
        } else {
            val dm = DisplayMetrics()
            @Suppress("DEPRECATION") wm.defaultDisplay.getRealMetrics(dm)
            dm.widthPixels to dm.heightPixels
        }
    }
}
