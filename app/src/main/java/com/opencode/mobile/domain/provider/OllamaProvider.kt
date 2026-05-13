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
class OllamaProvider @Inject constructor() : LLMProvider {

    companion object {
        private const val TAG = "OllamaProvider"
    }

    private var baseUrl: String = "http://localhost:11434/"
    private var defaultModel: String = "llama2"

    fun configure(baseUrl: String, defaultModel: String? = null) {
        this.baseUrl = baseUrl
        defaultModel?.let { this.defaultModel = it }
        LogManager.i(TAG, "配置完成")
    }

    override suspend fun chat(messages: List<Message>, model: String?): String {
        LogManager.i(TAG, "Ollama 暂不支持，请使用 OpenAI 兼容模式")
        throw Exception("Ollama 暂不支持，请在设置中使用 OpenAI 兼容模式")
    }

    override suspend fun chatStream(messages: List<Message>, model: String?): Flow<String> = flow {
        emit(chat(messages, model))
    }

    override suspend fun getModels(): List<Model> {
        return listOf(
            Model("llama2", "Llama 2", "Meta"),
            Model("mistral", "Mistral", "Mistral AI")
        )
    }

    override suspend fun validateApiKey(): Boolean = true

    override fun getProviderInfo(): Provider {
        return Provider(
            id = "ollama",
            name = "Ollama",
            description = "本地模型",
            baseUrl = baseUrl,
            requiresApiKey = false
        )
    }
}
