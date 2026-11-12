package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.PingHistoryEntity
import com.example.services.PingResult
import com.example.ui.components.NexoraTopBar
import com.example.ui.theme.NexoraAmberWarning
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBlueHighlight
import com.example.ui.theme.NexoraGreenSuccess
import com.example.ui.theme.NexoraPurpleAccent
import com.example.ui.theme.NexoraRedAlert
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceBorder
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary
import com.example.viewmodel.NetworkViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NetworkScreen(
    viewModel: NetworkViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NexoraBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NexoraTopBar(
                title = "Network",
                subtitle = "Multi-Server Ping & Latency Jitter"
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
            ) {
                // Live Connection Status Card
                item {
                    val conn = uiState.connectionState
                    val isConn = conn?.isConnected == true
                    val isValidated = conn?.isValidated == true

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(NexoraSurface)
                            .border(1.dp, NexoraSurfaceBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = if (isConn) NexoraGreenSuccess else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = conn?.typeName ?: "Detecting Network...",
                                        color = NexoraTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isConn) {
                                            if (isValidated) "Internet Validated  •  Low Latency Ready" else "Connected (Validation Pending)"
                                        } else {
                                            "No active network connection"
                                        },
                                        color = NexoraTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background((if (isConn) NexoraGreenSuccess else NexoraAmberWarning).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isConn) "ONLINE" else "OFFLINE",
                                    color = if (isConn) NexoraGreenSuccess else NexoraAmberWarning,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Host Selection Section
                item {
                    Text(
                        text = "TARGET HOST PRESET",
                        color = NexoraTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HostPresetChip(
                            label = "Cloudflare (1.1.1.1)",
                            host = "1.1.1.1",
                            isSelected = uiState.currentHost == "1.1.1.1",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectHost("1.1.1.1") }
                        )
                        HostPresetChip(
                            label = "Google (8.8.8.8)",
                            host = "8.8.8.8",
                            isSelected = uiState.currentHost == "8.8.8.8",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectHost("8.8.8.8") }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CustomHostPresetChip(
                            currentHost = uiState.currentHost,
                            onClick = { viewModel.openCustomHostDialog() }
                        )
                    }
                }

                // Ping Action Button
                item {
                    Button(
                        onClick = { viewModel.runPingTest() },
                        enabled = !uiState.isTesting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("run_ping_test_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NexoraPurpleAccent,
                            contentColor = Color.White,
                            disabledContainerColor = NexoraSurfaceBorder,
                            disabledContentColor = NexoraTextMuted
                        )
                    ) {
                        if (uiState.isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("MEASURING SOCKET LATENCY...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.NetworkCheck,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TEST PING TO ${uiState.currentHost}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // Current Ping Result Card
                item {
                    val result = uiState.lastPingResult
                    if (result != null) {
                        PingResultHeroCard(result = result)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(NexoraSurface)
                                .border(1.dp, NexoraSurfaceBorder, RoundedCornerShape(16.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Router,
                                    contentDescription = null,
                                    tint = NexoraTextMuted,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No Ping Test Conducted Yet",
                                    color = NexoraTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap above to measure real round-trip TCP latency and packet jitter.",
                                    color = NexoraTextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Ping History Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TEST HISTORY (${history.size})",
                            color = NexoraTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        if (history.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.clearHistory() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear History",
                                    tint = NexoraTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                if (history.isEmpty()) {
                    item {
                        Text(
                            text = "Past diagnostic sessions will appear here.",
                            color = NexoraTextMuted,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    items(history, key = { it.id }) { item ->
                        PingHistoryItemCard(entry = item)
                    }
                }
            }
        }

        // Custom Host Input Dialog
        if (uiState.showCustomDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.closeCustomHostDialog() },
                containerColor = NexoraSurface,
                title = {
                    Text(
                        text = "Custom Ping Host",
                        color = NexoraTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Enter a server IP or domain name for gaming latency checks (e.g., 8.8.4.4 or 1.0.0.1):",
                            color = NexoraTextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.customHostInput,
                            onValueChange = { viewModel.onCustomHostInputChanged(it) },
                            singleLine = true,
                            placeholder = { Text("e.g. 1.0.0.1", color = NexoraTextMuted) },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NexoraPurpleAccent,
                                unfocusedBorderColor = NexoraSurfaceBorder,
                                focusedTextColor = NexoraTextPrimary,
                                unfocusedTextColor = NexoraTextPrimary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.applyCustomHost() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NexoraPurpleAccent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("APPLY", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeCustomHostDialog() }) {
                        Text("CANCEL", color = NexoraTextMuted)
                    }
                }
            )
        }
    }
}

@Composable
private fun HostPresetChip(
    label: String,
    host: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) NexoraPurpleAccent.copy(alpha = 0.2f) else NexoraSurface)
            .border(
                1.dp,
                if (isSelected) NexoraPurpleAccent else NexoraSurfaceBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) NexoraPurpleAccent else NexoraTextPrimary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun CustomHostPresetChip(
    currentHost: String,
    onClick: () -> Unit
) {
    val isCustom = currentHost != "1.1.1.1" && currentHost != "8.8.8.8"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCustom) NexoraPurpleAccent.copy(alpha = 0.2f) else NexoraSurface)
            .border(
                1.dp,
                if (isCustom) NexoraPurpleAccent else NexoraSurfaceBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = if (isCustom) NexoraPurpleAccent else NexoraTextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isCustom) "Custom Host: $currentHost" else "Configure Custom Host...",
                color = if (isCustom) NexoraPurpleAccent else NexoraTextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isCustom) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PingResultHeroCard(result: PingResult) {
    val latencyColor = when {
        !result.isSuccessful -> NexoraRedAlert
        result.avgLatency < 35 -> NexoraGreenSuccess
        result.avgLatency < 75 -> NexoraAmberWarning
        else -> NexoraRedAlert
    }

    val statusText = when {
        !result.isSuccessful -> "Connection Failed"
        result.avgLatency < 35 -> "Excellent for Competitive Gaming"
        result.avgLatency < 75 -> "Good Gaming Latency"
        else -> "High Latency Detected"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NexoraSurface)
            .border(1.dp, NexoraSurfaceBorder, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TARGET: ${result.host}",
                        color = NexoraTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (result.isSuccessful) "${result.avgLatency} ms" else "ERR",
                        color = latencyColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(latencyColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (result.isSuccessful) "${result.packetLossPercent}% Loss" else "Offline",
                        color = latencyColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = statusText,
                color = NexoraTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-metrics row
            if (result.isSuccessful) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SubMetricItem(label = "MIN PING", value = "${result.minLatency} ms")
                    SubMetricItem(label = "MAX PING", value = "${result.maxLatency} ms")
                    SubMetricItem(label = "JITTER", value = "±${result.jitter} ms")
                }
            } else if (result.errorMessage != null) {
                Text(
                    text = result.errorMessage,
                    color = NexoraRedAlert,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SubMetricItem(label: String, value: String) {
    Column {
        Text(text = label, color = NexoraTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = NexoraTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PingHistoryItemCard(entry: PingHistoryEntity) {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeStr = formatter.format(Date(entry.timestamp))

    val color = when {
        entry.avgLatencyMs < 35 -> NexoraGreenSuccess
        entry.avgLatencyMs < 75 -> NexoraAmberWarning
        else -> NexoraRedAlert
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NexoraSurface)
            .border(1.dp, NexoraSurfaceBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = entry.host,
                    color = NexoraTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$timeStr  •  Min ${entry.minLatencyMs}ms / Max ${entry.maxLatencyMs}ms",
                    color = NexoraTextMuted,
                    fontSize = 11.sp
                )
            }

            Text(
                text = "${entry.avgLatencyMs} ms",
                color = color,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
