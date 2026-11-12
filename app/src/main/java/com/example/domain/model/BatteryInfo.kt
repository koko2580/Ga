package com.example.domain.model

enum class BatteryChargingStatus {
    CHARGING,
    DISCHARGING,
    FULL,
    NOT_CHARGING,
    UNKNOWN
}

enum class BatteryPlugType {
    AC,
    USB,
    WIRELESS,
    UNPLUGGED,
    UNKNOWN
}

data class BatteryInfo(
    val percentage: Int,
    val chargingStatus: BatteryChargingStatus,
    val temperatureC: Float?,
    val voltageMv: Int?,
    val technology: String?,
    val plugType: BatteryPlugType,
    val isPresent: Boolean = true
) {
    val isCharging: Boolean
        get() = chargingStatus == BatteryChargingStatus.CHARGING || chargingStatus == BatteryChargingStatus.FULL

    fun formattedVoltage(): String {
        return if (voltageMv != null && voltageMv > 0) {
            String.format(java.util.Locale.US, "%.2f V", voltageMv / 1000.0)
        } else {
            "Unavailable"
        }
    }

    fun formattedTemperature(): String {
        return if (temperatureC != null) {
            String.format(java.util.Locale.US, "%.1f°C", temperatureC)
        } else {
            "Unavailable"
        }
    }
}
