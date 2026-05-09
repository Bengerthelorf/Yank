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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import homes.snaix.app.yank.ui.theme.LocalTypeColors

@Composable
fun FilterChipsRow(
    selected: RecordFilter,
    onSelected: (RecordFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTypeColors.current
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RecordFilter.entries.filter { it != RecordFilter.All }.forEach { f ->
            val (container, onContainer) = when (f) {
                RecordFilter.Queue   -> colors.queue.container to colors.queue.onContainer
                RecordFilter.Pickup  -> colors.pickup.container to colors.pickup.onContainer
                RecordFilter.Voucher -> colors.voucher.container to colors.voucher.onContainer
                RecordFilter.Express -> colors.express.container to colors.express.onContainer
                RecordFilter.Ticket  -> colors.ticket.container to colors.ticket.onContainer
                RecordFilter.Todo    -> colors.todo.container to colors.todo.onContainer
                else                 -> Color.Unspecified to Color.Unspecified
            }
            FilterChip(
                selected = selected == f,
                onClick = {
                    if (selected == f) onSelected(RecordFilter.All)
                    else onSelected(f)
                },
                label = { Text(stringResource(f.labelRes)) },
                colors = if (container != Color.Unspecified)
                    FilterChipDefaults.filterChipColors(selectedContainerColor = container, selectedLabelColor = onContainer)
                else FilterChipDefaults.filterChipColors(),
            )
        }
    }
}
