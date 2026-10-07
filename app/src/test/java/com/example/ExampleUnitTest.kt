package com.example

import com.example.data.model.CitySearchResult
import com.example.data.model.TemperatureUnit
import com.example.data.model.WeatherAtmosphere
import com.example.data.model.WeatherConditionHelper
import com.example.data.model.WindSpeedUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun temperatureUnit_conversionsAreAccurate() {
        // 0 C = 32 F
        assertEquals(32.0, TemperatureUnit.FAHRENHEIT.convert(0.0), 0.01)
        // 100 C = 212 F
        assertEquals(212.0, TemperatureUnit.FAHRENHEIT.convert(100.0), 0.01)
        // 25 C = 77 F
        assertEquals(77.0, TemperatureUnit.FAHRENHEIT.convert(25.0), 0.01)

        // Celsius conversion identity
        assertEquals(22.5, TemperatureUnit.CELSIUS.convert(22.5), 0.01)

        // Formatting
        assertEquals("25°C", TemperatureUnit.CELSIUS.format(25.0))
        assertEquals("77°F", TemperatureUnit.FAHRENHEIT.format(25.0))
    }

    @Test
    fun windSpeedUnit_conversionsAreAccurate() {
        // 100 km/h is ~62.14 mph
        val mph = WindSpeedUnit.MPH.convert(100.0)
        assertEquals(62.14, mph, 0.05)

        // Formatting
        assertEquals("20.0 km/h", WindSpeedUnit.KMH.format(20.0))
        assertTrue(WindSpeedUnit.MPH.format(20.0).contains("mph"))
    }

    @Test
    fun weatherConditionHelper_mapsWmoCodesCorrectly() {
        val clearDay = WeatherConditionHelper.getConditionInfo(0, isDay = true)
        assertEquals(WeatherAtmosphere.SUNNY, clearDay.atmosphere)
        assertEquals("Clear Sky", clearDay.title)

        val clearNight = WeatherConditionHelper.getConditionInfo(0, isDay = false)
        assertEquals(WeatherAtmosphere.CLEAR_NIGHT, clearNight.atmosphere)

        val rain = WeatherConditionHelper.getConditionInfo(61, isDay = true)
        assertEquals(WeatherAtmosphere.RAINY, rain.atmosphere)

        val storm = WeatherConditionHelper.getConditionInfo(95, isDay = true)
        assertEquals(WeatherAtmosphere.STORMY, storm.atmosphere)

        val snow = WeatherConditionHelper.getConditionInfo(71, isDay = true)
        assertEquals(WeatherAtmosphere.SNOWY, snow.atmosphere)
    }

    @Test
    fun citySearchResult_fullDisplayNameFormatting() {
        val cityWithAdmin = CitySearchResult(
            id = 1,
            name = "Seattle",
            country = "United States",
            admin1 = "Washington",
            latitude = 47.6062,
            longitude = -122.3321
        )
        assertEquals("Seattle, Washington, United States", cityWithAdmin.fullDisplayName)

        val cityWithoutAdmin = CitySearchResult(
            id = 2,
            name = "Monaco",
            country = "Monaco",
            admin1 = null,
            latitude = 43.7384,
            longitude = 7.4246
        )
        assertEquals("Monaco, Monaco", cityWithoutAdmin.fullDisplayName)
    }
}
