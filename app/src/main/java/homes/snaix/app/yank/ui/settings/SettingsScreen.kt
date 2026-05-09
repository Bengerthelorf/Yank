package homes.snaix.app.yank.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import homes.snaix.app.yank.Defaults
import homes.snaix.app.yank.R
import homes.snaix.app.yank.YankApp
import homes.snaix.app.yank.domain.vlm.ModelTier
import homes.snaix.app.yank.domain.vlm.VlmProvider
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
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

    var currentProvider by remember { mutableStateOf<VlmProvider?>(null) }
    var currentTier by remember { mutableStateOf(ModelTier.CAPABLE) }
    var providerDropdownExpanded by remember { mutableStateOf(false) }
    var advancedExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val snap = ctx.di.configRepo.observeVlmConfig().first()
        baseUrl = snap.baseUrl
        model = snap.model
        apiKey = snap.apiKey.orEmpty()
        prompt = snap.systemPrompt
        keepScreenshot = ctx.di.configRepo.screenshotRetention().first()
        lockHide = ctx.di.configRepo.lockHide().first()

        // Derive provider/tier from current baseUrl + model
        val matched = VlmProvider.fromBaseUrl(snap.baseUrl)
        currentProvider = matched
        currentTier = if (matched != null && snap.model == matched.fastModel) {
            ModelTier.FAST
        } else {
            ModelTier.CAPABLE
        }
        // Custom URL → start with advanced section open so user sees their config
        if (matched == null) advancedExpanded = true
    }

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("API", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))

        // Provider dropdown
        Text("Provider", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
        val providerLabel = currentProvider?.displayName ?: "自定义"
        ExposedDropdownMenuBox(
            expanded = providerDropdownExpanded,
            onExpandedChange = { providerDropdownExpanded = it },
        ) {
            OutlinedTextField(
                value = providerLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Provider") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerDropdownExpanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = providerDropdownExpanded,
                onDismissRequest = { providerDropdownExpanded = false },
            ) {
                VlmProvider.entries.forEach { p ->
                    DropdownMenuItem(
                        text = { Text(p.displayName) },
                        onClick = {
                            currentProvider = p
                            // Apply tier preset
                            baseUrl = p.baseUrl
                            model = when (currentTier) {
                                ModelTier.FAST -> p.fastModel
                                ModelTier.CAPABLE -> p.capableModel
                            }
                            vm.setProvider(p, currentTier)
                            providerDropdownExpanded = false
                        },
                    )
                }
                if (currentProvider == null) {
                    DropdownMenuItem(
                        text = { Text("自定义") },
                        onClick = { providerDropdownExpanded = false },
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Tier toggle - only when a known provider is selected
        AnimatedVisibility(visible = currentProvider != null) {
            val provider = currentProvider
            Column {
                Text("Model tier", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 4.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val tiers = listOf(ModelTier.FAST, ModelTier.CAPABLE)
                    tiers.forEachIndexed { index, tier ->
                        SegmentedButton(
                            selected = currentTier == tier,
                            onClick = {
                                currentTier = tier
                                provider?.let {
                                    model = when (tier) {
                                        ModelTier.FAST -> it.fastModel
                                        ModelTier.CAPABLE -> it.capableModel
                                    }
                                    vm.setProvider(it, tier)
                                }
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = tiers.size),
                        ) {
                            Text(if (tier == ModelTier.FAST) "快" else "准")
                        }
                    }
                }
                if (provider != null) {
                    val resolvedModel = when (currentTier) {
                        ModelTier.FAST -> provider.fastModel
                        ModelTier.CAPABLE -> provider.capableModel
                    }
                    val tierTag = if (currentTier == ModelTier.FAST) "快" else "准"
                    Text(
                        "$tierTag: $resolvedModel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it; vm.setApiKey(it) },
            label = { Text("API Key") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
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

        Spacer(Modifier.height(16.dp))

        // Advanced expander
        TextButton(onClick = { advancedExpanded = !advancedExpanded }) {
            Icon(
                imageVector = if (advancedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
            )
            Spacer(Modifier.height(0.dp))
            Text(" 高级 / Advanced")
        }

        AnimatedVisibility(visible = advancedExpanded) {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { v ->
                        baseUrl = v
                        vm.setBaseUrl(v)
                        // Re-derive provider; if doesn't match any preset, drop tier UI
                        currentProvider = VlmProvider.fromBaseUrl(v)
                    },
                    label = { Text("Base URL") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = model,
                    onValueChange = { v ->
                        model = v
                        vm.setModel(v)
                        // De-sync tier if model no longer matches the provider preset
                        val p = currentProvider
                        if (p != null && v != p.fastModel && v != p.capableModel) {
                            currentProvider = null
                        }
                    },
                    label = { Text("Model") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "直接编辑会切换到「自定义」",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
        Spacer(Modifier.height(96.dp))
    }
}
