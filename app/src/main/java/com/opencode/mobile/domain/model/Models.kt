package com.opencode.mobile.domain.model

import java.util.UUID

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, Any> = emptyMap()
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Conversation",
    val messages: List<Message> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Model(
    val id: String,
    val name: String,
    val provider: String,
    val description: String = "",
    val maxTokens: Int = 4096,
    val supportsStreaming: Boolean = true
)

data class Provider(
    val id: String,
    val name: String,
    val description: String = "",
    val baseUrl: String = "",
    val requiresApiKey: Boolean = true,
    val models: List<Model> = emptyList()
)
