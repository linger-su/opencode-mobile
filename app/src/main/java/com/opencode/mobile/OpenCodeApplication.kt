package com.opencode.mobile

import android.app.Application
import com.opencode.mobile.utils.LogManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OpenCodeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LogManager.initialize(this)
        LogManager.i("OpenCodeApplication", "应用启动")
    }
}
