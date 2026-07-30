package ru.bitvibe.waggy

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.initialize
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        Firebase.analytics.setAnalyticsCollectionEnabled(true)
        Firebase.crashlytics.isCrashlyticsCollectionEnabled = true
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HiltWorkerFactoryEntryPoint {
        fun workerFactory(): HiltWorkerFactory
    }

    override val workManagerConfiguration = Configuration.Builder()
        .setWorkerFactory(
            EntryPoints.get(this, HiltWorkerFactoryEntryPoint::class.java).workerFactory()
        )
        .setMinimumLoggingLevel(Log.INFO)
        .build()
}
