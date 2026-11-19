package com.mlcounters.app.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Utility helper class for managing Android system overlay, media projection,
 * and notification permissions.
 */
object PermissionHelper {

    private const val TAG = "PermissionHelper"
    const val REQUEST_CODE_OVERLAY = 1001
    const val REQUEST_CODE_MEDIA_PROJECTION = 1002
    const val REQUEST_CODE_NOTIFICATION = 1003

    /**
     * Checks if the app has the SYSTEM_ALERT_WINDOW permission to display floating overlays.
     */
    fun checkOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    /**
     * Navigates the user to the system settings page to grant overlay permission.
     */
    fun requestOverlayPermission(activity: Activity) {
        if (!checkOverlayPermission(activity)) {
            Log.d(TAG, "Requesting SYSTEM_ALERT_WINDOW overlay permission from user...")
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${activity.packageName}")
            )
            activity.startActivityForResult(intent, REQUEST_CODE_OVERLAY)
        } else {
            Log.d(TAG, "Overlay permission is already granted.")
        }
    }

    /**
     * MediaProjection screen capture always requires runtime authorization via system dialog.
     * Always returns false until explicitly granted during a session via MediaProjectionManager.
     */
    fun checkMediaProjectionPermission(): Boolean {
        // By Android security design, MediaProjection cannot be permanently granted;
        // it requires an interactive user consent prompt per session.
        return false
    }

    /**
     * Creates the Intent required to launch the MediaProjection screen capture permission dialog.
     */
    fun createMediaProjectionIntent(context: Context): Intent {
        val manager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        return manager.createScreenCaptureIntent()
    }

    /**
     * Registers and returns an ActivityResultLauncher for the MediaProjection screen capture intent.
     * Must be called during Activity initialization (e.g. onCreate).
     */
    fun createMediaProjectionRequest(
        activity: ComponentActivity,
        onResult: (resultCode: Int, data: Intent?) -> Unit
    ): ActivityResultLauncher<Intent> {
        return activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d(TAG, "MediaProjection result received. ResultCode=${result.resultCode}")
            onResult(result.resultCode, result.data)
        }
    }

    /**
     * Legacy launcher for standard Activities using startActivityForResult.
     */
    fun launchMediaProjectionRequest(activity: Activity) {
        val intent = createMediaProjectionIntent(activity)
        activity.startActivityForResult(intent, REQUEST_CODE_MEDIA_PROJECTION)
    }

    /**
     * Checks if notification permission is granted (required on Android 13+ / API 33+).
     */
    fun checkNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Requests POST_NOTIFICATIONS permission on Android 13+.
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!checkNotificationPermission(activity)) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_NOTIFICATION
                )
            }
        }
    }
}
