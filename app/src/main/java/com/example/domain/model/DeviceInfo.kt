package com.example.domain.model

import android.os.Build
import java.util.Locale

data class DeviceSpecification(
    val manufacturer: String = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
    val model: String = Build.MODEL,
    val brand: String = Build.BRAND.replaceFirstChar { it.uppercase() },
    val androidVersion: String = Build.VERSION.RELEASE,
    val sdkInt: Int = Build.VERSION.SDK_INT,
    val supportedAbis: List<String> = Build.SUPPORTED_ABIS?.toList() ?: emptyList(),
    val hardware: String = Build.HARDWARE
)

data class MemoryStats(
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val isLowMemory: Boolean = false
) {
    val usedRamBytes: Long
        get() = (totalRamBytes - availableRamBytes).coerceAtLeast(0L)

    val ramUsageRatio: Float
        get() = if (totalRamBytes > 0) (usedRamBytes.toFloat() / totalRamBytes.toFloat()).coerceIn(0f, 1f) else 0f

    val ramUsagePercent: Int
        get() = (ramUsageRatio * 100).toInt()

    fun formattedSummary(): String {
        return "${DeviceTelemetry.formatBytes(availableRamBytes)} free of ${DeviceTelemetry.formatBytes(totalRamBytes)}"
    }
}

data class StorageStats(
    val totalStorageBytes: Long,
    val availableStorageBytes: Long
) {
    val usedStorageBytes: Long
        get() = (totalStorageBytes - availableStorageBytes).coerceAtLeast(0L)

    val storageUsageRatio: Float
        get() = if (totalStorageBytes > 0) (usedStorageBytes.toFloat() / totalStorageBytes.toFloat()).coerceIn(0f, 1f) else 0f

    val storageUsagePercent: Int
        get() = (storageUsageRatio * 100).toInt()

    fun formattedSummary(): String {
        return "${DeviceTelemetry.formatBytes(availableStorageBytes)} free of ${DeviceTelemetry.formatBytes(totalStorageBytes)}"
    }
}

data class DeviceTelemetry(
    val manufacturer: String,
    val model: String,
    val brand: String,
    val deviceCode: String,
    val androidVersion: String,
    val sdkInt: Int,
    val availableRamBytes: Long,
    val totalRamBytes: Long,
    val availableStorageBytes: Long,
    val totalStorageBytes: Long,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val batteryTemperatureC: Float?,
    val refreshRateHz: Float,
    val networkType: String,
    val isNetworkConnected: Boolean,
    val activeProfile: String = "BALANCED",
    val isLowMemory: Boolean = false,
    val hardware: String = Build.HARDWARE,
    val supportedAbis: List<String> = Build.SUPPORTED_ABIS?.toList() ?: emptyList(),
    val batteryVoltageMv: Int? = null,
    val batteryTech: String? = null,
    val thermalStatus: String = "Normal"
) {
    val ramUsedBytes: Long
        get() = (totalRamBytes - availableRamBytes).coerceAtLeast(0L)

    val ramUsageRatio: Float
        get() = if (totalRamBytes > 0) (ramUsedBytes.toFloat() / totalRamBytes.toFloat()).coerceIn(0f, 1f) else 0f

    val ramUsagePercent: Int
        get() = (ramUsageRatio * 100).toInt()

    val storageUsedBytes: Long
        get() = (totalStorageBytes - availableStorageBytes).coerceAtLeast(0L)

    val storageUsageRatio: Float
        get() = if (totalStorageBytes > 0) (storageUsedBytes.toFloat() / totalStorageBytes.toFloat()).coerceIn(0f, 1f) else 0f

    val storageUsagePercent: Int
        get() = (storageUsageRatio * 100).toInt()

    fun formattedRam(): String {
        return "${formatBytes(availableRamBytes)} free of ${formatBytes(totalRamBytes)}"
    }

    fun formattedStorage(): String {
        return "${formatBytes(availableStorageBytes)} free of ${formatBytes(totalStorageBytes)}"
    }

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val gb = bytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
            if (gb >= 1.0) {
                return String.format(Locale.US, "%.1f GB", gb)
            }
            val mb = bytes.toDouble() / (1024.0 * 1024.0)
            return String.format(Locale.US, "%.0f MB", mb)
        }
    }
}
