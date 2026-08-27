package com.pranay.espresso32.domain.model

import com.pranay.espresso32.data.network.ConnectionState
import com.pranay.espresso32.data.model.DeviceInfo

data class DashboardState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val deviceInfo: DeviceInfo? = null,
    val sensors: Map<String, SensorReading> = emptyMap(),
    val lastMessageTime: Long = 0L,
    val isFullscreen: Boolean = false
)
