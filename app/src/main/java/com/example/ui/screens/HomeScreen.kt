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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.GameEntity
import com.example.domain.model.DeviceTelemetry
import com.example.ui.components.MetricCard
import com.example.ui.components.NexoraTopBar
import com.example.ui.theme.NexoraAmberWarning
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBlueHighlight
import com.example.ui.theme.NexoraGreenSuccess
import com.example.ui.theme.NexoraPurpleAccent
import com.example.ui.theme.NexoraPurpleLight
import com.example.ui.theme.NexoraRedAlert
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceBorder
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary
import com.example.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToGames: () -> Unit,
    onNavigateToBoost: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val favoriteGames by viewModel.favoriteGames.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.lastLaunchError) {
        uiState.lastLaunchError?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearLaunchError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NexoraBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NexoraTopBar(
                title = "Home",
                subtitle = "Hardware Diagnostics & Engine",
                activeProfile = preferences.activeProfile,
                onRefresh = { viewModel.refreshTelemetry() }
            )

            if (uiState.isLoading && uiState.telemetry == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NexoraPurpleAccent)
                }
            } else {
                val telemetry = uiState.telemetry ?: return@Column

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
                ) {
                    // Performance Overview Hero Banner
                    item {
                        PerformanceHeroCard(
                            telemetry = telemetry,
                            activeProfile = preferences.activeProfile,
                            onOptimizeClick = onNavigateToBoost
                        )
                    }

                    // Device Spec Summary Card
                    item {
                        DeviceSpecCard(telemetry = telemetry)
                    }

                    // Dual Metric: RAM & Storage
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                title = "RAM Memory",
                                value = "${telemetry.ramUsagePercent}%",
                                subtitle = DeviceTelemetry.formatBytes(telemetry.availableRamBytes) + " Free",
                                icon = Icons.Default.Memory,
                                iconTint = NexoraPurpleAccent,
                                progress = telemetry.ramUsageRatio,
                                progressColor = if (telemetry.isLowMemory || telemetry.ramUsagePercent > 85) NexoraAmberWarning else NexoraPurpleAccent,
                                badgeText = if (telemetry.isLowMemory) "LOW MEMORY" else null,
                                badgeColor = NexoraRedAlert,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Internal Storage",
                                value = "${telemetry.storageUsagePercent}%",
                                subtitle = DeviceTelemetry.formatBytes(telemetry.availableStorageBytes) + " Free",
                                icon = Icons.Default.SdStorage,
                                iconTint = NexoraBlueHighlight,
                                progress = telemetry.storageUsageRatio,
                                progressColor = NexoraBlueHighlight,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Dual Metric: Battery & Thermal Status
                    item {
                        val batterySubtitle = buildString {
                            append(if (telemetry.isCharging) "Charging" else "Discharging")
                            telemetry.batteryTemperatureC?.let { append(" • %.1f°C".format(it)) }
                            telemetry.batteryVoltageMv?.let { append(" • %.2fV".format(it / 1000.0)) }
                        }

                        val thermalLevelColor = when (telemetry.thermalStatus) {
                            "Normal", "Light" -> NexoraGreenSuccess
                            "Moderate" -> NexoraAmberWarning
                            else -> NexoraRedAlert
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                title = "Battery",
                                value = "${telemetry.batteryLevel}%",
                                subtitle = batterySubtitle,
                                icon = if (telemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                                iconTint = if (telemetry.batteryLevel <= 20) NexoraAmberWarning else NexoraGreenSuccess,
                                badgeText = if (telemetry.isCharging) "Charging" else telemetry.batteryTech,
                                badgeColor = NexoraGreenSuccess,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Thermal Status",
                                value = telemetry.thermalStatus,
                                subtitle = if (telemetry.thermalStatus == "Normal") "No throttling active" else "SOC thermal control",
                                icon = Icons.Default.Thermostat,
                                iconTint = thermalLevelColor,
                                badgeText = if (telemetry.thermalStatus != "Normal") "Throttling" else "Optimal",
                                badgeColor = thermalLevelColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Network Status Card
                    item {
                        MetricCard(
                            title = "Network Interface",
                            value = telemetry.networkType,
                            subtitle = if (telemetry.isNetworkConnected) "Active Internet Connection (Validated)" else "Offline / No Connectivity",
                            icon = Icons.Default.Wifi,
                            iconTint = if (telemetry.isNetworkConnected) NexoraGreenSuccess else Color.Gray,
                            badgeText = if (telemetry.isNetworkConnected) "Connected" else "Offline",
                            badgeColor = if (telemetry.isNetworkConnected) NexoraGreenSuccess else Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Quick Launch Games Section
                    item {
                        QuickLaunchSection(
                            favorites = favoriteGames,
                            onLaunchGame = { viewModel.launchGame(it) },
                            onViewAll = onNavigateToGames
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        )
    }
}

@Composable
private fun PerformanceHeroCard(
    telemetry: DeviceTelemetry,
    activeProfile: String,
    onOptimizeClick: () -> Unit
) {
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
                        text = "Gaming Profile Active",
                        color = NexoraTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$activeProfile MODE",
                        color = NexoraTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(NexoraPurpleAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = NexoraPurpleAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Screen Refresh Rate: ${telemetry.refreshRateHz.toInt()} Hz  •  RAM: ${telemetry.formattedRam()}",
                color = NexoraTextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onOptimizeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("home_optimize_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NexoraPurpleAccent,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TUNE IN BOOST CENTER",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun DeviceSpecCard(telemetry: DeviceTelemetry) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NexoraSurface)
            .border(1.dp, NexoraSurfaceBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Smartphone,
                    contentDescription = null,
                    tint = NexoraBlueHighlight,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Device Specs",
                    color = NexoraTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Model", color = NexoraTextMuted, fontSize = 11.sp)
                    Text(
                        text = "${telemetry.manufacturer} ${telemetry.model}",
                        color = NexoraTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Android OS", color = NexoraTextMuted, fontSize = 11.sp)
                    Text(
                        text = "Android ${telemetry.androidVersion} (API ${telemetry.sdkInt})",
                        color = NexoraTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Hardware", color = NexoraTextMuted, fontSize = 11.sp)
                    Text(
                        text = telemetry.hardware,
                        color = NexoraTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Architecture (ABI)", color = NexoraTextMuted, fontSize = 11.sp)
                    Text(
                        text = telemetry.supportedAbis.firstOrNull() ?: "arm64-v8a",
                        color = NexoraTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickLaunchSection(
    favorites: List<GameEntity>,
    onLaunchGame: (String) -> Unit,
    onViewAll: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = NexoraPurpleLight,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Quick Launch (${favorites.size})",
                    color = NexoraTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "View Library",
                color = NexoraPurpleAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onViewAll() }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NexoraSurface)
                    .border(1.dp, NexoraSurfaceBorder, RoundedCornerShape(14.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No favorite games pinned yet",
                        color = NexoraTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Star games in your Game Library to quick-launch them here.",
                        color = NexoraTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(favorites, key = { it.packageName }) { game ->
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(NexoraSurface)
                            .border(1.dp, NexoraSurfaceBorder, RoundedCornerShape(14.dp))
                            .clickable { onLaunchGame(game.packageName) }
                            .padding(12.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NexoraPurpleAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = NexoraPurpleAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = game.appName,
                                color = NexoraTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tap to Launch",
                                color = NexoraTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
