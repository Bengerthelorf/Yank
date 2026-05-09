package homes.snaix.app.yank.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import homes.snaix.app.yank.R
import homes.snaix.app.yank.data.db.HistoryEntity
import kotlinx.coroutines.launch

data class ActionMenuItem(
    val icon: ImageVector,
    val labelRes: Int,
    val tint: Color? = null,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionMenuSheet(
    title: String,
    items: List<ActionMenuItem>,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Fire action first for immediate feedback, then suspend on hide() so
    // the sheet animates out instead of vanishing when onDismiss nulls the trigger.
    fun closeAfter(action: () -> Unit) {
        action()
        scope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            items.forEach { item ->
                ActionRow(item) { closeAfter(item.onClick) }
            }
        }
    }
}

@Composable
private fun ActionRow(item: ActionMenuItem, onTap: () -> Unit) {
    val tint = item.tint ?: MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(20.dp))
        Text(
            text = stringResource(item.labelRes),
            style = MaterialTheme.typography.bodyLarge,
            color = tint,
        )
    }
}

class RecordActionMenu internal constructor(
    val openMenu: (HistoryEntity) -> Unit,
    val Host: @Composable () -> Unit,
)

@Composable
fun rememberRecordActionMenu(
    onCopy: (HistoryEntity) -> Unit,
    onDelete: (HistoryEntity) -> Unit,
    onEdit: ((HistoryEntity) -> Unit)? = null,
    onArchive: ((HistoryEntity) -> Unit)? = null,
): RecordActionMenu {
    var target by remember { mutableStateOf<HistoryEntity?>(null) }
    return RecordActionMenu(
        openMenu = { target = it },
        Host = {
            val entity = target
            if (entity != null) {
                ActionMenuSheet(
                    title = entity.displayPrimary,
                    items = actionItemsFor(
                        onCopy = { onCopy(entity) },
                        onDelete = { onDelete(entity) },
                        onEdit = onEdit?.let { fn -> { fn(entity) } },
                        onArchive = onArchive?.let { fn -> { fn(entity) } },
                    ),
                    onDismiss = { target = null },
                )
            }
        },
    )
}

@Composable
private fun actionItemsFor(
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)?,
    onArchive: (() -> Unit)?,
): List<ActionMenuItem> = buildList {
    add(ActionMenuItem(Icons.Outlined.ContentCopy, R.string.action_copy, onClick = onCopy))
    if (onEdit != null) {
        add(ActionMenuItem(Icons.Outlined.Edit, R.string.action_edit, onClick = onEdit))
    }
    if (onArchive != null) {
        add(ActionMenuItem(Icons.Outlined.Archive, R.string.action_archive, onClick = onArchive))
    }
    add(
        ActionMenuItem(
            icon = Icons.Outlined.DeleteOutline,
            labelRes = R.string.action_delete,
            tint = MaterialTheme.colorScheme.error,
            onClick = onDelete,
        ),
    )
}
