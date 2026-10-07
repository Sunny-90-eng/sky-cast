package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.WeatherAtmosphere
import com.example.data.model.WeatherConditionHelper

@Composable
fun Weather3dIcon(
    conditionCode: Int,
    isDay: Boolean = true,
    size: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    val info = WeatherConditionHelper.getConditionInfo(conditionCode, isDay)

    val imageRes = when (info.atmosphere) {
        WeatherAtmosphere.RAINY, WeatherAtmosphere.STORMY -> R.drawable.weather_3d_rain_cloud_1791391259434
        WeatherAtmosphere.SUNNY, WeatherAtmosphere.CLOUDY, WeatherAtmosphere.OVERCAST -> R.drawable.weather_3d_sun_cloud_1791391242173
        else -> R.drawable.weather_3d_sun_cloud_1791391242173
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = info.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size)
        )
    }
}
