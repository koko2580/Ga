package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.NexoraDatabase
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.domain.model.DeviceTelemetry
import com.example.repository.GameRepository
import com.example.services.DeviceTelemetryService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class SettingsUiState(
    val messageBanner: String? = null,
    val showResetConfirmation: Boolean = false,
    val telemetry: DeviceTelemetry? = null
)

class SettingsViewModel(
    private val context: Context,
    private val preferencesRepository: UserPreferencesRepository,
    private val gameRepository: GameRepository,
    private val telemetryService: DeviceTelemetryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val preferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    init {
        _uiState.value = _uiState.value.copy(
            telemetry = telemetryService.getTelemetry()
        )
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHapticsEnabled(enabled)
        }
    }

    fun setSoundEffectsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSoundEffectsEnabled(enabled)
        }
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAnimationsEnabled(enabled)
        }
    }

    fun setDefaultProfile(profile: String) {
        viewModelScope.launch {
            preferencesRepository.setActiveProfile(profile)
            _uiState.value = _uiState.value.copy(messageBanner = "Default profile set to $profile")
        }
    }

    fun clearAppCache() {
        viewModelScope.launch {
            val cacheDir = context.cacheDir
            var freed = 0L
            if (cacheDir != null && cacheDir.exists()) {
                freed = getDirSize(cacheDir)
                clearDir(cacheDir)
            }
            System.gc()
            val freedStr = if (freed > 1024 * 1024) "${freed / (1024 * 1024)} MB" else "${freed / 1024} KB"
            _uiState.value = _uiState.value.copy(messageBanner = "Cleared $freedStr of internal cache")
        }
    }

    fun showResetDialog() {
        _uiState.value = _uiState.value.copy(showResetConfirmation = true)
    }

    fun hideResetDialog() {
        _uiState.value = _uiState.value.copy(showResetConfirmation = false)
    }

    fun resetAllData() {
        viewModelScope.launch {
            preferencesRepository.resetAllPreferences()
            gameRepository.clearAllGames()
            _uiState.value = _uiState.value.copy(
                showResetConfirmation = false,
                messageBanner = "All preferences and library data have been reset"
            )
        }
    }

    fun dismissBanner() {
        _uiState.value = _uiState.value.copy(messageBanner = null)
    }

    private fun getDirSize(dir: File): Long {
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) getDirSize(file) else file.length()
        }
        return size
    }

    private fun clearDir(dir: File) {
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) clearDir(file)
            try { file.delete() } catch (_: Exception) {}
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = NexoraDatabase.getInstance(context)
                val prefsRepo = UserPreferencesRepository(context)
                val gameRepo = GameRepository(context, db.gameDao())
                val telemetry = DeviceTelemetryService(context)
                return SettingsViewModel(context, prefsRepo, gameRepo, telemetry) as T
            }
        }
    }
}
