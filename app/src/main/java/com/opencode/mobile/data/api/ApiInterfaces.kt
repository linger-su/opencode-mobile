package com.opencode.mobile.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIApi {
    @POST("chat/completions")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): ChatResponse
}

data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 4096,
    val stream: Boolean = false
)

data class Message(
    val role: String,
    val content: String
)

data class ChatResponse(
    val id: String?,
    val choices: List<Choice>
)

data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String?
)

object ApiClient {
    private var currentBaseUrl: String = ""
    private var currentApi: OpenAIApi? = null

    fun getApi(baseUrl: String): OpenAIApi {
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        if (normalizedUrl != currentBaseUrl || currentApi == null) {
            currentBaseUrl = normalizedUrl
            currentApi = Retrofit.Builder()
                .baseUrl(normalizedUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenAIApi::class.java)
        }
        return currentApi!!
    }
}
