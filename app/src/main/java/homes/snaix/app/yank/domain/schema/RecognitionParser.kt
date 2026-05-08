// app/src/main/java/homes/snaix/app/yank/domain/schema/RecognitionParser.kt
package homes.snaix.app.yank.domain.schema

import kotlinx.serialization.json.Json

class RecognitionParser(
    private val json: Json = Json {
        ignoreUnknownKeys = true
    }
) {
    fun parse(raw: String): Result<List<Recognition>> = runCatching {
        val cleaned = stripCodeFences(raw).trim()
        require(cleaned.startsWith("[")) { "Top-level must be JSON array" }
        json.decodeFromString<List<Recognition>>(cleaned)
    }

    private fun stripCodeFences(raw: String): String {
        val fenceRegex = Regex("^```(?:json)?\\s*\\n([\\s\\S]*?)\\n```\\s*$", RegexOption.MULTILINE)
        return fenceRegex.find(raw.trim())?.groupValues?.get(1) ?: raw
    }
}
