package com.opencode.mobile.domain.provider

import com.opencode.mobile.domain.model.Message
import com.opencode.mobile.domain.model.Model
import com.opencode.mobile.domain.model.Provider
import com.opencode.mobile.utils.LogManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClaudeProvider @Inject constructor() : LLMProvider {

    companion object {
        private const val TAG = "ClaudeProvider"
    }

    private var apiKey: String = ""
    private var baseUrl: String = "https://api.anthropic.com/"
    private var defaultModel: String = "claude-3-sonnet-20240229"

    fun configure(apiKey: String, baseUrl: String? = null, defaultModel: String? = null) {
        this.apiKey = apiKey
        baseUrl?.let { this.baseUrl = it }
        defaultModel?.let { this.defaultModel = it }
        LogManager.i(TAG, "配置完成")
    }

    override suspend fun chat(messages: List<Message>, model: String?): String {
        LogManager.i(TAG, "Claude 暂不支持，请使用 OpenAI 兼容模式")
        throw Exception("Claude 暂不支持，请在设置中使用 OpenAI 兼容模式")
    }

    override suspend fun chatStream(messages: List<Message>, model: String?): Flow<String> = flow {
        emit(chat(messages, model))
    }

    override suspend fun getModels(): List<Model> {
        return listOf(
            Model("claude-3-opus", "Claude 3 Opus", "Anthropic"),
            Model("claude-3-sonnet", "Claude 3 Sonnet", "Anthropic")
        )
    }

    override suspend fun validateApiKey(): Boolean = apiKey.isNotBlank()

    override fun getProviderInfo(): Provider {
        return Provider(
            id = "claude",
            name = "Claude",
            description = "Anthropic Claude",
            baseUrl = baseUrl,
            requiresApiKey = true
        )
    }
}
