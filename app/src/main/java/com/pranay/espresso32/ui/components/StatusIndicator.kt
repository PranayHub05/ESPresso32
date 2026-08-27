package com.pranay.espresso32.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranay.espresso32.data.network.ConnectionState
import com.pranay.espresso32.theme.*

@Composable
fun StatusIndicator(
    connectionState: ConnectionState,
    modifier: Modifier = Modifier
) {
    val color = when (connectionState) {
        is ConnectionState.Connected -> StatusConnected
        is ConnectionState.Disconnected -> StatusDisconnected
        is ConnectionState.Connecting -> StatusWarning
        is ConnectionState.Reconnecting -> StatusReconnecting
        is ConnectionState.Error -> StatusError
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val currentAlpha = if (connectionState is ConnectionState.Connecting || connectionState is ConnectionState.Reconnecting) alpha else 1.0f

    val statusText = when (connectionState) {
        is ConnectionState.Connected -> "CONNECTED"
        is ConnectionState.Disconnected -> "DISCONNECTED"
        is ConnectionState.Connecting -> "CONNECTING..."
        is ConnectionState.Reconnecting -> "RECONNECTING (${connectionState.attempt})"
        is ConnectionState.Error -> "ERROR"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = color, alpha = currentAlpha)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = color
        )
    }
}
