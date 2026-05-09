package homes.snaix.app.yank.ui.common

import android.content.ClipData
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import homes.snaix.app.yank.data.db.HistoryEntity
import homes.snaix.app.yank.domain.schema.Recognition
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * Returns a `(HistoryEntity) -> Unit` callback that writes the entity's most
 * useful text into the system clipboard. On Android 13+ the platform itself
 * surfaces a "copied to clipboard" confirmation, so callers don't need to
 * render any in-app toast or snackbar.
 *
 * The "useful" text depends on type:
 *   - notes  → the full body (the actual summary content), falling back to
 *              title or displayPrimary if rawJson can't be decoded.
 *   - others → displayPrimary (queue/pickup/voucher number, title, etc.).
 *
 * Centralizing this here keeps every record-list screen in lockstep on
 * (a) which field is copied and (b) which clipboard primitive is used.
 * Change the policy once, here, and every surface picks it up.
 */
@Composable
fun rememberCopyEntity(): (HistoryEntity) -> Unit {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    return remember(clipboard, scope) {
        { entity ->
            scope.launch {
                val data = ClipData.newPlainText("Yank", entity.copyableText())
                clipboard.setClipEntry(ClipEntry(data))
            }
        }
    }
}

private val CopyJson = Json { ignoreUnknownKeys = true }

private fun HistoryEntity.copyableText(): String {
    if (type != "notes") return displayPrimary
    return runCatching {
        val note = CopyJson.decodeFromString<Recognition>(rawJson) as? Recognition.Note
        note?.body?.takeIf { it.isNotBlank() }
            ?: note?.title?.takeIf { it.isNotBlank() }
            ?: displayPrimary
    }.getOrDefault(displayPrimary)
}
