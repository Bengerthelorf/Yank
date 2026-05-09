package homes.snaix.app.yank.ui.nav

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import homes.snaix.app.yank.R
import homes.snaix.app.yank.YankApp
import homes.snaix.app.yank.ui.notes.ManualNoteSheet
import homes.snaix.app.yank.ui.notes.NotesScreen
import homes.snaix.app.yank.ui.notes.NotesViewModel
import homes.snaix.app.yank.ui.records.RecordsScreen
import homes.snaix.app.yank.ui.reminders.RemindersScreen
import homes.snaix.app.yank.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun YankNavGraph() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val ctx = LocalContext.current.applicationContext as YankApp
    val scope = rememberCoroutineScope()

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val outcome = ctx.di.imagePickPipeline.process(uri)
                ctx.di.captureOutcomeBus.emit(outcome)
            }
        }
    }

    val notesVm: NotesViewModel = viewModel(factory = viewModelFactory {
        initializer { NotesViewModel(ctx.di.historyRepo) }
    })
    var showNoteSheet by remember { mutableStateOf(false) }

    val title = when (currentRoute) {
        TopDest.Records.route   -> stringResource(R.string.tab_records)
        TopDest.Notes.route     -> stringResource(R.string.tab_notes)
        TopDest.Reminders.route -> stringResource(R.string.tab_reminders)
        TopDest.Settings.route  -> stringResource(R.string.tab_settings)
        else -> ""
    }

    val trailingFab: (@Composable RowScope.() -> Unit)? = when (currentRoute) {
        TopDest.Records.route -> {
            {
                FilledIconButton(
                    onClick = { pickImage.launch("image/*") },
                    modifier = Modifier
                        .size(56.dp)
                        .padding(4.dp),
                    shape = MaterialShapes.Cookie9Sided.toShape(),
                ) { Icon(Icons.Outlined.PhotoCamera, contentDescription = "选图识别") }
            }
        }
        TopDest.Notes.route -> {
            {
                FilledIconButton(
                    onClick = { showNoteSheet = true },
                    modifier = Modifier
                        .size(56.dp)
                        .padding(4.dp),
                    shape = MaterialShapes.Cookie9Sided.toShape(),
                ) { Icon(Icons.Outlined.Edit, contentDescription = "新建笔记") }
            }
        }
        else -> null
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                trailingFab = trailingFab,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
            )
        }
    }

    if (showNoteSheet) {
        ManualNoteSheet(
            onDismiss = { showNoteSheet = false },
            onSave = notesVm::saveManualNote,
        )
    }
}
