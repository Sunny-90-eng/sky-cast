package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrentWeather
import com.example.data.model.TemperatureUnit
import com.example.data.model.WindSpeedUnit

@Composable
fun WeatherDetailsGrid(
    current: CurrentWeather,
    tempUnit: TemperatureUnit,
    windUnit: WindSpeedUnit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weather_details_grid"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailTile(
                title = "Feels Like",
                value = tempUnit.format(current.feelsLikeCelsius),
                subtitle = "Similar to actual temp",
                icon = Icons.Default.Thermostat,
                iconTint = Color(0xFFF97316),
                modifier = Modifier.weight(1f)
            )

            DetailTile(
                title = "Humidity",
                value = "${current.humidityPercentage}%",
                subtitle = when {
                    current.humidityPercentage < 40 -> "Dry air"
                    current.humidityPercentage < 70 -> "Comfortable"
                    else -> "Humid & sticky"
                },
                icon = Icons.Default.WaterDrop,
                iconTint = Color(0xFF0284C7),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailTile(
                title = "Wind Speed",
                value = windUnit.format(current.windSpeedKmh),
                subtitle = when {
                    current.windSpeedKmh < 15 -> "Light breeze"
                    current.windSpeedKmh < 35 -> "Moderate wind"
                    else -> "Strong gusts"
                },
                icon = Icons.Default.Air,
                iconTint = Color(0xFF14B8A6),
                modifier = Modifier.weight(1f)
            )

            DetailTile(
                title = "Pressure",
                value = "${Math.round(current.surfacePressureHpa)} hPa",
                subtitle = if (current.surfacePressureHpa > 1013) "High pressure" else "Low pressure",
                icon = Icons.Default.Compress,
                iconTint = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailTile(
                title = "Visibility",
                value = "%.1f km".format(current.visibilityKm),
                subtitle = if (current.visibilityKm >= 10.0) "Clear visibility" else "Reduced visibility",
                icon = Icons.Default.Visibility,
                iconTint = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )

            val uvCategory = when {
                current.uvIndex < 3.0 -> "Low"
                current.uvIndex < 6.0 -> "Moderate"
                current.uvIndex < 8.0 -> "Very High"
                else -> "Extreme"
            }
            DetailTile(
                title = "UV Index",
                value = "%.1f".format(current.uvIndex),
                subtitle = "$uvCategory risk",
                icon = Icons.Default.WbSunny,
                iconTint = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DetailTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
