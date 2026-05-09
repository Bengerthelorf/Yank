package homes.snaix.app.yank.ui.reminders

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import homes.snaix.app.yank.R
import homes.snaix.app.yank.YankApp
import homes.snaix.app.yank.ui.common.EmptyState
import homes.snaix.app.yank.ui.records.TypeCard

@Composable
fun RemindersScreen() {
    val ctx = LocalContext.current.applicationContext as YankApp
    val vm: RemindersViewModel = viewModel(factory = viewModelFactory {
        initializer { RemindersViewModel(ctx.di.historyRepo) }
    })
    val state by vm.state.collectAsState()

    if (state.active.isEmpty() && state.upcoming.isEmpty()) {
        EmptyState(
            Icons.Outlined.NotificationsActive,
            stringResource(R.string.empty_reminders_title),
            stringResource(R.string.empty_reminders_subtitle),
        )
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (state.active.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.reminders_active),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }
                items(state.active, key = { it.id }) { e ->
                    TypeCard(
                        entity = e,
                        onClick = {},
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }
            }
            if (state.upcoming.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.reminders_upcoming),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }
                items(state.upcoming, key = { it.id }) { e ->
                    TypeCard(
                        entity = e,
                        onClick = {},
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }
            }
            item { Spacer(Modifier.height(96.dp)) }
        }
    }
}
