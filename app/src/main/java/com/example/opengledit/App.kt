package com.example.opengl

import android.app.Application

internal lateinit var yxApp: Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        yxApp = this
    }

}