package homes.snaix.app.yank.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import homes.snaix.app.yank.ui.notes.NotesScreen
import homes.snaix.app.yank.ui.records.RecordsScreen
import homes.snaix.app.yank.ui.reminders.RemindersScreen
import homes.snaix.app.yank.ui.settings.SettingsScreen

@Composable
fun YankNavGraph() {
    val nav = rememberNavController()
    Scaffold(bottomBar = { BottomNav(nav) }) { padding ->
        NavHost(nav, startDestination = TopDest.Records.route, modifier = Modifier.padding(padding)) {
            composable(TopDest.Records.route)   { RecordsScreen() }
            composable(TopDest.Notes.route)     { NotesScreen() }
            composable(TopDest.Reminders.route) { RemindersScreen() }
            composable(TopDest.Settings.route)  { SettingsScreen() }
        }
    }
}
