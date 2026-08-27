package com.pranay.espresso32.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranay.espresso32.domain.model.SensorReading
import com.pranay.espresso32.domain.model.SensorType
import com.pranay.espresso32.theme.*
import com.pranay.espresso32.utils.toRelativeTimeString

@Composable
fun SensorCard(
    reading: SensorReading,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val alpha = if (reading.isStale) 0.55f else 1.0f

    val (cardBg, accentColor, borderColor) = if (isDark) {
        when {
            reading.key.contains("temp", ignoreCase = true) -> 
                Triple(CardTemperatureBg, CardTemperatureAccent, CardTemperatureBorder)
            reading.key.contains("humid", ignoreCase = true) -> 
                Triple(CardHumidityBg, CardHumidityAccent, CardHumidityBorder)
            reading.key.contains("motion", ignoreCase = true) -> 
                Triple(CardMotionBg, CardMotionAccent, CardMotionBorder)
            reading.key.contains("light", ignoreCase = true) -> 
                Triple(CardLightBg, CardLightAccent, CardLightBorder)
            reading.key.contains("batter", ignoreCase = true) -> 
                Triple(CardBatteryBg, CardBatteryAccent, CardBatteryBorder)
            reading.key.contains("pressure", ignoreCase = true) -> 
                Triple(CardPressureBg, CardPressureAccent, CardPressureBorder)
            else -> 
                Triple(CardDefaultBg, CardDefaultAccent, CardDefaultBorder)
        }
    } else {
        Triple(LightCard, EspressoOrange, LightCardBorder)
    }

    val icon = when {
        reading.key.contains("temp", ignoreCase = true) -> Icons.Default.Thermostat
        reading.key.contains("humid", ignoreCase = true) -> Icons.Default.WaterDrop
        reading.key.contains("motion", ignoreCase = true) -> Icons.AutoMirrored.Filled.DirectionsRun
        reading.key.contains("light", ignoreCase = true) -> Icons.Default.LightMode
        reading.key.contains("batter", ignoreCase = true) -> Icons.Default.BatteryFull
        reading.key.contains("pressure", ignoreCase = true) -> Icons.Default.Compress
        reading.key.contains("volt", ignoreCase = true) -> Icons.Default.Bolt
        else -> Icons.Default.Sensors
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (isCompact) 14.dp else 18.dp)
        ) {
            // Header row: icon badge + title + stale warning
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isCompact) 32.dp else 38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(if (isCompact) 18.dp else 22.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Text(
                    text = reading.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isCompact) 15.sp else 17.sp
                    ),
                    color = if (isDark) TextPrimary else Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                if (reading.isStale) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatusWarning.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Stale data",
                                tint = StatusWarning,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "STALE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusWarning
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isCompact) 12.dp else 16.dp))

            // Value display based on type
            when (reading.type) {
                SensorType.NUMERIC -> {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = reading.displayValue,
                            style = if (isCompact) MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold)
                                    else MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = TextPrimary
                        )
                        if (reading.unit.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = reading.unit,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = if (isCompact) 16.sp else 18.sp
                                ),
                                color = accentColor,
                                modifier = Modifier.padding(bottom = if (isCompact) 4.dp else 6.dp)
                            )
                        }
                    }
                }
                SensorType.BOOLEAN -> {
                    val boolVal = reading.value as? Boolean 
                        ?: reading.value.toString().toBooleanStrictOrNull() 
                        ?: false
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (boolVal) StatusConnected.copy(alpha = 0.18f)
                                else Color(0xFF64748B).copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = if (boolVal) StatusConnected else Color(0xFF94A3B8),
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (boolVal) "DETECTED" else "CLEAR",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = if (boolVal) StatusConnected else Color(0xFFCBD5E1)
                        )
                    }
                }
                SensorType.PERCENTAGE -> {
                    val numVal = (reading.value as? Number)?.toFloat() 
                        ?: reading.value.toString().toFloatOrNull() 
                        ?: 0f
                    
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = reading.displayValue,
                            style = if (isCompact) MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold)
                                    else MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (isCompact) 16.sp else 18.sp
                            ),
                            color = accentColor,
                            modifier = Modifier.padding(bottom = if (isCompact) 4.dp else 6.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    LinearProgressIndicator(
                        progress = { (numVal / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = accentColor,
                        trackColor = Color(0xFF2E3648)
                    )
                }
                SensorType.TEXT -> {
                    Text(
                        text = reading.displayValue,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isCompact) 10.dp else 14.dp))

            // Footer: Timestamp
            Text(
                text = "Updated ${reading.timestamp.toRelativeTimeString()}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = TextSecondary
            )
        }
    }
}
