package com.mlcounters.app.overlay

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.mlcounters.app.MLCountersApp
import com.mlcounters.app.R
import com.mlcounters.app.data.repository.CounterRepository
import com.mlcounters.app.detection.DetectionResult
import com.mlcounters.app.detection.HeroDetectionEngine
import com.mlcounters.app.detection.ScreenCaptureService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    @Inject
    lateinit var detectionEngine: HeroDetectionEngine

    @Inject
    lateinit var repository: CounterRepository

    @Inject
    lateinit var viewModel: OverlayViewModel

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val vStore = ViewModelStore()

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = vStore

    override fun onBind(intent: Intent?): IBinder? = null

    private var windowManager: WindowManager? = null
    private var overlayComposeView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var isOverlayVisible = false

    // ScreenCaptureService binding
    private var screenCaptureService: ScreenCaptureService? = null
    private var isCaptureBound = false

    private val captureServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Connected to ScreenCaptureService.")
            val binder = service as? ScreenCaptureService.LocalBinder
            screenCaptureService = binder?.getService()
            isCaptureBound = true

            // Observe continuous detections from screen capture
            lifecycleScope.launch {
                screenCaptureService?.detectionFlow?.collectLatest { detections ->
                    updateResults(detections)
                }
            }

            // Observe if capture permission was revoked/stopped
            lifecycleScope.launch {
                screenCaptureService?.projectionLostFlow?.collectLatest {
                    viewModel.stopScan()
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Disconnected from ScreenCaptureService.")
            screenCaptureService = null
            isCaptureBound = false
        }
    }

    // Broadcast receiver for detection events
    private val detectionBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ScreenCaptureService.ACTION_HEROES_DETECTED) {
                val heroIds = intent.getIntegerArrayListExtra(ScreenCaptureService.EXTRA_HERO_IDS)
                if (heroIds != null) {
                    lifecycleScope.launch {
                        val heroes = repository.getHeroesByIds(heroIds)
                        viewModel.setDetectedHeroes(heroes)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Log.d(TAG, "OverlayService onCreate()")
        savedStateRegistryController.performRestore(null)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager

        startForegroundWithNotification()
        bindToCaptureService()

        val filter = IntentFilter(ScreenCaptureService.ACTION_HEROES_DETECTED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(detectionBroadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(detectionBroadcastReceiver, filter)
        }

        // Show floating overlay window
        showOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        when (intent?.action) {
            ACTION_SHOW_OVERLAY -> showOverlay()
            ACTION_HIDE_OVERLAY -> hideOverlay()
            ACTION_STOP_SERVICE -> {
                hideOverlay()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    /**
     * Creates and adds the floating ComposeView window to WindowManager.
     */
    fun showOverlay() {
        if (isOverlayVisible) {
            Log.d(TAG, "Overlay is already showing.")
            return
        }

        val wm = windowManager ?: return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 180
        }
        layoutParams = params

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)

            setContent {
                OverlayContent(
                    viewModel = viewModel,
                    onDragDelta = { dx, dy ->
                        updateOverlayPosition(dx, dy)
                    },
                    onManualScanRequested = {
                        triggerManualScan()
                    },
                    onSearchFocusChanged = { isFocused ->
                        updateKeyboardFocus(isFocused)
                    },
                    onCloseRequested = {
                        hideOverlay()
                        stopSelf()
                    }
                )
            }
        }

        overlayComposeView = composeView

        try {
            wm.addView(composeView, params)
            isOverlayVisible = true
            Log.d(TAG, "Overlay window added to WindowManager.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay window to WindowManager: ${e.message}", e)
        }
    }

    /**
     * Removes the floating window from WindowManager.
     */
    fun hideOverlay() {
        if (!isOverlayVisible) return
        val wm = windowManager ?: return
        val view = overlayComposeView ?: return

        try {
            wm.removeView(view)
            isOverlayVisible = false
            Log.d(TAG, "Overlay window removed from WindowManager.")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing overlay window: ${e.message}", e)
        }
    }

    /**
     * Updates displayed counters based on hero detection engine results.
     */
    fun updateResults(results: List<DetectionResult>) {
        lifecycleScope.launch {
            val heroIds = results.map { it.heroId }
            val heroes = repository.getHeroesByIds(heroIds)
            viewModel.setDetectedHeroes(heroes)
        }
    }

    /**
     * For manual hero input: loads hero entities for given IDs and sets them in viewModel.
     */
    fun setManualHeroes(heroIds: List<Int>) {
        lifecycleScope.launch {
            val heroes = repository.getHeroesByIds(heroIds)
            viewModel.setManualHeroes(heroes)
        }
    }

    /**
     * Handles dragging of the overlay view by delta (dx, dy).
     */
    private fun updateOverlayPosition(dx: Float, dy: Float) {
        val params = layoutParams ?: return
        val view = overlayComposeView ?: return
        val wm = windowManager ?: return

        params.x += dx.toInt()
        params.y += dy.toInt()

        try {
            wm.updateViewLayout(view, params)
        } catch (e: Exception) {
            Log.w(TAG, "Error updating overlay layout position: ${e.message}")
        }
    }

    /**
     * Enables or disables keyboard focus for search text fields in overlay.
     */
    private fun updateKeyboardFocus(enableFocus: Boolean) {
        val params = layoutParams ?: return
        val view = overlayComposeView ?: return
        val wm = windowManager ?: return

        if (enableFocus) {
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        } else {
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }

        try {
            wm.updateViewLayout(view, params)
        } catch (e: Exception) {
            Log.w(TAG, "Error updating window focus flags: ${e.message}")
        }
    }

    /**
     * Triggers immediate manual screen capture via bound ScreenCaptureService.
     */
    private fun triggerManualScan() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.startScan()
            val bitmap = screenCaptureService?.captureScreen()
            if (bitmap != null) {
                val results = detectionEngine.detectHeroes(bitmap)
                bitmap.recycle()
                updateResults(results)
            } else {
                Log.w(TAG, "Manual scan requested but screen capture returned null frame.")
            }
            viewModel.stopScan()
        }
    }

    private fun bindToCaptureService() {
        val intent = Intent(this, ScreenCaptureService::class.java)
        bindService(intent, captureServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun startForegroundWithNotification() {
        val stopIntent = Intent(this, OverlayService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, MLCountersApp.OVERLAY_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_channel_name))
            .setContentText("ML Counters floating overlay is active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close Overlay", stopPendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "OverlayService onDestroy()")
        hideOverlay()
        try {
            unregisterReceiver(detectionBroadcastReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "Broadcast receiver already unregistered: ${e.message}")
        }
        if (isCaptureBound) {
            unbindService(captureServiceConnection)
            isCaptureBound = false
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        vStore.clear()
    }

    companion object {
        private const val TAG = "OverlayService"
        private const val NOTIFICATION_ID = 3001

        const val ACTION_SHOW_OVERLAY = "com.mlcounters.app.action.SHOW_OVERLAY"
        const val ACTION_HIDE_OVERLAY = "com.mlcounters.app.action.HIDE_OVERLAY"
        const val ACTION_STOP_SERVICE = "com.mlcounters.app.action.STOP_OVERLAY_SERVICE"
    }
}
