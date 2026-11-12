package com.example.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.abs

data class PingResult(
    val host: String,
    val latencies: List<Long>,
    val minLatency: Long,
    val maxLatency: Long,
    val avgLatency: Long,
    val jitter: Long,
    val packetLossPercent: Int,
    val isSuccessful: Boolean,
    val errorMessage: String? = null
)

class NetworkDiagnosticsService {

    suspend fun measurePing(
        host: String,
        port: Int = if (host == "1.1.1.1" || host == "8.8.8.8") 53 else 443,
        count: Int = 4,
        timeoutMs: Int = 2000
    ): PingResult = withContext(Dispatchers.IO) {
        val latencies = mutableListOf<Long>()
        var failedCount = 0

        // Sanitize host string
        val targetHost = host.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .split("/").firstOrNull()?.trim() ?: "1.1.1.1"

        for (i in 0 until count) {
            val start = System.currentTimeMillis()
            var socket: Socket? = null
            try {
                socket = Socket()
                val socketAddress = InetSocketAddress(targetHost, port)
                socket.connect(socketAddress, timeoutMs)
                val duration = System.currentTimeMillis() - start
                latencies.add(duration)
            } catch (_: Exception) {
                // Fallback attempt on port 80 if 443/53 failed on custom host
                if (port != 80 && targetHost != "1.1.1.1" && targetHost != "8.8.8.8") {
                    try {
                        val retryStart = System.currentTimeMillis()
                        val retrySocket = Socket()
                        retrySocket.connect(InetSocketAddress(targetHost, 80), timeoutMs)
                        val duration = System.currentTimeMillis() - retryStart
                        latencies.add(duration)
                        retrySocket.close()
                    } catch (_: Exception) {
                        failedCount++
                    }
                } else {
                    failedCount++
                }
            } finally {
                try {
                    socket?.close()
                } catch (_: Exception) {}
            }
        }

        if (latencies.isEmpty()) {
            return@withContext PingResult(
                host = targetHost,
                latencies = emptyList(),
                minLatency = 0,
                maxLatency = 0,
                avgLatency = 0,
                jitter = 0,
                packetLossPercent = 100,
                isSuccessful = false,
                errorMessage = "Host unreachable or timed out."
            )
        }

        val min = latencies.minOrNull() ?: 0L
        val max = latencies.maxOrNull() ?: 0L
        val avg = latencies.average().toLong()

        // Calculate jitter (packet delay variation)
        val jitter = if (latencies.size > 1) {
            var diffSum = 0L
            for (i in 1 until latencies.size) {
                diffSum += abs(latencies[i] - latencies[i - 1])
            }
            diffSum / (latencies.size - 1)
        } else {
            0L
        }

        val packetLoss = ((failedCount.toFloat() / count.toFloat()) * 100).toInt()

        PingResult(
            host = targetHost,
            latencies = latencies,
            minLatency = min,
            maxLatency = max,
            avgLatency = avg,
            jitter = jitter,
            packetLossPercent = packetLoss,
            isSuccessful = true
        )
    }
}
