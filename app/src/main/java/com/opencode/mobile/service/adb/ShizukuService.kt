package com.opencode.mobile.service.adb

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import rikka.shizuku.Shizuku

class ShizukuService {

    companion object {
        private const val TAG = "ShizukuService"
        private const val SHIZUKU_PERMISSION_CODE = 1001

        var isAvailable: Boolean = false
            private set

        fun checkAvailability(context: Context): Boolean {
            return try {
                Shizuku.pingBinder()
            } catch (e: Exception) {
                false
            }
        }

        fun requestPermission() {
            try {
                if (Shizuku.isPreV11()) {
                    // Shizuku pre-v11
                    Log.w(TAG, "Shizuku pre-v11 is not supported")
                } else {
                    Shizuku.requestPermission(SHIZUKU_PERMISSION_CODE)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to request Shizuku permission", e)
            }
        }

        suspend fun executeCommand(command: String): Result<String> {
            return try {
                if (!Shizuku.pingBinder()) {
                    return Result.failure(Exception("Shizuku is not available"))
                }

                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                val output = process.inputStream.bufferedReader().readText()
                val error = process.errorStream.bufferedReader().readText()
                val exitCode = process.waitFor()

                if (exitCode == 0) {
                    Result.success(output)
                } else {
                    Result.failure(Exception("Command failed with exit code $exitCode: $error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        fun setWifiEnabled(enabled: Boolean): Result<Boolean> {
            return try {
                val command = if (enabled) "svc wifi enable" else "svc wifi disable"
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                val exitCode = process.waitFor()
                Result.success(exitCode == 0)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        fun setBluetoothEnabled(enabled: Boolean): Result<Boolean> {
            return try {
                val command = if (enabled) "svc bluetooth enable" else "svc bluetooth disable"
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                val exitCode = process.waitFor()
                Result.success(exitCode == 0)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        fun setAirplaneMode(enabled: Boolean): Result<Boolean> {
            return try {
                val value = if (enabled) "enable" else "disable"
                val command = "settings put global airplane_mode_on ${if (enabled) 1 else 0} && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state $enabled"
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                val exitCode = process.waitFor()
                Result.success(exitCode == 0)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        fun setFlashlight(enabled: Boolean): Result<Boolean> {
            return try {
                val command = if (enabled) "cmd camera_service flashlight on" else "cmd camera_service flashlight off"
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                val exitCode = process.waitFor()
                Result.success(exitCode == 0)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        fun takeScreenshot(): Result<String> {
            return try {
                val path = "/sdcard/Pictures/Screenshots/screenshot_${System.currentTimeMillis()}.png"
                val command = "screencap -p $path"
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                val exitCode = process.waitFor()
                if (exitCode == 0) {
                    Result.success(path)
                } else {
                    Result.failure(Exception("Screenshot failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        fun inputText(text: String): Result<Boolean> {
            return try {
                val command = "input text '$text'"
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                val exitCode = process.waitFor()
                Result.success(exitCode == 0)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        fun tap(x: Int, y: Int): Result<Boolean> {
            return try {
                val command = "input tap $x $y"
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                val exitCode = process.waitFor()
                Result.success(exitCode == 0)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        fun swipe(startX: Int, startY: Int, endX: Int, endY: Int, duration: Long = 300): Result<Boolean> {
            return try {
                val command = "input swipe $startX $startY $endX $endY $duration"
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                val exitCode = process.waitFor()
                Result.success(exitCode == 0)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
