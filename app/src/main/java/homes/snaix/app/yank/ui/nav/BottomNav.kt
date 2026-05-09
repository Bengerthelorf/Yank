package homes.snaix.app.yank.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
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
fun BottomNav(navController: NavHostController) {
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination
    ShortNavigationBar {
        TopDest.entries.forEach { dest ->
            ShortNavigationBarItem(
                selected = current?.hierarchy?.any { it.route == dest.route } == true,
                onClick = {
                    navController.navigate(dest.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(dest.icon, contentDescription = null) },
                label = { Text(stringResource(dest.labelRes)) },
            )
        }
    }
}
