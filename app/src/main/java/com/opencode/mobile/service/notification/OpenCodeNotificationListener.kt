package com.opencode.mobile.service.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class OpenCodeNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "OpenCodeNotification"
        var instance: OpenCodeNotificationListener? = null
            private set
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        Log.d(TAG, "Notification listener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {
            Log.d(TAG, "Notification posted: ${it.packageName}")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn?.let {
            Log.d(TAG, "Notification removed: ${it.packageName}")
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        Log.d(TAG, "Notification listener disconnected")
    }

    fun fetchActiveNotifications(): List<StatusBarNotification> {
        return try {
            activeNotifications?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun dismissNotification(key: String) {
        try {
            cancelNotification(key)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to dismiss notification", e)
        }
    }

    fun dismissAllNotifications() {
        try {
            cancelAllNotifications()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to dismiss all notifications", e)
        }
    }
}
