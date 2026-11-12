# AquaGuard AI
**Smart Water Billing, Leakage Detection & Pipe Health Monitoring System**

College project using ESP32 + two YF-S201 flow sensors + Firebase + Android / Web dashboard.

---

## 1. Connection Architecture (most important)

```
Sensor1 (GPIO27) ─┐
                  ├── ESP32 ── Wi-Fi ── Internet ── Firebase RTDB ── Android App / Web Dashboard
Sensor2 (GPIO26) ─┘                                   (any network / mobile data)
```

- Phone and ESP32 **do not** need to be on the same Wi-Fi.
- No port forwarding, no public IP, no Blynk.
- Online/offline is decided from `lastHeartbeat` timestamp.

---

## 2. Hardware Safety (do this first)

ESP32 GPIOs are **NOT 5 V tolerant**.

1. Power the YF-S201 from a separate 5 V supply.
2. Measure the signal pin voltage with a multimeter.
3. If the pulse is > 3.3 V → use a voltage divider (e.g. 10 kΩ + 20 kΩ) or logic-level shifter.
4. Connect common ground between ESP32 and both sensors.
5. Only then connect signal wires to GPIO 27 and GPIO 26.

---

## 3. Firebase Setup

1. Go to https://console.firebase.google.com → Create project.
2. Enable **Realtime Database** (start in test mode, later lock rules).
3. Enable **Authentication** → Email/Password (and optionally Anonymous for demo).
4. Add an Android app (package `com.college.aquaguardai`) → download `google-services.json`.
5. Project settings → copy the **Web API Key** and **Database URL**.

### Database structure used by all components

```
/waterSystems/{deviceId}/
  sensors/
    sensor1/ { flowRate, totalLitres, updatedAt }
    sensor2/ { flowRate, totalLitres, updatedAt }
  system/   { lastHeartbeat, firmwareVersion, online }
  analytics/{ totalConsumption, estimatedBill, leakageStatus, ... }
  history/{ epochMilliseconds }/{ deviceId, timestamp, sensor totals, pulse counts, validity }
  application/
    settings/{ ...AppSettings fields... }
    maintenance/{ recordId }/{ ...MaintenanceRecord fields... }
    alerts/{ alertId }/{ ...AlertItem fields... }
    billing/{ YYYY-MM }/{ ...BillingSummary fields... }
```

The Spring Boot application listens to `sensors/sensor1` and `sensors/sensor2` in this tree. Sensor entries use `flowRate`, `totalLitres`, `updatedAt` (Unix epoch milliseconds; epoch seconds are normalized when read), and optional `pulseCount` plus `sensorValid`. The backend samples history at most once per minute. Pulse counts are cumulative since the ESP32's persisted counter began; `sensorValid` means a pulse has been observed since the current firmware boot, not a physical sensor self-test.

### Example security rules (production)

```json
{
  "rules": {
    "waterSystems": {
      "$deviceId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    }
  }
}
```

---

## 4. ESP32 Firmware

Folder: `esp32/AquaGuardAI_ESP32.ino`

1. Install ESP32 board support in Arduino IDE.
2. Install library: **Firebase ESP Client** by Mobizt.
3. Open the `.ino` file.
4. Fill only these lines:

```cpp
#define WIFI_SSID        "YourHomeWiFi"
#define WIFI_PASSWORD    "YourPassword"
#define API_KEY          "AIza..."          // from Firebase console
#define DATABASE_URL     "https://xxxx-default-rtdb.firebaseio.com/"
#define DEVICE_ID        "esp32_aqua_001"
```

5. Upload. Open Serial Monitor (115200) to see connection status.

The firmware automatically reconnects Wi-Fi and keeps sending heartbeats.

---

## 5. Web Dashboard (works from any browser)

Folder: `web/`

1. Open `web/app.js`.
2. Replace the `firebaseConfig` object with your project values (same as Android).
3. Open `index.html` in a browser (or host on any static hosting / GitHub Pages / Netlify).
4. Enter the same `DEVICE_ID` used in the ESP32.
5. Live data appears as soon as the ESP32 uploads.

You can open the web dashboard on a phone or PC while the Android app is also connected – both read the same Firebase data.

---

## 6. Android App

Folder: `android/` contains a complete project skeleton ready for Android Studio.

1. Open the folder in Android Studio (File → Open).
2. Place your `google-services.json` inside `app/`.
3. Sync Gradle.
4. Build & run on a real phone.
5. Login / register → enter the same Device ID.

Core features implemented:
- Real-time Firebase listeners
- Online/offline detection from heartbeat
- Live flow rates, total litres, estimated bill
- Simple leakage status (rule-based)
- Bottom navigation (Home / Live / Leakage / Analytics / Settings)
- Diagnostics screen

---

## 7. Quick Test Checklist

- [ ] Sensor signal voltage verified safe for 3.3 V
- [ ] ESP32 connects to Wi-Fi (Serial Monitor)
- [ ] Firebase receives data under `/waterSystems/esp32_aqua_001/`
- [ ] Web dashboard shows live numbers
- [ ] Android app shows the same numbers
- [ ] Unplug ESP32 → status becomes “ESP32 Offline” within ~45 s
- [ ] Power cycle ESP32 → reconnects automatically

---

## 8. Calibration

1. Pass a known volume of water (e.g. 10 litres) through one sensor.
2. Note the pulse count.
3. `calibration = pulses / litres`
4. Update `calibration1` / `calibration2` in the firmware and re-upload.

---

## 9. Important Notes

- Never hard-code real Wi-Fi passwords or Firebase admin keys in public code.
- The Android APK and ESP32 firmware contain only the public client configuration.
- Real security comes from Firebase Authentication + Database Rules.
- Leakage detection and risk scores are transparent rule-based models – not magical pipe-break prediction.

---

**Project by:** College student  
**Stack:** ESP32 + Arduino, Firebase RTDB, Android (Java), Web (HTML/JS)
