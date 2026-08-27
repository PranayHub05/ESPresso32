package com.pranay.espresso32

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.pranay.espresso32.data.network.DiscoveryService
import com.pranay.espresso32.data.network.WebSocketClient
import com.pranay.espresso32.data.preferences.AppPreferences
import com.pranay.espresso32.data.preferences.ThemeMode
import com.pranay.espresso32.data.repository.ESP32Repository
import com.pranay.espresso32.theme.ESPresso32Theme

object ESPresso32App {
    private var _webSocketClient: WebSocketClient? = null
    private var _repository: ESP32Repository? = null
    private var _discoveryService: DiscoveryService? = null
    private var _preferences: AppPreferences? = null

    val webSocketClient: WebSocketClient get() = _webSocketClient ?: WebSocketClient().also { _webSocketClient = it }
    val repository: ESP32Repository get() = _repository ?: ESP32Repository(webSocketClient).also { _repository = it }
    fun discoveryService(): DiscoveryService = _discoveryService ?: DiscoveryService().also { _discoveryService = it }
    fun preferences(context: Context): AppPreferences = _preferences ?: AppPreferences(context.applicationContext).also { _preferences = it }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val prefs = ESPresso32App.preferences(this)

        setContent {
            val themeMode by prefs.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val keepAwake by prefs.keepScreenAwake.collectAsState(initial = false)
            val isFullscreen by prefs.fullscreenMode.collectAsState(initial = false)

            if (keepAwake) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }

            val windowInsetsController = remember {
                WindowCompat.getInsetsController(window, window.decorView)
            }
            if (isFullscreen) {
                windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }

            val darkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            ESPresso32Theme(darkTheme = darkTheme) {
                MainNavigation()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
