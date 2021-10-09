package com.overplay.videoplayer

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@GlobalApplication)
            modules(appModule)
        }
    }
}