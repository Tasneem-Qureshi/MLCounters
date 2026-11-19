package com.mlcounters.app.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mlcounters.app.ui.theme.BorderDark
import com.mlcounters.app.ui.theme.DarkGraySurface
import com.mlcounters.app.ui.theme.DeepBlack
import com.mlcounters.app.ui.theme.ElectricBlue
import com.mlcounters.app.ui.theme.NeonOrange
import com.mlcounters.app.ui.theme.SurfaceVariantDark
import com.mlcounters.app.ui.theme.TextLightGray
import com.mlcounters.app.ui.theme.TextMutedGray
import com.mlcounters.app.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val sensitivity by viewModel.detectionSensitivity.collectAsState()
    val opacity by viewModel.overlayOpacity.collectAsState()
    val autoDetect by viewModel.autoDetect.collectAsState()
    val dataSourceUrl by viewModel.dataSourceUrl.collectAsState()
    val appVersion by viewModel.appVersion.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "SETTINGS",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = TextWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DeepBlack)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Detection Settings
            SettingsSection(
                title = "DETECTION ENGINE",
                icon = Icons.Default.Psychology
            ) {
                // Auto Detect Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertizontally,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Detect Draft",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextWhite
                        )
                        Text(
                            text = "Continuously scan screen during draft picking phase",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMutedGray
                        )
                    }
                    Switch(
                        checked = autoDetect,
                        onCheckedChange = { viewModel.toggleAutoDetect() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextWhite,
                            checkedTrackColor = ElectricBlue,
                            uncheckedTrackColor = SurfaceVariantDark,
                            uncheckedThumbColor = TextMutedGray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sensitivity Slider
                Text(
                    text = "Matching Sensitivity: ${(sensitivity * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextWhite
                )
                Text(
                    text = "Higher threshold reduces false matches but requires clearer portraits",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMutedGray
                )
                Slider(
                    value = sensitivity,
                    onValueChange = { viewModel.setSensitivity(it) },
                    valueRange = 0.5f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricBlue,
                        activeTrackColor = ElectricBlue,
                        inactiveTrackColor = SurfaceVariantDark
                    )
                )
            }

            // Section 2: Floating Overlay Settings
            SettingsSection(
                title = "FLOATING OVERLAY",
                icon = Icons.Default.Layers
            ) {
                Text(
                    text = "Overlay Window Opacity: ${(opacity * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextWhite
                )
                Text(
                    text = "Adjust transparency of in-game overlay cards",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMutedGray
                )
                Slider(
                    value = opacity,
                    onValueChange = { viewModel.setOpacity(it) },
                    valueRange = 0.3f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricBlue,
                        activeTrackColor = ElectricBlue,
                        inactiveTrackColor = SurfaceVariantDark
                    )
                )
            }

            // Section 3: Data Synchronization
            SettingsSection(
                title = "DATA SYNCHRONIZATION",
                icon = Icons.Default.CloudSync
            ) {
                OutlinedTextField(
                    value = dataSourceUrl,
                    onValueChange = { viewModel.setDataSource(it) },
                    label = { Text("Meta Data Source API Endpoint", color = TextMutedGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextLightGray,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = BorderDark
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertizontally,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Last Synced",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMutedGray
                        )
                        Text(
                            text = lastSyncTime,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = NeonOrange
                        )
                    }

                    Button(
                        onClick = { viewModel.triggerSync() },
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = TextWhite,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(text = if (isSyncing) "Syncing..." else "Sync Now")
                    }
                }
            }

            // Section 4: About & Credits
            SettingsSection(
                title = "ABOUT",
                icon = Icons.Default.Info
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "App Version",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextLightGray
                    )
                    Text(
                        text = appVersion,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ML Counters is an esports draft strategy and live item counter recommendation tool for Mobile Legends: Bang Bang.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMutedGray
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Powered by OpenCV image analysis, Room database, and Jetpack Compose.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextMutedGray
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGraySurface),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertizontally,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = ElectricBlue
                )
            }

            content()
        }
    }
}
