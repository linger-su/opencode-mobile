package com.opencode.mobile.ui.log

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opencode.mobile.utils.LogManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogUiState(
    val logs: List<LogManager.LogEntry> = emptyList(),
    val filterLevel: LogManager.LogLevel? = null,
    val exportPath: String = "",
    val exportStatus: String = ""
)

@HiltViewModel
class LogViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "LogViewModel"
    }

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    init {
        LogManager.i(TAG, "LogViewModel 初始化")
        loadLogs()
    }

    private fun loadLogs() {
        viewModelScope.launch {
            LogManager.logs.collect { logs ->
                val filterLevel = _uiState.value.filterLevel
                val filteredLogs = if (filterLevel != null) {
                    logs.filter { it.level == filterLevel }
                } else {
                    logs
                }
                _uiState.update { it.copy(logs = filteredLogs.reversed()) }
            }
        }
    }

    fun setFilterLevel(level: LogManager.LogLevel?) {
        LogManager.d(TAG, "设置过滤级别: $level")
        _uiState.update { it.copy(filterLevel = level) }
        loadLogs()
    }

    fun setExportPath(path: String) {
        _uiState.update { it.copy(exportPath = path) }
    }

    fun exportLogs() {
        val path = _uiState.value.exportPath
        if (path.isBlank()) {
            _uiState.update { it.copy(exportStatus = "请输入导出路径") }
            return
        }

        LogManager.i(TAG, "导出日志到: $path")
        viewModelScope.launch {
            try {
                val success = LogManager.exportTo(context, path)
                if (success) {
                    _uiState.update { it.copy(exportStatus = "导出成功") }
                } else {
                    _uiState.update { it.copy(exportStatus = "导出失败") }
                }
            } catch (e: Exception) {
                LogManager.e(TAG, "导出日志失败", e)
                _uiState.update { it.copy(exportStatus = "导出失败: ${e.message}") }
            }
        }
    }

    fun clearLogs() {
        LogManager.i(TAG, "清除日志")
        LogManager.clearLogs()
    }

    fun clearExportStatus() {
        _uiState.update { it.copy(exportStatus = "") }
    }
}
