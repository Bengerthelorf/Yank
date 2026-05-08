package homes.snaix.app.yank.domain.vlm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.0,
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: List<MessageContent>,
)

@Serializable
sealed class MessageContent {
    @Serializable @SerialName("text")
    data class TextPart(val text: String) : MessageContent()

    @Serializable @SerialName("image_url")
    data class ImagePart(val image_url: ImageUrl) : MessageContent()
}

@Serializable
data class ImageUrl(val url: String)
