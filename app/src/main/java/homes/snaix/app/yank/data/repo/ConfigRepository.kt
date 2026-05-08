package homes.snaix.app.yank.data.repo

import homes.snaix.app.yank.Defaults
import homes.snaix.app.yank.data.prefs.ConfigStore
import homes.snaix.app.yank.data.prefs.ConfigStore.Keys
import homes.snaix.app.yank.data.prefs.EncryptedKeyStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

data class VlmConfigSnapshot(
    val baseUrl: String,
    val model: String,
    val apiKey: String?,
    val systemPrompt: String,
)

class ConfigRepository(
    private val store: ConfigStore,
    private val keyStore: EncryptedKeyStore,
) {
    fun observeVlmConfig(): Flow<VlmConfigSnapshot> = combine(
        store.stringFlow(Keys.BASE_URL, Defaults.BASE_URL),
        store.stringFlow(Keys.MODEL, Defaults.MODEL),
        store.stringFlow(Keys.SYSTEM_PROMPT, Defaults.SYSTEM_PROMPT),
    ) { base, model, prompt ->
        VlmConfigSnapshot(base, model, keyStore.getApiKey(), prompt)
    }

    suspend fun snapshot(): VlmConfigSnapshot = observeVlmConfig().first()

    fun screenshotRetention(): Flow<Boolean> =
        store.boolFlow(Keys.SCREENSHOT_RETENTION, Defaults.SCREENSHOT_RETENTION_DEFAULT)

    fun lockHide(): Flow<Boolean> =
        store.boolFlow(Keys.LOCK_HIDE, Defaults.LOCK_HIDE_DEFAULT)

    fun archiveRetentionDays(): Flow<Int> =
        store.intFlow(Keys.ARCHIVE_RETENTION_DAYS, Defaults.ARCHIVE_RETENTION_DAYS_DEFAULT)

    suspend fun setBaseUrl(value: String) = store.setString(Keys.BASE_URL, value)
    suspend fun setModel(value: String) = store.setString(Keys.MODEL, value)
    suspend fun setSystemPrompt(value: String) = store.setString(Keys.SYSTEM_PROMPT, value)
    suspend fun resetSystemPrompt() = store.setString(Keys.SYSTEM_PROMPT, Defaults.SYSTEM_PROMPT)
    suspend fun setScreenshotRetention(b: Boolean) = store.setBool(Keys.SCREENSHOT_RETENTION, b)
    suspend fun setLockHide(b: Boolean) = store.setBool(Keys.LOCK_HIDE, b)
    suspend fun setArchiveRetentionDays(d: Int) = store.setInt(Keys.ARCHIVE_RETENTION_DAYS, d)

    fun setApiKey(value: String?) = keyStore.setApiKey(value)
}
