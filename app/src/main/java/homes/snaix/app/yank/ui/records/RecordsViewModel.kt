package homes.snaix.app.yank.ui.records

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import homes.snaix.app.yank.R
import homes.snaix.app.yank.data.db.HistoryEntity
import homes.snaix.app.yank.data.repo.HistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/**
 * Sealed model of the visible record filter on the Records screen.
 *
 * - [All]: no filter, all active (non-archived) records show.
 * - [ByType]: a single type filter; the [RecordType] supplies discriminator
 *   and label without RecordFilter ever knowing the discriminator string.
 * - [Archived]: orthogonal "trash bin" view.
 */
sealed interface RecordFilter {
    @get:StringRes val labelRes: Int

    data object All : RecordFilter {
        override val labelRes: Int = R.string.filter_all
    }

    data class ByType(val type: RecordType) : RecordFilter {
        override val labelRes: Int get() = type.labelRes
    }

    data object Archived : RecordFilter {
        override val labelRes: Int = R.string.filter_archived
    }
}

class RecordsViewModel(
    private val repo: HistoryRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow<RecordFilter>(RecordFilter.All)
    val filter: StateFlow<RecordFilter> = _filter.asStateFlow()

    private val _query = MutableStateFlow<String?>(null)
    val query: StateFlow<String?> = _query.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<HistoryEntity>> =
        combine(_filter, _query) { f, q -> f to q }
            .flatMapLatest { (f, q) ->
                when (f) {
                    RecordFilter.All        -> repo.observeRecords(null, q)
                    is RecordFilter.ByType  -> repo.observeRecords(f.type.discriminator, q)
                    RecordFilter.Archived   -> repo.observeArchived(q)
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Whether the database has any non-notes record at all, archived or not.
     * The screen uses this to distinguish the genuine "nothing recorded yet"
     * empty state from "the current filter matched nothing". Includes
     * archived rows so a fully-archived DB still keeps the chip row (and
     * therefore the Archived chip) reachable.
     */
    val hasAnyData: StateFlow<Boolean> =
        repo.observeAnyRecord()
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setFilter(f: RecordFilter) { _filter.value = f }
    fun setQuery(q: String?) { _query.value = q?.takeIf { it.isNotBlank() } }
}
