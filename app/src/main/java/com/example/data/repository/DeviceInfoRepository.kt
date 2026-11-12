package com.example.data.repository

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.view.WindowManager
import com.example.domain.model.BatteryInfo
import com.example.domain.model.DeviceSpecification
import com.example.domain.model.DeviceTelemetry
import com.example.domain.model.MemoryStats
import com.example.domain.model.NetworkConnectionState
import com.example.domain.model.StorageStats
import com.example.domain.model.ThermalInfo

class DeviceInfoRepository(private val context: Context) {

    fun getDeviceSpecification(): DeviceSpecification {
        return DeviceSpecification()
    }

    fun getMemoryStats(): MemoryStats {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(memInfo)
        val total = if (memInfo.totalMem > 0L) {
            memInfo.totalMem
        } else {
            Runtime.getRuntime().maxMemory().coerceAtLeast(1024L * 1024L * 1024L)
        }
        val avail = if (memInfo.availMem > 0L) memInfo.availMem else Runtime.getRuntime().freeMemory()
        return MemoryStats(
            totalRamBytes = total,
            availableRamBytes = avail,
            isLowMemory = memInfo.lowMemory
        )
    }

    fun getStorageStats(): StorageStats {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong
            StorageStats(
                totalStorageBytes = totalBlocks * blockSize,
                availableStorageBytes = availableBlocks * blockSize
            )
        } catch (_: Exception) {
            StorageStats(0L, 0L)
        }
    }

    fun getScreenRefreshRate(): Float {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display?.refreshRate ?: 60f
            } else {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                @Suppress("DEPRECATION")
                wm?.defaultDisplay?.refreshRate ?: 60f
            }
        } catch (_: Exception) {
            60f
        }
    }

    fun getCompositeTelemetry(
        activeProfile: String = "BALANCED",
        batteryInfo: BatteryInfo? = null,
        thermalInfo: ThermalInfo? = null,
        networkState: NetworkConnectionState? = null
    ): DeviceTelemetry {
        val spec = getDeviceSpecification()
        val mem = getMemoryStats()
        val storage = getStorageStats()
        val refresh = getScreenRefreshRate()

        return DeviceTelemetry(
            manufacturer = spec.manufacturer,
            model = spec.model,
            brand = spec.brand,
            deviceCode = Build.DEVICE,
            androidVersion = spec.androidVersion,
            sdkInt = spec.sdkInt,
            availableRamBytes = mem.availableRamBytes,
            totalRamBytes = mem.totalRamBytes,
            availableStorageBytes = storage.availableStorageBytes,
            totalStorageBytes = storage.totalStorageBytes,
            batteryLevel = batteryInfo?.percentage ?: 100,
            isCharging = batteryInfo?.isCharging ?: false,
            batteryTemperatureC = batteryInfo?.temperatureC,
            refreshRateHz = refresh,
            networkType = networkState?.typeName ?: "Unknown",
            isNetworkConnected = networkState?.isConnected ?: false,
            activeProfile = activeProfile,
            isLowMemory = mem.isLowMemory,
            hardware = spec.hardware,
            supportedAbis = spec.supportedAbis,
            batteryVoltageMv = batteryInfo?.voltageMv,
            batteryTech = batteryInfo?.technology,
            thermalStatus = thermalInfo?.level?.displayName ?: "Normal"
        )
    }
}
