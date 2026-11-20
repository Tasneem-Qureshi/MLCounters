package com.mlcounters.app

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mlcounters.app.data.repository.HeroRepository
import com.mlcounters.app.data.repository.ItemRepository
import com.mlcounters.app.detection.ScreenCaptureService
import com.mlcounters.app.overlay.OverlayService
import com.mlcounters.app.ui.navigation.NavGraph
import com.mlcounters.app.ui.navigation.Screen
import com.mlcounters.app.ui.navigation.bottomNavItems
import com.mlcounters.app.ui.theme.DarkGraySurface
import com.mlcounters.app.ui.theme.DeepBlack
import com.mlcounters.app.ui.theme.ElectricBlue
import com.mlcounters.app.ui.theme.MLCountersTheme
import com.mlcounters.app.ui.theme.TextMutedGray
import com.mlcounters.app.ui.theme.TextWhite
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var heroRepository: HeroRepository

    @Inject
    lateinit var itemRepository: ItemRepository

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission granted! Requesting screen capture...", Toast.LENGTH_SHORT).show()
                requestMediaProjectionPermission()
            } else {
                Toast.makeText(this, "Overlay permission is required for ML Helper floating HUD", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            Toast.makeText(this, "Auto-detection enabled! Starting overlay...", Toast.LENGTH_SHORT).show()
            startCaptureService(result.resultCode, result.data!!)
            startOverlayService()
        } else {
            Toast.makeText(this, "Screen capture not granted. Starting overlay in manual mode.", Toast.LENGTH_LONG).show()
            startOverlayService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Load initial data on first launch
        loadInitialDataIfNeeded()

        setContent {
            MLCountersTheme {
                MainAppScaffold(
                    onToggleOverlay = {
                        val isRunning = OverlayService.isRunning.value
                        if (isRunning) {
                            stopOverlayAndCapture()
                        } else {
                            startOverlayFlow()
                        }
                    }
                )
            }
        }
    }

    fun startOverlayFlow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
            Toast.makeText(
                this,
                "Please enable 'Display over other apps' to use the floating overlay",
                Toast.LENGTH_LONG
            ).show()
        } else {
            requestMediaProjectionPermission()
        }
    }

    fun requestMediaProjectionPermission() {
        val mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        if (mediaProjectionManager != null) {
            try {
                val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
                mediaProjectionLauncher.launch(captureIntent)
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to launch screen capture intent: ${e.message}", e)
                startOverlayService()
            }
        } else {
            startOverlayService()
        }
    }

    private fun startCaptureService(resultCode: Int, data: Intent) {
        val captureIntent = Intent(this, ScreenCaptureService::class.java).apply {
            action = ScreenCaptureService.ACTION_START_CAPTURE
            putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, resultCode)
            putExtra(ScreenCaptureService.EXTRA_DATA_INTENT, data)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(captureIntent)
            } else {
                startService(captureIntent)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to start ScreenCaptureService: ${e.message}", e)
        }
    }

    private fun startOverlayService() {
        val overlayIntent = Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_SHOW_OVERLAY
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(overlayIntent)
            } else {
                startService(overlayIntent)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to start OverlayService: ${e.message}", e)
        }
    }

    fun stopOverlayAndCapture() {
        try {
            val stopCaptureIntent = Intent(this, ScreenCaptureService::class.java).apply {
                action = ScreenCaptureService.ACTION_STOP_CAPTURE
            }
            startService(stopCaptureIntent)
        } catch (e: Exception) {
            Log.w("MainActivity", "Error stopping ScreenCaptureService: ${e.message}")
        }

        try {
            val stopOverlayIntent = Intent(this, OverlayService::class.java).apply {
                action = OverlayService.ACTION_STOP_SERVICE
            }
            startService(stopOverlayIntent)
        } catch (e: Exception) {
            Log.w("MainActivity", "Error stopping OverlayService: ${e.message}")
        }
        Toast.makeText(this, "ML Helper floating overlay stopped", Toast.LENGTH_SHORT).show()
    }

    private fun loadInitialDataIfNeeded() {
        lifecycleScope.launch(Dispatchers.IO) {
            val prefs = getSharedPreferences("ml_counters_prefs", Context.MODE_PRIVATE)
            val isFirstRun = prefs.getBoolean("is_first_run", true)
            if (isFirstRun) {
                // Initial assets or room database bootstrap
                try {
                    val assetList = assets.list("") ?: emptyArray()
                    if (assetList.contains("heroes.json") || assetList.contains("items.json")) {
                        // Seed database from packaged assets if provided
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                prefs.edit().putBoolean("is_first_run", false).apply()
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    onToggleOverlay: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if bottom bar should be visible on top-level routes
    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepBlack,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DarkGraySurface,
                    contentColor = TextWhite
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ElectricBlue,
                                selectedTextColor = ElectricBlue,
                                unselectedIconColor = TextMutedGray,
                                unselectedTextColor = TextMutedGray,
                                indicatorColor = ElectricBlue.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            onToggleOverlay = onToggleOverlay,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}
