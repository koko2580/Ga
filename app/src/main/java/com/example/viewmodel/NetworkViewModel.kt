package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.NexoraDatabase
import com.example.data.local.PingHistoryEntity
import com.example.data.repository.NetworkRepository
import com.example.domain.model.NetworkConnectionState
import com.example.services.NetworkDiagnosticsService
import com.example.services.PingResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NetworkUiState(
    val currentHost: String = "1.1.1.1",
    val customHostInput: String = "",
    val isTesting: Boolean = false,
    val lastPingResult: PingResult? = null,
    val showCustomDialog: Boolean = false,
    val connectionState: NetworkConnectionState? = null
)

class NetworkViewModel(
    private val networkRepository: NetworkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NetworkUiState())
    val uiState: StateFlow<NetworkUiState> = _uiState.asStateFlow()

    val history: StateFlow<List<PingHistoryEntity>> = networkRepository.pingHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        _uiState.value = _uiState.value.copy(
            connectionState = networkRepository.getNetworkStateSnapshot()
        )

        viewModelScope.launch {
            networkRepository.networkStateUpdates.collect { state ->
                _uiState.value = _uiState.value.copy(connectionState = state)
            }
        }
    }

    fun selectHost(host: String) {
        _uiState.value = _uiState.value.copy(currentHost = host)
    }

    fun openCustomHostDialog() {
        _uiState.value = _uiState.value.copy(
            showCustomDialog = true,
            customHostInput = if (_uiState.value.currentHost != "1.1.1.1" && _uiState.value.currentHost != "8.8.8.8") {
                _uiState.value.currentHost
            } else ""
        )
    }

    fun closeCustomHostDialog() {
        _uiState.value = _uiState.value.copy(showCustomDialog = false)
    }

    fun onCustomHostInputChanged(input: String) {
        _uiState.value = _uiState.value.copy(customHostInput = input)
    }

    fun applyCustomHost() {
        val host = _uiState.value.customHostInput.trim()
        if (host.isNotBlank()) {
            _uiState.value = _uiState.value.copy(
                currentHost = host,
                showCustomDialog = false
            )
        }
    }

    fun runPingTest() {
        if (_uiState.value.isTesting) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTesting = true)
            val result = networkRepository.executePingTest(_uiState.value.currentHost)
            _uiState.value = _uiState.value.copy(
                isTesting = false,
                lastPingResult = result
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            networkRepository.clearHistory()
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = NexoraDatabase.getInstance(context)
                val diagnostics = NetworkDiagnosticsService()
                val repo = NetworkRepository(context, db.pingHistoryDao(), diagnostics)
                return NetworkViewModel(repo) as T
            }
        }
    }
}
