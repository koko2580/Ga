package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.services.OptimizationReport
import com.example.ui.components.NexoraTopBar
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBlueHighlight
import com.example.ui.theme.NexoraGreenSuccess
import com.example.ui.theme.NexoraPurpleAccent
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceBorder
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary
import com.example.viewmodel.BoostStatus
import com.example.viewmodel.BoostViewModel

@Composable
fun BoostScreen(
    viewModel: BoostViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NexoraBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NexoraTopBar(
                title = "Boost",
                subtitle = "Profile Tuning & Safe Diagnostics",
                activeProfile = uiState.selectedProfile
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
            ) {
                // Intro Guidance Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(NexoraSurface)
                            .border(1.dp, NexoraSurfaceBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "NEXORA X applies verified in-app tuning and native system shortcuts. It does not fabricate CPU clock speeds or terminate protected system processes.",
                            color = NexoraTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                // Section Title: Gaming Profiles
                item {
                    Text(
                        text = "SELECT GAMING PROFILE",
                        color = NexoraTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Profile A: Performance Mode
                item {
                    ProfileOptionCard(
                        title = "Performance Mode",
                        profileKey = "PERFORMANCE",
                        description = "Prioritizes touch responsiveness, low input delay, and frame pacing advice for competitive gaming.",
                        icon = Icons.Default.Bolt,
                        accentColor = NexoraPurpleAccent,
                        isSelected = uiState.selectedProfile == "PERFORMANCE",
                        onSelect = { viewModel.selectProfile("PERFORMANCE") }
                    )
                }

                // Profile B: Balanced Mode
                item {
                    ProfileOptionCard(
                        title = "Balanced Mode",
                        profileKey = "BALANCED",
                        description = "Maintains optimal stability between device thermals, battery longevity, and gaming performance.",
                        icon = Icons.Default.Speed,
                        accentColor = NexoraGreenSuccess,
                        isSelected = uiState.selectedProfile == "BALANCED",
                        onSelect = { viewModel.selectProfile("BALANCED") }
                    )
                }

                // Profile C: Battery Saver Mode
                item {
                    ProfileOptionCard(
                        title = "Battery Saver Mode",
                        profileKey = "BATTERY_SAVER",
                        description = "Reduces power consumption, advises 60Hz display cap, and mitigates long-session thermal throttling.",
                        icon = Icons.Default.BatteryChargingFull,
                        accentColor = NexoraBlueHighlight,
                        isSelected = uiState.selectedProfile == "BATTERY_SAVER",
                        onSelect = { viewModel.selectProfile("BATTERY_SAVER") }
                    )
                }

                // Primary Optimize Button & In-Progress Diagnostic Card
                item {
                    Spacer(modifier = Modifier.height(6.dp))

                    when (val status = uiState.status) {
                        is BoostStatus.InProgress -> {
                            OptimizationProgressCard(
                                stepIndex = status.currentStepIndex,
                                stepName = status.currentStepName
                            )
                        }
                        else -> {
                            Button(
                                onClick = { viewModel.startOptimization() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("run_optimization_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NexoraPurpleAccent,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "RUN OPTIMIZATION SEQUENCE",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                // Native Quick Shortcuts
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ANDROID SYSTEM SHORTCUTS",
                        color = NexoraTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SystemShortcutChip(
                            title = "Display & Refresh",
                            icon = Icons.Default.DisplaySettings,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.openDisplaySettings() }
                        )
                        SystemShortcutChip(
                            title = "Battery Settings",
                            icon = Icons.Default.BatteryChargingFull,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.openBatterySettings() }
                        )
                    }
                }
            }
        }

        // Optimization Completed Dialog
        val status = uiState.status
        if (status is BoostStatus.Completed) {
            OptimizationReportDialog(
                report = status.report,
                onDismiss = { viewModel.dismissReport() },
                onOpenDisplaySettings = { viewModel.openDisplaySettings() },
                onOpenBatterySettings = { viewModel.openBatterySettings() }
            )
        }
    }
}

@Composable
private fun ProfileOptionCard(
    title: String,
    profileKey: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NexoraSurface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accentColor else NexoraSurfaceBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() }
            .padding(16.dp)
            .testTag("profile_card_$profileKey")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = if (isSelected) 0.2f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = NexoraTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    color = NexoraTextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun OptimizationProgressCard(
    stepIndex: Int,
    stepName: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NexoraSurface)
            .border(1.dp, NexoraPurpleAccent, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Optimizing System...",
                    color = NexoraTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = NexoraPurpleAccent
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Step ${stepIndex + 1} of 4: $stepName",
                color = NexoraPurpleAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (stepIndex + 1) / 4f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = NexoraPurpleAccent,
                trackColor = NexoraSurfaceBorder
            )
        }
    }
}

@Composable
private fun SystemShortcutChip(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NexoraSurface)
            .border(1.dp, NexoraSurfaceBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NexoraBlueHighlight,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = NexoraTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun OptimizationReportDialog(
    report: OptimizationReport,
    onDismiss: () -> Unit,
    onOpenDisplaySettings: () -> Unit,
    onOpenBatterySettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NexoraSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NexoraGreenSuccess,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Optimization Complete",
                    color = NexoraTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Applied Profile: ${report.profileApplied}",
                    color = NexoraPurpleAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Actions Completed:",
                    color = NexoraTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                report.actionsCompleted.forEach { action ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(text = "• ", color = NexoraGreenSuccess, fontSize = 12.sp)
                        Text(text = action, color = NexoraTextPrimary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Recommended System Tweaks:",
                    color = NexoraTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                report.systemTips.forEach { tip ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(text = "• ", color = NexoraBlueHighlight, fontSize = 11.sp)
                        Text(text = tip, color = NexoraTextMuted, fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NexoraPurpleAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("DONE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onOpenDisplaySettings,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NexoraBlueHighlight)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("DISPLAY")
            }
        }
    )
}
