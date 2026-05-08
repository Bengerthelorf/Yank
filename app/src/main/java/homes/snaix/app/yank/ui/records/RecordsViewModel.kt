package homes.snaix.app.yank.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

enum class RecordFilter(val type: String?, val labelRes: Int) {
    All(null, homes.snaix.app.yank.R.string.filter_all),
    Queue("排队", homes.snaix.app.yank.R.string.filter_queue),
    Pickup("取餐", homes.snaix.app.yank.R.string.filter_pickup),
    Voucher("券码", homes.snaix.app.yank.R.string.filter_voucher),
    Express("快递", homes.snaix.app.yank.R.string.filter_express),
    Ticket("票券", homes.snaix.app.yank.R.string.filter_ticket),
    Todo("待办", homes.snaix.app.yank.R.string.filter_todo),
    Archived(null, homes.snaix.app.yank.R.string.filter_archived),
}

class RecordsViewModel(
    private val repo: HistoryRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow(RecordFilter.All)
    val filter: StateFlow<RecordFilter> = _filter.asStateFlow()

    private val _query = MutableStateFlow<String?>(null)
    val query: StateFlow<String?> = _query.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<HistoryEntity>> =
        combine(_filter, _query) { f, q -> f to q }
            .flatMapLatest { (f, q) ->
                if (f == RecordFilter.Archived) repo.observeArchived(q)
                else repo.observeRecords(f.type, q)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setFilter(f: RecordFilter) { _filter.value = f }
    fun setQuery(q: String?) { _query.value = q?.takeIf { it.isNotBlank() } }
}
