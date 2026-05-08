package homes.snaix.app.yank.domain.vlm

import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val id: String? = null,
    val choices: List<Choice> = emptyList(),
)

@Serializable
data class Choice(
    val index: Int = 0,
    val message: ResponseMessage,
    val finish_reason: String? = null,
)

@Serializable
data class ResponseMessage(
    val role: String,
    val content: String,
)
