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
    val note = CopyJson.decodeFromString<Recognition>(rawJson) as Recognition.Note
    return note.body ?: note.title.orEmpty()
}
