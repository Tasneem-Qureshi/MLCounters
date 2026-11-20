package com.mlcounters.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.mlcounters.app.data.repository.CounterRepository
import com.mlcounters.app.data.remote.DataSyncWorker
import com.mlcounters.app.detection.TemplateManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import javax.inject.Inject

@HiltAndroidApp
class MLCountersApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var templateManager: TemplateManager

    @Inject
    lateinit var counterRepository: CounterRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MLCountersApp onCreate() starting initialization...")

        // 1. Initialize OpenCV in onCreate using OpenCVLoader.initLocal()
        initOpenCV()

        // 2. Create notification channel for overlay service and screen capture
        createNotificationChannels()

        // 3. Schedule weekly data sync using DataSyncWorker.enqueuePeriodicWork()
        DataSyncWorker.enqueuePeriodicWork(this)

        // 4. Initialize TemplateManager and 5. Load initial data from assets if database is empty
        applicationScope.launch {
            initTemplateManager()
            seedInitialData()
        }
    }

    private fun initOpenCV() {
        try {
            if (OpenCVLoader.initLocal()) {
                Log.i(TAG, "OpenCV 4.x successfully initialized via OpenCVLoader.initLocal()")
            } else {
                Log.e(TAG, "OpenCV initialization failed with OpenCVLoader.initLocal().")
            }
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Native OpenCV library not found: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error initializing OpenCV: ${e.message}", e)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // Channel for floating overlay service
            val overlayChannel = NotificationChannel(
                OVERLAY_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }

            // Channel for screen capture foreground service
            val captureChannel = NotificationChannel(
                CAPTURE_CHANNEL_ID,
                getString(R.string.screen_capture_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.screen_capture_desc)
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }

            notificationManager.createNotificationChannel(overlayChannel)
            notificationManager.createNotificationChannel(captureChannel)
            Log.d(TAG, "Notification channels created: $OVERLAY_CHANNEL_ID, $CAPTURE_CHANNEL_ID")
        }
    }

    private fun initTemplateManager() {
        try {
            val metrics = resources.displayMetrics
            val selectedRes = templateManager.selectResolution(metrics.widthPixels, metrics.heightPixels)
            Log.d(TAG, "Auto-selected template resolution: ${selectedRes}px for screen ${metrics.widthPixels}x${metrics.heightPixels}")
            templateManager.loadTemplates(this, selectedRes)
            Log.d(TAG, "TemplateManager initialized successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TemplateManager: ${e.message}", e)
        }
    }

    private suspend fun seedInitialData() {
        try {
            counterRepository.seedInitialDataIfEmpty(this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load initial data from assets: ${e.message}", e)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "onLowMemory received. Clearing TemplateManager caches.")
        templateManager.clearCache()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_MODERATE) {
            Log.w(TAG, "onTrimMemory received (level=$level). Clearing TemplateManager caches.")
            templateManager.clearCache()
        }
    }

    companion object {
        private const val TAG = "MLCountersApp"
        const val OVERLAY_CHANNEL_ID = "ml_counters_overlay_channel"
        const val CAPTURE_CHANNEL_ID = "ml_counters_capture_channel"
    }
}
