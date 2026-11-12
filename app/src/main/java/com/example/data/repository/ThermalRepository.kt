package com.example.data.repository

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.example.domain.model.ThermalInfo
import com.example.domain.model.ThermalStatusLevel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ThermalRepository(private val context: Context) {

    private val powerManager: PowerManager? = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    val thermalUpdates: Flow<ThermalInfo> = callbackFlow {
        // Send initial reading
        trySend(getThermalStatusSnapshot())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            val listener = PowerManager.OnThermalStatusChangedListener { status ->
                val level = ThermalStatusLevel.fromInt(status)
                trySend(createThermalInfo(level))
            }

            val executor = ContextCompat.getMainExecutor(context)
            try {
                powerManager.addThermalStatusListener(executor, listener)
            } catch (_: Exception) {}

            awaitClose {
                try {
                    powerManager.removeThermalStatusListener(listener)
                } catch (_: Exception) {}
            }
        } else {
            // Static unsupported or normal on legacy devices
            awaitClose {}
        }
    }

    fun getThermalStatusSnapshot(): ThermalInfo {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            val rawStatus = try {
                powerManager.currentThermalStatus
            } catch (_: Exception) {
                PowerManager.THERMAL_STATUS_NONE
            }
            return createThermalInfo(ThermalStatusLevel.fromInt(rawStatus))
        }

        return ThermalInfo(
            level = ThermalStatusLevel.NORMAL,
            isThrottling = false,
            advisoryNotice = "Normal operational status (Device hardware thermal API requires Android 10+)"
        )
    }

    private fun createThermalInfo(level: ThermalStatusLevel): ThermalInfo {
        val isThrottling = level.rawLevel >= ThermalStatusLevel.MODERATE.rawLevel
        val notice = when (level) {
            ThermalStatusLevel.NORMAL -> "Optimal thermals. No hardware throttling active."
            ThermalStatusLevel.LIGHT -> "Slight thermal rise. Game performance remains unaffected."
            ThermalStatusLevel.MODERATE -> "Moderate heat. Frame pacing may throttle slightly."
            ThermalStatusLevel.SEVERE -> "High thermal load. SOC may drop GPU/CPU frequencies to cool."
            ThermalStatusLevel.CRITICAL -> "Critical heat level. Immediate cool-down recommended."
            ThermalStatusLevel.EMERGENCY -> "Emergency threshold. Hardware protective throttling engaged."
            ThermalStatusLevel.SHUTDOWN -> "Thermal shutdown imminent."
            ThermalStatusLevel.NONE -> "Device thermal API not supported on this Android release."
        }

        return ThermalInfo(
            level = level,
            isThrottling = isThrottling,
            advisoryNotice = notice
        )
    }
}
