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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyForecastItem
import com.example.data.model.TemperatureUnit
import com.example.data.model.WeatherConditionHelper

@Composable
fun DailyForecastCard(
    dailyItems: List<DailyForecastItem>,
    tempUnit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_forecast_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "7-Day Forecast",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Overall min and max across all 7 days for relative temp bar calculation
            val overallMin = dailyItems.minOfOrNull { it.minTempCelsius } ?: 0.0
            val overallMax = dailyItems.maxOfOrNull { it.maxTempCelsius } ?: 30.0

            dailyItems.forEachIndexed { index, item ->
                DailyForecastRow(
                    item = item,
                    tempUnit = tempUnit,
                    overallMin = overallMin,
                    overallMax = overallMax
                )
                if (index < dailyItems.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        thickness = 0.8.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyForecastRow(
    item: DailyForecastItem,
    tempUnit: TemperatureUnit,
    overallMin: Double,
    overallMax: Double
) {
    val cond = WeatherConditionHelper.getConditionInfo(item.conditionCode, true)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Day name & rain probability
        Column(
            modifier = Modifier.width(76.dp)
        ) {
            Text(
                text = item.dayOfWeek,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (item.dayOfWeek == "Today") FontWeight.Bold else FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (item.rainProbability > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Rain probability",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${item.rainProbability}%",
                        fontSize = 11.sp,
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Weather Icon & text
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(100.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(cond.iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = cond.icon,
                    contentDescription = cond.title,
                    tint = cond.iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = cond.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        // Min Temp
        Text(
            text = tempUnit.format(item.minTempCelsius),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(42.dp)
        )

        // Temperature range bar
        val totalRange = (overallMax - overallMin).coerceAtLeast(1.0)
        val startFraction = ((item.minTempCelsius - overallMin) / totalRange).coerceIn(0.0, 1.0).toFloat()
        val endFraction = ((item.maxTempCelsius - overallMin) / totalRange).coerceIn(0.0, 1.0).toFloat()

        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .padding(horizontal = 6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = endFraction)
                    .padding(start = (startFraction * 60).dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF38BDF8), Color(0xFFF59E0B))
                        )
                    )
            )
        }

        // Max Temp
        Text(
            text = tempUnit.format(item.maxTempCelsius),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(42.dp)
        )
    }
}
