package homes.snaix.app.yank.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import homes.snaix.app.yank.data.db.HistoryEntity
import homes.snaix.app.yank.data.repo.HistoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetailState {
    data object Loading : DetailState
    data class Loaded(val entity: HistoryEntity) : DetailState
    data object Missing : DetailState
}

class RecordDetailViewModel(
    private val repo: HistoryRepository,
    private val deletedBus: MutableSharedFlow<HistoryEntity>,
    private val id: String,
) : ViewModel() {

    private val _state = MutableStateFlow<DetailState>(DetailState.Loading)
    val state: StateFlow<DetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = repo.get(id)?.let(DetailState::Loaded) ?: DetailState.Missing
        }
    }

    fun delete() = viewModelScope.launch {
        val entity = repo.get(id) ?: return@launch
        repo.delete(id)
        deletedBus.emit(entity)
    }
    fun archive() = viewModelScope.launch { repo.setArchived(id) }
}
