package com.pranay.espresso32.data.network

import com.pranay.espresso32.data.model.DeviceInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class WebSocketClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .pingInterval(10, TimeUnit.SECONDS) // Keep-alive ping every 10s
        .retryOnConnectionFailure(false)    // Handled by our repository exponential backoff
        .build()

    private var webSocket: WebSocket? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 64)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _lastMessageTime = MutableStateFlow(0L)
    val lastMessageTime: StateFlow<Long> = _lastMessageTime.asStateFlow()

    @Synchronized
    fun connect(ip: String, port: Int) {
        // Cancel previous instance if any
        webSocket?.cancel()
        webSocket = null

        _connectionState.value = ConnectionState.Connecting
        val url = "ws://$ip:$port"
        val request = Request.Builder().url(url).build()

        val wsListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // Ensure this event belongs to the current active websocket
                if (webSocket === this@WebSocketClient.webSocket) {
                    _connectionState.value = ConnectionState.Connected(DeviceInfo(ipAddress = ip, port = port))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                if (webSocket === this@WebSocketClient.webSocket) {
                    _lastMessageTime.value = System.currentTimeMillis()
                    _messages.tryEmit(text)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                if (webSocket === this@WebSocketClient.webSocket) {
                    webSocket.close(1000, null)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (webSocket === this@WebSocketClient.webSocket) {
                    _connectionState.value = ConnectionState.Disconnected
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                if (webSocket === this@WebSocketClient.webSocket) {
                    val errMsg = when {
                        t.message?.contains("Failed to connect", ignoreCase = true) == true -> "Failed to connect to $ip:$port"
                        t.message?.contains("Software caused connection abort", ignoreCase = true) == true -> "Connection lost"
                        t.message?.contains("timeout", ignoreCase = true) == true -> "Connection timed out"
                        else -> t.message ?: "Connection error"
                    }
                    _connectionState.value = ConnectionState.Error(errMsg, t)
                }
            }
        }

        webSocket = client.newWebSocket(request, wsListener)
    }

    @Synchronized
    fun disconnect() {
        val currentWs = webSocket
        webSocket = null
        currentWs?.cancel()
        try {
            currentWs?.close(1000, "User requested disconnect")
        } catch (_: Exception) {}
        _connectionState.value = ConnectionState.Disconnected
    }

    fun setReconnectingState(attempt: Int) {
        _connectionState.value = ConnectionState.Reconnecting(attempt)
    }

    fun sendMessage(message: String): Boolean {
        return webSocket?.send(message) ?: false
    }
}
