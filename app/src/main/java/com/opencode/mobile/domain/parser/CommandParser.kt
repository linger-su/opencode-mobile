package com.opencode.mobile.domain.parser

import com.opencode.mobile.domain.model.CommandType
import com.opencode.mobile.domain.model.DeviceCommand

interface CommandParser {
    suspend fun parse(input: String): DeviceCommand?
    suspend fun isCommand(input: String): Boolean
}

class DefaultCommandParser : CommandParser {

    private val commandPatterns = mapOf(
        // 应用控制 - 中文
        "打开" to CommandType.APP_CONTROL,
        "启动" to CommandType.APP_CONTROL,
        "关闭" to CommandType.APP_CONTROL,
        "退出" to CommandType.APP_CONTROL,

        // 应用控制 - 英文
        "open" to CommandType.APP_CONTROL,
        "launch" to CommandType.APP_CONTROL,
        "close" to CommandType.APP_CONTROL,
        "exit" to CommandType.APP_CONTROL,
        "start" to CommandType.APP_CONTROL,

        // 系统设置 - 中文
        "亮度" to CommandType.SYSTEM_SETTINGS,
        "音量" to CommandType.SYSTEM_SETTINGS,
        "WiFi" to CommandType.SYSTEM_SETTINGS,
        "wifi" to CommandType.SYSTEM_SETTINGS,
        "蓝牙" to CommandType.SYSTEM_SETTINGS,
        "飞行模式" to CommandType.SYSTEM_SETTINGS,
        "手电筒" to CommandType.SYSTEM_SETTINGS,
        "闪光灯" to CommandType.SYSTEM_SETTINGS,

        // 系统设置 - 英文
        "brightness" to CommandType.SYSTEM_SETTINGS,
        "volume" to CommandType.SYSTEM_SETTINGS,
        "bluetooth" to CommandType.SYSTEM_SETTINGS,
        "airplane" to CommandType.SYSTEM_SETTINGS,
        "flashlight" to CommandType.SYSTEM_SETTINGS,
        "torch" to CommandType.SYSTEM_SETTINGS,

        // 通知 - 中文
        "通知" to CommandType.NOTIFICATION,
        "消息" to CommandType.NOTIFICATION,

        // 通知 - 英文
        "notification" to CommandType.NOTIFICATION,
        "notify" to CommandType.NOTIFICATION,

        // 媒体 - 中文
        "播放" to CommandType.MEDIA,
        "暂停" to CommandType.MEDIA,
        "下一首" to CommandType.MEDIA,
        "上一首" to CommandType.MEDIA,
        "音乐" to CommandType.MEDIA,

        // 媒体 - 英文
        "play" to CommandType.MEDIA,
        "pause" to CommandType.MEDIA,
        "next" to CommandType.MEDIA,
        "previous" to CommandType.MEDIA,
        "music" to CommandType.MEDIA,

        // 通讯 - 中文
        "短信" to CommandType.COMMUNICATION,
        "打电话" to CommandType.COMMUNICATION,
        "拨打" to CommandType.COMMUNICATION,
        "联系人" to CommandType.COMMUNICATION,

        // 通讯 - 英文
        "sms" to CommandType.COMMUNICATION,
        "message" to CommandType.COMMUNICATION,
        "call" to CommandType.COMMUNICATION,
        "contact" to CommandType.COMMUNICATION,

        // 截屏 - 中文
        "截屏" to CommandType.SCREEN_CONTROL,
        "截图" to CommandType.SCREEN_CONTROL,
        "录屏" to CommandType.SCREEN_CONTROL,
        "锁屏" to CommandType.SCREEN_CONTROL,

        // 截屏 - 英文
        "screenshot" to CommandType.SCREEN_CONTROL,
        "screen" to CommandType.SCREEN_CONTROL,
        "lock" to CommandType.SCREEN_CONTROL
    )

    override suspend fun parse(input: String): DeviceCommand? {
        val trimmedInput = input.trim()

        // 检查是否包含命令关键词
        for ((keyword, commandType) in commandPatterns) {
            if (trimmedInput.contains(keyword, ignoreCase = true)) {
                return parseCommand(commandType, trimmedInput)
            }
        }

        return null
    }

    override suspend fun isCommand(input: String): Boolean {
        return parse(input) != null
    }

    private fun parseCommand(type: CommandType, input: String): DeviceCommand {
        return when (type) {
            CommandType.APP_CONTROL -> parseAppCommand(input)
            CommandType.SYSTEM_SETTINGS -> parseSettingsCommand(input)
            CommandType.NOTIFICATION -> parseNotificationCommand(input)
            CommandType.MEDIA -> parseMediaCommand(input)
            CommandType.COMMUNICATION -> parseCommunicationCommand(input)
            CommandType.SCREEN_CONTROL -> parseScreenCommand(input)
            else -> DeviceCommand(type = type, action = "unknown")
        }
    }

    private fun parseAppCommand(input: String): DeviceCommand {
        val action = when {
            input.contains("打开") || input.contains("启动") || 
            input.contains("open", ignoreCase = true) || input.contains("launch", ignoreCase = true) ||
            input.contains("start", ignoreCase = true) -> "open"
            input.contains("关闭") || input.contains("退出") || 
            input.contains("close", ignoreCase = true) || input.contains("exit", ignoreCase = true) -> "close"
            else -> "open"
        }

        // 提取应用名称（移除命令关键词）
        val appName = input
            .replace(Regex("(打开|启动|关闭|退出|open|launch|start|close|exit)", RegexOption.IGNORE_CASE), "")
            .trim()

        return DeviceCommand(
            type = CommandType.APP_CONTROL,
            action = action,
            params = mapOf("appName" to appName)
        )
    }

    private fun parseSettingsCommand(input: String): DeviceCommand {
        return when {
            input.contains("亮度") -> {
                val level = extractNumber(input)
                DeviceCommand(
                    type = CommandType.SYSTEM_SETTINGS,
                    action = "set_brightness",
                    params = mapOf("level" to (level ?: 50))
                )
            }
            input.contains("音量") -> {
                val level = extractNumber(input)
                DeviceCommand(
                    type = CommandType.SYSTEM_SETTINGS,
                    action = "set_volume",
                    params = mapOf("level" to (level ?: 50))
                )
            }
            input.contains("WiFi") || input.contains("wifi") -> {
                val enable = !input.contains("关闭")
                DeviceCommand(
                    type = CommandType.SYSTEM_SETTINGS,
                    action = "toggle_wifi",
                    params = mapOf("enable" to enable)
                )
            }
            input.contains("蓝牙") -> {
                val enable = !input.contains("关闭")
                DeviceCommand(
                    type = CommandType.SYSTEM_SETTINGS,
                    action = "toggle_bluetooth",
                    params = mapOf("enable" to enable)
                )
            }
            input.contains("飞行模式") -> {
                val enable = !input.contains("关闭")
                DeviceCommand(
                    type = CommandType.SYSTEM_SETTINGS,
                    action = "toggle_airplane",
                    params = mapOf("enable" to enable)
                )
            }
            input.contains("手电筒") || input.contains("闪光灯") -> {
                val enable = !input.contains("关闭")
                DeviceCommand(
                    type = CommandType.SYSTEM_SETTINGS,
                    action = "toggle_flashlight",
                    params = mapOf("enable" to enable)
                )
            }
            else -> DeviceCommand(
                type = CommandType.SYSTEM_SETTINGS,
                action = "unknown"
            )
        }
    }

    private fun parseNotificationCommand(input: String): DeviceCommand {
        return when {
            input.contains("发送") || input.contains("发") -> {
                DeviceCommand(
                    type = CommandType.NOTIFICATION,
                    action = "send",
                    params = mapOf("content" to input)
                )
            }
            input.contains("读取") || input.contains("查看") -> {
                DeviceCommand(
                    type = CommandType.NOTIFICATION,
                    action = "read"
                )
            }
            input.contains("清除") || input.contains("清理") -> {
                DeviceCommand(
                    type = CommandType.NOTIFICATION,
                    action = "clear"
                )
            }
            else -> DeviceCommand(
                type = CommandType.NOTIFICATION,
                action = "read"
            )
        }
    }

    private fun parseMediaCommand(input: String): DeviceCommand {
        return when {
            input.contains("播放") -> DeviceCommand(
                type = CommandType.MEDIA,
                action = "play"
            )
            input.contains("暂停") -> DeviceCommand(
                type = CommandType.MEDIA,
                action = "pause"
            )
            input.contains("下一首") -> DeviceCommand(
                type = CommandType.MEDIA,
                action = "next"
            )
            input.contains("上一首") -> DeviceCommand(
                type = CommandType.MEDIA,
                action = "previous"
            )
            else -> DeviceCommand(
                type = CommandType.MEDIA,
                action = "play"
            )
        }
    }

    private fun parseCommunicationCommand(input: String): DeviceCommand {
        return when {
            input.contains("短信") -> {
                DeviceCommand(
                    type = CommandType.COMMUNICATION,
                    action = "send_sms",
                    params = mapOf("content" to input)
                )
            }
            input.contains("打电话") || input.contains("拨打") -> {
                DeviceCommand(
                    type = CommandType.COMMUNICATION,
                    action = "make_call",
                    params = mapOf("content" to input)
                )
            }
            input.contains("联系人") -> {
                DeviceCommand(
                    type = CommandType.COMMUNICATION,
                    action = "read_contacts"
                )
            }
            else -> DeviceCommand(
                type = CommandType.COMMUNICATION,
                action = "unknown"
            )
        }
    }

    private fun parseScreenCommand(input: String): DeviceCommand {
        return when {
            input.contains("截屏") || input.contains("截图") -> DeviceCommand(
                type = CommandType.SCREEN_CONTROL,
                action = "screenshot"
            )
            input.contains("录屏") -> DeviceCommand(
                type = CommandType.SCREEN_CONTROL,
                action = "record"
            )
            input.contains("锁屏") -> DeviceCommand(
                type = CommandType.SCREEN_CONTROL,
                action = "lock"
            )
            else -> DeviceCommand(
                type = CommandType.SCREEN_CONTROL,
                action = "screenshot"
            )
        }
    }

    private fun extractNumber(input: String): Int? {
        val regex = Regex("\\d+")
        return regex.find(input)?.value?.toIntOrNull()
    }
}
