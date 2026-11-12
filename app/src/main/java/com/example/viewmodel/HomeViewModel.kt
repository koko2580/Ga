package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.GameEntity
import com.example.data.local.NexoraDatabase
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.BatteryRepository
import com.example.data.repository.DeviceInfoRepository
import com.example.data.repository.GameRepository
import com.example.data.repository.NetworkRepository
import com.example.data.repository.ThermalRepository
import com.example.domain.model.BatteryInfo
import com.example.domain.model.DeviceSpecification
import com.example.domain.model.DeviceTelemetry
import com.example.domain.model.MemoryStats
import com.example.domain.model.NetworkConnectionState
import com.example.domain.model.StorageStats
import com.example.domain.model.ThermalInfo
import com.example.services.NetworkDiagnosticsService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val telemetry: DeviceTelemetry? = null,
    val batteryInfo: BatteryInfo? = null,
    val thermalInfo: ThermalInfo? = null,
    val networkState: NetworkConnectionState? = null,
    val memoryStats: MemoryStats? = null,
    val storageStats: StorageStats? = null,
    val specification: DeviceSpecification? = null,
    val isLoading: Boolean = true,
    val lastLaunchError: String? = null
)

class HomeViewModel(
    private val deviceInfoRepository: DeviceInfoRepository,
    private val batteryRepository: BatteryRepository,
    private val thermalRepository: ThermalRepository,
    private val networkRepository: NetworkRepository,
    private val gameRepository: GameRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    val favoriteGames: StateFlow<List<GameEntity>> = gameRepository.favoriteGames
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Collect live battery updates
        viewModelScope.launch {
            batteryRepository.batteryUpdates.collect { battery ->
                _uiState.value = _uiState.value.copy(batteryInfo = battery)
                recomputeTelemetry()
            }
        }

        // Collect live thermal updates
        viewModelScope.launch {
            thermalRepository.thermalUpdates.collect { thermal ->
                _uiState.value = _uiState.value.copy(thermalInfo = thermal)
                recomputeTelemetry()
            }
        }

        // Collect live network updates
        viewModelScope.launch {
            networkRepository.networkStateUpdates.collect { netState ->
                _uiState.value = _uiState.value.copy(networkState = netState)
                recomputeTelemetry()
            }
        }

        // Initial fetch
        refreshTelemetry()

        // Dynamic polling loop based on profile refresh interval
        viewModelScope.launch {
            while (true) {
                val interval = userPreferences.value.inAppRefreshIntervalMs
                delay(interval)
                refreshTelemetry()
            }
        }
    }

    fun refreshTelemetry() {
        val mem = deviceInfoRepository.getMemoryStats()
        val storage = deviceInfoRepository.getStorageStats()
        val spec = deviceInfoRepository.getDeviceSpecification()
        val battery = _uiState.value.batteryInfo ?: batteryRepository.getBatterySnapshot()
        val thermal = _uiState.value.thermalInfo ?: thermalRepository.getThermalStatusSnapshot()
        val network = _uiState.value.networkState ?: networkRepository.getNetworkStateSnapshot()
        val activeProfile = userPreferences.value.activeProfile

        val telemetry = deviceInfoRepository.getCompositeTelemetry(
            activeProfile = activeProfile,
            batteryInfo = battery,
            thermalInfo = thermal,
            networkState = network
        )

        _uiState.value = _uiState.value.copy(
            telemetry = telemetry,
            batteryInfo = battery,
            thermalInfo = thermal,
            networkState = network,
            memoryStats = mem,
            storageStats = storage,
            specification = spec,
            isLoading = false
        )
    }

    private fun recomputeTelemetry() {
        val current = _uiState.value
        val activeProfile = userPreferences.value.activeProfile
        val telemetry = deviceInfoRepository.getCompositeTelemetry(
            activeProfile = activeProfile,
            batteryInfo = current.batteryInfo,
            thermalInfo = current.thermalInfo,
            networkState = current.networkState
        )
        _uiState.value = current.copy(telemetry = telemetry)
    }

    fun launchGame(packageName: String) {
        viewModelScope.launch {
            val result = gameRepository.launchGame(packageName)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    lastLaunchError = result.exceptionOrNull()?.message ?: "Unable to launch game."
                )
            }
        }
    }

    fun clearLaunchError() {
        _uiState.value = _uiState.value.copy(lastLaunchError = null)
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = NexoraDatabase.getInstance(context)
                val prefsRepo = UserPreferencesRepository(context)
                val deviceRepo = DeviceInfoRepository(context)
                val batteryRepo = BatteryRepository(context)
                val thermalRepo = ThermalRepository(context)
                val diagnostics = NetworkDiagnosticsService()
                val networkRepo = NetworkRepository(context, db.pingHistoryDao(), diagnostics)
                val gameRepo = GameRepository(context, db.gameDao(), prefsRepo)
                return HomeViewModel(
                    deviceRepo,
                    batteryRepo,
                    thermalRepo,
                    networkRepo,
                    gameRepo,
                    prefsRepo
                ) as T
            }
        }
    }
}
