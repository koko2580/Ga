package com.example.domain.model

enum class NetworkTransportType {
    WIFI,
    CELLULAR,
    ETHERNET,
    NONE,
    OTHER
}

data class NetworkConnectionState(
    val isConnected: Boolean,
    val isValidated: Boolean,
    val transportType: NetworkTransportType,
    val typeName: String,
    val isMetered: Boolean = false,
    val linkDownstreamBandwidthKbps: Int? = null,
    val linkUpstreamBandwidthKbps: Int? = null
)
