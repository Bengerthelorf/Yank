package homes.snaix.app.yank.domain.routing

sealed class ResultEvent {
    data class PinPublished(val historyId: String, val notificationId: Int) : ResultEvent()
    data class TodoScheduled(val historyId: String, val pinTime: Long) : ResultEvent()
    data class TodoArchivedExpired(val historyId: String) : ResultEvent()
    data class NoteSaved(val historyId: String) : ResultEvent()
    data class DedupReplaced(val historyId: String, val notificationId: Int) : ResultEvent()
}
