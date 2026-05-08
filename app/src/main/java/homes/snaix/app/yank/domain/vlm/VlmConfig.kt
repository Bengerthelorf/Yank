package homes.snaix.app.yank.domain.vlm

data class VlmConfig(
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val systemPrompt: String,
    val timeoutSeconds: Int = 30,
)
