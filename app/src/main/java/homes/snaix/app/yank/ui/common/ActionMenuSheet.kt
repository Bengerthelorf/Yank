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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import homes.snaix.app.yank.R
import homes.snaix.app.yank.data.db.HistoryEntity
import kotlinx.coroutines.launch

/**
 * One row in the long-press action menu. Local to the sheet's API.
 */
data class ActionMenuItem(
    val icon: ImageVector,
    val labelRes: Int,
    val tint: Color? = null,
    val onClick: () -> Unit,
)

/**
 * Bottom-sheet quick-action menu shown on long-press of a record / note card.
 * Stateless from the caller's perspective: the caller passes the title and
 * items, and provides a single [onDismiss] that clears whatever trigger
 * state opened the sheet.
 *
 * Each row's tap fires its action *first* (immediate feedback) and then
 * suspends to animate the sheet out before reporting dismissal — without
 * the suspending hide(), nulling the trigger would cause the sheet to
 * disappear without the slide-down animation.
 *
 * The destructive action is rendered last with the error tint, matching M3
 * convention.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionMenuSheet(
    title: String,
    items: List<ActionMenuItem>,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

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

/**
 * Single-call host that owns the menu's open/close state and dispatches the
 * three or four standard actions. Pulls the per-screen long-press boilerplate
 * (12 lines × 3 screens of menuTarget / let / ActionMenuSheet) into one place.
 *
 * Pass `onArchive = null` for screens that don't expose an archive action
 * (Notes, currently). The returned `openMenu` is the lambda the caller wires
 * into [TypeCard]'s `onLongClick`.
 */
/**
 * Stable handle for the long-press quick-action menu. Pulls the
 * `var menuTarget by remember { … }` + `menuTarget?.let { ActionMenuSheet(…) }`
 * boilerplate that was previously triplicated across Records / Notes /
 * Reminders into a single call.
 *
 * Caller wires `openMenu(entity)` into [TypeCard]'s `onLongClick` and places
 * `Host()` somewhere stable (e.g., end of the screen), and the rest is
 * internal.
 */
class RecordActionMenu internal constructor(
    val openMenu: (HistoryEntity) -> Unit,
    val Host: @Composable () -> Unit,
)

@Composable
fun rememberRecordActionMenu(
    onCopy: (HistoryEntity) -> Unit,
    onDelete: (HistoryEntity) -> Unit,
    onArchive: ((HistoryEntity) -> Unit)? = null,
): RecordActionMenu {
    val target: MutableState<HistoryEntity?> = remember { mutableStateOf(null) }
    return RecordActionMenu(
        openMenu = { target.value = it },
        Host = { ActionMenuHost(target, onCopy, onArchive, onDelete) },
    )
}

@Composable
private fun ActionMenuHost(
    target: MutableState<HistoryEntity?>,
    onCopy: (HistoryEntity) -> Unit,
    onArchive: ((HistoryEntity) -> Unit)?,
    onDelete: (HistoryEntity) -> Unit,
) {
    val entity = target.value ?: return
    val items = if (onArchive != null) {
        actionItemsFor(
            onCopy = { onCopy(entity) },
            onArchive = { onArchive(entity) },
            onDelete = { onDelete(entity) },
        )
    } else {
        actionItemsFor(
            onCopy = { onCopy(entity) },
            onDelete = { onDelete(entity) },
        )
    }
    ActionMenuSheet(
        title = entity.displayPrimary,
        items = items,
        onDismiss = { target.value = null },
    )
}

@Composable
private fun actionItemsFor(
    onCopy: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
): List<ActionMenuItem> = listOf(
    ActionMenuItem(Icons.Outlined.ContentCopy, R.string.action_copy, onClick = onCopy),
    ActionMenuItem(Icons.Outlined.Archive, R.string.action_archive, onClick = onArchive),
    ActionMenuItem(
        icon = Icons.Outlined.DeleteOutline,
        labelRes = R.string.action_delete,
        tint = MaterialTheme.colorScheme.error,
        onClick = onDelete,
    ),
)

@Composable
private fun actionItemsFor(
    onCopy: () -> Unit,
    onDelete: () -> Unit,
): List<ActionMenuItem> = listOf(
    ActionMenuItem(Icons.Outlined.ContentCopy, R.string.action_copy, onClick = onCopy),
    ActionMenuItem(
        icon = Icons.Outlined.DeleteOutline,
        labelRes = R.string.action_delete,
        tint = MaterialTheme.colorScheme.error,
        onClick = onDelete,
    ),
)
