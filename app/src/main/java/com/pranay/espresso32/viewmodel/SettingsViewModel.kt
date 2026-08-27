package com.pranay.espresso32.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pranay.espresso32.data.preferences.AppPreferences
import com.pranay.espresso32.data.preferences.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: AppPreferences
) : ViewModel() {

    val ipAddress: StateFlow<String> = preferences.ipAddress.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ""
    )

    val port: StateFlow<String> = preferences.port.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "81"
    )

    val themeMode: StateFlow<ThemeMode> = preferences.themeMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.DARK
    )

    val keepAwake: StateFlow<Boolean> = preferences.keepScreenAwake.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )

    val fullscreen: StateFlow<Boolean> = preferences.fullscreenMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    val autoReconnect: StateFlow<Boolean> = preferences.autoReconnect.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )

    val animationsEnabled: StateFlow<Boolean> = preferences.animationsEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun setKeepAwake(enabled: Boolean) {
        viewModelScope.launch { preferences.setKeepScreenAwake(enabled) }
    }

    fun setFullscreen(enabled: Boolean) {
        viewModelScope.launch { preferences.setFullscreen(enabled) }
    }

    fun setAutoReconnect(enabled: Boolean) {
        viewModelScope.launch { preferences.setAutoReconnect(enabled) }
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setAnimationsEnabled(enabled) }
    }
}
