package homes.snaix.app.yank.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import homes.snaix.app.yank.data.db.HistoryEntity
import homes.snaix.app.yank.data.repo.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecordDetailViewModel(
    private val repo: HistoryRepository,
    private val id: String,
) : ViewModel() {

    private val _entity = MutableStateFlow<HistoryEntity?>(null)
    val entity: StateFlow<HistoryEntity?> = _entity.asStateFlow()

    init {
        viewModelScope.launch { _entity.value = repo.get(id) }
    }

    fun delete() = viewModelScope.launch { repo.delete(id) }
    fun archive() = viewModelScope.launch { repo.setArchived(id) }
}
