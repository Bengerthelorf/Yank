package homes.snaix.app.yank.domain.capture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import homes.snaix.app.yank.data.db.Source
import homes.snaix.app.yank.data.repo.ConfigRepository
import homes.snaix.app.yank.domain.routing.ResultEvent
import homes.snaix.app.yank.domain.routing.Router
import homes.snaix.app.yank.domain.schema.Recognition
import homes.snaix.app.yank.domain.vlm.VlmClient
import homes.snaix.app.yank.domain.vlm.VlmConfig
import homes.snaix.app.yank.domain.vlm.VlmException
import homes.snaix.app.yank.domain.zxing.ZxingDecoder
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

sealed class PipelineOutcome {
    data class Success(val events: List<ResultEvent>) : PipelineOutcome()
    data class ParseFailure(val raw: String, val cause: Throwable, val bitmap: Bitmap) : PipelineOutcome()
    data class TransportFailure(val error: homes.snaix.app.yank.domain.vlm.VlmError) : PipelineOutcome()
}

class CapturePipeline(
    private val context: Context,
    private val zxing: ZxingDecoder,
    private val vlm: VlmClient,
    private val router: Router,
    private val configRepo: ConfigRepository,
) {
    suspend fun process(bitmap: Bitmap, source: Source): PipelineOutcome {
        val cfgSnap = configRepo.observeVlmConfig().first()
        val key = cfgSnap.apiKey
            ?: return PipelineOutcome.TransportFailure(homes.snaix.app.yank.domain.vlm.VlmError.NoApiKey)

        val payloads = zxing.decodeAll(bitmap).map { it.text }
        val pngBytes = bitmap.toPng()
        val keepShot = configRepo.screenshotRetention().first()
        val screenshotPath = if (keepShot) writeScreenshot(pngBytes) else null

        val cfg = VlmConfig(cfgSnap.baseUrl, key, cfgSnap.model, cfgSnap.systemPrompt)
        val recognized: List<Recognition> = vlm.recognize(cfg, pngBytes, payloads)
            .getOrElse { ex ->
                val err = (ex as? VlmException)?.error
                    ?: homes.snaix.app.yank.domain.vlm.VlmError.Http(-1, ex.message)
                if (err is homes.snaix.app.yank.domain.vlm.VlmError.Parse) {
                    return PipelineOutcome.ParseFailure(err.raw, err.cause, bitmap)
                }
                return PipelineOutcome.TransportFailure(err)
            }

        val events = router.route(recognized, screenshotPath, payloads, source)
        return PipelineOutcome.Success(events)
    }

    private fun Bitmap.toPng(): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        compress(CompressFormat.PNG, 100, out)
        return out.toByteArray()
    }

    private fun writeScreenshot(bytes: ByteArray): String {
        val dir = File(context.filesDir, "screenshots").apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.png")
        FileOutputStream(file).use { it.write(bytes) }
        return file.absolutePath
    }
}
