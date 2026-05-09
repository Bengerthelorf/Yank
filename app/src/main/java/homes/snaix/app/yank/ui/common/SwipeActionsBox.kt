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
import androidx.compose.material.icons.outlined.PushPin
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SwipeActionsBox(
    onSwipeLeft: () -> Unit,
    onSwipeRight: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // confirmValueChange returns false in both directions: the box snaps back
    // and the row removal (or no-op for repin) comes from the underlying Flow.
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> onSwipeLeft()
                SwipeToDismissBoxValue.StartToEnd -> onSwipeRight?.invoke()
                SwipeToDismissBoxValue.Settled -> Unit
            }
            false
        },
    )
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = onSwipeRight != null,
        backgroundContent = { SwipeBackground(state.dismissDirection) },
        modifier = modifier,
    ) {
        content()
    }
}

@Composable
private fun SwipeBackground(direction: SwipeToDismissBoxValue) {
    when (direction) {
        SwipeToDismissBoxValue.EndToStart -> ActionBackground(
            icon = Icons.Outlined.DeleteOutline,
            container = MaterialTheme.colorScheme.errorContainer,
            tint = MaterialTheme.colorScheme.onErrorContainer,
            alignment = Arrangement.End,
        )
        SwipeToDismissBoxValue.StartToEnd -> ActionBackground(
            icon = Icons.Outlined.PushPin,
            container = MaterialTheme.colorScheme.tertiaryContainer,
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
            alignment = Arrangement.Start,
        )
        SwipeToDismissBoxValue.Settled -> Unit
    }
}

@Composable
private fun ActionBackground(
    icon: ImageVector,
    container: Color,
    tint: Color,
    alignment: Arrangement.Horizontal,
) {
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
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
    }
}
