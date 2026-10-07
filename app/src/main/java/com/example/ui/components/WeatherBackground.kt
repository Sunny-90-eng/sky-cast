package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.model.WeatherAtmosphere
import com.example.ui.theme.DarkBgCanvas
import com.example.ui.theme.LightBgCanvas

@Composable
fun WeatherBackground(
    atmosphere: WeatherAtmosphere,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val (targetTop, targetBottom) = if (isDarkTheme) {
        when (atmosphere) {
            WeatherAtmosphere.SUNNY -> Pair(Color(0xFF0F2042), DarkBgCanvas)
            WeatherAtmosphere.CLEAR_NIGHT -> Pair(Color(0xFF080C1E), DarkBgCanvas)
            WeatherAtmosphere.STORMY -> Pair(Color(0xFF13112E), DarkBgCanvas)
            else -> Pair(Color(0xFF131D35), DarkBgCanvas)
        }
    } else {
        // Pristine Light Theme - Soft luminous sky gradients matching the design screenshot
        when (atmosphere) {
            WeatherAtmosphere.SUNNY -> Pair(LightBgCanvas, Color(0xFFE4F0FB))
            WeatherAtmosphere.CLEAR_NIGHT -> Pair(Color(0xFFEEF3FA), Color(0xFFE0EAFC))
            WeatherAtmosphere.CLOUDY -> Pair(LightBgCanvas, Color(0xFFE5EEF8))
            WeatherAtmosphere.OVERCAST -> Pair(Color(0xFFF0F4F8), Color(0xFFE3EAF2))
            WeatherAtmosphere.RAINY -> Pair(Color(0xFFEDF4FB), Color(0xFFDFEAF7))
            WeatherAtmosphere.STORMY -> Pair(Color(0xFFECEFF8), Color(0xFFDFE4F2))
            WeatherAtmosphere.SNOWY -> Pair(Color(0xFFF3F7FC), Color(0xFFE6EFF8))
        }
    }

    val animatedTop by animateColorAsState(
        targetValue = targetTop,
        animationSpec = tween(500),
        label = "bgTop"
    )
    val animatedBottom by animateColorAsState(
        targetValue = targetBottom,
        animationSpec = tween(500),
        label = "bgBottom"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(animatedTop, animatedBottom)
                )
            )
    ) {
        content()
    }
}
