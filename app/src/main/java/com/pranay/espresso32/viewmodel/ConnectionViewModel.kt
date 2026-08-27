package com.pranay.espresso32.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pranay.espresso32.data.model.DeviceInfo
import com.pranay.espresso32.data.network.ConnectionState
import com.pranay.espresso32.data.network.DiscoveryService
import com.pranay.espresso32.data.preferences.AppPreferences
import com.pranay.espresso32.data.repository.ESP32Repository
import com.pranay.espresso32.data.repository.LogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConnectionViewModel(
    private val repository: ESP32Repository,
    private val discoveryService: DiscoveryService,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _ipAddress = MutableStateFlow("")
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()

    private val _port = MutableStateFlow("81")
    val port: StateFlow<String> = _port.asStateFlow()

    private val _ipError = MutableStateFlow<String?>(null)
    val ipError: StateFlow<String?> = _ipError.asStateFlow()

    private val _portError = MutableStateFlow<String?>(null)
    val portError: StateFlow<String?> = _portError.asStateFlow()

    val connectionState: StateFlow<ConnectionState> = repository.connectionState

    val connectionLog: StateFlow<List<LogEntry>> = repository.connectionLog

    val deviceInfo: StateFlow<DeviceInfo?> = repository.deviceInfo

    private val _discoveredDevices = MutableStateFlow<List<DeviceInfo>>(emptyList())
    val discoveredDevices: StateFlow<List<DeviceInfo>> = _discoveredDevices.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.ipAddress.collect { savedIp ->
                if (savedIp.isNotEmpty() && _ipAddress.value.isEmpty()) {
                    _ipAddress.value = savedIp
                }
            }
        }
        viewModelScope.launch {
            preferences.port.collect { savedPort ->
                if (savedPort.isNotEmpty() && _port.value == "81") {
                    _port.value = savedPort
                }
            }
        }
    }

    fun onIpChanged(ip: String) {
        _ipAddress.value = ip
        _ipError.value = null
    }

    fun onPortChanged(p: String) {
        _port.value = p
        _portError.value = null
    }

    private fun validateIp(ip: String): Boolean {
        if (ip.isEmpty()) {
            _ipError.value = "IP address required"
            return false
        }
        val ipRegex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$".toRegex()
        return if (ip.matches(ipRegex)) {
            _ipError.value = null
            true
        } else {
            _ipError.value = "Invalid IP address"
            false
        }
    }

    private fun validatePort(portStr: String): Boolean {
        val portInt = portStr.toIntOrNull()
        return if (portInt != null && portInt in 1..65535) {
            _portError.value = null
            true
        } else {
            _portError.value = "Port must be 1-65535"
            false
        }
    }

    fun connect() {
        val ip = _ipAddress.value
        val p = _port.value
        if (!validateIp(ip) || !validatePort(p)) return

        repository.autoReconnectEnabled = true
        viewModelScope.launch {
            preferences.setIpAddress(ip)
            preferences.setPort(p)
        }
        repository.connect(ip, p.toInt())
    }

    fun disconnect() {
        repository.disconnect()
    }

    fun startDiscovery(context: Context) {
        viewModelScope.launch {
            _isDiscovering.value = true
            _discoveredDevices.value = emptyList()
            try {
                discoveryService.discoverDevices(context).collect { device ->
                    _discoveredDevices.value = _discoveredDevices.value + device
                }
            } catch (_: Exception) {
                // Discovery can fail silently
            } finally {
                _isDiscovering.value = false
            }
        }
    }

    fun selectDevice(device: DeviceInfo) {
        _ipAddress.value = device.ipAddress
        _port.value = device.port.toString()
        _ipError.value = null
        _portError.value = null
    }
}
