# ESPresso32 — ESP32 Wi-Fi Dashboard for Android

<p align="center">
  <strong>Turn your Android phone or tablet into a wireless IoT dashboard for ESP32</strong>
</p>

ESPresso32 is a native Android application that connects to an ESP32 over your local Wi-Fi network via WebSocket, receiving real-time sensor data and displaying it on a clean, responsive dashboard UI.

## Architecture

```
[ESP32 + Sensors]
        │
        │ Wi-Fi
        ▼
[Local Wi-Fi Router]
        │
        │ WebSocket (ws://)
        ▼
[Android Phone / Tablet]
        │
        ▼
[ESPresso32 Dashboard]
```

**No internet, cloud, or external server required.** Everything runs on your local network.

## Features

- **Real-time WebSocket communication** — ESP32 pushes data, Android displays instantly
- **Dynamic sensor cards** — Automatically adapts to any sensor the ESP32 sends
- **Responsive layout** — 2-4 column grid adapts to phones, tablets, and orientations
- **Auto-discovery** — Find ESP32 devices via mDNS and UDP broadcast
- **Auto-reconnect** — Exponential backoff reconnection when ESP32 disconnects
- **Dark IoT theme** — Professional dark dashboard optimized for readability
- **Fullscreen/kiosk mode** — Dedicated display mode for tablets
- **Keep screen awake** — Perfect for wall-mounted displays
- **Stale data indicators** — Clearly shows when data is outdated
- **Lightweight** — Minimal CPU, RAM, and battery usage
- **Offline capable** — No internet connection needed after installation

## Supported Sensor Types

| Type | Examples | Display |
|------|----------|---------|
| **Numeric** | temperature, pressure, light, voltage | `28.6 °C` |
| **Boolean** | motion, door, relay, alarm | `DETECTED` / `CLEAR` |
| **Percentage** | battery, humidity, signal | `87%` with progress bar |
| **Text** | status, mode, device_name | `NORMAL` |

## Requirements

### Android
- Android 8.0 (API 26) or higher
- Wi-Fi connectivity
- Tested on: Lenovo Tab 4 10, various phones

### ESP32
- Any ESP32 variant (ESP32, ESP32-S2, ESP32-S3, ESP32-C3)
- Arduino IDE with libraries:
  - `WiFi.h` (built-in)
  - `WebSocketsServer` by Markus Sattler
  - `ArduinoJson` by Benoit Blanchon
  - `ESPmDNS` (built-in)

## Quick Start

### 1. ESP32 Setup

1. Install required Arduino libraries (WebSocketsServer, ArduinoJson)
2. Open `esp32/esp32_dashboard_example.ino`
3. Set your Wi-Fi credentials:
   ```cpp
   const char* WIFI_SSID = "YOUR_WIFI_SSID";
   const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";
   ```
4. Upload to your ESP32
5. Open Serial Monitor at 115200 baud to see the IP address

### 2. Android Setup

1. Build the APK (see Build Instructions below)
2. Install on your Android device
3. Connect your Android device to the **same Wi-Fi network** as the ESP32
4. Open ESPresso32 app
5. Enter the ESP32's IP address (or tap Auto Discover)
6. Tap **Connect**
7. Dashboard will display real-time sensor data

## Build Instructions

### Prerequisites
- JDK 17+
- Android SDK (API 36)
- The `android` CLI tool or Android Studio

### Build via command line
```bash
cd ESPresso32
./gradlew :app:assembleDebug
```

The APK will be at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Install APK
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or use the `android` CLI:
```bash
android install --apks=app/build/outputs/apk/debug/app-debug.apk
```

## JSON Protocol

### ESP32 → Android (Sensor Data)

```json
{
  "device": {
    "name": "ESP32-S3",
    "id": "ESP32_001"
  },
  "timestamp": 1724780000,
  "data": {
    "temperature": 28.6,
    "humidity": 64.2,
    "motion": true,
    "light": 740,
    "battery": 87
  }
}
```

All fields are optional except `data`. Unknown fields are safely ignored.

**Adding new sensors:** Simply include new keys in the `data` object. The app automatically creates cards for unknown sensor keys.

### Android → ESP32 (Commands, future)

```json
{
  "command": "setRelay",
  "value": true
}
```

### Auto-Discovery Protocol

**mDNS:** ESP32 advertises `_ws._tcp` service. Android discovers via NSD (Network Service Discovery).

**UDP Fallback:**
- Android broadcasts: `ESP32_DISCOVER` on port 4210
- ESP32 responds: `ESP32_DEVICE|192.168.1.50|ESP32-S3|81`

## Project Structure

```
app/src/main/java/com/pranay/espresso32/
├── MainActivity.kt              # Activity + DI container
├── Navigation.kt                # Nav3 routing
├── NavigationKeys.kt            # Route definitions
│
├── data/
│   ├── model/                   # JSON data models
│   │   ├── DeviceInfo.kt
│   │   └── ESP32Message.kt
│   ├── network/                 # Networking layer
│   │   ├── ConnectionState.kt
│   │   ├── DiscoveryService.kt
│   │   └── WebSocketClient.kt
│   ├── preferences/             # DataStore settings
│   │   └── AppPreferences.kt
│   └── repository/              # Data facade
│       └── ESP32Repository.kt
│
├── domain/model/                # Domain models
│   ├── DashboardState.kt
│   ├── SensorReading.kt
│   └── SensorType.kt
│
├── viewmodel/                   # ViewModels
│   ├── ConnectionViewModel.kt
│   ├── DashboardViewModel.kt
│   └── SettingsViewModel.kt
│
├── ui/
│   ├── screens/                 # App screens
│   │   ├── ConnectionScreen.kt
│   │   ├── DashboardScreen.kt
│   │   └── SettingsScreen.kt
│   └── components/              # Reusable UI
│       ├── ConnectionLog.kt
│       ├── SensorCard.kt
│       ├── StatusIndicator.kt
│       └── TopBar.kt
│
├── theme/                       # Material3 theming
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
│
└── utils/
    └── Extensions.kt            # Utility functions

esp32/
└── esp32_dashboard_example.ino  # Complete ESP32 Arduino sketch
```

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 2.3 |
| UI Framework | Jetpack Compose + Material 3 |
| Navigation | Navigation 3 |
| Networking | OkHttp 4.12 WebSocket |
| JSON | kotlinx.serialization |
| State | StateFlow + Coroutines |
| Preferences | DataStore |
| Build | Gradle (AGP 9.0) |
| Min SDK | 26 (Android 8.0) |

## How to Add a New Sensor

### ESP32 Side
Add the new key to the `data` JSON object:
```cpp
doc["data"]["pressure"] = 1013.25;
doc["data"]["co2"] = 412;
```

### Android Side
**No code changes needed!** The app automatically detects new keys and creates sensor cards.

To customize the unit or icon for a new sensor, add entries to `ESP32Repository.kt`:
```kotlin
// In getUnitForKey():
key.contains("co2", ignoreCase = true) -> "ppm"

// In getIconForKey():
key.contains("co2", ignoreCase = true) -> "Cloud"
```

## Settings

| Setting | Description |
|---------|-------------|
| **Theme** | Dark / Light / System |
| **Keep Screen Awake** | Prevents screen from turning off |
| **Fullscreen Mode** | Hides system bars for kiosk use |
| **Auto Reconnect** | Automatically reconnects on disconnect |
| **Animations** | Enable/disable UI animations |

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Can't connect | Ensure ESP32 and Android are on the same Wi-Fi network |
| Connection drops | Check Wi-Fi signal strength; auto-reconnect will retry |
| No data shown | Verify ESP32 is sending JSON in the correct format |
| Discovery fails | Try manual IP entry; check if mDNS is blocked by router |
| App crashes | Report the issue — the app is designed to never crash on bad data |

## Connection States

| State | Description |
|-------|-------------|
| **Disconnected** | No connection to ESP32 |
| **Connecting** | WebSocket handshake in progress |
| **Connected** | Receiving data from ESP32 |
| **Reconnecting** | Auto-reconnecting with exponential backoff (1s → 30s max) |
| **Error** | Connection failed — will auto-retry if enabled |

## Permissions

The app requests only essential permissions:
- `INTERNET` — WebSocket communication
- `ACCESS_NETWORK_STATE` — Check Wi-Fi connectivity
- `ACCESS_WIFI_STATE` — Wi-Fi status
- `CHANGE_WIFI_MULTICAST_STATE` — UDP discovery broadcast

**No location, camera, microphone, contacts, or storage permissions.**

## Future Improvements

- [ ] Historical data graphs (line charts over time)
- [ ] Multiple ESP32 device support
- [ ] Dashboard customization (card reorder, resize)
- [ ] Bidirectional commands (control relays, LEDs from Android)
- [ ] WSS (secure WebSocket) support
- [ ] Token-based authentication
- [ ] Data export (CSV)
- [ ] Notification alerts for sensor thresholds
- [ ] Home screen widget

## License

This project is for personal/educational use.

## Developer

Built by **Pranay** for the ESP32 IoT community.
