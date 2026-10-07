package com.example.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CitySearchResult
import com.example.data.model.FavoriteCity
import com.example.data.model.TemperatureUnit
import com.example.data.model.ThemeMode
import com.example.data.model.WeatherReport
import com.example.data.model.WindSpeedUnit
import com.example.data.repository.UserPreferencesRepository
import com.example.data.repository.WeatherRepository
import com.example.location.LocationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeatherUiState(
    val weatherReport: WeatherReport? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val tempUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val windUnit: WindSpeedUnit = WindSpeedUnit.KMH,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val favoriteCities: List<FavoriteCity> = emptyList(),
    val isCurrentCityFavorite: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<CitySearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null,
    val isUsingGpsLocation: Boolean = false,
    val hasLocationPermission: Boolean = false
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val weatherRepository = WeatherRepository(application)
    private val preferencesRepository = UserPreferencesRepository(application)
    private val locationHelper = LocationHelper(application)

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Observe preferences
        viewModelScope.launch {
            preferencesRepository.tempUnitFlow.collect { unit ->
                _uiState.update { it.copy(tempUnit = unit) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.windUnitFlow.collect { unit ->
                _uiState.update { it.copy(windUnit = unit) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.themeModeFlow.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.favoriteCitiesFlow.collect { favorites ->
                _uiState.update { state ->
                    val isFav = state.weatherReport?.let { report ->
                        favorites.any { fav ->
                            fav.name.equals(report.cityName, ignoreCase = true) ||
                                (Math.abs(fav.latitude - report.latitude) < 0.05 && Math.abs(fav.longitude - report.longitude) < 0.05)
                        }
                    } ?: false
                    state.copy(favoriteCities = favorites, isCurrentCityFavorite = isFav)
                }
            }
        }

        // Check permission and load initial weather
        checkPermissionAndLoadWeather()
    }

    fun checkPermissionAndLoadWeather() {
        val hasPerm = locationHelper.hasLocationPermission()
        _uiState.update { it.copy(hasLocationPermission = hasPerm) }

        if (hasPerm) {
            loadGpsWeather()
        } else {
            // Load last viewed or default
            loadDefaultOrLastCity()
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = granted) }
        if (granted) {
            loadGpsWeather()
        } else {
            if (_uiState.value.weatherReport == null) {
                loadDefaultOrLastCity()
            }
        }
    }

    fun loadGpsWeather() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val location = locationHelper.getCurrentLocation()

            if (location != null) {
                val result = weatherRepository.fetchWeather(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                result.onSuccess { report ->
                    updateWeatherSuccess(report, isGps = true)
                }.onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.localizedMessage ?: "Failed to load weather for current location"
                        )
                    }
                }
            } else {
                // If GPS is disabled or unavailable, load default city with info
                loadCityWeather("London", "United Kingdom", 51.5074, -0.1278, isGps = false)
            }
        }
    }

    fun loadCityWeather(
        cityName: String,
        country: String,
        latitude: Double,
        longitude: Double,
        isGps: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = weatherRepository.fetchWeather(
                latitude = latitude,
                longitude = longitude,
                cityNameHint = cityName,
                countryHint = country
            )
            result.onSuccess { report ->
                updateWeatherSuccess(report, isGps = isGps)
                preferencesRepository.saveLastLocation(cityName, country, latitude, longitude)
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Failed to load weather data"
                    )
                }
            }
        }
    }

    fun refreshWeather() {
        val current = _uiState.value.weatherReport ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            val result = weatherRepository.fetchWeather(
                latitude = current.latitude,
                longitude = current.longitude,
                cityNameHint = current.cityName,
                countryHint = current.country
            )
            result.onSuccess { report ->
                _uiState.update {
                    it.copy(
                        weatherReport = report,
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = err.localizedMessage ?: "Could not refresh weather"
                    )
                }
            }
        }
    }

    private fun loadDefaultOrLastCity() {
        viewModelScope.launch {
            // Default to Paris / London
            loadCityWeather("Paris", "France", 48.8566, 2.3522, isGps = false)
        }
    }

    private fun updateWeatherSuccess(report: WeatherReport, isGps: Boolean) {
        val isFav = _uiState.value.favoriteCities.any { fav ->
            fav.name.equals(report.cityName, ignoreCase = true) ||
                (Math.abs(fav.latitude - report.latitude) < 0.05 && Math.abs(fav.longitude - report.longitude) < 0.05)
        }
        _uiState.update {
            it.copy(
                weatherReport = report,
                isLoading = false,
                isRefreshing = false,
                errorMessage = null,
                isUsingGpsLocation = isGps,
                isCurrentCityFavorite = isFav
            )
        }
    }

    fun toggleFavoriteCurrentCity() {
        val report = _uiState.value.weatherReport ?: return
        viewModelScope.launch {
            val isFav = _uiState.value.isCurrentCityFavorite
            if (isFav) {
                val match = _uiState.value.favoriteCities.firstOrNull {
                    it.name.equals(report.cityName, ignoreCase = true) ||
                        (Math.abs(it.latitude - report.latitude) < 0.05 && Math.abs(it.longitude - report.longitude) < 0.05)
                }
                if (match != null) {
                    preferencesRepository.removeFavoriteCity(match.id)
                }
            } else {
                val fav = FavoriteCity(
                    id = "${report.cityName}_${System.currentTimeMillis()}",
                    name = report.cityName,
                    country = report.country,
                    latitude = report.latitude,
                    longitude = report.longitude
                )
                preferencesRepository.addFavoriteCity(fav)
            }
        }
    }

    fun removeFavorite(cityId: String) {
        viewModelScope.launch {
            preferencesRepository.removeFavoriteCity(cityId)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()

        if (query.trim().length < 2) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false, searchError = null) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(400) // Debounce typing
            _uiState.update { it.copy(isSearching = true, searchError = null) }
            val result = weatherRepository.searchCities(query)
            result.onSuccess { cities ->
                _uiState.update {
                    it.copy(
                        searchResults = cities,
                        isSearching = false,
                        searchError = if (cities.isEmpty()) "No cities found for '$query'" else null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        searchError = err.localizedMessage ?: "Failed to search cities"
                    )
                }
            }
        }
    }

    fun selectCity(city: CitySearchResult) {
        loadCityWeather(city.name, city.country, city.latitude, city.longitude, isGps = false)
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList()) }
    }

    fun selectFavorite(city: FavoriteCity) {
        loadCityWeather(city.name, city.country, city.latitude, city.longitude, isGps = false)
    }

    fun setTempUnit(unit: TemperatureUnit) {
        viewModelScope.launch { preferencesRepository.setTempUnit(unit) }
    }

    fun setWindUnit(unit: WindSpeedUnit) {
        viewModelScope.launch { preferencesRepository.setWindUnit(unit) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }
}
