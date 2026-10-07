package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class WeatherReport(
    val cityName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeather,
    val hourly: List<HourlyForecastItem>,
    val daily: List<DailyForecastItem>,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class CurrentWeather(
    val temperatureCelsius: Double,
    val feelsLikeCelsius: Double,
    val conditionCode: Int,
    val conditionDescription: String,
    val isDay: Boolean,
    val humidityPercentage: Int,
    val windSpeedKmh: Double,
    val surfacePressureHpa: Double,
    val visibilityKm: Double,
    val uvIndex: Double,
    val sunriseTime: String,
    val sunsetTime: String
)

data class HourlyForecastItem(
    val timeIso: String,
    val displayHour: String,
    val temperatureCelsius: Double,
    val conditionCode: Int,
    val conditionDescription: String,
    val rainProbability: Int,
    val isDay: Boolean
)

data class DailyForecastItem(
    val dateIso: String,
    val dayOfWeek: String,
    val minTempCelsius: Double,
    val maxTempCelsius: Double,
    val conditionCode: Int,
    val conditionDescription: String,
    val rainProbability: Int,
    val uvIndexMax: Double,
    val sunrise: String,
    val sunset: String
)

data class CitySearchResult(
    val id: Long,
    val name: String,
    val country: String,
    val admin1: String?,
    val latitude: Double,
    val longitude: Double
) {
    val fullDisplayName: String
        get() = when {
            admin1.isNullOrBlank() && country.isBlank() -> name
            admin1.isNullOrBlank() -> "$name, $country"
            else -> "$name, $admin1, $country"
        }
}

data class FavoriteCity(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val addedAt: Long = System.currentTimeMillis()
)

enum class TemperatureUnit(val symbol: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F");

    fun convert(tempC: Double): Double {
        return when (this) {
            CELSIUS -> tempC
            FAHRENHEIT -> (tempC * 9.0 / 5.0) + 32.0
        }
    }

    fun format(tempC: Double): String {
        val converted = convert(tempC)
        return "${Math.round(converted)}$symbol"
    }
}

enum class WindSpeedUnit(val symbol: String) {
    KMH("km/h"),
    MPH("mph");

    fun convert(speedKmh: Double): Double {
        return when (this) {
            KMH -> speedKmh
            MPH -> speedKmh * 0.621371
        }
    }

    fun format(speedKmh: Double): String {
        val converted = convert(speedKmh)
        return "%.1f %s".format(converted, symbol)
    }
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

object WeatherConditionHelper {
    fun getConditionInfo(code: Int, isDay: Boolean = true): ConditionInfo {
        return when (code) {
            0 -> if (isDay) {
                ConditionInfo("Clear Sky", Icons.Default.WbSunny, Color(0xFFF59E0B), WeatherAtmosphere.SUNNY)
            } else {
                ConditionInfo("Clear Night", Icons.Default.NightsStay, Color(0xFF818CF8), WeatherAtmosphere.CLEAR_NIGHT)
            }
            1 -> if (isDay) {
                ConditionInfo("Mainly Clear", Icons.Default.WbSunny, Color(0xFFFBBF24), WeatherAtmosphere.SUNNY)
            } else {
                ConditionInfo("Mainly Clear", Icons.Default.NightsStay, Color(0xFF93C5FD), WeatherAtmosphere.CLEAR_NIGHT)
            }
            2 -> ConditionInfo("Partly Cloudy", Icons.Default.Cloud, Color(0xFF38BDF8), WeatherAtmosphere.CLOUDY)
            3 -> ConditionInfo("Overcast", Icons.Default.Cloud, Color(0xFF94A3B8), WeatherAtmosphere.OVERCAST)
            45, 48 -> ConditionInfo("Foggy", Icons.Default.Cloud, Color(0xFFCBD5E1), WeatherAtmosphere.OVERCAST)
            51, 53, 55 -> ConditionInfo("Drizzle", Icons.Default.Grain, Color(0xFF67E8F9), WeatherAtmosphere.RAINY)
            56, 57 -> ConditionInfo("Freezing Drizzle", Icons.Default.Grain, Color(0xFF93C5FD), WeatherAtmosphere.SNOWY)
            61, 63, 65 -> ConditionInfo("Rain", Icons.Default.WaterDrop, Color(0xFF0284C7), WeatherAtmosphere.RAINY)
            66, 67 -> ConditionInfo("Freezing Rain", Icons.Default.WaterDrop, Color(0xFF38BDF8), WeatherAtmosphere.SNOWY)
            71, 73, 75, 77 -> ConditionInfo("Snow", Icons.Default.Grain, Color(0xFFE2E8F0), WeatherAtmosphere.SNOWY)
            80, 81, 82 -> ConditionInfo("Rain Showers", Icons.Default.WaterDrop, Color(0xFF0369A1), WeatherAtmosphere.RAINY)
            85, 86 -> ConditionInfo("Snow Showers", Icons.Default.Grain, Color(0xFF93C5FD), WeatherAtmosphere.SNOWY)
            95, 96, 99 -> ConditionInfo("Thunderstorm", Icons.Default.Thunderstorm, Color(0xFF8B5CF6), WeatherAtmosphere.STORMY)
            else -> ConditionInfo("Cloudy", Icons.Default.Cloud, Color(0xFF64748B), WeatherAtmosphere.CLOUDY)
        }
    }
}

data class ConditionInfo(
    val title: String,
    val icon: ImageVector,
    val iconTint: Color,
    val atmosphere: WeatherAtmosphere
)

enum class WeatherAtmosphere {
    SUNNY,
    CLEAR_NIGHT,
    CLOUDY,
    OVERCAST,
    RAINY,
    STORMY,
    SNOWY
}
