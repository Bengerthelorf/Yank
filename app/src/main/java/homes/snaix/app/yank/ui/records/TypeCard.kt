package homes.snaix.app.yank.ui.records

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import homes.snaix.app.yank.R
import homes.snaix.app.yank.data.db.HistoryEntity
import homes.snaix.app.yank.ui.theme.LocalTypeColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TypeCard(
    entity: HistoryEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTypeColors.current
    // Match the JSON-discriminator type (which is always Chinese; it's a model contract value)
    // back to a UI color role.
    val role = remember(entity.type) {
        when (entity.type) {
            "排队" -> colors.queue
            "取餐" -> colors.pickup
            "券码" -> colors.voucher
            "快递" -> colors.express
            "票券" -> colors.ticket
            "待办" -> colors.todo
            else   -> colors.notes
        }
    }
    // Translated display label for the same type; falls back to the raw discriminator
    // (e.g. "notes") when the type isn't one of the seven enum values.
    val typeLabel = when (entity.type) {
        "排队" -> stringResource(R.string.type_label_queue)
        "取餐" -> stringResource(R.string.type_label_pickup)
        "券码" -> stringResource(R.string.type_label_voucher)
        "快递" -> stringResource(R.string.type_label_express)
        "票券" -> stringResource(R.string.type_label_ticket)
        "待办" -> stringResource(R.string.type_label_todo)
        "notes" -> stringResource(R.string.type_label_notes)
        else -> entity.type
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = role.container),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "$typeLabel · ${entity.displaySecondary.orEmpty()}",
                style = MaterialTheme.typography.labelLarge,
                color = role.onContainer,
            )
            Text(
                entity.displayPrimary,
                style = MaterialTheme.typography.displayMedium,
                color = role.onContainer,
            )
            Text(
                formatTime(entity.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

private fun formatTime(epochMs: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMs))

