package homes.snaix.app.yank.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import homes.snaix.app.yank.R

/**
 * One row in the long-press action menu. Local to this file because the
 * shape is internal to the sheet and would clutter the public API.
 */
data class ActionMenuItem(
    val icon: ImageVector,
    val labelRes: Int,
    val tint: Color? = null,
    val onClick: () -> Unit,
)

/**
 * Bottom-sheet quick-action menu shown on long-press of a record / note card.
 * Stateless: caller owns the visibility state and dismisses by clearing the
 * trigger (typically `var menuTarget by remember { mutableStateOf(...) }`).
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
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            items.forEach { item ->
                ActionRow(item)
            }
        }
    }
}

@Composable
private fun ActionRow(item: ActionMenuItem) {
    val tint = item.tint ?: MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
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
 * Convenience builders for the common menu shapes. Records get
 * Copy / Archive / Delete; Notes don't archive (they live on a separate
 * screen with no archive view), so they get Copy / Delete only.
 */
object ActionMenus {
    @Composable
    fun copyArchiveDelete(
        onCopy: () -> Unit,
        onArchive: () -> Unit,
        onDelete: () -> Unit,
    ): List<ActionMenuItem> = listOf(
        ActionMenuItem(Icons.Outlined.ContentCopy, R.string.action_copy, onClick = onCopy),
        ActionMenuItem(Icons.Outlined.Archive, R.string.action_archive, onClick = onArchive),
        ActionMenuItem(
            Icons.Outlined.DeleteOutline,
            R.string.action_delete,
            tint = MaterialTheme.colorScheme.error,
            onClick = onDelete,
        ),
    )

    @Composable
    fun copyDelete(
        onCopy: () -> Unit,
        onDelete: () -> Unit,
    ): List<ActionMenuItem> = listOf(
        ActionMenuItem(Icons.Outlined.ContentCopy, R.string.action_copy, onClick = onCopy),
        ActionMenuItem(
            Icons.Outlined.DeleteOutline,
            R.string.action_delete,
            tint = MaterialTheme.colorScheme.error,
            onClick = onDelete,
        ),
    )
}
