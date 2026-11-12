package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.data.local.PingHistoryDao
import com.example.data.local.PingHistoryEntity
import com.example.domain.model.NetworkConnectionState
import com.example.domain.model.NetworkTransportType
import com.example.services.NetworkDiagnosticsService
import com.example.services.PingResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class NetworkRepository(
    private val context: Context,
    private val pingHistoryDao: PingHistoryDao,
    private val diagnosticsService: NetworkDiagnosticsService
) {
    private val connectivityManager: ConnectivityManager? =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    val pingHistory: Flow<List<PingHistoryEntity>> = pingHistoryDao.getRecentPings(25)

    /**
     * Emits network connectivity changes reactively via ConnectivityManager.NetworkCallback
     */
    val networkStateUpdates: Flow<NetworkConnectionState> = callbackFlow {
        trySend(getNetworkStateSnapshot())

        val cm = connectivityManager
        if (cm == null) {
            awaitClose {}
            return@callbackFlow
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(getNetworkStateSnapshot())
            }

            override fun onLost(network: Network) {
                trySend(getNetworkStateSnapshot())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(getNetworkStateSnapshot())
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            cm.registerNetworkCallback(request, callback)
        } catch (_: Exception) {}

        awaitClose {
            try {
                cm.unregisterNetworkCallback(callback)
            } catch (_: Exception) {}
        }
    }

    /**
     * Instant snapshot of connectivity state
     */
    fun getNetworkStateSnapshot(): NetworkConnectionState {
        val cm = connectivityManager ?: return NetworkConnectionState(
            isConnected = false,
            isValidated = false,
            transportType = NetworkTransportType.NONE,
            typeName = "Offline"
        )

        val activeNetwork = cm.activeNetwork ?: return NetworkConnectionState(
            isConnected = false,
            isValidated = false,
            transportType = NetworkTransportType.NONE,
            typeName = "Offline"
        )

        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return NetworkConnectionState(
            isConnected = false,
            isValidated = false,
            transportType = NetworkTransportType.NONE,
            typeName = "Offline"
        )

        val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val isMetered = !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)

        val transport = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkTransportType.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkTransportType.CELLULAR
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkTransportType.ETHERNET
            else -> NetworkTransportType.OTHER
        }

        val typeName = when (transport) {
            NetworkTransportType.WIFI -> if (isValidated) "Wi-Fi (Validated)" else "Wi-Fi (Unvalidated)"
            NetworkTransportType.CELLULAR -> if (isValidated) "Cellular 5G/LTE" else "Cellular (Unvalidated)"
            NetworkTransportType.ETHERNET -> "Ethernet"
            NetworkTransportType.NONE -> "Offline"
            NetworkTransportType.OTHER -> "Connected"
        }

        return NetworkConnectionState(
            isConnected = hasInternet,
            isValidated = isValidated,
            transportType = transport,
            typeName = typeName,
            isMetered = isMetered,
            linkDownstreamBandwidthKbps = caps.linkDownstreamBandwidthKbps,
            linkUpstreamBandwidthKbps = caps.linkUpstreamBandwidthKbps
        )
    }

    suspend fun executePingTest(host: String): PingResult = withContext(Dispatchers.IO) {
        val result = diagnosticsService.measurePing(host)
        if (result.isSuccessful && result.latencies.isNotEmpty()) {
            pingHistoryDao.insertPing(
                PingHistoryEntity(
                    host = result.host,
                    avgLatencyMs = result.avgLatency,
                    minLatencyMs = result.minLatency,
                    maxLatencyMs = result.maxLatency,
                    jitterMs = result.jitter,
                    packetLossPercent = result.packetLossPercent
                )
            )
        }
        result
    }

    suspend fun clearHistory() {
        pingHistoryDao.clearHistory()
    }
}
