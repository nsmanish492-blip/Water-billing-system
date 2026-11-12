package com.aquaguard.model;

/**
 * System health, connection mode, and diagnostic flags.
 */
public class SystemStatus {
    private boolean hardwareConnected;
    private boolean demoMode;
    private String wifiStatus;
    private String firebaseStatus;
    private String lastUpdateTimestamp;
    private long hardwareTelemetryTimestamp;
    private String uptime;
    private int activeAlertCount;
    private String esp32DeviceName;
    private String diagnosticMessage;

    public SystemStatus() {
        this.hardwareConnected = false;
        this.demoMode = true;
        this.wifiStatus = "NOT CONNECTED (Simulation Mode)";
        this.firebaseStatus = "NOT CONFIGURED (Local Presentation Demo)";
        this.esp32DeviceName = "esp32_aqua_001 (Planned)";
        this.diagnosticMessage = "Illustrative sample data — physical ESP32 and sensors not connected.";
    }

    public boolean isHardwareConnected() { return hardwareConnected; }
    public void setHardwareConnected(boolean hardwareConnected) { this.hardwareConnected = hardwareConnected; }

    public boolean isDemoMode() { return demoMode; }
    public void setDemoMode(boolean demoMode) { this.demoMode = demoMode; }

    public String getWifiStatus() { return wifiStatus; }
    public void setWifiStatus(String wifiStatus) { this.wifiStatus = wifiStatus; }

    public String getFirebaseStatus() { return firebaseStatus; }
    public void setFirebaseStatus(String firebaseStatus) { this.firebaseStatus = firebaseStatus; }

    public String getLastUpdateTimestamp() { return lastUpdateTimestamp; }
    public void setLastUpdateTimestamp(String lastUpdateTimestamp) { this.lastUpdateTimestamp = lastUpdateTimestamp; }

    public long getHardwareTelemetryTimestamp() { return hardwareTelemetryTimestamp; }
    public void setHardwareTelemetryTimestamp(long hardwareTelemetryTimestamp) { this.hardwareTelemetryTimestamp = hardwareTelemetryTimestamp; }

    public String getUptime() { return uptime; }
    public void setUptime(String uptime) { this.uptime = uptime; }

    public int getActiveAlertCount() { return activeAlertCount; }
    public void setActiveAlertCount(int activeAlertCount) { this.activeAlertCount = activeAlertCount; }

    public String getEsp32DeviceName() { return esp32DeviceName; }
    public void setEsp32DeviceName(String esp32DeviceName) { this.esp32DeviceName = esp32DeviceName; }

    public String getDiagnosticMessage() { return diagnosticMessage; }
    public void setDiagnosticMessage(String diagnosticMessage) { this.diagnosticMessage = diagnosticMessage; }
}
