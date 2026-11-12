package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.BatteryRepository
import com.example.data.repository.DeviceInfoRepository
import com.example.data.repository.ThermalRepository
import com.example.domain.model.BatteryInfo
import com.example.domain.model.DeviceSpecification
import com.example.domain.model.MemoryStats
import com.example.domain.model.StorageStats
import com.example.domain.model.ThermalInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DeviceDetailsUiState(
    val specification: DeviceSpecification? = null,
    val memory: MemoryStats? = null,
    val storage: StorageStats? = null,
    val battery: BatteryInfo? = null,
    val thermal: ThermalInfo? = null,
    val refreshRateHz: Float = 60f
)

class DeviceViewModel(
    private val deviceInfoRepository: DeviceInfoRepository,
    private val batteryRepository: BatteryRepository,
    private val thermalRepository: ThermalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceDetailsUiState())
    val uiState: StateFlow<DeviceDetailsUiState> = _uiState.asStateFlow()

    init {
        refresh()

        viewModelScope.launch {
            batteryRepository.batteryUpdates.collect { battery ->
                _uiState.value = _uiState.value.copy(battery = battery)
            }
        }

        viewModelScope.launch {
            thermalRepository.thermalUpdates.collect { thermal ->
                _uiState.value = _uiState.value.copy(thermal = thermal)
            }
        }
    }

    fun refresh() {
        val spec = deviceInfoRepository.getDeviceSpecification()
        val mem = deviceInfoRepository.getMemoryStats()
        val storage = deviceInfoRepository.getStorageStats()
        val refresh = deviceInfoRepository.getScreenRefreshRate()
        val battery = batteryRepository.getBatterySnapshot()
        val thermal = thermalRepository.getThermalStatusSnapshot()

        _uiState.value = DeviceDetailsUiState(
            specification = spec,
            memory = mem,
            storage = storage,
            battery = battery,
            thermal = thermal,
            refreshRateHz = refresh
        )
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val deviceRepo = DeviceInfoRepository(context)
                val batteryRepo = BatteryRepository(context)
                val thermalRepo = ThermalRepository(context)
                return DeviceViewModel(deviceRepo, batteryRepo, thermalRepo) as T
            }
        }
    }
}
