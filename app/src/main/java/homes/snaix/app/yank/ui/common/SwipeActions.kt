package homes.snaix.app.yank.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import homes.snaix.app.yank.YankApp

class SwipeActionsBinding(
    val leftAction: SwipeAction?,
    val rightAction: SwipeAction?,
    val dispatch: (SwipeAction, String) -> Unit,
)

@Composable
fun rememberSwipeActions(
    onDelete: (String) -> Unit,
    onArchive: (String) -> Unit,
    onPin: (String) -> Unit,
): SwipeActionsBinding {
    val app = LocalContext.current.applicationContext as YankApp
    val leftKey by app.di.configRepo.swipeLeftKey().collectAsState(initial = "Delete")
    val rightKey by app.di.configRepo.swipeRightKey().collectAsState(initial = "Pin")
    val left = SwipeAction.fromKey(leftKey)
    val right = SwipeAction.fromKey(rightKey)
    return SwipeActionsBinding(
        leftAction = left,
        rightAction = right,
        dispatch = { action, id ->
            when (action) {
                SwipeAction.Delete -> onDelete(id)
                SwipeAction.Archive -> onArchive(id)
                SwipeAction.Pin -> onPin(id)
            }
        },
    )
}
