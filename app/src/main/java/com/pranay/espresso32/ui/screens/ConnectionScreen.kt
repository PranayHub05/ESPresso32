package com.pranay.espresso32.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pranay.espresso32.ESPresso32App
import com.pranay.espresso32.R
import com.pranay.espresso32.data.network.ConnectionState
import com.pranay.espresso32.theme.*
import com.pranay.espresso32.ui.components.ConnectionLog
import com.pranay.espresso32.ui.components.StatusIndicator
import com.pranay.espresso32.viewmodel.ConnectionViewModel

@Composable
fun ConnectionScreen(
    onConnected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: ConnectionViewModel = viewModel {
        ConnectionViewModel(
            repository = ESPresso32App.repository,
            discoveryService = ESPresso32App.discoveryService(),
            preferences = ESPresso32App.preferences(context)
        )
    }

    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val ipAddress by viewModel.ipAddress.collectAsStateWithLifecycle()
    val port by viewModel.port.collectAsStateWithLifecycle()
    val ipError by viewModel.ipError.collectAsStateWithLifecycle()
    val portError by viewModel.portError.collectAsStateWithLifecycle()
    val discoveredDevices by viewModel.discoveredDevices.collectAsStateWithLifecycle()
    val isDiscovering by viewModel.isDiscovering.collectAsStateWithLifecycle()
    val connectionLog by viewModel.connectionLog.collectAsStateWithLifecycle()
    val deviceInfo by viewModel.deviceInfo.collectAsStateWithLifecycle()

    LaunchedEffect(connectionState) {
        if (connectionState is ConnectionState.Connected) {
            onConnected()
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = DarkInputBackground,
        unfocusedContainerColor = DarkInputBackground,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedBorderColor = EspressoOrange,
        unfocusedBorderColor = DarkCardBorder,
        focusedLabelColor = EspressoOrange,
        unfocusedLabelColor = TextSecondary,
        cursorColor = EspressoOrange,
        errorBorderColor = StatusError,
        errorTextColor = TextPrimary,
        errorLabelColor = StatusError
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        // App Logo Image
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(26.dp))
                .border(2.dp, EspressoOrange.copy(alpha = 0.6f), RoundedCornerShape(26.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "ESPresso32 Logo",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "ESPresso32",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            ),
            color = TextPrimary
        )
        Text(
            text = "Wireless ESP32 IoT Dashboard",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = EspressoOrange
        )

        Spacer(modifier = Modifier.height(18.dp))

        StatusIndicator(connectionState = connectionState)

        if (connectionState is ConnectionState.Connected && deviceInfo != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Connected: ${deviceInfo?.name} (${deviceInfo?.ipAddress})",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = StatusConnected
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Input Card Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkCardBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "DEVICE CONNECTION",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = EspressoOrange
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { viewModel.onIpChanged(it) },
                    label = { Text("ESP32 IP Address") },
                    placeholder = { Text("192.168.1.50", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    isError = ipError != null,
                    supportingText = ipError?.let { error -> { Text(error, color = StatusError) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = port,
                    onValueChange = { viewModel.onPortChanged(it) },
                    label = { Text("WebSocket Port") },
                    placeholder = { Text("81", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    isError = portError != null,
                    supportingText = portError?.let { error -> { Text(error, color = StatusError) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.connect() },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EspressoOrange,
                            contentColor = Color.Black,
                            disabledContainerColor = EspressoOrange.copy(alpha = 0.3f),
                            disabledContentColor = Color.Black.copy(alpha = 0.5f)
                        ),
                        enabled = connectionState !is ConnectionState.Connecting
                                && connectionState !is ConnectionState.Connected
                    ) {
                        Text(
                            text = if (connectionState is ConnectionState.Connecting) "CONNECTING..." else "CONNECT",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    if (connectionState is ConnectionState.Connected || connectionState is ConnectionState.Connecting) {
                        OutlinedButton(
                            onClick = { viewModel.disconnect() },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, StatusDisconnected),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = StatusDisconnected
                            )
                        ) {
                            Text(
                                "DISCONNECT",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                FilledTonalButton(
                    onClick = { viewModel.startDiscovery(context) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = DarkCardElevated,
                        contentColor = ElectricCyan
                    ),
                    border = BorderStroke(1.dp, Color(0xFF2E384D)),
                    enabled = !isDiscovering
                ) {
                    if (isDiscovering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Searching Local Network...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Auto Discover ESP32",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }

        // Discovered Devices Section
        if (discoveredDevices.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "FOUND ON NETWORK",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = ElectricCyan,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            discoveredDevices.forEach { device ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.selectDevice(device) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "ws://${device.ipAddress}:${device.port}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ElectricCyan
                            )
                        }
                        Text(
                            text = "USE",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = EspressoOrange
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Connection Log Header & Box
        Text(
            text = "EVENT LOG",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        ConnectionLog(
            logs = connectionLog,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
