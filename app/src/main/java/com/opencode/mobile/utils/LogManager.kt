package com.opencode.mobile.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogManager {
    private const val TAG = "OpenCodeMobile"
    private const val MAX_LOG_ENTRIES = 1000

    data class LogEntry(
        val timestamp: Long = System.currentTimeMillis(),
        val level: LogLevel,
        val tag: String,
        val message: String,
        val throwable: Throwable? = null
    ) {
        val formattedTime: String
            get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))

        val formattedMessage: String
            get() = "[$formattedTime] $level/$tag: $message"
    }

    enum class LogLevel(val icon: String) {
        DEBUG("D"),
        INFO("I"),
        WARN("W"),
        ERROR("E")
    }

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private var logFile: File? = null
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return

        val logDir = File(context.getExternalFilesDir(null), "logs")
        if (!logDir.exists()) {
            logDir.mkdirs()
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fileName = "opencode_${dateFormat.format(Date())}.log"
        logFile = File(logDir, fileName)

        isInitialized = true
        i(TAG, "日志系统初始化完成，日志文件: ${logFile?.absolutePath}")
    }

    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        val entry = LogEntry(
            level = level,
            tag = tag,
            message = message,
            throwable = throwable
        )

        // 更新内存中的日志
        _logs.value = (_logs.value + entry).takeLast(MAX_LOG_ENTRIES)

        // 输出到 Logcat
        when (level) {
            LogLevel.DEBUG -> Log.d(tag, message, throwable)
            LogLevel.INFO -> Log.i(tag, message, throwable)
            LogLevel.WARN -> Log.w(tag, message, throwable)
            LogLevel.ERROR -> Log.e(tag, message, throwable)
        }

        // 写入文件
        writeToFile(entry)
    }

    fun d(tag: String, message: String) = log(LogLevel.DEBUG, tag, message)
    fun i(tag: String, message: String) = log(LogLevel.INFO, tag, message)
    fun w(tag: String, message: String) = log(LogLevel.WARN, tag, message)
    fun e(tag: String, message: String, throwable: Throwable? = null) = log(LogLevel.ERROR, tag, message, throwable)

    private fun writeToFile(entry: LogEntry) {
        try {
            logFile?.let { file ->
                FileWriter(file, true).use { writer ->
                    writer.appendLine(entry.formattedMessage)
                    entry.throwable?.let { throwable ->
                        writer.appendLine(Log.getStackTraceString(throwable))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "写入日志文件失败", e)
        }
    }

    fun exportTo(context: Context, targetPath: String): Boolean {
        return try {
            val sourceFile = logFile ?: return false
            if (!sourceFile.exists()) return false

            val targetDir = File(targetPath)
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val targetFile = File(targetDir, "opencode_export_${dateFormat.format(Date())}.log")

            sourceFile.copyTo(targetFile, overwrite = true)
            i(TAG, "日志已导出到: ${targetFile.absolutePath}")
            true
        } catch (e: Exception) {
            e(TAG, "导出日志失败", e)
            false
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
        try {
            logFile?.let { file ->
                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "清除日志文件失败", e)
        }
        i(TAG, "日志已清除")
    }

    fun getLogFilePath(): String? {
        return logFile?.absolutePath
    }
}
