package edu.utap.flavorquest

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.initialize

class FlavorQuestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(context = this)
    }
}