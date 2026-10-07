package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ThemeMode
import com.example.ui.components.Screen
import com.example.ui.components.SkyCastFloatingBottomNav
import com.example.ui.screens.forecast.ForecastScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.WeatherUiState
import com.example.ui.screens.home.WeatherViewModel
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.theme.SkyCastTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WeatherViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (uiState.themeMode) {
                ThemeMode.SYSTEM -> isSystemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            SkyCastTheme(darkTheme = isDarkTheme) {
                WeatherAppRoot(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }
}

@Composable
fun WeatherAppRoot(
    viewModel: WeatherViewModel,
    uiState: WeatherUiState
) {
    var isSplashFinished by remember { mutableStateOf(false) }
    var currentRoute by remember { mutableStateOf(Screen.Home.route) }

    if (!isSplashFinished) {
        SplashScreen(
            onSplashFinished = { isSplashFinished = true }
        )
    } else {
        // Back navigation returns to Home from any secondary screen
        BackHandler(enabled = currentRoute != Screen.Home.route) {
            currentRoute = Screen.Home.route
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Main Content Area with animated transitions
            AnimatedContent(
                targetState = currentRoute,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition",
                modifier = Modifier.fillMaxSize()
            ) { route ->
                when (route) {
                    Screen.Home.route -> {
                        HomeScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onNavigateToForecast = { currentRoute = Screen.Forecast.route }
                        )
                    }
                    Screen.Forecast.route -> {
                        ForecastScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onNavigateBack = { currentRoute = Screen.Home.route }
                        )
                    }
                    Screen.Search.route -> {
                        SearchScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onCitySelected = { currentRoute = Screen.Home.route }
                        )
                    }
                    Screen.Settings.route -> {
                        SettingsScreen(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }
                }
            }

            // Floating Capsule Bottom Navigation Bar anchored at bottom
            SkyCastFloatingBottomNav(
                currentRoute = currentRoute,
                onNavigate = { route -> currentRoute = route },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
