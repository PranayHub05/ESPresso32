package com.pranay.espresso32.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranay.espresso32.data.repository.LogEntry
import com.pranay.espresso32.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ConnectionLog(
    logs: List<LogEntry>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF090B0E))
            .border(1.dp, Color(0xFF222834), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        if (logs.isEmpty()) {
            Text(
                text = "No connection activity yet...",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                ),
                color = TextMuted,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(logs) { entry ->
                    val textColor = when {
                        entry.message.contains("Connected", ignoreCase = true) -> StatusConnected
                        entry.message.contains("Error", ignoreCase = true) || entry.message.contains("Failed", ignoreCase = true) -> StatusError
                        entry.message.contains("Reconnecting", ignoreCase = true) -> StatusReconnecting
                        else -> TextSecondary
                    }

                    Row(modifier = Modifier.padding(vertical = 3.dp)) {
                        Text(
                            text = "${formatter.format(Date(entry.timestamp))}  ",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            color = EspressoOrange.copy(alpha = 0.8f)
                        )
                        Text(
                            text = entry.message,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            ),
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
