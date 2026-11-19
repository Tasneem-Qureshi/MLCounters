package com.mlcounters.app.detection

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.OrientationEventListener
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.mlcounters.app.MLCountersApp
import com.mlcounters.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScreenCaptureService : Service() {

    @Inject
    lateinit var detectionEngine: HeroDetectionEngine

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private val backgroundHandler = Handler(Looper.getMainLooper())

    private var captureJob: Job? = null
    private var captureIntervalMs: Long = DEFAULT_CAPTURE_INTERVAL_MS

    private var screenWidth = 1920
    private var screenHeight = 1080
    private var screenDensity = DisplayMetrics.DENSITY_DEFAULT

    private var orientationListener: OrientationEventListener? = null
    private var lastRotation = -1

    // State & Event Flows
    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    private val _detectionFlow = MutableSharedFlow<List<DetectionResult>>(replay = 1)
    val detectionFlow: SharedFlow<List<DetectionResult>> = _detectionFlow.asSharedFlow()

    private val _projectionLostFlow = MutableSharedFlow<Unit>()
    val projectionLostFlow: SharedFlow<Unit> = _projectionLostFlow.asSharedFlow()

    inner class LocalBinder : Binder() {
        fun getService(): ScreenCaptureService = this@ScreenCaptureService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ScreenCaptureService onCreate()")
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        updateScreenDimensions()
        setupOrientationListener()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_CAPTURE -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val data = intent.getParcelableExtra<Intent>(EXTRA_DATA_INTENT)
                if (resultCode != 0 && data != null) {
                    startCapture(resultCode, data)
                } else {
                    Log.e(TAG, "Cannot start capture: Missing resultCode or data Intent.")
                }
            }
            ACTION_STOP_CAPTURE -> {
                stopCapture()
            }
            ACTION_SET_INTERVAL -> {
                val interval = intent.getLongExtra(EXTRA_INTERVAL_MS, DEFAULT_CAPTURE_INTERVAL_MS)
                setCaptureInterval(interval)
            }
        }
        return START_NOT_STICKY
    }

    /**
     * Starts screen capture using MediaProjection and registers foreground service.
     */
    fun startCapture(resultCode: Int, data: Intent) {
        if (_isCapturing.value) {
            Log.w(TAG, "Screen capture already active.")
            return
        }

        Log.d(TAG, "Starting screen capture foreground service...")
        // Start foreground service first before obtaining media projection (required on Android 14+)
        startForegroundWithNotification()

        try {
            val projection = mediaProjectionManager?.getMediaProjection(resultCode, data)
            if (projection == null) {
                Log.e(TAG, "MediaProjectionManager returned null projection.")
                stopCapture()
                return
            }
            mediaProjection = projection

            // Register callback to handle MediaProjection lifecycle
            projection.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    super.onStop()
                    Log.w(TAG, "MediaProjection revoked or stopped by user.")
                    serviceScope.launch {
                        _projectionLostFlow.emit(Unit)
                    }
                    stopCapture()
                }
            }, backgroundHandler)

            createVirtualDisplay()
            _isCapturing.value = true
            startCaptureLoop()
            Log.i(TAG, "Screen capture successfully started.")
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating screen capture: ${e.message}", e)
            stopCapture()
        }
    }

    /**
     * Stops screen capture and releases all resources.
     */
    fun stopCapture() {
        Log.d(TAG, "Stopping screen capture...")
        captureJob?.cancel()
        captureJob = null
        _isCapturing.value = false

        try {
            virtualDisplay?.release()
            virtualDisplay = null
            imageReader?.close()
            imageReader = null
            mediaProjection?.stop()
            mediaProjection = null
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing projection resources: ${e.message}")
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Log.d(TAG, "Screen capture stopped.")
    }

    /**
     * Captures a single Bitmap snapshot from ImageReader.
     */
    fun captureScreen(): Bitmap? {
        val reader = imageReader ?: return null
        val image = try {
            reader.acquireLatestImage()
        } catch (e: Exception) {
            Log.w(TAG, "Error acquiring latest image: ${e.message}")
            null
        } ?: return null

        return try {
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * screenWidth

            val bitmap = Bitmap.createBitmap(
                screenWidth + rowPadding / pixelStride,
                screenHeight,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)

            if (rowPadding != 0) {
                val croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight)
                bitmap.recycle()
                croppedBitmap
            } else {
                bitmap
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse frame bitmap: ${e.message}", e)
            null
        } finally {
            image.close()
        }
    }

    /**
     * Periodic coroutine loop capturing screen frames and running hero detection.
     */
    private fun startCaptureLoop() {
        captureJob?.cancel()
        captureJob = serviceScope.launch {
            while (isActive) {
                try {
                    val bitmap = captureScreen()
                    if (bitmap != null) {
                        val results = detectionEngine.detectHeroes(bitmap)
                        bitmap.recycle()

                        if (results.isNotEmpty()) {
                            Log.d(TAG, "Captured and detected ${results.size} heroes.")
                            _detectionFlow.emit(results)
                            broadcastResults(results)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception in capture loop: ${e.message}", e)
                }
                delay(captureIntervalMs)
            }
        }
    }

    fun setCaptureInterval(intervalMs: Long) {
        captureIntervalMs = intervalMs.coerceAtLeast(500L)
        Log.d(TAG, "Capture interval updated to ${captureIntervalMs}ms")
    }

    private fun createVirtualDisplay() {
        updateScreenDimensions()
        virtualDisplay?.release()
        imageReader?.close()

        imageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight,
            PixelFormat.RGBA_8888,
            2
        )

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            VIRTUAL_DISPLAY_NAME,
            screenWidth,
            screenHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            backgroundHandler
        )
        Log.d(TAG, "Created VirtualDisplay: ${screenWidth}x${screenHeight} density $screenDensity")
    }

    private fun updateScreenDimensions() {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val bounds = metrics.bounds
            screenWidth = bounds.width()
            screenHeight = bounds.height()
            screenDensity = resources.configuration.densityDpi
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(metrics)
            screenWidth = metrics.widthPixels
            screenHeight = metrics.heightPixels
            screenDensity = metrics.densityDpi
        }
    }

    private fun setupOrientationListener() {
        orientationListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                val windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return
                @Suppress("DEPRECATION")
                val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    display?.rotation ?: 0
                } else {
                    windowManager.defaultDisplay.rotation
                }

                if (lastRotation != -1 && rotation != lastRotation) {
                    Log.d(TAG, "Screen orientation rotated from $lastRotation to $rotation. Rebuilding VirtualDisplay...")
                    backgroundHandler.postDelayed({
                        if (_isCapturing.value && mediaProjection != null) {
                            createVirtualDisplay()
                        }
                    }, 500)
                }
                lastRotation = rotation
            }
        }
        if (orientationListener?.canDetectOrientation() == true) {
            orientationListener?.enable()
        }
    }

    private fun startForegroundWithNotification() {
        val stopIntent = Intent(this, ScreenCaptureService::class.java).apply {
            action = ACTION_STOP_CAPTURE
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, MLCountersApp.CAPTURE_CHANNEL_ID)
            .setContentTitle(getString(R.string.screen_capture_title))
            .setContentText(getString(R.string.detection_scanning))
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Scan", stopPendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun broadcastResults(results: List<DetectionResult>) {
        val intent = Intent(ACTION_HEROES_DETECTED).apply {
            putIntegerArrayListExtra(EXTRA_HERO_IDS, ArrayList(results.map { it.heroId }))
            putExtra(EXTRA_DETECTED_COUNT, results.size)
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "ScreenCaptureService onDestroy()")
        stopCapture()
        orientationListener?.disable()
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "ScreenCaptureService"
        private const val NOTIFICATION_ID = 2001
        private const val VIRTUAL_DISPLAY_NAME = "MLCountersCaptureDisplay"

        const val DEFAULT_CAPTURE_INTERVAL_MS = 2000L

        const val ACTION_START_CAPTURE = "com.mlcounters.app.action.START_CAPTURE"
        const val ACTION_STOP_CAPTURE = "com.mlcounters.app.action.STOP_CAPTURE"
        const val ACTION_SET_INTERVAL = "com.mlcounters.app.action.SET_INTERVAL"
        const val ACTION_HEROES_DETECTED = "com.mlcounters.app.action.HEROES_DETECTED"

        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_DATA_INTENT = "extra_data_intent"
        const val EXTRA_INTERVAL_MS = "extra_interval_ms"
        const val EXTRA_HERO_IDS = "extra_hero_ids"
        const val EXTRA_DETECTED_COUNT = "extra_detected_count"
    }
}
