package com.example.alphaflow

import android.app.Application
import com.example.alphaflow.data.local.AlphaFlowDatabase
import com.example.alphaflow.data.repository.AlphaFlowRepository

class AlphaFlowApplication : Application() {

    val database by lazy { AlphaFlowDatabase.getInstance(this) }
    val repository by lazy { AlphaFlowRepository(database.dao()) }

    override fun onCreate() {
        super.onCreate()
    }
}
