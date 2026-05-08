package homes.snaix.app.yank.domain.vlm

sealed class VlmError {
    data object NoNetwork : VlmError()
    data object NoApiKey : VlmError()
    data object Timeout : VlmError()
    data class Http(val code: Int, val body: String?) : VlmError()
    data class Parse(val raw: String, val cause: Throwable) : VlmError()
}
