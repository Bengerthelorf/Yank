package homes.snaix.app.yank.ui.records

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import homes.snaix.app.yank.ui.theme.LocalTypeColors

@Composable
fun FilterChipsRow(
    selected: RecordFilter,
    onSelected: (RecordFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typeColors = LocalTypeColors.current
    // Single ordered list of visible filters: all RecordType entries, plus Archived.
    // RecordFilter.All is the implicit "no chip selected" state and is never rendered.
    val visible: List<RecordFilter> = remember {
        RecordType.entries.map<RecordType, RecordFilter> { RecordFilter.ByType(it) } +
            RecordFilter.Archived
    }
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        visible.forEach { f ->
            val role = (f as? RecordFilter.ByType)?.let { typeColors.roleFor(it.type) }
            FilterChip(
                selected = selected == f,
                onClick = {
                    if (selected == f) onSelected(RecordFilter.All)
                    else onSelected(f)
                },
                label = { Text(stringResource(f.labelRes)) },
                colors = if (role != null) {
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = role.container,
                        selectedLabelColor = role.onContainer,
                    )
                } else {
                    FilterChipDefaults.filterChipColors()
                },
            )
        }
    }
}
