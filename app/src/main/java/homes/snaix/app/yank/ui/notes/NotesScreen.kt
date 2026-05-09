package homes.snaix.app.yank.ui.notes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import homes.snaix.app.yank.R
import homes.snaix.app.yank.YankApp
import homes.snaix.app.yank.ui.common.EmptyState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotesScreen() {
    val ctx = LocalContext.current.applicationContext as YankApp
    val vm: NotesViewModel = viewModel(factory = viewModelFactory {
        initializer { NotesViewModel(ctx.di.historyRepo) }
    })
    val items by vm.items.collectAsState()
    var sheetOpen by remember { mutableStateOf(false) }

    val fabShape = MaterialShapes.Cookie9Sided.toShape()

    Scaffold(
        topBar = { LargeTopAppBar(title = { Text(stringResource(R.string.tab_notes)) }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { sheetOpen = true },
                shape = fabShape,
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = "手写新建")
            }
        },
    ) { padding ->
        if (items.isEmpty()) {
            EmptyState(
                Icons.Outlined.Description,
                stringResource(R.string.empty_notes_title),
                stringResource(R.string.empty_notes_subtitle),
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(items, key = { it.id }) { e ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxSize(),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(e.displayPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(e.displaySecondary.orEmpty(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item { Spacer(Modifier.height(96.dp)) }
            }
        }
    }

    if (sheetOpen) {
        ManualNoteSheet(
            onDismiss = { sheetOpen = false },
            onSave = vm::saveManualNote,
        )
    }
}
