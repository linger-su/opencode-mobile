package com.opencode.mobile.core

import kotlinx.coroutines.flow.Flow

/**
 * 统一的 LLM Provider 接口
 * 所有模型提供商都需要实现此接口
 */
interface LLMProviderInterface {
    /**
     * 发送消息并获取响应
     */
    suspend fun chat(messages: List<ChatMessage>, config: ChatConfig? = null): ChatResponse

    /**
     * 流式发送消息
     */
    suspend fun chatStream(messages: List<ChatMessage>, config: ChatConfig? = null): Flow<String>

    /**
     * 获取可用模型列表
     */
    suspend fun getModels(): List<ModelInfo>

    /**
     * 验证 API Key
     */
    suspend fun validateApiKey(): Boolean

    /**
     * 获取提供商信息
     */
    fun getProviderInfo(): ProviderInfo
}

data class ChatMessage(
    val role: MessageRole,
    val content: String,
    val name: String? = null
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

data class ChatConfig(
    val model: String? = null,
    val temperature: Double = 0.7,
    val maxTokens: Int = 4096,
    val topP: Double = 1.0,
    val frequencyPenalty: Double = 0.0,
    val presencePenalty: Double = 0.0,
    val stop: List<String>? = null
)

data class ChatResponse(
    val content: String,
    val model: String,
    val usage: Usage? = null,
    val finishReason: String? = null
)

data class Usage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)

data class ModelInfo(
    val id: String,
    val name: String,
    val provider: String,
    val description: String = "",
    val maxTokens: Int = 4096,
    val supportsStreaming: Boolean = true,
    val supportsVision: Boolean = false,
    val supportsFunctions: Boolean = false
)

data class ProviderInfo(
    val id: String,
    val name: String,
    val description: String,
    val baseUrl: String,
    val requiresApiKey: Boolean,
    val models: List<ModelInfo> = emptyList()
)

/**
 * 命令解析器接口
 */
interface CommandParserInterface {
    /**
     * 解析用户输入为设备命令
     * @return 如果是命令返回 DeviceCommand，否则返回 null
     */
    suspend fun parse(input: String): DeviceCommandInterface?

    /**
     * 检查输入是否是命令
     */
    suspend fun isCommand(input: String): Boolean
}

data class DeviceCommandInterface(
    val type: CommandTypeInterface,
    val action: String,
    val params: Map<String, Any> = emptyMap()
)

enum class CommandTypeInterface {
    APP_CONTROL,
    SYSTEM_SETTINGS,
    NOTIFICATION,
    MEDIA,
    COMMUNICATION,
    FILE_OPERATION,
    SHELL_COMMAND,
    INPUT_SIMULATION,
    SCREEN_CONTROL
}

/**
 * 设备控制器接口
 */
interface DeviceControllerInterface {
    /**
     * 执行设备命令
     */
    suspend fun execute(command: DeviceCommandInterface): String

    /**
     * 打开应用
     */
    suspend fun openApp(packageName: String): Boolean

    /**
     * 关闭应用
     */
    suspend fun closeApp(packageName: String): Boolean

    /**
     * 设置亮度
     */
    suspend fun setBrightness(level: Int): Boolean

    /**
     * 设置音量
     */
    suspend fun setVolume(level: Int, stream: String = "music"): Boolean

    /**
     * 切换 WiFi
     */
    suspend fun toggleWifi(enable: Boolean): Boolean

    /**
     * 切换蓝牙
     */
    suspend fun toggleBluetooth(enable: Boolean): Boolean

    /**
     * 点击屏幕
     */
    suspend fun tap(x: Int, y: Int): Boolean

    /**
     * 滑动屏幕
     */
    suspend fun swipe(startX: Int, startY: Int, endX: Int, endY: Int, duration: Long = 300): Boolean

    /**
     * 输入文本
     */
    suspend fun inputText(text: String): Boolean

    /**
     * 截屏
     */
    suspend fun screenshot(): String?
}
