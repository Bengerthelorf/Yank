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
import androidx.compose.ui.unit.dp
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
            Text("识别失败", style = MaterialTheme.typography.titleMedium)
            when (outcome) {
                is PipelineOutcome.ParseFailure -> {
                    Text("模型返回内容无法解析为闭枚举 type。", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                    Text(outcome.raw.take(800), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                    Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { current = null }) { Text("丢弃") }
                        TextButton(onClick = { current = null }) { Text("存为 notes") }
                        Button(onClick = { current = null }) { Text("重试") }
                    }
                }
                is PipelineOutcome.TransportFailure -> {
                    Text("网络/调用失败：${outcome.error}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                    Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                        Button(onClick = { current = null }) { Text("好的") }
                    }
                }
                else -> Unit
            }
        }
    }
}
