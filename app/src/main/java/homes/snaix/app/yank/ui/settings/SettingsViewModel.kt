package homes.snaix.app.yank.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import homes.snaix.app.yank.data.repo.ConfigRepository
import homes.snaix.app.yank.domain.schema.type
import homes.snaix.app.yank.domain.vlm.ModelTier
import homes.snaix.app.yank.domain.vlm.VlmClient
import homes.snaix.app.yank.domain.vlm.VlmConfig
import homes.snaix.app.yank.domain.vlm.VlmException
import homes.snaix.app.yank.domain.vlm.VlmProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class TestResult {
    data object Idle : TestResult()
    data object Running : TestResult()
    data object Ok : TestResult()
    data class Failed(val message: String) : TestResult()
}

class SettingsViewModel(
    private val ctx: Context,
    private val repo: ConfigRepository,
    private val vlm: VlmClient,
) : ViewModel() {

    private val _testState = MutableStateFlow<TestResult>(TestResult.Idle)
    val testState: StateFlow<TestResult> = _testState.asStateFlow()

    fun setBaseUrl(v: String) { viewModelScope.launch { repo.setBaseUrl(v) } }
    fun setModel(v: String) { viewModelScope.launch { repo.setModel(v) } }

    /** Atomically set baseUrl + model from a known provider preset. */
    fun setProvider(provider: VlmProvider, tier: ModelTier) {
        viewModelScope.launch {
            repo.setBaseUrl(provider.baseUrl)
            repo.setModel(
                when (tier) {
                    ModelTier.FAST -> provider.fastModel
                    ModelTier.CAPABLE -> provider.capableModel
                }
            )
        }
    }

    fun setSystemPrompt(v: String) { viewModelScope.launch { repo.setSystemPrompt(v) } }
    fun resetSystemPrompt() { viewModelScope.launch { repo.resetSystemPrompt() } }
    fun setApiKey(v: String?) { repo.setApiKey(v) }
    fun setScreenshotRetention(b: Boolean) { viewModelScope.launch { repo.setScreenshotRetention(b) } }
    fun setLockHide(b: Boolean) { viewModelScope.launch { repo.setLockHide(b) } }
    fun setArchiveRetentionDays(d: Int) { viewModelScope.launch { repo.setArchiveRetentionDays(d) } }

    fun testConnection() {
        viewModelScope.launch {
            _testState.value = TestResult.Running
            val snap = repo.observeVlmConfig().first()
            val key = snap.apiKey
            if (key.isNullOrBlank()) {
                _testState.value = TestResult.Failed("未填 API key")
                return@launch
            }
            val bytes = ctx.assets.open("test_screenshot.png").use { it.readBytes() }
            val cfg = VlmConfig(snap.baseUrl, key, snap.model, snap.systemPrompt)
            val res = vlm.recognize(cfg, bytes, emptyList())
            _testState.value = res.fold(
                onSuccess = { items ->
                    val ok = items.isNotEmpty() && items.first().type in setOf("排队","取餐","券码","快递","票券","待办","notes")
                    if (ok) TestResult.Ok else TestResult.Failed("返回非闭枚举 type")
                },
                onFailure = { e -> TestResult.Failed((e as? VlmException)?.error?.toString() ?: e.message ?: "unknown") }
            )
        }
    }
}
