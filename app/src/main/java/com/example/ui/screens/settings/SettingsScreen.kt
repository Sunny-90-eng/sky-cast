package com.example.ui.screens.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.TemperatureUnit
import com.example.data.model.ThemeMode
import com.example.data.model.WeatherAtmosphere
import com.example.data.model.WindSpeedUnit
import com.example.ui.components.WeatherBackground
import com.example.ui.screens.home.WeatherUiState
import com.example.ui.screens.home.WeatherViewModel
import com.example.ui.theme.AccentBluePrimary

@Composable
fun SettingsScreen(
    uiState: WeatherUiState,
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    WeatherBackground(atmosphere = WeatherAtmosphere.SUNNY) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("settings_scroll_list"),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 16.dp,
                    bottom = 120.dp // Generous bottom clearance to fix scroll clipping!
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = AccentBluePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Theme Mode Selection
                item {
                    SettingsSectionCard(
                        title = "Appearance",
                        icon = Icons.Default.DarkMode,
                        iconTint = Color(0xFF8B5CF6)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeOptionChip(
                                label = "System",
                                selected = uiState.themeMode == ThemeMode.SYSTEM,
                                onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                                modifier = Modifier.weight(1f).testTag("theme_system_chip")
                            )
                            ThemeOptionChip(
                                label = "Light",
                                selected = uiState.themeMode == ThemeMode.LIGHT,
                                onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                                modifier = Modifier.weight(1f).testTag("theme_light_chip")
                            )
                            ThemeOptionChip(
                                label = "Dark",
                                selected = uiState.themeMode == ThemeMode.DARK,
                                onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                                modifier = Modifier.weight(1f).testTag("theme_dark_chip")
                            )
                        }
                    }
                }

                // Temperature Unit Selection
                item {
                    SettingsSectionCard(
                        title = "Temperature Unit",
                        icon = Icons.Default.Thermostat,
                        iconTint = Color(0xFFF97316)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ThemeOptionChip(
                                label = "Celsius (°C)",
                                selected = uiState.tempUnit == TemperatureUnit.CELSIUS,
                                onClick = { viewModel.setTempUnit(TemperatureUnit.CELSIUS) },
                                modifier = Modifier.weight(1f).testTag("unit_celsius_chip")
                            )
                            ThemeOptionChip(
                                label = "Fahrenheit (°F)",
                                selected = uiState.tempUnit == TemperatureUnit.FAHRENHEIT,
                                onClick = { viewModel.setTempUnit(TemperatureUnit.FAHRENHEIT) },
                                modifier = Modifier.weight(1f).testTag("unit_fahrenheit_chip")
                            )
                        }
                    }
                }

                // Wind Speed Unit Selection
                item {
                    SettingsSectionCard(
                        title = "Wind Speed Unit",
                        icon = Icons.Default.Air,
                        iconTint = Color(0xFF14B8A6)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ThemeOptionChip(
                                label = "km/h",
                                selected = uiState.windUnit == WindSpeedUnit.KMH,
                                onClick = { viewModel.setWindUnit(WindSpeedUnit.KMH) },
                                modifier = Modifier.weight(1f).testTag("unit_kmh_chip")
                            )
                            ThemeOptionChip(
                                label = "mph",
                                selected = uiState.windUnit == WindSpeedUnit.MPH,
                                onClick = { viewModel.setWindUnit(WindSpeedUnit.MPH) },
                                modifier = Modifier.weight(1f).testTag("unit_mph_chip")
                            )
                        }
                    }
                }

                // Weather API Information
                item {
                    SettingsSectionCard(
                        title = "Weather Data Source",
                        icon = Icons.Default.CloudQueue,
                        iconTint = AccentBluePrimary
                    ) {
                        Column {
                            Text(
                                text = "Open-Meteo High Resolution Meteorological Model",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Combining global forecasts from ECMWF, DWD ICON, and NOAA GFS. Free, accurate, and real-time without rate limits.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // About Card
                item {
                    SettingsSectionCard(
                        title = "About SkyCast",
                        icon = Icons.Default.Info,
                        iconTint = Color(0xFF64748B)
                    ) {
                        Text(
                            text = "SkyCast v2.0 • Modern Weather Application",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Crafted with Jetpack Compose & Material 3. Features live GPS detection, 3D claymorphic illustrations, 24-hour hourly forecast, 7-day outlook, and instant city search.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeOptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                ),
                maxLines = 1
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AccentBluePrimary,
            selectedLabelColor = Color.White,
            containerColor = Color(0xFFF1F5F9),
            labelColor = Color(0xFF475569)
        ),
        border = null,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    )
}
