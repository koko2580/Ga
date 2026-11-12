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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.components.NexoraTopBar
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBlueHighlight
import com.example.ui.theme.NexoraPurpleAccent
import com.example.ui.theme.NexoraRedAlert
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceBorder
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary
import com.example.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showProfileDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.messageBanner) {
        uiState.messageBanner?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissBanner()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NexoraBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NexoraTopBar(
                title = "Settings",
                subtitle = "App Preferences & Integrity"
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
            ) {
                // Section: App Experience
                item {
                    Text(
                        text = "APP EXPERIENCE",
                        color = NexoraTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                item {
                    SettingToggleCard(
                        title = "Dark Theme",
                        description = "AMOLED deep black design language (#080808)",
                        icon = Icons.Default.DarkMode,
                        checked = true,
                        onCheckedChange = { /* Always enabled for gaming dark theme */ },
                        enabled = false
                    )
                }

                item {
                    SettingToggleCard(
                        title = "Haptic Feedback",
                        description = "Vibrate on profile change and optimization steps",
                        icon = Icons.Default.Vibration,
                        checked = preferences.hapticsEnabled,
                        onCheckedChange = { viewModel.setHapticsEnabled(it) }
                    )
                }

                item {
                    SettingToggleCard(
                        title = "Sound Effects",
                        description = "Play subtle auditory cues on actions",
                        icon = Icons.Default.VolumeUp,
                        checked = preferences.soundEffectsEnabled,
                        onCheckedChange = { viewModel.setSoundEffectsEnabled(it) }
                    )
                }

                item {
                    SettingToggleCard(
                        title = "Interface Animations",
                        description = "Smooth motion for cards, progress, and dialogs",
                        icon = Icons.Default.Animation,
                        checked = preferences.animationsEnabled,
                        onCheckedChange = { viewModel.setAnimationsEnabled(it) }
                    )
                }

                // Section: Default Performance Profile
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "TUNING DEFAULTS",
                        color = NexoraTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(NexoraSurface)
                            .border(1.dp, NexoraSurfaceBorder, RoundedCornerShape(14.dp))
                            .clickable { showProfileDropdown = true }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = NexoraPurpleAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Startup Gaming Profile",
                                        color = NexoraTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Currently set to ${preferences.activeProfile}",
                                        color = NexoraTextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Box {
                                Text(
                                    text = preferences.activeProfile,
                                    color = NexoraPurpleAccent,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                DropdownMenu(
                                    expanded = showProfileDropdown,
                                    onDismissRequest = { showProfileDropdown = false },
                                    modifier = Modifier.background(NexoraSurface)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("PERFORMANCE", color = NexoraTextPrimary) },
                                        onClick = {
                                            viewModel.setDefaultProfile("PERFORMANCE")
                                            showProfileDropdown = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("BALANCED", color = NexoraTextPrimary) },
                                        onClick = {
                                            viewModel.setDefaultProfile("BALANCED")
                                            showProfileDropdown = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("BATTERY_SAVER", color = NexoraTextPrimary) },
                                        onClick = {
                                            viewModel.setDefaultProfile("BATTERY_SAVER")
                                            showProfileDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: Maintenance
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "MAINTENANCE & STORAGE",
                        color = NexoraTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                item {
                    SettingActionCard(
                        title = "Clear App Cache",
                        description = "Delete temporary diagnostics and logs created by NEXORA X",
                        icon = Icons.Default.CleaningServices,
                        iconTint = NexoraBlueHighlight,
                        actionLabel = "CLEAR",
                        onClick = { viewModel.clearAppCache() }
                    )
                }

                item {
                    SettingActionCard(
                        title = "Reset App Data",
                        description = "Reset all preferences and custom game bookmarks",
                        icon = Icons.Default.RestartAlt,
                        iconTint = NexoraRedAlert,
                        actionLabel = "RESET",
                        onClick = { viewModel.showResetDialog() }
                    )
                }

                // Section: About & Privacy
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ABOUT & INTEGRITY",
                        color = NexoraTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(NexoraSurface)
                            .border(1.dp, NexoraSurfaceBorder, RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = NexoraPurpleAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "NEXORA X v1.0",
                                    color = NexoraTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "A native Android gaming utility built for high-performance devices. Provides truthful hardware statistics without background memory manipulation or fake boost metrics.",
                                color = NexoraTextMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = NexoraBlueHighlight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Privacy: 100% On-Device  •  Zero Telemetry Uploads  •  No Root",
                                    color = NexoraBlueHighlight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Reset Confirmation Dialog
        if (uiState.showResetConfirmation) {
            AlertDialog(
                onDismissRequest = { viewModel.hideResetDialog() },
                containerColor = NexoraSurface,
                title = {
                    Text(
                        text = "Reset All Preferences?",
                        color = NexoraTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "This will reset all your gaming profile settings, haptic preferences, and custom game library entries. This action cannot be undone.",
                        color = NexoraTextSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.resetAllData() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NexoraRedAlert,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("RESET DATA", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideResetDialog() }) {
                        Text("CANCEL", color = NexoraTextMuted)
                    }
                }
            )
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
private fun SettingToggleCard(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
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
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NexoraPurpleAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = NexoraTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        color = NexoraTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = NexoraPurpleAccent,
                    uncheckedThumbColor = NexoraTextMuted,
                    uncheckedTrackColor = NexoraSurfaceBorder
                )
            )
        }
    }
}

@Composable
private fun SettingActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    actionLabel: String,
    onClick: () -> Unit
) {
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
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = NexoraTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        color = NexoraTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NexoraSurfaceBorder,
                    contentColor = NexoraTextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(actionLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
