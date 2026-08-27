package com.pranay.espresso32.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranay.espresso32.R
import com.pranay.espresso32.data.network.ConnectionState
import com.pranay.espresso32.theme.*

@Composable
fun TopBar(
    deviceName: String?,
    connectionState: ConnectionState,
    onSettingsClick: () -> Unit,
    onFullscreenToggle: () -> Unit,
    onDisconnect: () -> Unit,
    isFullscreen: Boolean,
    modifier: Modifier = Modifier
) {
    if (isFullscreen) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatusIndicator(connectionState = connectionState)
            
            FilledTonalIconButton(
                onClick = onFullscreenToggle,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.6f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FullscreenExit,
                    contentDescription = "Exit Fullscreen"
                )
            }
        }
    } else {
        Surface(
            color = DarkSurface,
            modifier = modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color(0xFF1E2430))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "ESPresso32 Logo",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deviceName ?: "ESPresso32",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusIndicator(connectionState = connectionState)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onDisconnect,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = StatusDisconnected)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LinkOff,
                            contentDescription = "Disconnect"
                        )
                    }
                    IconButton(
                        onClick = onFullscreenToggle,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = ElectricCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Enter Fullscreen"
                        )
                    }
                    IconButton(
                        onClick = onSettingsClick,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = TextSecondary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            }
        }
    }
}
