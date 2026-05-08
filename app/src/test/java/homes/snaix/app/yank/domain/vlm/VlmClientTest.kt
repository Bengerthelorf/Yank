package homes.snaix.app.yank.domain.vlm

import com.google.common.truth.Truth.assertThat
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.HttpHeaders
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test

class VlmClientTest {
    private val cfg = VlmConfig(
        baseUrl = "https://example.test/v1",
        apiKey = "key123",
        model = "qwen3-vl-plus",
        systemPrompt = "你是助手",
    )

    private fun client(handler: io.ktor.client.engine.mock.MockRequestHandleScope.(io.ktor.client.request.HttpRequestData) -> io.ktor.client.request.HttpResponseData) = HttpClient(MockEngine(handler)) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    @Test fun returns_parsed_recognitions_on_200_with_array_content() = runTest {
        val responseBody = """{"choices":[{"index":0,"message":{"role":"assistant","content":"[{\"type\":\"取餐\",\"number\":\"A123\"}]"},"finish_reason":"stop"}]}"""
        val http = client { respond(content = ByteReadChannel(responseBody), status = HttpStatusCode.OK, headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())) }
        val client = VlmClient(http)
        val result = client.recognize(cfg, screenshotPng = byteArrayOf(0x1, 0x2), zxingPayloads = emptyList())
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).hasSize(1)
    }

    @Test fun returns_Http_401_on_unauthorized() = runTest {
        val http = client { respond("unauthorized", HttpStatusCode.Unauthorized) }
        val client = VlmClient(http)
        val res = client.recognize(cfg, byteArrayOf(), emptyList())
        assertThat(res.exceptionOrNull()).isInstanceOf(VlmException::class.java)
        val err = (res.exceptionOrNull() as VlmException).error
        assertThat(err).isInstanceOf(VlmError.Http::class.java)
        assertThat((err as VlmError.Http).code).isEqualTo(401)
    }

    @Test fun returns_Parse_when_content_is_not_array() = runTest {
        val responseBody = """{"choices":[{"index":0,"message":{"role":"assistant","content":"{\"type\":\"取餐\",\"number\":\"A123\"}"}}]}"""
        val http = client { respond(content = ByteReadChannel(responseBody), status = HttpStatusCode.OK, headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())) }
        val client = VlmClient(http)
        val res = client.recognize(cfg, byteArrayOf(), emptyList())
        assertThat((res.exceptionOrNull() as VlmException).error).isInstanceOf(VlmError.Parse::class.java)
    }
}
