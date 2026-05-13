package com.opencode.mobile.domain.model

data class DeviceCommand(
    val type: CommandType,
    val action: String,
    val params: Map<String, Any> = emptyMap()
)

enum class CommandType {
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

enum class SystemAction {
    // 应用控制
    OPEN_APP,
    CLOSE_APP,
    INSTALL_APP,
    UNINSTALL_APP,

    // 系统设置
    SET_BRIGHTNESS,
    SET_VOLUME,
    TOGGLE_WIFI,
    TOGGLE_BLUETOOTH,
    TOGGLE_AIRPLANE,
    TOGGLE_FLASHLIGHT,
    TOGGLE_NFC,
    SET_RINGTONE,

    // 通知
    SEND_NOTIFICATION,
    READ_NOTIFICATIONS,
    CLEAR_NOTIFICATIONS,
    DISMISS_NOTIFICATION,

    // 媒体
    PLAY_MUSIC,
    PAUSE_MUSIC,
    NEXT_TRACK,
    PREVIOUS_TRACK,
    SET_MEDIA_VOLUME,

    // 通讯
    SEND_SMS,
    MAKE_CALL,
    READ_CONTACTS,

    // 文件
    READ_FILE,
    WRITE_FILE,
    LIST_FILES,
    DELETE_FILE,

    // 输入
    TAP,
    SWIPE,
    INPUT_TEXT,
    PRESS_KEY,
    LONG_PRESS,

    // 屏幕
    SCREENSHOT,
    RECORD_SCREEN,
    LOCK_SCREEN,
    UNLOCK_SCREEN
}
