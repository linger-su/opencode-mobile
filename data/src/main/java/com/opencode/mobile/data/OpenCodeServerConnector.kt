package com.opencode.mobile.data

import com.opencode.mobile.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.TimeUnit

/**
 * OpenCode Server 连接器
 * 用于连接 PC 端的 OpenCode 服务器
 */
class OpenCodeServerConnector(
    private var baseUrl: String = "http://localhost:4096",
    private var username: String = "opencode",
    private var password: String? = null
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    fun configure(url: String, username: String = "opencode", password: String? = null) {
        this.baseUrl = url.trimEnd('/')
        this.username = username
        this.password = password
    }

    /**
     * 检查服务器健康状态
     */
    suspend fun checkHealth(): Boolean {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/global/health")
                .addAuthHeader()
                .get()
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取服务器版本
     */
    suspend fun getVersion(): String? {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/global/health")
                .addAuthHeader()
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                val json = JSONObject(body ?: "{}")
                json.optString("version")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 创建新会话
     */
    suspend fun createSession(title: String? = null): String? {
        return try {
            val body = JSONObject().apply {
                title?.let { put("title", it) }
            }

            val request = Request.Builder()
                .url("$baseUrl/session")
                .addAuthHeader()
                .post(body.toString().toRequestBody(mediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody ?: "{}")
                json.optString("id")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 发送消息
     */
    suspend fun sendMessage(sessionId: String, message: String): String? {
        return try {
            val body = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("type", "text")
                        put("text", message)
                    })
                })
            }

            val request = Request.Builder()
                .url("$baseUrl/session/$sessionId/message")
                .addAuthHeader()
                .post(body.toString().toRequestBody(mediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody ?: "{}")
                val info = json.optJSONObject("info")
                val parts = json.optJSONArray("parts")

                // 提取响应文本
                val result = StringBuilder()
                parts?.let { partsArray ->
                    for (i in 0 until partsArray.length()) {
                        val part = partsArray.getJSONObject(i)
                        if (part.optString("type") == "text") {
                            result.append(part.optString("text"))
                        }
                    }
                }

                result.toString()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取会话列表
     */
    suspend fun getSessions(): List<SessionInfo> {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/session")
                .addAuthHeader()
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val jsonArray = JSONArray(responseBody ?: "[]")

                (0 until jsonArray.length()).map { i ->
                    val json = jsonArray.getJSONObject(i)
                    SessionInfo(
                        id = json.optString("id"),
                        title = json.optString("title")
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 获取消息列表
     */
    suspend fun getMessages(sessionId: String, limit: Int = 50): List<MessageInfo> {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/session/$sessionId/message?limit=$limit")
                .addAuthHeader()
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val jsonArray = JSONArray(responseBody ?: "[]")

                (0 until jsonArray.length()).map { i ->
                    val json = jsonArray.getJSONObject(i)
                    val info = json.optJSONObject("info")
                    val parts = json.optJSONArray("parts")

                    val content = StringBuilder()
                    parts?.let { partsArray ->
                        for (j in 0 until partsArray.length()) {
                            val part = partsArray.getJSONObject(j)
                            if (part.optString("type") == "text") {
                                content.append(part.optString("text"))
                            }
                        }
                    }

                    MessageInfo(
                        id = info?.optString("id") ?: "",
                        role = info?.optString("role") ?: "",
                        content = content.toString()
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 获取可用模型
     */
    suspend fun getModels(): List<ModelInfo> {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/config/providers")
                .addAuthHeader()
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody ?: "{}")
                val providers = json.optJSONArray("providers")

                val models = mutableListOf<ModelInfo>()
                providers?.let { providersArray ->
                    for (i in 0 until providersArray.length()) {
                        val provider = providersArray.getJSONObject(i)
                        val providerName = provider.optString("name")
                        val modelsArray = provider.optJSONArray("models")

                        modelsArray?.let { modelsArray ->
                            for (j in 0 until modelsArray.length()) {
                                val model = modelsArray.getJSONObject(j)
                                models.add(
                                    ModelInfo(
                                        id = model.optString("id"),
                                        name = model.optString("name"),
                                        provider = providerName,
                                        description = model.optString("description")
                                    )
                                )
                            }
                        }
                    }
                }

                models
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun Request.Builder.addAuthHeader(): Request.Builder {
        password?.let { pass ->
            val credentials = okhttp3.Credentials.basic(username, pass)
            addHeader("Authorization", credentials)
        }
        return this
    }
}

data class SessionInfo(
    val id: String,
    val title: String
)

data class MessageInfo(
    val id: String,
    val role: String,
    val content: String
)
