package homes.snaix.app.yank.ui.nav

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import homes.snaix.app.yank.R

enum class TopDest(val route: String, val labelRes: Int, val icon: ImageVector) {
    Records("records", R.string.tab_records, Icons.Outlined.History),
    Notes("notes", R.string.tab_notes, Icons.Outlined.Description),
    Reminders("reminders", R.string.tab_reminders, Icons.Outlined.NotificationsActive),
    Settings("settings", R.string.tab_settings, Icons.Outlined.Settings),
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomNav(
    navController: NavHostController,
    trailingFab: (@Composable RowScope.() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination

    val trailingWithDivider: (@Composable RowScope.() -> Unit)? = trailingFab?.let { fab ->
        {
            VerticalDivider(
                modifier = Modifier
                    .height(32.dp)
                    .padding(horizontal = 4.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            fab()
        }
    }

    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier,
        trailingContent = trailingWithDivider,
    ) {
        TopDest.entries.forEach { dest ->
            val selected = current?.hierarchy?.any { it.route == dest.route } == true
            val onClick: () -> Unit = {
                navController.navigate(dest.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            if (selected) {
                FilledIconButton(
                    onClick = onClick,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(4.dp),
                ) { Icon(dest.icon, contentDescription = null) }
            } else {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(4.dp),
                ) { Icon(dest.icon, contentDescription = null) }
            }
        }
    }
}
