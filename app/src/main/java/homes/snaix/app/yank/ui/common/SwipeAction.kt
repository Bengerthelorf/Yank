package homes.snaix.app.yank.ui.common

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.ui.graphics.vector.ImageVector
import homes.snaix.app.yank.R

enum class SwipeAction(
    val icon: ImageVector,
    @StringRes val labelRes: Int,
) {
    Delete(Icons.Outlined.DeleteOutline, R.string.swipe_action_delete),
    Archive(Icons.Outlined.Archive, R.string.swipe_action_archive),
    Pin(Icons.Outlined.PushPin, R.string.swipe_action_pin);

    companion object {
        // Returns null on unknown key so a renamed enum or corrupted DataStore
        // disables the swipe rather than crashing the screen on launch. The
        // ConfigRepository default backstops fresh installs.
        fun fromKey(key: String?): SwipeAction? = entries.firstOrNull { it.name == key }
    }
}
