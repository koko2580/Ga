package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.navigation.Screen
import com.example.ui.components.NexoraBottomBar
import com.example.ui.screens.BoostScreen
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NetworkScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraTheme
import com.example.viewmodel.BoostViewModel
import com.example.viewmodel.GamesViewModel
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.NetworkViewModel
import com.example.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexoraTheme {
                NexoraApp()
            }
        }
    }
}

@Composable
fun NexoraApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val context = androidx.compose.ui.platform.LocalContext.current.applicationContext
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory(context))
    val gamesViewModel: GamesViewModel = viewModel(factory = GamesViewModel.provideFactory(context))
    val boostViewModel: BoostViewModel = viewModel(factory = BoostViewModel.provideFactory(context))
    val networkViewModel: NetworkViewModel = viewModel(factory = NetworkViewModel.provideFactory(context))
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.provideFactory(context))

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NexoraBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NexoraBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToGames = {
                            navController.navigate(Screen.Games.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToBoost = {
                            navController.navigate(Screen.Boost.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable(Screen.Games.route) {
                    GamesScreen(viewModel = gamesViewModel)
                }

                composable(Screen.Boost.route) {
                    BoostScreen(viewModel = boostViewModel)
                }

                composable(Screen.Network.route) {
                    NetworkScreen(viewModel = networkViewModel)
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = settingsViewModel)
                }
            }
        }
    }
}
