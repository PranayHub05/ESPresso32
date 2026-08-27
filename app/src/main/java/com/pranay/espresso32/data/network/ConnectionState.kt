package com.pranay.espresso32.data.network

import com.pranay.espresso32.data.model.DeviceInfo

sealed interface ConnectionState {
    object Disconnected : ConnectionState
    object Connecting : ConnectionState
    data class Connected(val deviceInfo: DeviceInfo) : ConnectionState
    data class Reconnecting(val attempt: Int) : ConnectionState
    data class Error(val message: String, val cause: Throwable? = null) : ConnectionState
}
