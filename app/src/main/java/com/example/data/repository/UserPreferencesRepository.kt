package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.FavoriteCity
import com.example.data.model.TemperatureUnit
import com.example.data.model.ThemeMode
import com.example.data.model.WindSpeedUnit
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "skycast_settings")

class UserPreferencesRepository(private val context: Context) {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val favoriteListType = Types.newParameterizedType(List::class.java, FavoriteCity::class.java)
    private val favoriteListAdapter = moshi.adapter<List<FavoriteCity>>(favoriteListType)

    companion object {
        private val KEY_TEMP_UNIT = stringPreferencesKey("temp_unit")
        private val KEY_WIND_UNIT = stringPreferencesKey("wind_unit")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_FAVORITES_JSON = stringPreferencesKey("favorites_json")
        private val KEY_LAST_CITY_NAME = stringPreferencesKey("last_city_name")
        private val KEY_LAST_CITY_COUNTRY = stringPreferencesKey("last_city_country")
        private val KEY_LAST_LAT = stringPreferencesKey("last_lat")
        private val KEY_LAST_LON = stringPreferencesKey("last_lon")
    }

    val tempUnitFlow: Flow<TemperatureUnit> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_TEMP_UNIT] ?: TemperatureUnit.CELSIUS.name
        try {
            TemperatureUnit.valueOf(raw)
        } catch (_: Exception) {
            TemperatureUnit.CELSIUS
        }
    }

    val windUnitFlow: Flow<WindSpeedUnit> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_WIND_UNIT] ?: WindSpeedUnit.KMH.name
        try {
            WindSpeedUnit.valueOf(raw)
        } catch (_: Exception) {
            WindSpeedUnit.KMH
        }
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(raw)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    val favoriteCitiesFlow: Flow<List<FavoriteCity>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_FAVORITES_JSON]
        if (!json.isNullOrBlank()) {
            try {
                favoriteListAdapter.fromJson(json) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            // Default favorite cities if none yet
            listOf(
                FavoriteCity("london", "London", "United Kingdom", 51.5074, -0.1278),
                FavoriteCity("tokyo", "Tokyo", "Japan", 35.6762, 139.6503),
                FavoriteCity("new_york", "New York", "United States", 40.7128, -74.0060)
            )
        }
    }

    suspend fun setTempUnit(unit: TemperatureUnit) {
        context.dataStore.edit { it[KEY_TEMP_UNIT] = unit.name }
    }

    suspend fun setWindUnit(unit: WindSpeedUnit) {
        context.dataStore.edit { it[KEY_WIND_UNIT] = unit.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun addFavoriteCity(city: FavoriteCity) {
        context.dataStore.edit { prefs ->
            val json = prefs[KEY_FAVORITES_JSON]
            val current = if (!json.isNullOrBlank()) {
                try {
                    favoriteListAdapter.fromJson(json)?.toMutableList() ?: mutableListOf()
                } catch (_: Exception) {
                    mutableListOf()
                }
            } else {
                mutableListOf(
                    FavoriteCity("london", "London", "United Kingdom", 51.5074, -0.1278),
                    FavoriteCity("tokyo", "Tokyo", "Japan", 35.6762, 139.6503),
                    FavoriteCity("new_york", "New York", "United States", 40.7128, -74.0060)
                )
            }

            // Remove existing duplicate by id or coordinates
            current.removeAll { it.id == city.id || (Math.abs(it.latitude - city.latitude) < 0.05 && Math.abs(it.longitude - city.longitude) < 0.05) }
            current.add(0, city)
            prefs[KEY_FAVORITES_JSON] = favoriteListAdapter.toJson(current)
        }
    }

    suspend fun removeFavoriteCity(cityId: String) {
        context.dataStore.edit { prefs ->
            val json = prefs[KEY_FAVORITES_JSON]
            if (!json.isNullOrBlank()) {
                val current = try {
                    favoriteListAdapter.fromJson(json)?.toMutableList() ?: mutableListOf()
                } catch (_: Exception) {
                    mutableListOf()
                }
                current.removeAll { it.id == cityId }
                prefs[KEY_FAVORITES_JSON] = favoriteListAdapter.toJson(current)
            }
        }
    }

    suspend fun saveLastLocation(name: String, country: String, lat: Double, lon: Double) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_CITY_NAME] = name
            prefs[KEY_LAST_CITY_COUNTRY] = country
            prefs[KEY_LAST_LAT] = lat.toString()
            prefs[KEY_LAST_LON] = lon.toString()
        }
    }

    fun getLastLocationFlow(): Flow<FavoriteCity?> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_LAST_CITY_NAME]
        val country = prefs[KEY_LAST_CITY_COUNTRY] ?: ""
        val lat = prefs[KEY_LAST_LAT]?.toDoubleOrNull()
        val lon = prefs[KEY_LAST_LON]?.toDoubleOrNull()

        if (name != null && lat != null && lon != null) {
            FavoriteCity("last_selected", name, country, lat, lon)
        } else {
            null
        }
    }
}
