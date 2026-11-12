package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.GameEntity
import com.example.data.local.NexoraDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.GameRepository
import com.example.data.repository.InstalledAppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GamesFilter {
    ALL,
    FAVORITES
}

data class GamesUiState(
    val searchQuery: String = "",
    val activeFilter: GamesFilter = GamesFilter.ALL,
    val isScanning: Boolean = false,
    val showAddAppSheet: Boolean = false,
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val appSheetQuery: String = "",
    val isLoadingInstalled: Boolean = false,
    val messageBanner: String? = null
)

class GamesViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GamesUiState())
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    private val allGamesFlow = gameRepository.allGames

    val filteredGames: StateFlow<List<GameEntity>> = combine(
        allGamesFlow,
        _uiState
    ) { games, state ->
        games.filter { game ->
            val matchesFilter = when (state.activeFilter) {
                GamesFilter.ALL -> true
                GamesFilter.FAVORITES -> game.isFavorite
            }
            val matchesSearch = if (state.searchQuery.isBlank()) {
                true
            } else {
                game.appName.contains(state.searchQuery, ignoreCase = true) ||
                        game.packageName.contains(state.searchQuery, ignoreCase = true)
            }
            matchesFilter && matchesSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            scanGames(initialCheck = true)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onFilterChanged(filter: GamesFilter) {
        _uiState.value = _uiState.value.copy(activeFilter = filter)
    }

    fun scanGames(initialCheck: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            val addedCount = gameRepository.scanAndAutoAddGames()
            _uiState.value = _uiState.value.copy(
                isScanning = false,
                messageBanner = if (!initialCheck) {
                    if (addedCount > 0) "Found $addedCount game(s) on device" else "No new games auto-detected. Use 'Add Manually'."
                } else null
            )
        }
    }

    fun toggleFavorite(packageName: String, isFavorite: Boolean) {
        viewModelScope.launch {
            gameRepository.toggleFavorite(packageName, isFavorite)
        }
    }

    fun removeGame(packageName: String) {
        viewModelScope.launch {
            gameRepository.removeGame(packageName)
            _uiState.value = _uiState.value.copy(messageBanner = "Game removed from library")
        }
    }

    fun launchGame(packageName: String) {
        viewModelScope.launch {
            val result = gameRepository.launchGame(packageName)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    messageBanner = result.exceptionOrNull()?.message ?: "Failed to launch game"
                )
            }
        }
    }

    fun openAddAppSheet() {
        _uiState.value = _uiState.value.copy(
            showAddAppSheet = true,
            isLoadingInstalled = true,
            appSheetQuery = ""
        )
        viewModelScope.launch {
            val apps = gameRepository.getInstalledLaunchableApps()
            _uiState.value = _uiState.value.copy(
                installedApps = apps,
                isLoadingInstalled = false
            )
        }
    }

    fun closeAddAppSheet() {
        _uiState.value = _uiState.value.copy(showAddAppSheet = false)
    }

    fun onAppSheetQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(appSheetQuery = query)
    }

    fun addAppToLibrary(app: InstalledAppInfo) {
        viewModelScope.launch {
            gameRepository.addGame(
                packageName = app.packageName,
                appName = app.appName,
                isFavorite = false
            )
            _uiState.value = _uiState.value.copy(
                messageBanner = "Added \"${app.appName}\" to Library"
            )
        }
    }

    fun dismissBanner() {
        _uiState.value = _uiState.value.copy(messageBanner = null)
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = NexoraDatabase.getInstance(context)
                val prefsRepo = UserPreferencesRepository(context)
                val repo = GameRepository(context, db.gameDao(), prefsRepo)
                return GamesViewModel(repo) as T
            }
        }
    }
}
