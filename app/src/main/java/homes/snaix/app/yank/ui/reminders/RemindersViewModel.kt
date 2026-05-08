package homes.snaix.app.yank.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import homes.snaix.app.yank.data.db.HistoryEntity
import homes.snaix.app.yank.data.repo.HistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class RemindersUiState(
    val active: List<HistoryEntity>,
    val upcoming: List<HistoryEntity>,
)

class RemindersViewModel(repo: HistoryRepository) : ViewModel() {
    val state: StateFlow<RemindersUiState> = repo.observeUpcoming(0L)
        .map { all ->
            val now = System.currentTimeMillis()
            val (upcoming, active) = all.partition { (it.eventTime ?: 0L) > now + (60 * 60_000L) }
            // v0.1 heuristic: items within 1h are "active", further out are "upcoming"
            RemindersUiState(active, upcoming)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, RemindersUiState(emptyList(), emptyList()))
}
