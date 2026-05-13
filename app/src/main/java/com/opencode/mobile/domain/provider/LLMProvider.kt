package com.opencode.mobile.domain.provider

import com.opencode.mobile.domain.model.Message
import com.opencode.mobile.domain.model.Model
import com.opencode.mobile.domain.model.Provider
import kotlinx.coroutines.flow.Flow

interface LLMProvider {
    suspend fun chat(messages: List<Message>, model: String? = null): String
    suspend fun chatStream(messages: List<Message>, model: String? = null): Flow<String>
    suspend fun getModels(): List<Model>
    suspend fun validateApiKey(): Boolean
    fun getProviderInfo(): Provider
}

interface ProviderConfig {
    val apiKey: String
    val baseUrl: String
    val defaultModel: String
}
