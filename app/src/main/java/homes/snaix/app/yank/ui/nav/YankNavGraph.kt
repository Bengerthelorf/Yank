package homes.snaix.app.yank.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            nav,
            startDestination = TopDest.Records.route,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(TopDest.Records.route)   { RecordsScreen() }
            composable(TopDest.Notes.route)     { NotesScreen() }
            composable(TopDest.Reminders.route) { RemindersScreen() }
            composable(TopDest.Settings.route)  { SettingsScreen() }
        }
        BottomNav(
            navController = nav,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
        )
    }
}
