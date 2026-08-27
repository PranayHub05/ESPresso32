package com.pranay.espresso32.domain.model

data class SensorReading(
    val key: String,
    val displayName: String,
    val value: Any,
    val formattedValue: String,
    val unit: String,
    val type: SensorType,
    val iconName: String,
    val timestamp: Long,
    val isStale: Boolean = false
) {
    /** Convenience to get value as display string */
    val displayValue: String
        get() = when (value) {
            is Double -> {
                if (value == value.toLong().toDouble()) value.toLong().toString()
                else "%.1f".format(value)
            }
            is Float -> "%.1f".format(value)
            is Boolean -> if (value) "true" else "false"
            else -> value.toString()
        }
}
