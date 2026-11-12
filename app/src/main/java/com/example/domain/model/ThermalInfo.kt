package com.example.domain.model

enum class ThermalStatusLevel(val rawLevel: Int, val displayName: String) {
    NONE(-1, "Unsupported"),
    NORMAL(0, "Normal"),
    LIGHT(1, "Light"),
    MODERATE(2, "Moderate"),
    SEVERE(3, "Severe"),
    CRITICAL(4, "Critical"),
    EMERGENCY(5, "Emergency"),
    SHUTDOWN(6, "Shutdown");

    companion object {
        fun fromInt(value: Int): ThermalStatusLevel {
            return entries.firstOrNull { it.rawLevel == value } ?: NORMAL
        }
    }
}

data class ThermalInfo(
    val level: ThermalStatusLevel,
    val isThrottling: Boolean,
    val advisoryNotice: String
)
