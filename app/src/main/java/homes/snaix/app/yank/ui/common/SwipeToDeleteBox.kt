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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Wraps [content] with a left (EndToStart) swipe gesture that calls [onDelete]
 * once the swipe crosses the M3 threshold. The right (StartToEnd) direction
 * is intentionally disabled at the gesture layer — pin / progress-notification
 * actions land in a follow-up commit so this primitive is reviewable on its own.
 *
 * Behavior on threshold cross:
 *   - Fires [onDelete] (which the caller routes to a repo delete; the row
 *     leaves composition naturally when the underlying Flow emits without it).
 *   - Returns `false` from `confirmValueChange` so the box snaps back instead
 *     of latching to the dismissed anchor. That avoids a visible flicker on
 *     the happy path (where the row would otherwise animate to dismissed and
 *     then jump back when the new list arrives) and means a failed delete
 *     leaves the row in its original position with no recovery code needed.
 *
 * The swipe gesture itself does not visually dismiss the row; the list emit
 * does. That's correct for a Flow-driven list and removes the second-swipe
 * trap a `confirmValueChange { true }` design would create if the repo delete
 * ever stalled or failed.
 */
@Composable
fun SwipeToDeleteBox(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onDelete()
            false
        },
    )

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
    // The clip + padding here matches the inner Card's `padding(horizontal=16.dp,
    // vertical=6.dp)` so the red rounded rectangle aligns flush with the card
    // edges — no halo, no bleed past the rounded corners.
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
