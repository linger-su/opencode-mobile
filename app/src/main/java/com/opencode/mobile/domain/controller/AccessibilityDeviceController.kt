package com.opencode.mobile.domain.controller

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import com.opencode.mobile.domain.model.CommandType
import com.opencode.mobile.domain.model.DeviceCommand
import com.opencode.mobile.utils.LogManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccessibilityDeviceController @Inject constructor(
    @ApplicationContext private val context: Context
) : DeviceController {

    companion object {
        private const val TAG = "DeviceController"
    }

    private var accessibilityService: AccessibilityService? = null

    fun setAccessibilityService(service: AccessibilityService) {
        this.accessibilityService = service
    }

    override suspend fun execute(command: DeviceCommand): String {
        return when (command.type) {
            CommandType.APP_CONTROL -> executeAppCommand(command)
            CommandType.SYSTEM_SETTINGS -> executeSettingsCommand(command)
            CommandType.NOTIFICATION -> executeNotificationCommand(command)
            CommandType.MEDIA -> executeMediaCommand(command)
            CommandType.COMMUNICATION -> executeCommunicationCommand(command)
            CommandType.SCREEN_CONTROL -> executeScreenCommand(command)
            CommandType.INPUT_SIMULATION -> executeInputCommand(command)
            else -> "未知命令类型"
        }
    }

    private suspend fun executeAppCommand(command: DeviceCommand): String {
        val appName = command.params["appName"] as? String ?: return "未指定应用名称"

        return when (command.action) {
            "open" -> {
                val packageName = findAppByName(appName)
                if (packageName != null) {
                    if (openApp(packageName)) {
                        "已打开 $appName"
                    } else {
                        "无法打开 $appName"
                    }
                } else {
                    "未找到应用: $appName"
                }
            }
            "close" -> {
                val packageName = findAppByName(appName)
                if (packageName != null) {
                    if (closeApp(packageName)) {
                        "已关闭 $appName"
                    } else {
                        "无法关闭 $appName"
                    }
                } else {
                    "未找到应用: $appName"
                }
            }
            else -> "未知应用操作"
        }
    }

    private suspend fun executeSettingsCommand(command: DeviceCommand): String {
        return when (command.action) {
            "set_brightness" -> {
                val level = command.params["level"] as? Int ?: 50
                if (setBrightness(level)) {
                    "已设置亮度为 $level%"
                } else {
                    "无法设置亮度"
                }
            }
            "set_volume" -> {
                val level = command.params["level"] as? Int ?: 50
                if (setVolume(level)) {
                    "已设置音量为 $level%"
                } else {
                    "无法设置音量"
                }
            }
            "toggle_wifi" -> {
                val enable = command.params["enable"] as? Boolean ?: true
                if (toggleWifi(enable)) {
                    if (enable) "已开启 WiFi" else "已关闭 WiFi"
                } else {
                    "无法切换 WiFi"
                }
            }
            "toggle_bluetooth" -> {
                val enable = command.params["enable"] as? Boolean ?: true
                if (toggleBluetooth(enable)) {
                    if (enable) "已开启蓝牙" else "已关闭蓝牙"
                } else {
                    "无法切换蓝牙"
                }
            }
            "toggle_flashlight" -> {
                val enable = command.params["enable"] as? Boolean ?: true
                if (toggleFlashlight(enable)) {
                    if (enable) "已开启手电筒" else "已关闭手电筒"
                } else {
                    "无法切换手电筒"
                }
            }
            else -> "未知设置操作"
        }
    }

    private suspend fun executeNotificationCommand(command: DeviceCommand): String {
        return when (command.action) {
            "send" -> {
                val content = command.params["content"] as? String ?: ""
                if (sendNotification("OpenCode", content)) {
                    "已发送通知"
                } else {
                    "无法发送通知"
                }
            }
            "read" -> {
                val notifications = getNotifications()
                if (notifications.isEmpty()) {
                    "没有通知"
                } else {
                    "有 ${notifications.size} 条通知"
                }
            }
            "clear" -> {
                if (clearNotifications()) {
                    "已清除所有通知"
                } else {
                    "无法清除通知"
                }
            }
            else -> "未知通知操作"
        }
    }

    private suspend fun executeMediaCommand(command: DeviceCommand): String {
        return when (command.action) {
            "play" -> if (playMusic()) "已开始播放" else "无法播放"
            "pause" -> if (pauseMusic()) "已暂停播放" else "无法暂停"
            "next" -> if (nextTrack()) "已切换到下一首" else "无法切换"
            "previous" -> if (previousTrack()) "已切换到上一首" else "无法切换"
            else -> "未知媒体操作"
        }
    }

    private suspend fun executeCommunicationCommand(command: DeviceCommand): String {
        return when (command.action) {
            "send_sms" -> "短信功能需要进一步实现"
            "make_call" -> "拨打电话功能需要进一步实现"
            "read_contacts" -> "联系人功能需要进一步实现"
            else -> "未知通讯操作"
        }
    }

    private suspend fun executeScreenCommand(command: DeviceCommand): String {
        return when (command.action) {
            "screenshot" -> {
                val path = screenshot()
                if (path != null) {
                    "已截屏，保存至: $path"
                } else {
                    "无法截屏"
                }
            }
            "record" -> "录屏功能需要进一步实现"
            "lock" -> {
                if (lockScreen()) {
                    "已锁屏"
                } else {
                    "无法锁屏"
                }
            }
            else -> "未知屏幕操作"
        }
    }

    private suspend fun executeInputCommand(command: DeviceCommand): String {
        return when (command.action) {
            "tap" -> {
                val x = command.params["x"] as? Int ?: 0
                val y = command.params["y"] as? Int ?: 0
                if (tap(x, y)) {
                    "已点击 ($x, $y)"
                } else {
                    "无法点击"
                }
            }
            "swipe" -> {
                val startX = command.params["startX"] as? Int ?: 0
                val startY = command.params["startY"] as? Int ?: 0
                val endX = command.params["endX"] as? Int ?: 0
                val endY = command.params["endY"] as? Int ?: 0
                if (swipe(startX, startY, endX, endY)) {
                    "已滑动"
                } else {
                    "无法滑动"
                }
            }
            "input_text" -> {
                val text = command.params["text"] as? String ?: ""
                if (inputText(text)) {
                    "已输入文本"
                } else {
                    "无法输入文本"
                }
            }
            else -> "未知输入操作"
        }
    }

    // 实现接口方法
    override suspend fun openApp(packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun closeApp(packageName: String): Boolean {
        // 需要 root 或特殊权限
        return false
    }

    override suspend fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun findAppByName(appName: String): String? {
        // 常见应用的包名映射
        val commonApps = mapOf(
            "gmail" to "com.google.android.gm",
            "邮箱" to "com.google.android.gm",
            "chrome" to "com.android.chrome",
            "浏览器" to "com.android.chrome",
            "youtube" to "com.google.android.youtube",
            "抖音" to "com.ss.android.ugc.aweme",
            "微信" to "com.tencent.mm",
            "wechat" to "com.tencent.mm",
            "qq" to "com.tencent.mobileqq",
            "支付宝" to "com.eg.android.AlipayGphone",
            "alipay" to "com.eg.android.AlipayGphone",
            "淘宝" to "com.taobao.taobao",
            "地图" to "com.google.android.apps.maps",
            "maps" to "com.google.android.apps.maps",
            "相机" to "com.android.camera",
            "camera" to "com.android.camera",
            "设置" to "com.android.settings",
            "settings" to "com.android.settings",
            "电话" to "com.android.dialer",
            "phone" to "com.android.dialer",
            "短信" to "com.android.mms",
            "messages" to "com.android.mms",
            "日历" to "com.google.android.calendar",
            "calendar" to "com.google.android.calendar",
            "时钟" to "com.android.deskclock",
            "clock" to "com.android.deskclock",
            "计算器" to "com.android.calculator2",
            "calculator" to "com.android.calculator2",
            "文件" to "com.android.filemanager",
            "files" to "com.android.filemanager",
            "音乐" to "com.android.music",
            "music" to "com.android.music",
            "图库" to "com.android.gallery3d",
            "gallery" to "com.android.gallery3d",
            "照片" to "com.google.android.apps.photos",
            "photos" to "com.google.android.apps.photos",
            "play" to "com.android.vending",
            "play商店" to "com.android.vending",
            "应用商店" to "com.android.vending"
        )

        // 先检查常见应用映射
        val lowerAppName = appName.lowercase()
        LogManager.d(TAG, "查找应用: $appName (小写: $lowerAppName)")
        
        for ((key, packageName) in commonApps) {
            if (lowerAppName.contains(key) || key.contains(lowerAppName)) {
                LogManager.d(TAG, "匹配关键词: $key -> $packageName")
                // 验证应用是否已安装
                try {
                    context.packageManager.getPackageInfo(packageName, 0)
                    LogManager.d(TAG, "通过映射找到应用: $appName -> $packageName")
                    return packageName
                } catch (e: Exception) {
                    LogManager.d(TAG, "应用未安装: $packageName")
                }
            }
        }

        // 通过 PackageManager 查找
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)

        for (resolveInfo in resolveInfos) {
            val label = resolveInfo.loadLabel(packageManager).toString()
            if (label.contains(appName, ignoreCase = true) || appName.contains(label, ignoreCase = true)) {
                val packageName = resolveInfo.activityInfo.packageName
                LogManager.d(TAG, "通过标签找到应用: $appName -> $packageName (标签: $label)")
                return packageName
            }
        }

        // 最后尝试包名匹配
        for (resolveInfo in resolveInfos) {
            val packageName = resolveInfo.activityInfo.packageName
            if (packageName.contains(lowerAppName, ignoreCase = true)) {
                LogManager.d(TAG, "通过包名找到应用: $appName -> $packageName")
                return packageName
            }
        }

        LogManager.w(TAG, "未找到应用: $appName")
        return null
    }

    override suspend fun setBrightness(level: Int): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(context)) {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return false
                }
            }
            val normalizedLevel = (level * 255 / 100).coerceIn(0, 255)
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                normalizedLevel
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun setVolume(level: Int, stream: String): Boolean {
        // 需要 AudioService 或 root
        return false
    }

    override suspend fun toggleWifi(enable: Boolean): Boolean {
        // 需要 root 或特殊权限
        return false
    }

    override suspend fun toggleBluetooth(enable: Boolean): Boolean {
        // 需要 root 或特殊权限
        return false
    }

    override suspend fun toggleAirplaneMode(enable: Boolean): Boolean {
        // 需要 root 或特殊权限
        return false
    }

    override suspend fun toggleFlashlight(enable: Boolean): Boolean {
        // 需要 Camera2 API 或特殊权限
        return false
    }

    override suspend fun sendNotification(title: String, content: String): Boolean {
        // 需要 NotificationManager
        return false
    }

    override suspend fun getNotifications(): List<Map<String, String>> {
        return emptyList()
    }

    override suspend fun clearNotifications(): Boolean {
        return false
    }

    override suspend fun playMusic(): Boolean {
        // 需要 MediaController
        return false
    }

    override suspend fun pauseMusic(): Boolean {
        return false
    }

    override suspend fun nextTrack(): Boolean {
        return false
    }

    override suspend fun previousTrack(): Boolean {
        return false
    }

    override suspend fun tap(x: Int, y: Int): Boolean {
        val service = accessibilityService ?: return false
        return try {
            val path = Path().apply {
                moveTo(x.toFloat(), y.toFloat())
            }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
                .build()
            service.dispatchGesture(gesture, null, null)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun swipe(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        duration: Long
    ): Boolean {
        val service = accessibilityService ?: return false
        return try {
            val path = Path().apply {
                moveTo(startX.toFloat(), startY.toFloat())
                lineTo(endX.toFloat(), endY.toFloat())
            }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                .build()
            service.dispatchGesture(gesture, null, null)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun inputText(text: String): Boolean {
        val service = accessibilityService ?: return false
        return try {
            val rootNode = service.rootInActiveWindow ?: return false
            val focusedNode = findFocusedEditText(rootNode)
            if (focusedNode != null) {
                val arguments = android.os.Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                }
                focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun findFocusedEditText(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isFocused && node.isEditable) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findFocusedEditText(child)
            if (result != null) {
                return result
            }
        }
        return null
    }

    override suspend fun pressKey(keyCode: Int): Boolean {
        val service = accessibilityService ?: return false
        return try {
            service.performGlobalAction(keyCode)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun longPress(x: Int, y: Int, duration: Long): Boolean {
        val service = accessibilityService ?: return false
        return try {
            val path = Path().apply {
                moveTo(x.toFloat(), y.toFloat())
            }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                .build()
            service.dispatchGesture(gesture, null, null)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun screenshot(): String? {
        // 需要 MediaProjection API
        return null
    }

    override suspend fun lockScreen(): Boolean {
        return pressKey(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
    }

    override suspend fun isScreenOn(): Boolean {
        // 退要 PowerManager
        return true
    }
}
