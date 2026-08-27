package com.pranay.espresso32.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pranay.espresso32.data.model.DeviceInfo
import com.pranay.espresso32.data.network.ConnectionState
import com.pranay.espresso32.data.preferences.AppPreferences
import com.pranay.espresso32.data.repository.ESP32Repository
import com.pranay.espresso32.domain.model.SensorReading
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: ESP32Repository,
    private val preferences: AppPreferences
) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = repository.connectionState

    val deviceInfo: StateFlow<DeviceInfo?> = repository.deviceInfo

    val sensors: StateFlow<List<SensorReading>> = repository.sensorData
        .map { sensorMap ->
            val lastMsg = repository.lastMessageTime.value
            val isStale = lastMsg > 0 && (System.currentTimeMillis() - lastMsg) > 30_000L
            sensorMap.values
                .map { if (isStale) it.copy(isStale = true) else it }
                .sortedBy { it.displayName }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    private val _lastUpdateText = MutableStateFlow("Never")
    val lastUpdateText: StateFlow<String> = _lastUpdateText.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.fullscreenMode.collect {
                _isFullscreen.value = it
            }
        }

        // Update "last updated" text every second
        viewModelScope.launch {
            while (true) {
                val lastTime = repository.lastMessageTime.value
                _lastUpdateText.value = if (lastTime > 0) {
                    val diffSec = (System.currentTimeMillis() - lastTime) / 1000
                    when {
                        diffSec < 2 -> "just now"
                        diffSec < 60 -> "$diffSec seconds ago"
                        else -> "${diffSec / 60} minutes ago"
                    }
                } else {
                    "Never"
                }
                delay(1000)
            }
        }
    }

    fun setFullscreen(enabled: Boolean) {
        _isFullscreen.value = enabled
        viewModelScope.launch {
            preferences.setFullscreen(enabled)
        }
    }

    fun toggleFullscreen() {
        setFullscreen(!_isFullscreen.value)
    }

    fun disconnect() {
        repository.disconnect()
    }
}
