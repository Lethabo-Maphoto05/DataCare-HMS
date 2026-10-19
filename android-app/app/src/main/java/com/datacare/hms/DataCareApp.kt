package com.datacare.hms

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class DataCareApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // google-services.json (if present) already initialised Firebase; otherwise use the web config.
        if (FirebaseApp.getApps(this).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApiKey(FirebaseConfig.API_KEY)
                .setApplicationId(FirebaseConfig.APPLICATION_ID)
                .setProjectId(FirebaseConfig.PROJECT_ID)
                .setStorageBucket(FirebaseConfig.STORAGE_BUCKET)
                .setGcmSenderId(FirebaseConfig.SENDER_ID)
                .build()
            FirebaseApp.initializeApp(this, options)
        }
    }
}
