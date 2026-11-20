package com.mlcounters.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlcounters.app.data.repository.HeroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val heroRepository: HeroRepository
) : ViewModel() {

    private val _detectionSensitivity = MutableStateFlow(0.85f)
    val detectionSensitivity: StateFlow<Float> = _detectionSensitivity.asStateFlow()

    private val _overlayOpacity = MutableStateFlow(0.90f)
    val overlayOpacity: StateFlow<Float> = _overlayOpacity.asStateFlow()

    private val _autoDetect = MutableStateFlow(true)
    val autoDetect: StateFlow<Boolean> = _autoDetect.asStateFlow()

    private val _dataSourceUrl = MutableStateFlow("https://raw.githubusercontent.com/Tasneem-Qureshi/MLCounters/main/data/")
    val dataSourceUrl: StateFlow<String> = _dataSourceUrl.asStateFlow()

    private val _appVersion = MutableStateFlow("1.0.2 (Build 36)")
    val appVersion: StateFlow<String> = _appVersion.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(getFormattedTimestamp())
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()

    fun setSensitivity(value: Float) {
        _detectionSensitivity.value = value.coerceIn(0.5f, 1.0f)
    }

    fun setOpacity(value: Float) {
        _overlayOpacity.value = value.coerceIn(0.3f, 1.0f)
    }

    fun toggleAutoDetect() {
        _autoDetect.value = !_autoDetect.value
    }

    fun setDataSource(url: String) {
        _dataSourceUrl.value = url
    }

    fun triggerSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatus.value = "Syncing data from GitHub..."
            try {
                val res = heroRepository.refreshData()
                if (res.isSuccess) {
                    _lastSyncTime.value = getFormattedTimestamp()
                    _syncStatus.value = "Sync successful (${res.getOrNull()} heroes loaded)"
                } else {
                    _syncStatus.value = "Sync error: ${res.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _syncStatus.value = "Sync failed: ${e.message}"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    private fun getFormattedTimestamp(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}
