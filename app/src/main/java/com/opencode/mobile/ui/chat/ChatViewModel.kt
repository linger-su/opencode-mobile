package com.opencode.mobile.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opencode.mobile.domain.model.Message
import com.opencode.mobile.domain.model.MessageRole
import com.opencode.mobile.domain.provider.LLMProvider
import com.opencode.mobile.domain.provider.OpenAIProvider
import com.opencode.mobile.domain.parser.CommandParser
import com.opencode.mobile.domain.controller.DeviceController
import com.opencode.mobile.data.repository.ChatRepository
import com.opencode.mobile.data.repository.SettingsRepository
import com.opencode.mobile.utils.LogManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isConfigured: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val llmProvider: LLMProvider,
    private val commandParser: CommandParser,
    private val deviceController: DeviceController,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
    }

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        LogManager.i(TAG, "ChatViewModel 初始化")
        observeSettings()
        observeMessages()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.apiKey,
                settingsRepository.baseUrl,
                settingsRepository.model
            ) { apiKey, baseUrl, model ->
                Triple(apiKey, baseUrl, model)
            }.collect { (apiKey, baseUrl, model) ->
                LogManager.d(TAG, "设置更新: apiKey=${apiKey.take(10)}..., baseUrl=$baseUrl, model=$model")
                if (apiKey.isNotBlank() && baseUrl.isNotBlank()) {
                    (llmProvider as? OpenAIProvider)?.configure(apiKey, baseUrl, model)
                    _uiState.update { it.copy(isConfigured = true) }
                    LogManager.i(TAG, "LLM Provider 已配置")
                }
            }
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            chatRepository.getMessages().collect { messages ->
                LogManager.d(TAG, "消息更新: ${messages.size} 条")
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    fun updateInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) {
            LogManager.w(TAG, "消息为空，忽略")
            return
        }

        LogManager.i(TAG, "发送消息: ${text.take(50)}...")

        viewModelScope.launch {
            _uiState.update { it.copy(inputText = "", isLoading = true, error = null) }

            val userMessage = Message(
                role = MessageRole.USER,
                content = text
            )

            try {
                chatRepository.addMessage(userMessage)
                LogManager.d(TAG, "用户消息已保存")
            } catch (e: Exception) {
                LogManager.e(TAG, "保存用户消息失败", e)
            }

            try {
                // 检查是否是设备控制指令
                val command = commandParser.parse(text)

                if (command != null) {
                    LogManager.i(TAG, "识别为设备指令: ${command.type}/${command.action}")
                    val result = deviceController.execute(command)
                    LogManager.i(TAG, "设备指令执行结果: $result")
                    val assistantMessage = Message(
                        role = MessageRole.ASSISTANT,
                        content = result
                    )
                    chatRepository.addMessage(assistantMessage)
                } else {
                    LogManager.i(TAG, "识别为普通对话，调用 LLM")

                    if (!_uiState.value.isConfigured) {
                        throw Exception("请先在设置中配置 API Key 和模型地址")
                    }

                    val messages = _uiState.value.messages + userMessage
                    LogManager.d(TAG, "发送到 LLM，消息数: ${messages.size}")

                    val response = llmProvider.chat(messages)
                    LogManager.i(TAG, "LLM 响应成功")

                    val assistantMessage = Message(
                        role = MessageRole.ASSISTANT,
                        content = response
                    )
                    chatRepository.addMessage(assistantMessage)
                }
            } catch (e: Exception) {
                LogManager.e(TAG, "处理消息失败", e)
                val errorMsg = e.message ?: "未知错误"
                _uiState.update { it.copy(error = errorMsg) }

                try {
                    val errorMessage = Message(
                        role = MessageRole.ASSISTANT,
                        content = "错误: $errorMsg"
                    )
                    chatRepository.addMessage(errorMessage)
                } catch (addError: Exception) {
                    LogManager.e(TAG, "保存错误消息失败", addError)
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
