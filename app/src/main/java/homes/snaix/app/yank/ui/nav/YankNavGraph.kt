package homes.snaix.app.yank.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun YankNavGraph() {
    val nav = rememberNavController()
    Scaffold(bottomBar = { BottomNav(nav) }) { padding ->
        NavHost(nav, startDestination = TopDest.Records.route, modifier = Modifier.padding(padding)) {
            composable(TopDest.Records.route)   { Text("Records — Phase 13") }
            composable(TopDest.Notes.route)     { Text("Notes — Phase 14") }
            composable(TopDest.Reminders.route) { Text("Reminders — Phase 15") }
            composable(TopDest.Settings.route)  { Text("Settings — Phase 16") }
        }
    }
}
