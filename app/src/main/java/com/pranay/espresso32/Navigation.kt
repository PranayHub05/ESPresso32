package com.pranay.espresso32

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.pranay.espresso32.ui.screens.ConnectionScreen
import com.pranay.espresso32.ui.screens.DashboardScreen
import com.pranay.espresso32.ui.screens.SettingsScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(ConnectionDest)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<ConnectionDest> {
                ConnectionScreen(
                    onConnected = {
                        backStack.clear()
                        backStack.add(DashboardDest)
                    },
                    modifier = Modifier.safeDrawingPadding()
                )
            }
            entry<DashboardDest> {
                DashboardScreen(
                    onSettingsClick = { backStack.add(SettingsDest) },
                    onDisconnect = {
                        backStack.clear()
                        backStack.add(ConnectionDest)
                    },
                    modifier = Modifier.safeDrawingPadding()
                )
            }
            entry<SettingsDest> {
                SettingsScreen(
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding()
                )
            }
        }
    )
}
