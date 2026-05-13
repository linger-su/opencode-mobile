package com.opencode.mobile.domain.controller

import com.opencode.mobile.domain.model.DeviceCommand

interface DeviceController {
    suspend fun execute(command: DeviceCommand): String

    // 应用控制
    suspend fun openApp(packageName: String): Boolean
    suspend fun closeApp(packageName: String): Boolean
    suspend fun isAppInstalled(packageName: String): Boolean
    suspend fun findAppByName(appName: String): String?

    // 系统设置
    suspend fun setBrightness(level: Int): Boolean
    suspend fun setVolume(level: Int, stream: String = "music"): Boolean
    suspend fun toggleWifi(enable: Boolean): Boolean
    suspend fun toggleBluetooth(enable: Boolean): Boolean
    suspend fun toggleAirplaneMode(enable: Boolean): Boolean
    suspend fun toggleFlashlight(enable: Boolean): Boolean

    // 通知
    suspend fun sendNotification(title: String, content: String): Boolean
    suspend fun getNotifications(): List<Map<String, String>>
    suspend fun clearNotifications(): Boolean

    // 媒体控制
    suspend fun playMusic(): Boolean
    suspend fun pauseMusic(): Boolean
    suspend fun nextTrack(): Boolean
    suspend fun previousTrack(): Boolean

    // 输入模拟
    suspend fun tap(x: Int, y: Int): Boolean
    suspend fun swipe(startX: Int, startY: Int, endX: Int, endY: Int, duration: Long = 300): Boolean
    suspend fun inputText(text: String): Boolean
    suspend fun pressKey(keyCode: Int): Boolean
    suspend fun longPress(x: Int, y: Int, duration: Long = 1000): Boolean

    // 屏幕控制
    suspend fun screenshot(): String?
    suspend fun lockScreen(): Boolean
    suspend fun isScreenOn(): Boolean
}
