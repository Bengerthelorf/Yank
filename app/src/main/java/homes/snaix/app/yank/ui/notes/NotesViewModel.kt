package homes.snaix.app.yank.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import homes.snaix.app.yank.data.db.HistoryEntity
import homes.snaix.app.yank.data.db.Source
import homes.snaix.app.yank.data.repo.HistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class NotesViewModel(
    private val repo: HistoryRepository,
) : ViewModel() {

    private val _query = MutableStateFlow<String?>(null)
    val query: StateFlow<String?> = _query.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<HistoryEntity>> =
        _query.flatMapLatest { repo.observeNotes(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setQuery(q: String?) { _query.value = q?.takeIf { it.isNotBlank() } }

    fun saveManualNote(title: String?, body: String, date: String?, time: String?) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val recognition: homes.snaix.app.yank.domain.schema.Recognition =
                homes.snaix.app.yank.domain.schema.Recognition.Note(
                    title = title?.takeIf { it.isNotBlank() },
                    number = body,
                    date = date,
                    time = time,
                )
            val entity = HistoryEntity(
                id = UUID.randomUUID().toString(),
                type = "notes",
                displayPrimary = (recognition as homes.snaix.app.yank.domain.schema.Recognition.Note).title ?: body.take(20),
                displaySecondary = listOfNotNull(date, time).joinToString(" ").ifEmpty { null },
                rawText = listOfNotNull(title, body).joinToString(" "),
                rawJson = Json.encodeToString(recognition),
                zxingPayloads = null,
                screenshotPath = null,
                createdAt = now,
                eventTime = null,
                archiveAt = now + Long.MAX_VALUE / 2,
                archived = false,
                source = Source.MANUAL,
            )
            repo.upsert(entity)
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repo.delete(id) }
    }
}
