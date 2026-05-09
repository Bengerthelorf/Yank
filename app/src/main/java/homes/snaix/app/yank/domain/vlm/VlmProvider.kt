package homes.snaix.app.yank.domain.vlm

enum class VlmProvider(
    val displayName: String,
    val baseUrl: String,
    val fastModel: String,
    val capableModel: String,
) {
    DASHSCOPE(
        displayName = "百炼 (阿里)",
        baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1",
        fastModel = "qwen3-vl-flash",
        capableModel = "qwen3-vl-plus",
    ),
    VOLCENGINE(
        displayName = "火山方舟 (字节)",
        baseUrl = "https://ark.cn-beijing.volces.com/api/v3",
        fastModel = "doubao-seed-1-6-vision",
        capableModel = "doubao-1-5-vision-pro-32k-250115",
    ),
    ZHIPU(
        displayName = "智谱 GLM",
        baseUrl = "https://open.bigmodel.cn/api/paas/v4/",
        fastModel = "glm-4v-flash",
        capableModel = "glm-4v-plus",
    ),
    MOONSHOT(
        displayName = "月之暗面 (Kimi)",
        baseUrl = "https://api.moonshot.cn/v1",
        fastModel = "moonshot-v1-8k-vision-preview",
        capableModel = "moonshot-v1-32k-vision-preview",
    ),
    LINGYIWANWU(
        displayName = "零一万物 (Yi)",
        baseUrl = "https://api.lingyiwanwu.com/v1",
        fastModel = "yi-vision-v2",
        capableModel = "yi-vision-v2",
    ),
    SILICONFLOW(
        displayName = "硅基流动 (SiliconFlow)",
        baseUrl = "https://api.siliconflow.cn/v1",
        fastModel = "Qwen/Qwen2.5-VL-7B-Instruct",
        capableModel = "Qwen/Qwen2.5-VL-72B-Instruct",
    ),
    HUNYUAN(
        displayName = "腾讯混元",
        baseUrl = "https://api.hunyuan.cloud.tencent.com/v1",
        fastModel = "hunyuan-vision",
        capableModel = "hunyuan-vision",
    ),
    OPENAI(
        displayName = "OpenAI",
        baseUrl = "https://api.openai.com/v1",
        fastModel = "gpt-5.4-mini",
        capableModel = "gpt-5.5",
    ),
    ANTHROPIC(
        displayName = "Anthropic",
        baseUrl = "https://api.anthropic.com/v1/",
        fastModel = "claude-haiku-4-5",
        capableModel = "claude-sonnet-4-6",
    ),
    GEMINI(
        displayName = "Gemini",
        baseUrl = "https://generativelanguage.googleapis.com/v1beta/openai/",
        fastModel = "gemini-3.1-flash-lite",
        capableModel = "gemini-3.1-pro-preview",
    ),
    XAI(
        displayName = "xAI (Grok)",
        baseUrl = "https://api.x.ai/v1",
        fastModel = "grok-4.3",
        capableModel = "grok-4.3",
    );

    companion object {
        /** Match baseUrl back to a provider. Returns null if URL is custom (advanced override). */
        fun fromBaseUrl(url: String): VlmProvider? =
            entries.firstOrNull { url.trim().trimEnd('/') == it.baseUrl.trimEnd('/') }
    }
}

enum class ModelTier { FAST, CAPABLE }
