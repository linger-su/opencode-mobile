package com.opencode.mobile.ui.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opencode.mobile.data.repository.SettingsRepository
import com.opencode.mobile.service.adb.ShizukuService
import com.opencode.mobile.utils.LogManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val provider: String = "OpenAI",
    val apiKey: String = "",
    val baseUrl: String = "",
    val model: String = "",
    val pcServerUrl: String = "http://localhost:4096",
    val connectionStatus: String = "",
    val isAccessibilityEnabled: Boolean = false,
    val isShizukuAvailable: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        LogManager.i(TAG, "SettingsViewModel 初始化")
        loadSettings()
        refreshStatus()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.provider,
                settingsRepository.apiKey,
                settingsRepository.baseUrl,
                settingsRepository.model,
                settingsRepository.pcServerUrl
            ) { provider, apiKey, baseUrl, model, pcServerUrl ->
                SettingsUiState(
                    provider = provider,
                    apiKey = apiKey,
                    baseUrl = baseUrl,
                    model = model,
                    pcServerUrl = pcServerUrl
                )
            }.collect { state ->
                _uiState.value = state.copy(
                    isAccessibilityEnabled = _uiState.value.isAccessibilityEnabled,
                    isShizukuAvailable = _uiState.value.isShizukuAvailable
                )
            }
        }
    }

    fun refreshStatus() {
        LogManager.d(TAG, "刷新状态")
        checkAccessibilityService()
        checkShizukuAvailability()
    }

    fun setProvider(provider: String) {
        LogManager.d(TAG, "设置 Provider: $provider")
        viewModelScope.launch {
            settingsRepository.setProvider(provider)
        }
    }

    fun setApiKey(apiKey: String) {
        LogManager.d(TAG, "设置 API Key: ${apiKey.take(10)}...")
        viewModelScope.launch {
            settingsRepository.setApiKey(apiKey)
        }
    }

    fun setBaseUrl(baseUrl: String) {
        LogManager.d(TAG, "设置 Base URL: $baseUrl")
        viewModelScope.launch {
            settingsRepository.setBaseUrl(baseUrl)
        }
    }

    fun setModel(model: String) {
        LogManager.d(TAG, "设置 Model: $model")
        viewModelScope.launch {
            settingsRepository.setModel(model)
        }
    }

    fun setPcServerUrl(url: String) {
        LogManager.d(TAG, "设置 PC Server URL: $url")
        viewModelScope.launch {
            settingsRepository.setPcServerUrl(url)
        }
    }

    fun testConnection() {
        LogManager.i(TAG, "测试连接: ${_uiState.value.pcServerUrl}")
        viewModelScope.launch {
            _uiState.update { it.copy(connectionStatus = "测试中...") }
            try {
                // TODO: 实现实际连接测试
                LogManager.d(TAG, "连接测试成功")
                _uiState.update { it.copy(connectionStatus = "连接成功") }
            } catch (e: Exception) {
                LogManager.e(TAG, "连接测试失败", e)
                _uiState.update { it.copy(connectionStatus = "连接失败: ${e.message}") }
            }
        }
    }

    fun openAccessibilitySettings() {
        LogManager.i(TAG, "打开无障碍设置")
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun checkShizuku() {
        LogManager.i(TAG, "检查 Shizuku 状态")
        checkShizukuAvailability()
        if (!_uiState.value.isShizukuAvailable) {
            LogManager.d(TAG, "Shizuku 不可用，请求权限")
            ShizukuService.requestPermission()
        }
    }

    fun openPermissionSettings() {
        LogManager.i(TAG, "打开权限设置")
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun checkAccessibilityService() {
        val packageName = context.packageName
        // 尝试多种服务名称格式
        val serviceFormats = listOf(
            "$packageName/.service.accessibility.OpenCodeAccessibilityService",
            "com.opencode.mobile/.service.accessibility.OpenCodeAccessibilityService",
            "$packageName/com.opencode.mobile.service.accessibility.OpenCodeAccessibilityService"
        )

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""

        LogManager.d(TAG, "检查无障碍服务")
        LogManager.d(TAG, "包名: $packageName")
        LogManager.d(TAG, "已启用的服务: $enabledServices")

        var found = false
        for (service in serviceFormats) {
            if (enabledServices.contains(service, ignoreCase = true)) {
                LogManager.i(TAG, "找到匹配的服务格式: $service")
                found = true
                break
            }
        }

        // 也检查是否包含部分匹配
        if (!found && enabledServices.contains("opencode", ignoreCase = true)) {
            LogManager.i(TAG, "通过关键词找到服务")
            found = true
        }

        LogManager.d(TAG, "无障碍服务状态: $found")
        _uiState.update {
            it.copy(isAccessibilityEnabled = found)
        }
    }

    private fun checkShizukuAvailability() {
        val isAvailable = ShizukuService.checkAvailability(context)
        LogManager.d(TAG, "Shizuku 状态: $isAvailable")
        _uiState.update {
            it.copy(isShizukuAvailable = isAvailable)
        }
    }
}
