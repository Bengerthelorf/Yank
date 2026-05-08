package homes.snaix.app.yank.domain.vlm

import homes.snaix.app.yank.domain.schema.Recognition
import homes.snaix.app.yank.domain.schema.RecognitionParser
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.UnknownHostException
import java.util.Base64
import kotlin.time.Duration.Companion.seconds

private val ResponseJson = Json { ignoreUnknownKeys = true }

class VlmException(val error: VlmError) : RuntimeException(error.toString())

class VlmClient(
    private val http: HttpClient = defaultHttpClient(),
    private val parser: RecognitionParser = RecognitionParser(),
) {
    suspend fun recognize(
        cfg: VlmConfig,
        screenshotPng: ByteArray,
        zxingPayloads: List<String>,
    ): Result<List<Recognition>> {
        val key = cfg.apiKey.takeIf { it.isNotBlank() }
            ?: return Result.failure(VlmException(VlmError.NoApiKey))

        val dataUrl = "data:image/png;base64,${Base64.getEncoder().encodeToString(screenshotPng)}"

        val userTextPart = buildString {
            append("识别这张截图。")
            if (zxingPayloads.isNotEmpty()) {
                append("\n本地解析到的 QR/Barcode 内容：")
                zxingPayloads.forEach { append("\n- ").append(it) }
            }
        }

        val req = ChatRequest(
            model = cfg.model,
            messages = listOf(
                ChatMessage("system", listOf(MessageContent.TextPart(cfg.systemPrompt))),
                ChatMessage("user", listOf(
                    MessageContent.ImagePart(ImageUrl(dataUrl)),
                    MessageContent.TextPart(userTextPart),
                )),
            ),
        )

        val resp: HttpResponse = try {
            http.post("${cfg.baseUrl.trimEnd('/')}/chat/completions") {
                contentType(ContentType.Application.Json)
                headers { append("Authorization", "Bearer $key") }
                setBody(req)
            }
        } catch (e: IOException) {
            val err = if (e is UnknownHostException) VlmError.NoNetwork else VlmError.Timeout
            return Result.failure(VlmException(err))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.failure(VlmException(VlmError.Http(-1, e.message)))
        }

        if (!resp.status.isSuccess()) {
            val body = runCatching { resp.bodyAsText() }.getOrNull()
            return Result.failure(VlmException(VlmError.Http(resp.status.value, body)))
        }

        val rawBody = resp.bodyAsText()
        val parsed: ChatResponse = try {
            ResponseJson.decodeFromString(rawBody)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.failure(VlmException(VlmError.Parse(rawBody, e)))
        }

        val content = parsed.choices.firstOrNull()?.message?.content
            ?: return Result.failure(VlmException(VlmError.Parse("(no content)", IllegalStateException("empty choices"))))

        return parser.parse(content).recoverCatching {
            throw VlmException(VlmError.Parse(content, it))
        }
    }

    companion object {
        fun defaultHttpClient(): HttpClient = HttpClient {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            install(HttpTimeout) {
                requestTimeoutMillis = 30.seconds.inWholeMilliseconds
                connectTimeoutMillis = 10.seconds.inWholeMilliseconds
                socketTimeoutMillis = 30.seconds.inWholeMilliseconds
            }
            install(Logging) { level = LogLevel.NONE }
        }
    }
}
