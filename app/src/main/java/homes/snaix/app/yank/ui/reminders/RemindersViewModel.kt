package homes.snaix.app.yank.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import homes.snaix.app.yank.data.db.HistoryEntity
import homes.snaix.app.yank.data.repo.HistoryRepository
import homes.snaix.app.yank.domain.routing.Router
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RemindersUiState(
    val active: List<HistoryEntity>,
    val upcoming: List<HistoryEntity>,
)

class RemindersViewModel(
    private val repo: HistoryRepository,
    private val router: Router,
) : ViewModel() {
    val state: StateFlow<RemindersUiState> = repo.observeUpcoming(0L)
        .map { all ->
            val now = System.currentTimeMillis()
            val (upcoming, active) = all.partition { (it.eventTime ?: 0L) > now + (60 * 60_000L) }
            // v0.1 heuristic: items within 1h are "active", further out are "upcoming"
            RemindersUiState(active, upcoming)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, RemindersUiState(emptyList(), emptyList()))

    fun delete(id: String) = viewModelScope.launch { repo.delete(id) }
    fun archive(id: String) = viewModelScope.launch { repo.setArchived(id) }
    fun repin(id: String) = viewModelScope.launch {
        repo.get(id)?.let { router.repin(it) }
    }
    fun updatePrimary(entity: HistoryEntity, newPrimary: String) = viewModelScope.launch {
        repo.updatePrimary(entity, newPrimary)
    }
}
