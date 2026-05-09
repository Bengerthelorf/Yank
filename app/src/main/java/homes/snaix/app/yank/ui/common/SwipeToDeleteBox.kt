package homes.snaix.app.yank.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

/**
 * Wraps [content] with a left (EndToStart) swipe gesture that calls [onDelete]
 * once the swipe crosses the M3 threshold. The right (StartToEnd) direction
 * is intentionally disabled here — pin / progress-notification actions land
 * in a follow-up commit so this primitive is reviewable on its own.
 *
 * Behavior:
 *   - Left-swipe past threshold: invoke [onDelete], dismiss the row. The
 *     caller is responsible for actually removing the entity from the list
 *     (typically via a Room flow, which happens automatically once the
 *     repository delete completes).
 *   - Right-swipe: blocked at the gesture layer.
 *
 * The dismiss state is reset to Settled if [onDelete] does not actually
 * remove the row from the list (e.g., the repository call failed) — without
 * that reset, the row would render permanently dismissed even though it's
 * still in the underlying list.
 */
@Composable
fun SwipeToDeleteBox(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
    )

    // If the row is still composed (i.e., the repo didn't actually remove it
    // because the delete is racing the snapshot), snap back to Settled so the
    // user sees the original card again instead of a permanently-dismissed
    // empty cell.
    LaunchedEffect(state) {
        snapshotFlow { state.currentValue }
            .filter { it == SwipeToDismissBoxValue.EndToStart }
            .first()
        state.reset()
    }

    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = { DeleteSwipeBackground() },
        modifier = modifier,
    ) {
        content()
    }
}

@Composable
private fun DeleteSwipeBackground() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Icon(
            imageVector = Icons.Outlined.DeleteOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.size(24.dp),
        )
    }
}
