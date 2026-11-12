package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ping_history")
data class PingHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val host: String,
    val avgLatencyMs: Long,
    val minLatencyMs: Long,
    val maxLatencyMs: Long,
    val jitterMs: Long,
    val packetLossPercent: Int,
    val timestamp: Long = System.currentTimeMillis()
)
