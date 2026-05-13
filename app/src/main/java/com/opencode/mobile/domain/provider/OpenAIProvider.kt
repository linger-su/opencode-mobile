package com.opencode.mobile.domain.provider

import com.opencode.mobile.data.api.ApiClient
import com.opencode.mobile.data.api.ChatRequest
import com.opencode.mobile.data.api.Message as ApiMessage
import com.opencode.mobile.domain.model.Message
import com.opencode.mobile.domain.model.Model
import com.opencode.mobile.domain.model.Provider
import com.opencode.mobile.utils.LogManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAIProvider @Inject constructor() : LLMProvider {

    companion object {
        private const val TAG = "OpenAIProvider"
    }

    private var apiKey: String = ""
    private var baseUrl: String = "https://api.openai.com/v1/"
    private var defaultModel: String = "gpt-3.5-turbo"

    fun configure(apiKey: String, baseUrl: String? = null, defaultModel: String? = null) {
        this.apiKey = apiKey
        baseUrl?.let { this.baseUrl = it }
        defaultModel?.let { this.defaultModel = it }
        LogManager.i(TAG, "配置完成: baseUrl=$baseUrl, model=$defaultModel")
    }

    override suspend fun chat(messages: List<Message>, model: String?): String {
        LogManager.i(TAG, "开始调用 LLM: baseUrl=$baseUrl, model=${model ?: defaultModel}")

        if (apiKey.isBlank()) {
            val error = "API Key 未配置"
            LogManager.e(TAG, error)
            throw Exception(error)
        }

        val api = ApiClient.getApi(baseUrl)

        val apiMessages = messages.map { msg ->
            ApiMessage(
                role = msg.role.name.lowercase(),
                content = msg.content
            )
        }

        val request = ChatRequest(
            model = model ?: defaultModel,
            messages = apiMessages
        )

        LogManager.d(TAG, "发送请求: ${request.messages.size} 条消息")

        try {
            val response = api.chat("Bearer $apiKey", request)
            val content = response.choices.firstOrNull()?.message?.content ?: ""
            LogManager.i(TAG, "收到响应: ${content.take(100)}...")
            return content
        } catch (e: Exception) {
            LogManager.e(TAG, "调用失败: ${e.message}", e)
            throw e
        }
    }

    override suspend fun chatStream(messages: List<Message>, model: String?): Flow<String> = flow {
        emit(chat(messages, model))
    }

    override suspend fun getModels(): List<Model> {
        return listOf(
            Model("mimo-v2.5", "MiMo v2.5", "Xiaomi", "小米大模型"),
            Model("gpt-4", "GPT-4", "OpenAI", "最强大的模型"),
            Model("gpt-3.5-turbo", "GPT-3.5 Turbo", "OpenAI", "经济实惠")
        )
    }

    override suspend fun validateApiKey(): Boolean {
        return apiKey.isNotBlank()
    }

    override fun getProviderInfo(): Provider {
        return Provider(
            id = "openai",
            name = "OpenAI Compatible",
            description = "兼容 OpenAI API 的模型",
            baseUrl = baseUrl,
            requiresApiKey = true
        )
    }
}
