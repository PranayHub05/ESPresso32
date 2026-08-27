package com.pranay.espresso32.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeMode { LIGHT, DARK, SYSTEM }

class AppPreferences(private val context: Context) {
    
    companion object {
        val ESP32_IP = stringPreferencesKey("ESP32_IP")
        val ESP32_PORT = stringPreferencesKey("ESP32_PORT")
        val AUTO_RECONNECT = booleanPreferencesKey("AUTO_RECONNECT")
        val THEME_MODE = stringPreferencesKey("THEME_MODE")
        val KEEP_SCREEN_AWAKE = booleanPreferencesKey("KEEP_SCREEN_AWAKE")
        val FULLSCREEN = booleanPreferencesKey("FULLSCREEN")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("ANIMATIONS_ENABLED")
        val UPDATE_INTERVAL_MS = longPreferencesKey("UPDATE_INTERVAL_MS")
    }

    // Getters
    val ipAddress: Flow<String> = context.dataStore.data.map { it[ESP32_IP] ?: "" }
    val port: Flow<String> = context.dataStore.data.map { it[ESP32_PORT] ?: "81" }
    val autoReconnect: Flow<Boolean> = context.dataStore.data.map { it[AUTO_RECONNECT] ?: true }
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { 
        val themeStr = it[THEME_MODE] ?: "DARK"
        ThemeMode.entries.find { mode -> mode.name.equals(themeStr, ignoreCase = true) } ?: ThemeMode.DARK
    }
    val keepScreenAwake: Flow<Boolean> = context.dataStore.data.map { it[KEEP_SCREEN_AWAKE] ?: true }
    val fullscreenMode: Flow<Boolean> = context.dataStore.data.map { it[FULLSCREEN] ?: false }
    val animationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[ANIMATIONS_ENABLED] ?: false }
    val updateIntervalMs: Flow<Long> = context.dataStore.data.map { it[UPDATE_INTERVAL_MS] ?: 1000L }

    // Setters
    suspend fun setIpAddress(ip: String) { context.dataStore.edit { it[ESP32_IP] = ip } }
    suspend fun setPort(port: String) { context.dataStore.edit { it[ESP32_PORT] = port } }
    suspend fun setAutoReconnect(enabled: Boolean) { context.dataStore.edit { it[AUTO_RECONNECT] = enabled } }
    suspend fun setThemeMode(mode: ThemeMode) { context.dataStore.edit { it[THEME_MODE] = mode.name } }
    suspend fun setKeepScreenAwake(enabled: Boolean) { context.dataStore.edit { it[KEEP_SCREEN_AWAKE] = enabled } }
    suspend fun setFullscreen(enabled: Boolean) { context.dataStore.edit { it[FULLSCREEN] = enabled } }
    suspend fun setAnimationsEnabled(enabled: Boolean) { context.dataStore.edit { it[ANIMATIONS_ENABLED] = enabled } }
    suspend fun setUpdateIntervalMs(interval: Long) { context.dataStore.edit { it[UPDATE_INTERVAL_MS] = interval } }
}
