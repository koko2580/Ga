package com.example.services

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.view.WindowManager
import com.example.domain.model.DeviceTelemetry

class DeviceTelemetryService(private val context: Context) {

    fun getTelemetry(activeProfile: String = "BALANCED"): DeviceTelemetry {
        // Memory Info
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)
        val availableRam = memInfo.availMem
        val totalRam = memInfo.totalMem

        // Storage Info
        val (availableStorage, totalStorage) = try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong
            Pair(availableBlocks * blockSize, totalBlocks * blockSize)
        } catch (_: Exception) {
            Pair(0L, 0L)
        }

        // Battery Info
        val batteryIntent = try {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        } catch (_: Exception) {
            null
        }

        val batteryLevel = batteryIntent?.let { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) (level * 100) / scale else 100
        } ?: 100

        val isCharging = batteryIntent?.let { intent ->
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        } ?: false

        val batteryTempC = batteryIntent?.let { intent ->
            val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            if (temp > 0) temp / 10f else null
        }

        // Refresh Rate
        val refreshRate = try {
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

        // Network Info
        val (networkType, isConnected) = getNetworkStatus()

        return DeviceTelemetry(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            brand = Build.BRAND.replaceFirstChar { it.uppercase() },
            deviceCode = Build.DEVICE,
            androidVersion = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT,
            availableRamBytes = availableRam,
            totalRamBytes = totalRam,
            availableStorageBytes = availableStorage,
            totalStorageBytes = totalStorage,
            batteryLevel = batteryLevel,
            isCharging = isCharging,
            batteryTemperatureC = batteryTempC,
            refreshRateHz = refreshRate,
            networkType = networkType,
            isNetworkConnected = isConnected,
            activeProfile = activeProfile
        )
    }

    private fun getNetworkStatus(): Pair<String, Boolean> {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return Pair("Unavailable", false)

        val activeNetwork = cm.activeNetwork ?: return Pair("Offline", false)
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return Pair("Offline", false)

        val isConnected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        val type = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular 5G/LTE"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Connected"
        }

        return Pair(type, isConnected)
    }
}
