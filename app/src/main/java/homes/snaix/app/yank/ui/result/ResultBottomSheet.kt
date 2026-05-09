package homes.snaix.app.yank.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import homes.snaix.app.yank.R
import homes.snaix.app.yank.YankApp
import homes.snaix.app.yank.domain.capture.PipelineOutcome

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultBottomSheetHost() {
    val ctx = LocalContext.current.applicationContext as YankApp
    val outcomes = ctx.di.captureOutcomes
    var current by remember { mutableStateOf<PipelineOutcome?>(null) }

    LaunchedEffect(outcomes) {
        outcomes.collect { o ->
            // Show sheet only on parse / transport failure; success silently writes to history
            if (o is PipelineOutcome.ParseFailure || o is PipelineOutcome.TransportFailure) {
                current = o
            }
        }
    }

    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val outcome = current ?: return

    ModalBottomSheet(onDismissRequest = { current = null }, sheetState = state) {
        Column(Modifier.padding(24.dp)) {
            Text(stringResource(R.string.result_recognize_failed), style = MaterialTheme.typography.titleMedium)
            when (outcome) {
                is PipelineOutcome.ParseFailure -> {
                    Text(stringResource(R.string.result_parse_failure_message), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                    Text(outcome.raw.take(800), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                    Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { current = null }) { Text(stringResource(R.string.action_discard)) }
                        TextButton(onClick = { current = null }) { Text(stringResource(R.string.action_save_as_notes)) }
                        Button(onClick = { current = null }) { Text(stringResource(R.string.action_retry)) }
                    }
                }
                is PipelineOutcome.TransportFailure -> {
                    Text(stringResource(R.string.result_transport_failure_message, outcome.error.toString()), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                    Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                        Button(onClick = { current = null }) { Text(stringResource(R.string.action_ok)) }
                    }
                }
                else -> Unit
            }
        }
    }
}
