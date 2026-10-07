package com.example.ui.screens.forecast

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TemperatureUnit
import com.example.data.model.WeatherAtmosphere
import com.example.data.model.WeatherConditionHelper
import com.example.ui.components.DailyForecastCard
import com.example.ui.components.ForecastMainCard
import com.example.ui.components.HourlyForecastBar
import com.example.ui.components.QuickMetricsRow
import com.example.ui.components.TomorrowExpandableBanner
import com.example.ui.components.WeatherBackground
import com.example.ui.screens.home.WeatherUiState
import com.example.ui.screens.home.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ForecastScreen(
    uiState: WeatherUiState,
    viewModel: WeatherViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val report = uiState.weatherReport
    val atmosphere = report?.let {
        WeatherConditionHelper.getConditionInfo(it.current.conditionCode, it.current.isDay).atmosphere
    } ?: WeatherAtmosphere.SUNNY

    val dateSubtitle = SimpleDateFormat("d MMMM, EEEE", Locale.getDefault()).format(Date())
    var showMenu by remember { mutableStateOf(false) }

    WeatherBackground(atmosphere = atmosphere) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header matching Screenshot 2: Circular Back + Center Title/Date + Circular More Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(onClick = onNavigateBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = report?.cityName ?: "Weather Forecast",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { showMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (uiState.isCurrentCityFavorite) "Remove from Favorites" else "Save to Favorites"
                                )
                            },
                            onClick = {
                                viewModel.toggleFavoriteCurrentCity()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (uiState.isCurrentCityFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (uiState.isCurrentCityFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Refresh Weather") },
                            onClick = {
                                viewModel.refreshWeather()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (uiState.tempUnit == TemperatureUnit.CELSIUS) "Switch to °F" else "Switch to °C"
                                )
                            },
                            onClick = {
                                viewModel.setTempUnit(
                                    if (uiState.tempUnit == TemperatureUnit.CELSIUS) TemperatureUnit.FAHRENHEIT else TemperatureUnit.CELSIUS
                                )
                                showMenu = false
                            }
                        )
                    }
                }
            }

            if (report != null) {
                val tomorrowDaily = report.daily.getOrNull(1)
                val tomorrowMin = tomorrowDaily?.minTempCelsius ?: (report.current.temperatureCelsius - 4.0)
                val tomorrowMax = tomorrowDaily?.maxTempCelsius ?: (report.current.temperatureCelsius + 4.0)
                val tomorrowDesc = tomorrowDaily?.conditionDescription ?: "Light Rain Showers"

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("forecast_scroll_list"),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 10.dp,
                        bottom = 120.dp // Generous bottom clearance to fix scroll clipping!
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Main Weather Card (Screenshot 2: Thursday, 02:42 pm, 24°C, 3D sun cloud)
                    item {
                        ForecastMainCard(
                            current = report.current,
                            tempUnit = uiState.tempUnit
                        )
                    }

                    // 3-Metric Row (Screenshot 2: Wind, Temperature, Humidity)
                    item {
                        QuickMetricsRow(
                            current = report.current,
                            tempUnit = uiState.tempUnit,
                            windUnit = uiState.windUnit
                        )
                    }

                    // Hourly Forecast Section (Screenshot 2)
                    item {
                        HourlyForecastBar(
                            hourlyItems = report.hourly,
                            tempUnit = uiState.tempUnit
                        )
                    }

                    // Tomorrow Section (Screenshot 2: Blue rounded card with high/low)
                    item {
                        TomorrowExpandableBanner(
                            conditionDesc = tomorrowDesc,
                            minTempC = tomorrowMin,
                            maxTempC = tomorrowMax,
                            tempUnit = uiState.tempUnit,
                            onClick = {}
                        )
                    }

                    // 7-Day Outlook
                    item {
                        DailyForecastCard(
                            dailyItems = report.daily,
                            tempUnit = uiState.tempUnit
                        )
                    }
                }
            }
        }
    }
}
