package homes.snaix.app.yank.ui.records

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import homes.snaix.app.yank.R
import homes.snaix.app.yank.data.db.HistoryEntity
import homes.snaix.app.yank.ui.common.ScreenshotImage
import homes.snaix.app.yank.ui.theme.LocalTypeColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TypeCard(
    entity: HistoryEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    val typeColors = LocalTypeColors.current
    val knownType = remember(entity.type) { RecordType.fromDiscriminator(entity.type) }
    val role = if (knownType != null) typeColors.roleFor(knownType) else typeColors.notes
    val typeLabel = when {
        knownType != null    -> stringResource(knownType.labelRes)
        entity.type == "notes" -> stringResource(R.string.type_label_notes)
        else                 -> entity.type
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = role.container),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
            entity.screenshotPath?.let { path ->
                ScreenshotImage(
                    path = path,
                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(14.dp)),
                )
            }
        }
    }
}

private fun formatTime(epochMs: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMs))
