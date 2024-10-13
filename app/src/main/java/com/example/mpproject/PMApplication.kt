package com.example.mpproject

import android.app.Application
import android.content.Context
import com.example.mpproject.db.DBSynchronizer

// 13.10.2024 by Hafiz
// Helper class to easily access application context. Launches DBSynchronizer on application start
class PMApplication : Application(){
    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        DBSynchronizer.start()
    }
}