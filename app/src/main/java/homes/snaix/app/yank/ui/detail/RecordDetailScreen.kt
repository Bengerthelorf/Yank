package homes.snaix.app.yank.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import homes.snaix.app.yank.R
import homes.snaix.app.yank.YankApp
import homes.snaix.app.yank.data.db.HistoryEntity
import homes.snaix.app.yank.ui.common.EmptyState
import homes.snaix.app.yank.ui.common.ScreenshotImage
import homes.snaix.app.yank.ui.common.rememberCopyEntity
import homes.snaix.app.yank.ui.records.RecordType
import homes.snaix.app.yank.ui.theme.LocalTypeColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordDetailScreen(
    entityId: String,
    onBack: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as YankApp
    val vm: RecordDetailViewModel = viewModel(factory = viewModelFactory {
        initializer { RecordDetailViewModel(app.di.historyRepo, entityId) }
    })
    val state by vm.state.collectAsState()
    val copy = rememberCopyEntity()

    when (val s = state) {
        DetailState.Loading -> Box(modifier = Modifier.fillMaxSize())
        DetailState.Missing -> {
            LaunchedEffect(Unit) { onBack() }
            EmptyState(
                icon = Icons.Outlined.History,
                title = stringResource(R.string.detail_missing_title),
                subtitle = stringResource(R.string.detail_missing_subtitle),
            )
        }
        is DetailState.Loaded -> Loaded(
            entity = s.entity,
            onCopy = { copy(s.entity) },
            onArchive = if (s.entity.type != "notes") {
                {
                    vm.archive()
                    onBack()
                }
            } else null,
            onDelete = {
                vm.delete()
                onBack()
            },
        )
    }
}

@Composable
private fun Loaded(
    entity: HistoryEntity,
    onCopy: () -> Unit,
    onArchive: (() -> Unit)?,
    onDelete: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { TypeChipRow(entity) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        entity.displayPrimary,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    entity.displaySecondary?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            entity.screenshotPath?.let { path ->
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().aspectRatio(0.7f),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        ScreenshotImage(
                            path = path,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                        )
                    }
                }
            }
            item {
                Text(
                    text = stringResource(
                        R.string.detail_created_at,
                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(entity.createdAt)),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        ActionBar(
            onCopy = onCopy,
            onArchive = onArchive,
            onDelete = onDelete,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun TypeChipRow(entity: HistoryEntity) {
    val typeColors = LocalTypeColors.current
    val knownType = RecordType.fromDiscriminator(entity.type)
    val role = if (knownType != null) typeColors.roleFor(knownType) else typeColors.notes
    val typeLabel = when {
        knownType != null    -> stringResource(knownType.labelRes)
        entity.type == "notes" -> stringResource(R.string.type_label_notes)
        else                 -> entity.type
    }
    AssistChip(
        onClick = {},
        label = { Text(typeLabel) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = role.container,
            labelColor = role.onContainer,
        ),
    )
}

@Composable
private fun ActionBar(
    onCopy: () -> Unit,
    onArchive: (() -> Unit)?,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            FilledTonalButton(onClick = onCopy) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.action_copy))
            }
            if (onArchive != null) {
                FilledTonalButton(onClick = onArchive) {
                    Icon(Icons.Outlined.Archive, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.action_archive))
                }
            }
            FilledTonalButton(
                onClick = onDelete,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.action_delete))
            }
        }
    }
}
