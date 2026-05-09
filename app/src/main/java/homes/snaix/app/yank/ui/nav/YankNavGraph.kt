package homes.snaix.app.yank.ui.nav

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import homes.snaix.app.yank.R
import homes.snaix.app.yank.YankApp
import homes.snaix.app.yank.domain.capture.PipelineOutcome
import homes.snaix.app.yank.ui.common.ProcessingCard
import homes.snaix.app.yank.ui.detail.RecordDetailScreen
import homes.snaix.app.yank.ui.notes.ManualNoteSheet
import homes.snaix.app.yank.ui.notes.NotesScreen
import homes.snaix.app.yank.ui.notes.NotesViewModel
import homes.snaix.app.yank.ui.records.RecordsScreen
import homes.snaix.app.yank.ui.reminders.RemindersScreen
import homes.snaix.app.yank.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

private const val DetailRoute = "detail/{id}"
private fun detailRouteFor(id: String) = "detail/$id"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun YankNavGraph() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val app = LocalContext.current.applicationContext as YankApp
    val scope = rememberCoroutineScope()

    var processingCount by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMsg = stringResource(R.string.recognize_saved)

    LaunchedEffect(Unit) {
        app.di.captureOutcomes.collect { outcome ->
            if (outcome is PipelineOutcome.Success) {
                snackbarHostState.showSnackbar(savedMsg)
            }
        }
    }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                processingCount++
                try {
                    val outcome = app.di.imagePickPipeline.process(uri)
                    app.di.captureOutcomeBus.emit(outcome)
                } finally {
                    processingCount--
                }
            }
        }
    }

    val notesVm: NotesViewModel = viewModel(factory = viewModelFactory {
        initializer { NotesViewModel(app.di.historyRepo) }
    })
    var showNoteSheet by remember { mutableStateOf(false) }

    val isTopLevel = TopDest.entries.any { it.route == currentRoute }
    val isDetail = currentRoute == DetailRoute

    val openDetail: (String) -> Unit = { id -> nav.navigate(detailRouteFor(id)) }

    Scaffold(
        topBar = {
            when {
                isTopLevel -> TopAppBar(title = { Text(topTitle(currentRoute)) })
                isDetail -> TopAppBar(
                    title = { Text(stringResource(R.string.detail_title)) },
                    navigationIcon = {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                        }
                    },
                )
                else -> TopAppBar(title = { Text("") })
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = BottomNavReservedHeight),
            ) { Snackbar(it) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavHost(
                nav,
                startDestination = TopDest.Records.route,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(TopDest.Records.route)   { RecordsScreen(onOpenDetail = openDetail) }
                composable(TopDest.Notes.route)     { NotesScreen(onOpenDetail = openDetail) }
                composable(TopDest.Reminders.route) { RemindersScreen(onOpenDetail = openDetail) }
                composable(TopDest.Settings.route)  { SettingsScreen() }
                composable(
                    DetailRoute,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    RecordDetailScreen(
                        entityId = entry.arguments!!.getString("id")!!,
                        onBack = { nav.popBackStack() },
                    )
                }
            }
            AnimatedVisibility(
                visible = isTopLevel && processingCount > 0,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
            ) {
                ProcessingCard()
            }
            if (isTopLevel) {
                BottomNav(
                    navController = nav,
                    trailingFab = trailingFab(currentRoute, pickImage::launch) { showNoteSheet = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                )
            }
        }
    }

    if (showNoteSheet) {
        ManualNoteSheet(
            onDismiss = { showNoteSheet = false },
            onSave = notesVm::saveManualNote,
        )
    }
}

@Composable
private fun topTitle(currentRoute: String?): String =
    TopDest.entries.firstOrNull { it.route == currentRoute }
        ?.let { stringResource(it.labelRes) }
        .orEmpty()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun trailingFab(
    currentRoute: String?,
    onPickImage: (String) -> Unit,
    onNewNote: () -> Unit,
): (@Composable RowScope.() -> Unit)? = when (currentRoute) {
    TopDest.Records.route -> {
        {
            FilledIconButton(
                onClick = { onPickImage("image/*") },
                modifier = Modifier.size(60.dp).padding(4.dp),
                shape = MaterialShapes.Cookie9Sided.toShape(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                ),
            ) { Icon(Icons.Outlined.PhotoCamera, contentDescription = stringResource(R.string.action_pick_image)) }
        }
    }
    TopDest.Notes.route -> {
        {
            FilledIconButton(
                onClick = onNewNote,
                modifier = Modifier.size(60.dp).padding(4.dp),
                shape = MaterialShapes.Cookie9Sided.toShape(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                ),
            ) { Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.action_new_note)) }
        }
    }
    else -> null
}
