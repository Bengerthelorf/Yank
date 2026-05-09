package homes.snaix.app.yank.ui.notes

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Card
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
import homes.snaix.app.yank.data.db.HistoryEntity
import homes.snaix.app.yank.ui.common.EmptyState
import homes.snaix.app.yank.ui.common.SwipeAction
import homes.snaix.app.yank.ui.common.SwipeActionsBox
import homes.snaix.app.yank.ui.common.rememberCopyEntity
import homes.snaix.app.yank.ui.common.rememberRecordActionMenu
import homes.snaix.app.yank.ui.nav.BottomNavReservedHeight

@Composable
fun NotesScreen(onOpenDetail: (String) -> Unit) {
    val app = LocalContext.current.applicationContext as YankApp
    val vm: NotesViewModel = viewModel(factory = viewModelFactory {
        initializer { NotesViewModel(app.di.historyRepo) }
    })
    val items by vm.items.collectAsState()
    val copy = rememberCopyEntity()
    val edit = rememberNoteEditHost { entity, title, body, date, time ->
        vm.updateNote(entity, title, body, date, time)
    }
    val menu = rememberRecordActionMenu(
        onCopy = copy,
        onDelete = { vm.delete(it.id) },
        onEdit = edit.open,
    )

    if (items.isEmpty()) {
        EmptyState(
            Icons.Outlined.Description,
            stringResource(R.string.empty_notes_title),
            stringResource(R.string.empty_notes_subtitle),
        )
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items, key = { it.id }) { e ->
                SwipeActionsBox(
                    leftAction = SwipeAction.Delete,
                    rightAction = null,
                    onAction = { vm.delete(e.id) },
                ) {
                    NoteCard(
                        entity = e,
                        onClick = { onOpenDetail(e.id) },
                        onLongClick = { menu.openMenu(e) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }
            }
            item { Spacer(Modifier.height(BottomNavReservedHeight)) }
        }
    }

    menu.Host()
    edit.Host()
}

@Composable
private fun NoteCard(
    entity: HistoryEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(16.dp)
        ) {
            Text(entity.displayPrimary, style = MaterialTheme.typography.titleMedium)
            Text(
                entity.displaySecondary.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
