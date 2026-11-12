/*
  AquaGuard AI - ESP32 Firmware
  =================================
  Hardware:
    - ESP32 Dev Board
    - YF-S201 Flow Sensor 1 → GPIO 27
    - YF-S201 Flow Sensor 2 → GPIO 26

  IMPORTANT SAFETY:
    ESP32 GPIOs are NOT 5V tolerant.
    Measure sensor signal voltage first.
    Use voltage divider or level shifter if pulse > 3.3V.
    Common ground is mandatory.

  Libraries (Arduino Library Manager):
    - Firebase ESP Client by Mobizt
    - (WiFi is built-in)

  Fill the USER CONFIG section only.
  Never commit real passwords to public repos.
*/

#include <WiFi.h>
#include <time.h>
#include <Preferences.h>
#include <Firebase_ESP_Client.h>
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

// ========== USER CONFIG (FILL THESE) ==========
#define WIFI_SSID        "YOUR_WIFI_SSID"
#define WIFI_PASSWORD    "YOUR_WIFI_PASSWORD"
#define API_KEY          "YOUR_FIREBASE_WEB_API_KEY"
#define DATABASE_URL     "https://YOUR_PROJECT-default-rtdb.firebaseio.com/"
#define DEVICE_ID        "esp32_aqua_001"   // same ID used in Android & Web
// =============================================

#define SENSOR1_PIN 27
#define SENSOR2_PIN 26

// Calibration: pulses per litre (measure with known volume)
float calibration1 = 450.0;
float calibration2 = 450.0;

volatile unsigned long pulseCount1 = 0;
volatile unsigned long pulseCount2 = 0;
volatile bool pulseObservedSinceBoot1 = false;
volatile bool pulseObservedSinceBoot2 = false;
float flowRate1 = 0.0, flowRate2 = 0.0;
float totalLitres1 = 0.0, totalLitres2 = 0.0;

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;
Preferences preferences;

unsigned long lastUpload = 0;
const unsigned long UPLOAD_INTERVAL_MS = 5000;
unsigned long lastHeartbeat = 0;
unsigned long lastNtpAttempt = 0;
const unsigned long NTP_RETRY_INTERVAL_MS = 30000;
const time_t MIN_VALID_EPOCH_SECONDS = 1700000000;
bool timeSynchronized = false;
uint64_t totalPulseCount1 = 0;
uint64_t totalPulseCount2 = 0;
uint64_t persistedWholeLitres1 = 0;
uint64_t persistedWholeLitres2 = 0;

void IRAM_ATTR onPulse1() { pulseCount1++; pulseObservedSinceBoot1 = true; }
void IRAM_ATTR onPulse2() { pulseCount2++; pulseObservedSinceBoot2 = true; }

void connectWiFi() {
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to WiFi");
  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 40) {
    delay(500);
    Serial.print(".");
    attempts++;
  }
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nWiFi connected");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("\nWiFi failed - will retry later");
  }
}

uint64_t getTimestampMs() {
  time_t now;
  time(&now);
  if (!timeSynchronized || now < MIN_VALID_EPOCH_SECONDS) return 0;
  return static_cast<uint64_t>(now) * 1000ULL;
}

bool synchronizeTime() {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("NTP sync deferred: Wi-Fi is not connected.");
    timeSynchronized = false;
    return false;
  }

  lastNtpAttempt = millis();
  configTime(0, 0, "pool.ntp.org", "time.google.com", "time.nist.gov");
  Serial.println("Synchronizing system clock with NTP...");

  struct tm timeInfo;
  if (getLocalTime(&timeInfo, 10000)) {
    time_t now;
    time(&now);
    timeSynchronized = now >= MIN_VALID_EPOCH_SECONDS;
  } else {
    timeSynchronized = false;
  }

  if (timeSynchronized) {
    Serial.printf("NTP synchronized. Unix timestamp: %llu ms\n",
                  static_cast<unsigned long long>(getTimestampMs()));
  } else {
    Serial.println("ERROR: valid NTP time unavailable; telemetry will not be sent. Retrying.");
  }
  return timeSynchronized;
}

void persistTotalsAfterWholeLitre() {
  uint64_t wholeLitres1 = static_cast<uint64_t>(totalLitres1);
  uint64_t wholeLitres2 = static_cast<uint64_t>(totalLitres2);
  if (wholeLitres1 != persistedWholeLitres1) {
    preferences.putULong64("inletPulses", totalPulseCount1);
    preferences.putFloat("inletLitres", totalLitres1);
    persistedWholeLitres1 = wholeLitres1;
  }
  if (wholeLitres2 != persistedWholeLitres2) {
    preferences.putULong64("outletPulses", totalPulseCount2);
    preferences.putFloat("outletLitres", totalLitres2);
    persistedWholeLitres2 = wholeLitres2;
  }
}

void setup() {
  Serial.begin(115200);
  delay(500);

  pinMode(SENSOR1_PIN, INPUT_PULLUP);
  pinMode(SENSOR2_PIN, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(SENSOR1_PIN), onPulse1, FALLING);
  attachInterrupt(digitalPinToInterrupt(SENSOR2_PIN), onPulse2, FALLING);

  preferences.begin("aquaguard", false);
  totalPulseCount1 = preferences.getULong64("inletPulses", 0);
  totalPulseCount2 = preferences.getULong64("outletPulses", 0);
  totalLitres1 = preferences.getFloat("inletLitres", 0.0f);
  totalLitres2 = preferences.getFloat("outletLitres", 0.0f);
  persistedWholeLitres1 = static_cast<uint64_t>(totalLitres1);
  persistedWholeLitres2 = static_cast<uint64_t>(totalLitres2);

  connectWiFi();
  synchronizeTime();

  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;

  // Demo auth - replace with secure method for production
  if (Firebase.signUp(&config, &auth, "", "")) {
    Serial.println("Firebase anonymous signup OK (demo)");
  } else {
    Serial.printf("Auth note: %s\n", config.signer.signupError.message.c_str());
  }

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  Serial.println("AquaGuard AI ESP32 ready");
}

void calculateFlow() {
  static unsigned long lastCalc = 0;
  unsigned long now = millis();
  if (now - lastCalc < 1000) return;

  float seconds = (now - lastCalc) / 1000.0f;
  lastCalc = now;

  noInterrupts();
  unsigned long p1 = pulseCount1; pulseCount1 = 0;
  unsigned long p2 = pulseCount2; pulseCount2 = 0;
  interrupts();

  flowRate1 = (p1 / calibration1) / seconds * 60.0f;  // L/min
  flowRate2 = (p2 / calibration2) / seconds * 60.0f;
  totalLitres1 += p1 / calibration1;
  totalLitres2 += p2 / calibration2;
  totalPulseCount1 += p1;
  totalPulseCount2 += p2;
  persistTotalsAfterWholeLitre();
}

void uploadToFirebase() {
  uint64_t timestampMs = getTimestampMs();
  if (timestampMs == 0) {
    Serial.println("Telemetry skipped: NTP epoch time is not valid.");
    return;
  }
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("Telemetry skipped: Wi-Fi disconnected.");
    connectWiFi();
    return;
  }
  if (!Firebase.ready()) {
    Serial.println("Telemetry skipped: Firebase is not ready.");
    return;
  }

  String base = "/waterSystems/" + String(DEVICE_ID);
  bool inletPulseObserved;
  bool outletPulseObserved;
  noInterrupts();
  inletPulseObserved = pulseObservedSinceBoot1;
  outletPulseObserved = pulseObservedSinceBoot2;
  interrupts();

  FirebaseJson s1;
  s1.set("flowRate", flowRate1);
  s1.set("totalLitres", totalLitres1);
  s1.set("pulseCount", static_cast<double>(totalPulseCount1));
  s1.set("sensorValid", inletPulseObserved);
  s1.set("updatedAt", static_cast<double>(timestampMs));
  bool inletSaved = Firebase.RTDB.setJSON(&fbdo, (base + "/sensors/sensor1").c_str(), &s1);
  if (!inletSaved) Serial.printf("Inlet Firebase write failed: %s\n", fbdo.errorReason().c_str());

  FirebaseJson s2;
  s2.set("flowRate", flowRate2);
  s2.set("totalLitres", totalLitres2);
  s2.set("pulseCount", static_cast<double>(totalPulseCount2));
  s2.set("sensorValid", outletPulseObserved);
  s2.set("updatedAt", static_cast<double>(timestampMs));
  bool outletSaved = Firebase.RTDB.setJSON(&fbdo, (base + "/sensors/sensor2").c_str(), &s2);
  if (!outletSaved) Serial.printf("Outlet Firebase write failed: %s\n", fbdo.errorReason().c_str());

  FirebaseJson sys;
  sys.set("lastHeartbeat", static_cast<double>(timestampMs));
  sys.set("firmwareVersion", "1.0.0");
  sys.set("online", true);
  Firebase.RTDB.setJSON(&fbdo, (base + "/system").c_str(), &sys);

  float total = totalLitres1 + totalLitres2;
  Firebase.RTDB.setFloat(&fbdo, (base + "/analytics/totalConsumption").c_str(), total);

  if (inletSaved && outletSaved) {
    Serial.printf("Telemetry saved | timestamp=%llu ms | inlet=%.2f L/min (%llu pulses) | outlet=%.2f L/min (%llu pulses) | total=%.2f L\n",
                  static_cast<unsigned long long>(timestampMs), flowRate1,
                  static_cast<unsigned long long>(totalPulseCount1), flowRate2,
                  static_cast<unsigned long long>(totalPulseCount2), total);
  }
}

void loop() {
  calculateFlow();

  if (!timeSynchronized && millis() - lastNtpAttempt >= NTP_RETRY_INTERVAL_MS) {
    synchronizeTime();
  }

  if (millis() - lastUpload >= UPLOAD_INTERVAL_MS) {
    lastUpload = millis();
    uploadToFirebase();
  }

  // Heartbeat every 15 s
  if (millis() - lastHeartbeat >= 15000) {
    if (Firebase.ready()) {
      Firebase.RTDB.setDouble(&fbdo,
        ("/waterSystems/" + String(DEVICE_ID) + "/system/lastHeartbeat").c_str(),
        static_cast<double>(getTimestampMs()));
    }
    lastHeartbeat = millis();
  }

  // Auto reconnect WiFi
  if (WiFi.status() != WL_CONNECTED) {
    static unsigned long lastRetry = 0;
    if (millis() - lastRetry > 10000) {
      Serial.println("WiFi lost - reconnecting...");
      connectWiFi();
      lastRetry = millis();
    }
  }

  delay(20);  // small yield, never long blocking delay
}
