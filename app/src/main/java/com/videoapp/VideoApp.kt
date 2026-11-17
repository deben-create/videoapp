package com.videoapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VideoApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
