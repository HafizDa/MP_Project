package com.example.mpproject

import android.app.Application
import android.content.Context

class PMApplication : Application() {

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
}