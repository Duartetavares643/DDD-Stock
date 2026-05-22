package com.example.ddd_stock

import android.app.Application
import com.google.firebase.FirebaseApp

class DDDStockApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
