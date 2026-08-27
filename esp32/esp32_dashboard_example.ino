/*
 * ESPresso32 Dashboard - ESP32 WebSocket Server Example
 * 
 * This Arduino sketch turns an ESP32 into a WebSocket server that sends
 * sensor data to the ESPresso32 Android dashboard app.
 * 
 * Hardware: ESP32 (any variant: ESP32, ESP32-S2, ESP32-S3, ESP32-C3)
 * 
 * Required Libraries:
 *   - WiFi (built-in)
 *   - WebSocketsServer by Markus Sattler (install via Library Manager)
 *   - ArduinoJson by Benoit Blanchon (install via Library Manager)
 *   - ESPmDNS (built-in)
 * 
 * Wiring (optional - works with simulated data too):
 *   - DHT22 data pin -> GPIO 4
 *   - PIR motion sensor -> GPIO 5
 *   - Light sensor (LDR) -> GPIO 34 (ADC)
 * 
 * Usage:
 *   1. Set your WiFi credentials below
 *   2. Upload to ESP32
 *   3. Open Serial Monitor at 115200 baud to see IP address
 *   4. Enter the IP address in the ESPresso32 Android app
 *   5. Dashboard will display sensor data in real-time
 */

#include <WiFi.h>
#include <WebSocketsServer.h>
#include <ArduinoJson.h>
#include <ESPmDNS.h>
#include <WiFiUdp.h>

// ============================================================================
// CONFIGURATION - Edit these values
// ============================================================================

// WiFi credentials
const char* WIFI_SSID = "YOUR_WIFI_SSID";
const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";

// WebSocket server port
const int WS_PORT = 81;

// Device identification
const char* DEVICE_NAME = "ESP32-S3";
const char* DEVICE_ID = "ESP32_001";

// mDNS hostname (Android app can discover this)
const char* MDNS_HOSTNAME = "esp32-dashboard";

// UDP discovery port
const int UDP_DISCOVERY_PORT = 4210;

// Data send interval (milliseconds)
const unsigned long SEND_INTERVAL = 1000;

// Simulate sensor data (set to false if real sensors are connected)
const bool SIMULATE_SENSORS = true;

// ============================================================================
// Pin definitions (for real sensors)
// ============================================================================
const int PIN_DHT = 4;
const int PIN_PIR = 5;
const int PIN_LDR = 34;

// ============================================================================
// Global objects
// ============================================================================
WebSocketsServer webSocket = WebSocketsServer(WS_PORT);
WiFiUDP udp;

unsigned long lastSendTime = 0;
unsigned long lastSimUpdate = 0;

// Simulated sensor values
float simTemperature = 25.0;
float simHumidity = 50.0;
bool simMotion = false;
int simLight = 500;
int simBattery = 100;
float simPressure = 1013.25;

// ============================================================================
// WiFi Setup
// ============================================================================
void setupWiFi() {
    Serial.println("\n=== ESPresso32 Dashboard Server ===\n");
    Serial.printf("Connecting to WiFi: %s", WIFI_SSID);
    
    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    
    int attempts = 0;
    while (WiFi.status() != WL_CONNECTED && attempts < 30) {
        delay(500);
        Serial.print(".");
        attempts++;
    }
    
    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("\n\nWiFi Connected!");
        Serial.printf("IP Address: %s\n", WiFi.localIP().toString().c_str());
        Serial.printf("WebSocket:  ws://%s:%d\n", WiFi.localIP().toString().c_str(), WS_PORT);
    } else {
        Serial.println("\nWiFi connection FAILED! Restarting...");
        delay(1000);
        ESP.restart();
    }
}

// ============================================================================
// mDNS Setup
// ============================================================================
void setupMDNS() {
    if (MDNS.begin(MDNS_HOSTNAME)) {
        // Register WebSocket service for Android NSD discovery
        MDNS.addService("ws", "tcp", WS_PORT);
        MDNS.addServiceTxt("ws", "tcp", "device", DEVICE_NAME);
        MDNS.addServiceTxt("ws", "tcp", "id", DEVICE_ID);
        Serial.printf("mDNS: %s.local\n", MDNS_HOSTNAME);
    } else {
        Serial.println("mDNS setup failed!");
    }
}

// ============================================================================
// UDP Discovery Setup
// ============================================================================
void setupUDP() {
    udp.begin(UDP_DISCOVERY_PORT);
    Serial.printf("UDP Discovery listening on port %d\n", UDP_DISCOVERY_PORT);
}

// ============================================================================
// WebSocket Event Handler
// ============================================================================
void webSocketEvent(uint8_t num, WStype_t type, uint8_t* payload, size_t length) {
    switch (type) {
        case WStype_DISCONNECTED:
            Serial.printf("[WS] Client #%u disconnected\n", num);
            break;
            
        case WStype_CONNECTED:
            {
                IPAddress ip = webSocket.remoteIP(num);
                Serial.printf("[WS] Client #%u connected from %s\n", num, ip.toString().c_str());
                
                // Send initial device info
                sendDeviceInfo(num);
            }
            break;
            
        case WStype_TEXT:
            {
                Serial.printf("[WS] Client #%u sent: %s\n", num, payload);
                
                // Parse incoming commands from Android app
                handleCommand(num, (char*)payload);
            }
            break;
            
        case WStype_PING:
            Serial.printf("[WS] Client #%u ping\n", num);
            break;
            
        case WStype_PONG:
            break;
            
        default:
            break;
    }
}

// ============================================================================
// Handle Commands from Android App (bidirectional support)
// ============================================================================
void handleCommand(uint8_t clientNum, const char* payload) {
    JsonDocument doc;
    DeserializationError error = deserializeJson(doc, payload);
    
    if (error) {
        Serial.printf("[CMD] JSON parse error: %s\n", error.c_str());
        return;
    }
    
    const char* command = doc["command"];
    if (command == nullptr) return;
    
    Serial.printf("[CMD] Received command: %s\n", command);
    
    // Example command handling (extend as needed)
    if (strcmp(command, "setRelay") == 0) {
        bool value = doc["value"] | false;
        Serial.printf("[CMD] Set relay: %s\n", value ? "ON" : "OFF");
        // digitalWrite(RELAY_PIN, value ? HIGH : LOW);
        
        // Send acknowledgment
        JsonDocument response;
        response["response"] = "ok";
        response["command"] = command;
        response["value"] = value;
        String json;
        serializeJson(response, json);
        webSocket.sendTXT(clientNum, json);
    }
    else if (strcmp(command, "setBrightness") == 0) {
        int value = doc["value"] | 0;
        Serial.printf("[CMD] Set brightness: %d\n", value);
        // ledcWrite(LED_CHANNEL, map(value, 0, 100, 0, 255));
    }
    else if (strcmp(command, "getStatus") == 0) {
        sendDeviceInfo(clientNum);
    }
}

// ============================================================================
// Send Device Info
// ============================================================================
void sendDeviceInfo(uint8_t clientNum) {
    JsonDocument doc;
    doc["device"]["name"] = DEVICE_NAME;
    doc["device"]["id"] = DEVICE_ID;
    doc["timestamp"] = millis() / 1000;
    
    String json;
    serializeJson(doc, json);
    webSocket.sendTXT(clientNum, json);
}

// ============================================================================
// Read Sensor Data
// ============================================================================
void readSensors(float &temperature, float &humidity, bool &motion, 
                 int &light, int &battery, float &pressure) {
    if (SIMULATE_SENSORS) {
        // Simulate realistic sensor values with gradual changes
        simTemperature += random(-10, 11) * 0.1;
        simTemperature = constrain(simTemperature, 15.0, 45.0);
        
        simHumidity += random(-5, 6) * 0.1;
        simHumidity = constrain(simHumidity, 20.0, 95.0);
        
        // Motion toggles randomly (10% chance each second)
        if (random(100) < 10) {
            simMotion = !simMotion;
        }
        
        simLight += random(-20, 21);
        simLight = constrain(simLight, 0, 1500);
        
        // Battery slowly decreases
        if (random(100) < 5) {
            simBattery--;
            if (simBattery < 10) simBattery = 100; // "recharge"
        }
        
        simPressure += random(-5, 6) * 0.1;
        simPressure = constrain(simPressure, 980.0, 1040.0);
        
        temperature = simTemperature;
        humidity = simHumidity;
        motion = simMotion;
        light = simLight;
        battery = simBattery;
        pressure = simPressure;
    } else {
        // Read real sensors here
        // temperature = dht.readTemperature();
        // humidity = dht.readHumidity();
        // motion = digitalRead(PIN_PIR) == HIGH;
        // light = analogRead(PIN_LDR);
        // battery = map(analogRead(VBAT_PIN), 0, 4095, 0, 100);
        // pressure = bmp.readPressure() / 100.0;
        
        // Placeholder values if no sensors connected
        temperature = 25.0;
        humidity = 50.0;
        motion = false;
        light = 500;
        battery = 100;
        pressure = 1013.25;
    }
}

// ============================================================================
// Send Sensor Data to All Connected Clients
// ============================================================================
void sendSensorData() {
    float temperature, humidity, pressure;
    bool motion;
    int light, battery;
    
    readSensors(temperature, humidity, motion, light, battery, pressure);
    
    // Build JSON message
    JsonDocument doc;
    
    // Device info
    doc["device"]["name"] = DEVICE_NAME;
    doc["device"]["id"] = DEVICE_ID;
    
    // Timestamp
    doc["timestamp"] = millis() / 1000;
    
    // Sensor data
    doc["data"]["temperature"] = round(temperature * 10) / 10.0;
    doc["data"]["humidity"] = round(humidity * 10) / 10.0;
    doc["data"]["motion"] = motion;
    doc["data"]["light"] = light;
    doc["data"]["battery"] = battery;
    doc["data"]["pressure"] = round(pressure * 10) / 10.0;
    
    // Serialize and send
    String json;
    serializeJson(doc, json);
    
    webSocket.broadcastTXT(json);
    
    // Debug output (every 5 seconds to reduce serial spam)
    static unsigned long lastDebug = 0;
    if (millis() - lastDebug > 5000) {
        Serial.printf("[DATA] T:%.1f°C H:%.1f%% M:%s L:%d B:%d%% P:%.1fhPa\n",
                      temperature, humidity, motion ? "YES" : "NO",
                      light, battery, pressure);
        lastDebug = millis();
    }
}

// ============================================================================
// Handle UDP Discovery Requests
// ============================================================================
void handleUDPDiscovery() {
    int packetSize = udp.parsePacket();
    if (packetSize > 0) {
        char buffer[64];
        int len = udp.read(buffer, sizeof(buffer) - 1);
        buffer[len] = '\0';
        
        if (strcmp(buffer, "ESP32_DISCOVER") == 0) {
            Serial.printf("[UDP] Discovery request from %s:%d\n",
                         udp.remoteIP().toString().c_str(), udp.remotePort());
            
            // Build response: ESP32_DEVICE|IP|Name|Port
            String response = "ESP32_DEVICE|";
            response += WiFi.localIP().toString();
            response += "|";
            response += DEVICE_NAME;
            response += "|";
            response += String(WS_PORT);
            
            // Reply to the requester
            udp.beginPacket(udp.remoteIP(), udp.remotePort());
            udp.write((const uint8_t*)response.c_str(), response.length());
            udp.endPacket();
            
            Serial.printf("[UDP] Sent response: %s\n", response.c_str());
        }
    }
}

// ============================================================================
// Setup
// ============================================================================
void setup() {
    Serial.begin(115200);
    delay(1000);
    
    // Setup WiFi
    setupWiFi();
    
    // Setup mDNS for auto-discovery
    setupMDNS();
    
    // Setup UDP for discovery fallback
    setupUDP();
    
    // Setup WebSocket server
    webSocket.begin();
    webSocket.onEvent(webSocketEvent);
    webSocket.enableHeartbeat(15000, 3000, 2);  // ping every 15s, timeout 3s, 2 retries
    
    Serial.println("\n=== Server Ready ===");
    Serial.printf("Connect your Android app to: ws://%s:%d\n", 
                  WiFi.localIP().toString().c_str(), WS_PORT);
    Serial.printf("Or discover via mDNS: %s.local\n\n", MDNS_HOSTNAME);
}

// ============================================================================
// Main Loop
// ============================================================================
void loop() {
    // Handle WebSocket events
    webSocket.loop();
    
    // Handle UDP discovery requests
    handleUDPDiscovery();
    
    // Send sensor data at configured interval
    unsigned long now = millis();
    if (now - lastSendTime >= SEND_INTERVAL) {
        lastSendTime = now;
        sendSensorData();
    }
    
    // Reconnect WiFi if lost
    if (WiFi.status() != WL_CONNECTED) {
        Serial.println("[WiFi] Connection lost! Reconnecting...");
        WiFi.reconnect();
        delay(5000);
    }
}
