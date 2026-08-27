package com.pranay.espresso32.data.repository

import com.pranay.espresso32.data.model.DeviceInfo
import com.pranay.espresso32.data.model.ESP32MessageDto
import com.pranay.espresso32.data.network.ConnectionState
import com.pranay.espresso32.data.network.WebSocketClient
import com.pranay.espresso32.domain.model.SensorReading
import com.pranay.espresso32.domain.model.SensorType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

data class LogEntry(val timestamp: Long, val message: String)

class ESP32Repository(
    private val webSocketClient: WebSocketClient
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val connectionState: StateFlow<ConnectionState> = webSocketClient.connectionState

    private val _sensorData = MutableStateFlow<Map<String, SensorReading>>(emptyMap())
    val sensorData: StateFlow<Map<String, SensorReading>> = _sensorData.asStateFlow()

    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    val deviceInfo: StateFlow<DeviceInfo?> = _deviceInfo.asStateFlow()

    val lastMessageTime: StateFlow<Long> = webSocketClient.lastMessageTime

    private val _connectionLog = MutableStateFlow<List<LogEntry>>(emptyList())
    val connectionLog: StateFlow<List<LogEntry>> = _connectionLog.asStateFlow()

    var autoReconnectEnabled: Boolean = true
    
    private var lastIp: String? = null
    private var lastPort: Int? = null

    private val buffer = mutableMapOf<String, MutableList<SensorReading>>()

    init {
        repositoryScope.launch {
            webSocketClient.messages.collect { jsonString ->
                try {
                    val dto = ESP32MessageDto.json.decodeFromString<ESP32MessageDto>(jsonString)
                    
                    if (dto.device != null) {
                        _deviceInfo.value = DeviceInfo(
                            name = dto.device.name ?: "ESP32",
                            id = dto.device.id ?: ""
                        )
                    }

                    val timestamp = dto.timestamp?.times(1000) ?: System.currentTimeMillis()
                    val newData = mutableMapOf<String, SensorReading>()

                    dto.data?.forEach { (key, jsonElement) ->
                        val primitive = jsonElement.jsonPrimitive
                        
                        val booleanVal = primitive.booleanOrNull
                        val doubleVal = primitive.doubleOrNull
                        val stringVal = primitive.content

                        val type: SensorType
                        val value: Any
                        val unit: String
                        var formattedValue = stringVal
                        val iconName: String

                        when {
                            booleanVal != null -> {
                                type = SensorType.BOOLEAN
                                value = booleanVal
                                unit = ""
                                iconName = if (key.contains("motion", ignoreCase = true)) "DirectionsRun" else "ToggleOn"
                                formattedValue = if (booleanVal) "On" else "Off"
                            }
                            doubleVal != null && key in listOf("battery", "humidity", "signal") -> {
                                type = SensorType.PERCENTAGE
                                value = doubleVal
                                unit = "%"
                                iconName = when(key) {
                                    "battery" -> "BatteryFull"
                                    "humidity" -> "WaterDrop"
                                    else -> "SettingsEthernet"
                                }
                                formattedValue = "%.1f %s".format(doubleVal, unit)
                            }
                            doubleVal != null -> {
                                type = SensorType.NUMERIC
                                value = doubleVal
                                unit = getUnitForKey(key)
                                iconName = getIconForKey(key)
                                formattedValue = "%.1f %s".format(doubleVal, unit)
                            }
                            else -> {
                                type = SensorType.TEXT
                                value = stringVal
                                unit = ""
                                iconName = "Info"
                                formattedValue = stringVal
                            }
                        }

                        val reading = SensorReading(
                            key = key,
                            displayName = key.replaceFirstChar { it.uppercase() },
                            value = value,
                            formattedValue = formattedValue,
                            unit = unit,
                            type = type,
                            iconName = iconName,
                            timestamp = timestamp
                        )
                        newData[key] = reading

                        val list = buffer.getOrPut(key) { mutableListOf() }
                        list.add(reading)
                        if (list.size > 60) list.removeAt(0)
                    }

                    if (newData.isNotEmpty()) {
                        _sensorData.update { current -> current + newData }
                    }

                } catch (e: Exception) {
                    addLog("Parse error: ${e.message}")
                }
            }
        }

        repositoryScope.launch {
            var backoff = 1000L
            connectionState.collect { state ->
                when (state) {
                    is ConnectionState.Connected -> {
                        addLog("Connected to ${state.deviceInfo.ipAddress}")
                        backoff = 1000L
                    }
                    is ConnectionState.Disconnected, is ConnectionState.Error -> {
                        val reason = if (state is ConnectionState.Error) state.message else "Disconnected"
                        addLog(reason)
                        if (autoReconnectEnabled && lastIp != null && lastPort != null) {
                            delay(backoff)
                            if (autoReconnectEnabled) {
                                addLog("Reconnecting to $lastIp:$lastPort...")
                                webSocketClient.connect(lastIp!!, lastPort!!)
                                backoff = (backoff * 2).coerceAtMost(30000L)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun connect(ip: String, port: Int) {
        lastIp = ip
        lastPort = port
        addLog("Connecting to $ip:$port...")
        webSocketClient.connect(ip, port)
    }

    fun disconnect() {
        autoReconnectEnabled = false
        webSocketClient.disconnect()
    }

    fun sendCommand(json: String) {
        webSocketClient.sendMessage(json)
    }

    private fun addLog(message: String) {
        _connectionLog.update { current ->
            val newEntry = LogEntry(System.currentTimeMillis(), message)
            (current + newEntry).takeLast(100)
        }
    }

    private fun getUnitForKey(key: String): String = when {
        key.contains("temp", ignoreCase = true) -> "°C"
        key.contains("pressure", ignoreCase = true) -> "hPa"
        key.contains("light", ignoreCase = true) -> "lux"
        key.contains("voltage", ignoreCase = true) -> "V"
        key.contains("current", ignoreCase = true) -> "A"
        key.contains("speed", ignoreCase = true) -> "m/s"
        key.contains("distance", ignoreCase = true) -> "m"
        else -> ""
    }

    private fun getIconForKey(key: String): String = when {
        key.contains("temp", ignoreCase = true) -> "Thermostat"
        key.contains("pressure", ignoreCase = true) -> "Compress"
        key.contains("light", ignoreCase = true) -> "LightMode"
        key.contains("voltage", ignoreCase = true) -> "Bolt"
        else -> "Sensors"
    }
}
