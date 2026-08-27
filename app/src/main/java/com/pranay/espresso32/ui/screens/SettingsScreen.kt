package com.pranay.espresso32.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pranay.espresso32.BuildConfig
import com.pranay.espresso32.ESPresso32App
import com.pranay.espresso32.data.preferences.ThemeMode
import com.pranay.espresso32.theme.*
import com.pranay.espresso32.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel {
        SettingsViewModel(preferences = ESPresso32App.preferences(context))
    }

    val ipAddress by viewModel.ipAddress.collectAsStateWithLifecycle()
    val port by viewModel.port.collectAsStateWithLifecycle()
    val autoReconnect by viewModel.autoReconnect.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val keepAwake by viewModel.keepAwake.collectAsStateWithLifecycle()
    val fullscreen by viewModel.fullscreen.collectAsStateWithLifecycle()
    val animationsEnabled by viewModel.animationsEnabled.collectAsStateWithLifecycle()

    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = Color.Black,
        checkedTrackColor = EspressoOrange,
        uncheckedThumbColor = Color(0xFF94A3B8),
        uncheckedTrackColor = Color(0xFF1E2430)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item { SectionHeader("Connection") }
            item {
                SettingsCard {
                    ListItem(
                        headlineContent = { Text("ESP32 IP Address", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(if (ipAddress.isEmpty()) "Not configured" else ipAddress, color = if (ipAddress.isEmpty()) TextMuted else ElectricCyan) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(color = DarkCardBorder)
                    ListItem(
                        headlineContent = { Text("Port", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(port, color = ElectricCyan) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(color = DarkCardBorder)
                    ListItem(
                        headlineContent = { Text("Auto Reconnect", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Automatically reconnect when signal returns", color = TextSecondary, fontSize = 12.sp) },
                        trailingContent = {
                            Switch(checked = autoReconnect, onCheckedChange = viewModel::setAutoReconnect, colors = switchColors)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            item { SectionHeader("Display & Kiosk") }
            item {
                SettingsCard {
                    ListItem(
                        headlineContent = { Text("Theme Mode", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(themeMode.name, color = EspressoOrange) },
                        trailingContent = {
                            FilledTonalButton(
                                onClick = {
                                    val next = ThemeMode.entries[(themeMode.ordinal + 1) % ThemeMode.entries.size]
                                    viewModel.setThemeMode(next)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = DarkCardElevated,
                                    contentColor = EspressoOrange
                                )
                            ) {
                                Text("Toggle")
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(color = DarkCardBorder)
                    ListItem(
                        headlineContent = { Text("Keep Screen Awake", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Prevent tablet screen from sleeping", color = TextSecondary, fontSize = 12.sp) },
                        trailingContent = {
                            Switch(checked = keepAwake, onCheckedChange = viewModel::setKeepAwake, colors = switchColors)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(color = DarkCardBorder)
                    ListItem(
                        headlineContent = { Text("Fullscreen Kiosk Mode", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Hide Android navigation & status bars", color = TextSecondary, fontSize = 12.sp) },
                        trailingContent = {
                            Switch(checked = fullscreen, onCheckedChange = viewModel::setFullscreen, colors = switchColors)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            item { SectionHeader("Performance") }
            item {
                SettingsCard {
                    ListItem(
                        headlineContent = { Text("UI Animations", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Enable subtle card transitions", color = TextSecondary, fontSize = 12.sp) },
                        trailingContent = {
                            Switch(checked = animationsEnabled, onCheckedChange = viewModel::setAnimationsEnabled, colors = switchColors)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            item { SectionHeader("About") }
            item {
                SettingsCard {
                    ListItem(
                        headlineContent = { Text("App Version", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(BuildConfig.VERSION_NAME, color = TextSecondary) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(color = DarkCardBorder)
                    ListItem(
                        headlineContent = { Text("Protocol", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("WebSocket (ws://) with JSON payload", color = ElectricCyan) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(color = DarkCardBorder)
                    ListItem(
                        headlineContent = { Text("Developer", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Pranay", color = EspressoOrange) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        ),
        color = EspressoOrange,
        modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 8.dp)
    )
}
