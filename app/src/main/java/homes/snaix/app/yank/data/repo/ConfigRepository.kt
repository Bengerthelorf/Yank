package homes.snaix.app.yank.data.repo

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
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
    private val context: Context,
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

    /**
     * UI locale tag. "system" → follow system locale; otherwise a BCP-47 tag like "zh-CN" or "en".
     * Stored separately from system per-app locale because we want it to survive AppCompat's
     * round-trip and re-apply on cold start (the per-app locale survives, but reading the
     * "system" sentinel value back lets the picker UI restore its selection).
     */
    fun localeTag(): Flow<String> = store.stringFlow(Keys.LOCALE, "system")

    suspend fun setLocaleTag(tag: String) {
        store.setString(Keys.LOCALE, tag)
        applyLocaleTag(context, tag)
    }

    companion object {
        /**
         * Apply the locale tag. On API 33+ uses the system [LocaleManager] (which is what
         * triggers the platform's Activity recreation); falls back to AppCompat's backport
         * on older devices. minSdk = 29.
         */
        fun applyLocaleTag(context: Context, tag: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val lm = context.getSystemService(LocaleManager::class.java)
                lm?.applicationLocales = if (tag == "system") {
                    LocaleList.getEmptyLocaleList()
                } else {
                    LocaleList.forLanguageTags(tag)
                }
            } else {
                val locales = if (tag == "system") {
                    LocaleListCompat.getEmptyLocaleList()
                } else {
                    LocaleListCompat.forLanguageTags(tag)
                }
                AppCompatDelegate.setApplicationLocales(locales)
            }
        }
    }
}
