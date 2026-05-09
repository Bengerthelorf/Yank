package homes.snaix.app.yank.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import homes.snaix.app.yank.Defaults
import homes.snaix.app.yank.R
import homes.snaix.app.yank.YankApp
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen() {
    val ctx = LocalContext.current.applicationContext as YankApp
    val vm: SettingsViewModel = viewModel(factory = viewModelFactory {
        initializer { SettingsViewModel(ctx, ctx.di.configRepo, ctx.di.vlmClient) }
    })
    val test by vm.testState.collectAsState()

    var baseUrl by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var keepScreenshot by remember { mutableStateOf(Defaults.SCREENSHOT_RETENTION_DEFAULT) }
    var lockHide by remember { mutableStateOf(Defaults.LOCK_HIDE_DEFAULT) }

    LaunchedEffect(Unit) {
        val snap = ctx.di.configRepo.observeVlmConfig().first()
        baseUrl = snap.baseUrl
        model = snap.model
        apiKey = snap.apiKey.orEmpty()
        prompt = snap.systemPrompt
        keepScreenshot = ctx.di.configRepo.screenshotRetention().first()
        lockHide = ctx.di.configRepo.lockHide().first()
    }

    Scaffold(topBar = { LargeTopAppBar(title = { Text(stringResource(R.string.tab_settings)) }) }) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {

            Text("API", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
            OutlinedTextField(value = baseUrl, onValueChange = { baseUrl = it; vm.setBaseUrl(it) }, label = { Text("Base URL") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = model, onValueChange = { model = it; vm.setModel(it) }, label = { Text("Model") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = apiKey, onValueChange = { apiKey = it; vm.setApiKey(it) }, label = { Text("API Key") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = { vm.testConnection() }) { Text(stringResource(R.string.action_test_connection)) }

            when (val s = test) {
                TestResult.Idle -> Spacer(Modifier.height(8.dp))
                TestResult.Running -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    LoadingIndicator()
                    Text("测试中…")
                }
                TestResult.Ok -> Text("连接正常 ✓", modifier = Modifier.padding(top = 8.dp))
                is TestResult.Failed -> Text("失败：${s.message}", modifier = Modifier.padding(top = 8.dp))
            }

            HorizontalDivider(Modifier.padding(vertical = 24.dp))

            Text("Prompt", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = prompt, onValueChange = { prompt = it; vm.setSystemPrompt(it) }, label = { Text("System Prompt") }, modifier = Modifier.fillMaxWidth().height(280.dp))
            OutlinedButton(onClick = { vm.resetSystemPrompt(); prompt = Defaults.SYSTEM_PROMPT }) { Text(stringResource(R.string.action_restore_default)) }

            HorizontalDivider(Modifier.padding(vertical = 24.dp))

            Text("行为", style = MaterialTheme.typography.titleMedium)
            ListItem(
                headlineContent = { Text("截图保留") },
                trailingContent = { Switch(checked = keepScreenshot, onCheckedChange = { keepScreenshot = it; vm.setScreenshotRetention(it) }) },
            )
            ListItem(
                headlineContent = { Text("锁屏隐藏敏感信息") },
                trailingContent = { Switch(checked = lockHide, onCheckedChange = { lockHide = it; vm.setLockHide(it) }) },
            )
        }
    }
}
