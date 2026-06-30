package com.example.proyectofinalmoviles1

import android.app.Application

class QuinielaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
