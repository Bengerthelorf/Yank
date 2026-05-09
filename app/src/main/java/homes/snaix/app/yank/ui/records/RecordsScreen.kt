package homes.snaix.app.yank.ui.records

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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

@Composable
fun RecordsScreen() {
    val context = LocalContext.current
    val ctx = context.applicationContext as YankApp
    val vm: RecordsViewModel = viewModel(factory = viewModelFactory {
        initializer { RecordsViewModel(ctx.di.historyRepo) }
    })
    val items by vm.items.collectAsState()
    val filter by vm.filter.collectAsState()

    if (items.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.History,
            title = stringResource(R.string.empty_records_title),
            subtitle = stringResource(R.string.empty_records_subtitle),
        )
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { FilterChipsRow(selected = filter, onSelected = vm::setFilter) }
            item { Spacer(Modifier.height(12.dp)) }
            items(items, key = { it.id }) { entity ->
                TypeCard(
                    entity = entity,
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("Yank", entity.displayPrimary))
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
            item { Spacer(Modifier.height(96.dp)) }
        }
    }
}
