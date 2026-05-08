package homes.snaix.app.yank

object Defaults {
    const val BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1"
    const val MODEL = "qwen3-vl-plus"
    const val TIMEOUT_SECONDS = 30
    const val DEDUP_WINDOW_MS = 30 * 60 * 1000L
    const val SCREENSHOT_RETENTION_DEFAULT = true
    const val LOCK_HIDE_DEFAULT = false
    const val TODO_DEFAULT_LEAD_MINUTES = 60
    const val ARCHIVE_RETENTION_DAYS_DEFAULT = 90

    // System prompt populated in Phase 6 / Task 6.1; placeholder for now
    const val SYSTEM_PROMPT = "[populated in Task 6.1]"
}
