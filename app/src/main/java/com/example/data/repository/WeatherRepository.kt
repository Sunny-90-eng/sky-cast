package com.example.data.repository

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.example.data.api.ApiClient
import com.example.data.api.OpenMeteoForecastResponse
import com.example.data.model.CitySearchResult
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyForecastItem
import com.example.data.model.HourlyForecastItem
import com.example.data.model.WeatherConditionHelper
import com.example.data.model.WeatherReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeatherRepository(private val context: Context) {

    private val forecastApi = ApiClient.forecastService
    private val geocodingApi = ApiClient.geocodingService

    suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        cityNameHint: String? = null,
        countryHint: String? = null
    ): Result<WeatherReport> = withContext(Dispatchers.IO) {
        try {
            val response = forecastApi.getForecast(latitude = latitude, longitude = longitude)

            // Resolve location name
            val (resolvedCity, resolvedCountry) = if (!cityNameHint.isNullOrBlank()) {
                Pair(cityNameHint, countryHint ?: "")
            } else {
                resolveCityName(latitude, longitude)
            }

            val report = mapToWeatherReport(
                response = response,
                cityName = resolvedCity,
                country = resolvedCountry,
                lat = latitude,
                lon = longitude
            )
            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchCities(query: String): Result<List<CitySearchResult>> = withContext(Dispatchers.IO) {
        try {
            val trimmed = query.trim()
            if (trimmed.length < 2) {
                return@withContext Result.success(emptyList())
            }
            val response = geocodingApi.searchLocations(query = trimmed)
            val list = response.results?.map { item ->
                CitySearchResult(
                    id = item.id,
                    name = item.name,
                    country = item.country ?: "",
                    admin1 = item.admin1,
                    latitude = item.latitude,
                    longitude = item.longitude
                )
            } ?: emptyList()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Suppress("DEPRECATION")
    private fun resolveCityName(latitude: Double, longitude: Double): Pair<String, String> {
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val city = address.locality
                        ?: address.subAdminArea
                        ?: address.adminArea
                        ?: address.featureName
                    val country = address.countryName ?: ""
                    if (!city.isNullOrBlank()) {
                        return Pair(city, country)
                    }
                }
            }
        } catch (_: Exception) {
            // Geocoder service can sometimes fail or be absent
        }
        return Pair("Current Location", "")
    }

    private fun mapToWeatherReport(
        response: OpenMeteoForecastResponse,
        cityName: String,
        country: String,
        lat: Double,
        lon: Double
    ): WeatherReport {
        val currentDto = response.current
        val hourlyDto = response.hourly
        val dailyDto = response.daily

        val currentTemp = currentDto?.temperature2m ?: 0.0
        val feelsLike = currentDto?.apparentTemperature ?: currentTemp
        val conditionCode = currentDto?.weatherCode ?: 0
        val isDay = (currentDto?.isDay ?: 1) == 1
        val humidity = currentDto?.relativeHumidity2m ?: 50
        val windSpeed = currentDto?.windSpeed10m ?: 0.0
        val pressure = currentDto?.surfacePressure ?: 1013.0

        val conditionInfo = WeatherConditionHelper.getConditionInfo(conditionCode, isDay)

        // Sunrise & sunset formatting
        val sunriseIso = dailyDto?.sunrise?.firstOrNull() ?: ""
        val sunsetIso = dailyDto?.sunset?.firstOrNull() ?: ""
        val sunriseFormatted = formatIsoTimeOnly(sunriseIso) ?: "06:00 AM"
        val sunsetFormatted = formatIsoTimeOnly(sunsetIso) ?: "07:30 PM"

        // Visibility & UV from hourly/daily
        val currentVisibilityMeters = hourlyDto?.visibility?.firstOrNull() ?: 10000.0
        val currentVisibilityKm = currentVisibilityMeters / 1000.0
        val maxUv = dailyDto?.uvIndexMax?.firstOrNull() ?: 3.0

        val current = CurrentWeather(
            temperatureCelsius = currentTemp,
            feelsLikeCelsius = feelsLike,
            conditionCode = conditionCode,
            conditionDescription = conditionInfo.title,
            isDay = isDay,
            humidityPercentage = humidity,
            windSpeedKmh = windSpeed,
            surfacePressureHpa = pressure,
            visibilityKm = currentVisibilityKm,
            uvIndex = maxUv,
            sunriseTime = sunriseFormatted,
            sunsetTime = sunsetFormatted
        )

        // Map next 24 hours
        val hourlyList = mutableListOf<HourlyForecastItem>()
        val hourlyTimes = hourlyDto?.time ?: emptyList()
        val hourlyTemps = hourlyDto?.temperature2m ?: emptyList()
        val hourlyCodes = hourlyDto?.weatherCode ?: emptyList()
        val hourlyPrecip = hourlyDto?.precipitationProbability ?: emptyList()
        val hourlyIsDay = hourlyDto?.isDay ?: emptyList()

        // Find index closest to current hour
        val currentIsoPrefix = SimpleDateFormat("yyyy-MM-dd'T'HH", Locale.US).format(Date())
        var startIndex = hourlyTimes.indexOfFirst { it.startsWith(currentIsoPrefix) }
        if (startIndex == -1) startIndex = 0

        val maxHours = minOf(24, hourlyTimes.size - startIndex)
        for (i in 0 until maxHours) {
            val idx = startIndex + i
            val timeStr = hourlyTimes.getOrNull(idx) ?: ""
            val temp = hourlyTemps.getOrNull(idx) ?: 0.0
            val code = hourlyCodes.getOrNull(idx) ?: 0
            val rainProb = hourlyPrecip.getOrNull(idx) ?: 0
            val hourIsDay = (hourlyIsDay.getOrNull(idx) ?: 1) == 1
            val hourCond = WeatherConditionHelper.getConditionInfo(code, hourIsDay)

            val displayHour = if (i == 0) "Now" else (formatIsoHourOnly(timeStr) ?: "")

            hourlyList.add(
                HourlyForecastItem(
                    timeIso = timeStr,
                    displayHour = displayHour,
                    temperatureCelsius = temp,
                    conditionCode = code,
                    conditionDescription = hourCond.title,
                    rainProbability = rainProb,
                    isDay = hourIsDay
                )
            )
        }

        // Map 7-day daily forecast
        val dailyList = mutableListOf<DailyForecastItem>()
        val dailyDates = dailyDto?.time ?: emptyList()
        val dailyCodes = dailyDto?.weatherCode ?: emptyList()
        val dailyMaxTemps = dailyDto?.temperature2mMax ?: emptyList()
        val dailyMinTemps = dailyDto?.temperature2mMin ?: emptyList()
        val dailyPrecip = dailyDto?.precipitationProbabilityMax ?: emptyList()
        val dailyUv = dailyDto?.uvIndexMax ?: emptyList()
        val dailySunrise = dailyDto?.sunrise ?: emptyList()
        val dailySunset = dailyDto?.sunset ?: emptyList()

        for (i in 0 until minOf(7, dailyDates.size)) {
            val dateStr = dailyDates[i]
            val code = dailyCodes.getOrNull(i) ?: 0
            val maxT = dailyMaxTemps.getOrNull(i) ?: 0.0
            val minT = dailyMinTemps.getOrNull(i) ?: 0.0
            val rainProb = dailyPrecip.getOrNull(i) ?: 0
            val uv = dailyUv.getOrNull(i) ?: 0.0
            val sr = formatIsoTimeOnly(dailySunrise.getOrNull(i) ?: "") ?: "06:00 AM"
            val ss = formatIsoTimeOnly(dailySunset.getOrNull(i) ?: "") ?: "07:00 PM"
            val dCond = WeatherConditionHelper.getConditionInfo(code, true)

            val dayOfWeek = if (i == 0) "Today" else formatIsoDayOfWeek(dateStr)

            dailyList.add(
                DailyForecastItem(
                    dateIso = dateStr,
                    dayOfWeek = dayOfWeek,
                    minTempCelsius = minT,
                    maxTempCelsius = maxT,
                    conditionCode = code,
                    conditionDescription = dCond.title,
                    rainProbability = rainProb,
                    uvIndexMax = uv,
                    sunrise = sr,
                    sunset = ss
                )
            )
        }

        return WeatherReport(
            cityName = cityName,
            country = country,
            latitude = lat,
            longitude = lon,
            current = current,
            hourly = hourlyList,
            daily = dailyList
        )
    }

    private fun formatIsoTimeOnly(iso: String): String? {
        if (iso.isBlank()) return null
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            val date = parser.parse(iso) ?: return null
            SimpleDateFormat("hh:mm a", Locale.US).format(date)
        } catch (_: Exception) {
            null
        }
    }

    private fun formatIsoHourOnly(iso: String): String? {
        if (iso.isBlank()) return null
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            val date = parser.parse(iso) ?: return null
            SimpleDateFormat("h a", Locale.US).format(date)
        } catch (_: Exception) {
            null
        }
    }

    private fun formatIsoDayOfWeek(isoDate: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = parser.parse(isoDate) ?: return isoDate
            SimpleDateFormat("EEE", Locale.US).format(date)
        } catch (_: Exception) {
            isoDate
        }
    }
}
