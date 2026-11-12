package com.example.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.services.OptimizationReport
import com.example.services.SystemOptimizerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class BoostStatus {
    object Idle : BoostStatus()
    data class InProgress(val currentStepIndex: Int, val currentStepName: String) : BoostStatus()
    data class Completed(val report: OptimizationReport) : BoostStatus()
}

data class BoostUiState(
    val selectedProfile: String = "BALANCED",
    val status: BoostStatus = BoostStatus.Idle
)

class BoostViewModel(
    private val context: Context,
    private val optimizerService: SystemOptimizerService,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoostUiState())
    val uiState: StateFlow<BoostUiState> = _uiState.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    init {
        viewModelScope.launch {
            userPreferences.collect { prefs ->
                _uiState.value = _uiState.value.copy(selectedProfile = prefs.activeProfile)
            }
        }
    }

    fun selectProfile(profile: String) {
        _uiState.value = _uiState.value.copy(selectedProfile = profile)
        viewModelScope.launch {
            preferencesRepository.setActiveProfile(profile)
            triggerHapticFeedback()
        }
    }

    fun startOptimization() {
        if (_uiState.value.status is BoostStatus.InProgress) return

        viewModelScope.launch {
            triggerHapticFeedback()
            val targetProfile = _uiState.value.selectedProfile
            val report = optimizerService.runOptimizationWorkflow(targetProfile) { stepIndex, stepName ->
                _uiState.value = _uiState.value.copy(
                    status = BoostStatus.InProgress(stepIndex, stepName)
                )
            }
            triggerHapticFeedback()
            _uiState.value = _uiState.value.copy(
                status = BoostStatus.Completed(report)
            )
        }
    }

    fun dismissReport() {
        _uiState.value = _uiState.value.copy(status = BoostStatus.Idle)
    }

    fun openDisplaySettings() {
        try {
            context.startActivity(optimizerService.openDisplaySettingsIntent())
        } catch (_: Exception) {}
    }

    fun openBatterySettings() {
        try {
            context.startActivity(optimizerService.openBatterySettingsIntent())
        } catch (_: Exception) {}
    }

    fun openApplicationSettings() {
        try {
            context.startActivity(optimizerService.openApplicationDetailsIntent())
        } catch (_: Exception) {}
    }

    private fun triggerHapticFeedback() {
        if (!userPreferences.value.hapticsEnabled) return
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(40)
                }
            }
        } catch (_: Exception) {}
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val prefsRepo = UserPreferencesRepository(context)
                val optimizerService = SystemOptimizerService(context, prefsRepo)
                return BoostViewModel(context, optimizerService, prefsRepo) as T
            }
        }
    }
}
