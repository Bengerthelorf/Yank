package homes.snaix.app.yank.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("yank_config")

class ConfigStore(private val context: Context) {

    object Keys {
        val BASE_URL = stringPreferencesKey("base_url")
        val MODEL = stringPreferencesKey("model")
        val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")
        val SCREENSHOT_RETENTION = booleanPreferencesKey("screenshot_retention")
        val LOCK_HIDE = booleanPreferencesKey("lock_hide")
        val ARCHIVE_RETENTION_DAYS = intPreferencesKey("archive_retention_days")
    }

    fun stringFlow(key: androidx.datastore.preferences.core.Preferences.Key<String>, default: String): Flow<String> =
        context.dataStore.data.map { it[key] ?: default }

    fun boolFlow(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, default: Boolean): Flow<Boolean> =
        context.dataStore.data.map { it[key] ?: default }

    fun intFlow(key: androidx.datastore.preferences.core.Preferences.Key<Int>, default: Int): Flow<Int> =
        context.dataStore.data.map { it[key] ?: default }

    suspend fun setString(key: androidx.datastore.preferences.core.Preferences.Key<String>, value: String) {
        context.dataStore.edit { it[key] = value }
    }
    suspend fun setBool(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { it[key] = value }
    }
    suspend fun setInt(key: androidx.datastore.preferences.core.Preferences.Key<Int>, value: Int) {
        context.dataStore.edit { it[key] = value }
    }
}
