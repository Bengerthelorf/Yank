package homes.snaix.app.yank.ui.common

import android.content.ClipData
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import homes.snaix.app.yank.data.db.HistoryEntity
import kotlinx.coroutines.launch

/**
 * Returns a `(HistoryEntity) -> Unit` callback that writes the entity's
 * primary display text into the system clipboard. On Android 13+ the
 * platform itself surfaces a "copied to clipboard" confirmation, so callers
 * don't need to render any in-app toast or snackbar.
 *
 * Centralizing this here keeps every record-list screen (Records, Reminders,
 * future detail view) in lockstep on (a) which field is copied and (b) which
 * clipboard primitive is used. Change the policy once, here, and every
 * surface picks it up.
 */
@Composable
fun rememberCopyEntity(): (HistoryEntity) -> Unit {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    return remember(clipboard, scope) {
        { entity ->
            scope.launch {
                val data = ClipData.newPlainText("Yank", entity.displayPrimary)
                clipboard.setClipEntry(ClipEntry(data))
            }
        }
    }
}
