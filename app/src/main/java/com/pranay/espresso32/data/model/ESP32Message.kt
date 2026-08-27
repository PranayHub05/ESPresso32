package com.pranay.espresso32.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

@Serializable
data class ESP32MessageDto(
    val device: DeviceDto? = null,
    val timestamp: Long? = null,
    val data: JsonObject? = null
) {
    companion object {
        val json = Json { 
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
    }
}

@Serializable
data class DeviceDto(
    val name: String? = null,
    val id: String? = null
)
