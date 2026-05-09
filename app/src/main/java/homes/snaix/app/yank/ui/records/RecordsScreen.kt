package homes.snaix.app.yank.ui.records

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material.icons.outlined.History
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
import homes.snaix.app.yank.ui.common.SwipeAction
import homes.snaix.app.yank.ui.common.SwipeActionsBox
import homes.snaix.app.yank.ui.common.rememberCopyEntity
import homes.snaix.app.yank.ui.common.rememberEditPrimaryHost
import homes.snaix.app.yank.ui.common.rememberRecordActionMenu
import homes.snaix.app.yank.ui.common.rememberSwipeActions
import homes.snaix.app.yank.ui.nav.BottomNavReservedHeight

@Composable
fun RecordsScreen(onOpenDetail: (String) -> Unit) {
    val app = LocalContext.current.applicationContext as YankApp
    val vm: RecordsViewModel = viewModel(factory = viewModelFactory {
        initializer { RecordsViewModel(app.di.historyRepo, app.di.router, app.di.deletedBus) }
    })
    val items by vm.items.collectAsState()
    val filter by vm.filter.collectAsState()
    val hasAnyData by vm.hasAnyData.collectAsState()
    val copy = rememberCopyEntity()
    val edit = rememberEditPrimaryHost { entity, value -> vm.updatePrimary(entity, value) }
    val menu = rememberRecordActionMenu(
        onCopy = copy,
        onDelete = { vm.delete(it.id) },
        onEdit = edit.open,
        onArchive = { vm.archive(it.id) },
    )
    val swipe = rememberSwipeActions(
        onDelete = { id -> vm.delete(id) },
        onArchive = { id -> vm.archive(id) },
        onPin = { id -> vm.repin(id) },
    )

    if (!hasAnyData) {
        EmptyState(
            icon = Icons.Outlined.History,
            title = stringResource(R.string.empty_records_title),
            subtitle = stringResource(R.string.empty_records_subtitle),
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        FilterChipsRow(selected = filter, onSelected = vm::setFilter)
        Spacer(Modifier.height(12.dp))
        if (items.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.FilterAltOff,
                title = stringResource(R.string.empty_filter_title),
                subtitle = stringResource(R.string.empty_filter_subtitle),
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items, key = { it.id }) { entity ->
                    SwipeActionsBox(
                        leftAction = swipe.leftAction,
                        rightAction = swipe.rightAction,
                        onAction = { swipe.dispatch(it, entity.id) },
                    ) {
                        TypeCard(
                            entity = entity,
                            onClick = { onOpenDetail(entity.id) },
                            onLongClick = { menu.openMenu(entity) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                    }
                }
                item { Spacer(Modifier.height(BottomNavReservedHeight)) }
            }
        }
    }

    menu.Host()
    edit.Host()
}
