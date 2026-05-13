package com.opencode.mobile.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.opencode.mobile.domain.controller.AccessibilityDeviceController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OpenCodeAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "OpenCodeAccessibility"
        var instance: OpenCodeAccessibilityService? = null
            private set
    }

    @Inject
    lateinit var deviceController: AccessibilityDeviceController

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        deviceController.setAccessibilityService(this)
        Log.d(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let { accessibilityEvent ->
            when (accessibilityEvent.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    Log.d(TAG, "Window changed: ${accessibilityEvent.packageName}")
                }
                AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                    Log.d(TAG, "View clicked")
                }
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                    Log.d(TAG, "Text changed: ${accessibilityEvent.text}")
                }
                else -> {
                    // 其他事件
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "Accessibility service destroyed")
    }
}
