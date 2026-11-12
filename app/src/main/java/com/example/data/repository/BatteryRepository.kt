package com.example.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.domain.model.BatteryChargingStatus
import com.example.domain.model.BatteryInfo
import com.example.domain.model.BatteryPlugType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BatteryRepository(private val context: Context) {

    /**
     * Emits live battery updates using ACTION_BATTERY_CHANGED.
     * The BroadcastReceiver is cleanly registered when collected and unregistered on cancel/dispose.
     */
    val batteryUpdates: Flow<BatteryInfo> = callbackFlow {
        // Send initial sticky state immediately
        trySend(getBatterySnapshot())

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent != null && intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    trySend(parseBatteryIntent(intent))
                }
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    /**
     * Synchronously queries the current sticky ACTION_BATTERY_CHANGED broadcast without keeping a receiver registered.
     */
    fun getBatterySnapshot(): BatteryInfo {
        val intent = try {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        } catch (_: Exception) {
            null
        }
        return parseBatteryIntent(intent)
    }

    private fun parseBatteryIntent(intent: Intent?): BatteryInfo {
        if (intent == null) {
            return BatteryInfo(
                percentage = 100,
                chargingStatus = BatteryChargingStatus.UNKNOWN,
                temperatureC = null,
                voltageMv = null,
                technology = null,
                plugType = BatteryPlugType.UNKNOWN,
                isPresent = false
            )
        }

        // Percentage
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percentage = if (level >= 0 && scale > 0) {
            ((level.toFloat() / scale.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else {
            100
        }

        // Charging Status
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val chargingStatus = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> BatteryChargingStatus.CHARGING
            BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryChargingStatus.DISCHARGING
            BatteryManager.BATTERY_STATUS_FULL -> BatteryChargingStatus.FULL
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryChargingStatus.NOT_CHARGING
            else -> BatteryChargingStatus.UNKNOWN
        }

        // Temperature (Android gives tenths of a degree Celsius, e.g. 295 = 29.5°C)
        val rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        val temperatureC = if (rawTemp != Int.MIN_VALUE && rawTemp > 0) {
            rawTemp / 10.0f
        } else {
            null
        }

        // Voltage (in mV)
        val rawVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val voltageMv = if (rawVoltage > 0) rawVoltage else null

        // Technology
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)

        // Plug Type
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val plugType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> BatteryPlugType.AC
            BatteryManager.BATTERY_PLUGGED_USB -> BatteryPlugType.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> BatteryPlugType.WIRELESS
            0 -> BatteryPlugType.UNPLUGGED
            else -> BatteryPlugType.UNKNOWN
        }

        val isPresent = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true)

        return BatteryInfo(
            percentage = percentage,
            chargingStatus = chargingStatus,
            temperatureC = temperatureC,
            voltageMv = voltageMv,
            technology = technology,
            plugType = plugType,
            isPresent = isPresent
        )
    }
}
