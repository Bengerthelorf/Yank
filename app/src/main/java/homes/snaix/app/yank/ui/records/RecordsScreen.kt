package homes.snaix.app.yank.ui.records

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen() {
    val ctx = LocalContext.current.applicationContext as YankApp
    val vm: RecordsViewModel = viewModel(factory = viewModelFactory {
        initializer { RecordsViewModel(ctx.di.historyRepo) }
    })
    val items by vm.items.collectAsState()
    val filter by vm.filter.collectAsState()
    val scope = rememberCoroutineScope()

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val outcome = ctx.di.imagePickPipeline.process(uri)
                ctx.di.captureOutcomeBus.emit(outcome)
            }
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.tab_records)) },
                colors = TopAppBarDefaults.largeTopAppBarColors(),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { pickImage.launch("image/*") }) {
                Icon(Icons.Outlined.PhotoCamera, contentDescription = "选图识别")
            }
        },
    ) { padding ->
        if (items.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.History,
                title = stringResource(R.string.empty_records_title),
                subtitle = stringResource(R.string.empty_records_subtitle),
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                item { FilterChipsRow(selected = filter, onSelected = vm::setFilter) }
                item { Spacer(Modifier.height(12.dp)) }
                items(items, key = { it.id }) { entity ->
                    TypeCard(
                        entity = entity,
                        onClick = { /* Phase 17 detail sheet */ },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }
                item { Spacer(Modifier.height(96.dp)) }
            }
        }
    }
}
