package homes.snaix.app.yank.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SwipeActionsBox(
    leftAction: SwipeAction?,
    rightAction: SwipeAction?,
    onAction: (SwipeAction) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // Snap back; the row leaves via the underlying Flow when the action removes it.
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> leftAction?.let(onAction)
                SwipeToDismissBoxValue.StartToEnd -> rightAction?.let(onAction)
                SwipeToDismissBoxValue.Settled -> Unit
            }
            false
        },
    )
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = rightAction != null,
        enableDismissFromEndToStart = leftAction != null,
        backgroundContent = {
            when (state.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> leftAction?.let { ActionBackground(it, Arrangement.End) }
                SwipeToDismissBoxValue.StartToEnd -> rightAction?.let { ActionBackground(it, Arrangement.Start) }
                SwipeToDismissBoxValue.Settled -> Unit
            }
        },
        modifier = modifier,
    ) {
        content()
    }
}

@Composable
private fun ActionBackground(action: SwipeAction, alignment: Arrangement.Horizontal) {
    val (container, tint) = action.colors()
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(container)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = alignment,
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun SwipeAction.colors(): Pair<Color, Color> = when (this) {
    SwipeAction.Delete -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    SwipeAction.Archive -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    SwipeAction.Pin -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
}
